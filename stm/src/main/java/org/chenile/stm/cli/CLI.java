package org.chenile.stm.cli;

import org.chenile.stm.ConfigProvider;
import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;
import picocli.CommandLine;
import static picocli.CommandLine.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Command(name = "stm-cli", mixinStandardHelpOptions = true, version = "stm-cli 1.0",
        description = "Reads a State Definition file and allows a few operations on it. STM is not created. Hence components don't have to be in the class path.")
public class CLI implements Runnable {
    @Parameters(index = "0", paramLabel = "<XML File name>", description = "The XML filename to read. Must be a valid states XML. Component names in file will be ignored.")
    public File xmlFileName;
    @Option(names = {"-s", "--uml-state-diagram"}, description = "Generate a UML state diagram")
    public boolean umlStateDiagram;
    @Option(names = {"-a", "--allowed-actions"},paramLabel = "state", description = "Return allowed actions for a state")
    public String stateForAllowedActions;
    @Option(names = {"-o", "--output"},paramLabel = "output-file", description = "Writes output to the specified file")
    public String outputFile;
    @Option(names = {"-S", "--styling-properties-file"},paramLabel = "Styling-properties-file", description = "Use the properties file for setting styles according to metadata in states and transitions")
    public File stylingPropertiesFile;
    @Option(names = {"-e", "--enablement-properties-file"},paramLabel = "enablement-properties-file", description = "Use the properties file for enablement properties")
    public File enablementPropertiesFile;
    @Option(names = {"-p", "--prefix"},paramLabel = "prefix", description = "The prefix for all properties")
    public String prefix;
    /**
     * Use this to specify the contents of the XML file as text.
     * This is useful if we want to use this class as an internal class and not as a CLI.
     * This is a 2 way variable and will contain the output as well.
     */
    public String text;
    /**
     * used to suppress the output to file. Instead, the caller will get the output from text field
     */
    public boolean suppressOutput = false;
    @Spec
    Model.CommandSpec spec;

    public static void main(String... args) {
        System.exit(new CommandLine(new CLI()).execute(args));
    }
    @Override
    public void run() {
        try {
            if (umlStateDiagram) {
                renderStateDiagram();
            } else if (stateForAllowedActions != null && !stateForAllowedActions.isEmpty())
                allowedActions();
            else {
                System.err.println("Missing option: at least one of the " +
                        "-s or -a options must be specified");
                spec.commandLine().usage(System.err);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadStylingProperties() throws Exception{
        try (InputStream inputStream = Files.newInputStream(stylingPropertiesFile.toPath())){
            this.generator.transitionStyler.loadFromXML(inputStream);
        }
    }

    public void writeFile(String s)
            throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(s);
        printWriter.close();
    }

    private void out(String s) throws IOException{
        if(suppressOutput) return;
        if (outputFile != null && !outputFile.isEmpty()){
            writeFile(s);
        }else {
            System.out.println(s);
        }
    }
    private void allowedActions() throws Exception {
        process();
        String defaultFlowId = this.stmFlowStore.getDefaultFlow();
        State state = new State(stateForAllowedActions, defaultFlowId);
        List<String> allowedActions = this.infoProvider.getAllowedActions(state);
        text = allowedActions.toString();
        out(text);
    }
    private void renderStateDiagram() throws Exception {
        process();
        if (stylingPropertiesFile != null){
            loadStylingProperties();
        }
        text = this.generator.toStateDiagram();
        out(text);
    }


    public void process() throws Exception {
        if (text != null)
            processText();
        else if (xmlFileName != null)
            processXmlFile();
    }

    private void processXmlFile() throws Exception {
        try (InputStream inputStream = Files.newInputStream(xmlFileName.toPath())) {
            processStream(inputStream);
        }
    }

    private void processText() throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(text.getBytes())) {
            processStream(inputStream);
        }
    }


    private void processStream(InputStream inputStream) throws Exception {
        STMFlowStoreImpl stmFlowStoreImpl = obtainFlowStore();
        XmlFlowReader xfr = new XmlFlowReader(stmFlowStoreImpl);
        xfr.parse(inputStream);
        initProcessors(stmFlowStoreImpl);
    }

    private STMFlowStoreImpl obtainFlowStore() {
        if (enablementPropertiesFile == null) return new DummyStore();
        return new DummyStore(){

            @Override
            public EnablementStrategy makeEnablementStrategy(String componentName) throws STMException {
                try{
                    return new ConfigBasedEnablementStrategy(obtainConfigProvider(),prefix);
                }catch(Exception e){
                    throw new STMException("Cannot create enablement strategy", 5000, e);
                }
            }
        };
    }
    private ConfigProvider obtainConfigProvider() throws Exception{
        try (InputStream inputStream = Files.newInputStream(enablementPropertiesFile.toPath())){
            return new ConfigProviderImpl(inputStream);
        }
    }

    private void initProcessors(STMFlowStoreImpl stmFlowStoreImpl) {
        this.generator = new STMPlantUmlSDGenerator(stmFlowStoreImpl);
        this.infoProvider = new STMActionsInfoProvider(stmFlowStoreImpl);
        this.stmFlowStore = stmFlowStoreImpl;
    }
    private STMPlantUmlSDGenerator generator;
    private STMActionsInfoProvider infoProvider;
    private STMFlowStore stmFlowStore;
}
