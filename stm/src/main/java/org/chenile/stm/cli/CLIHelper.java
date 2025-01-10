package org.chenile.stm.cli;

import org.chenile.stm.ConfigProvider;
import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.dummy.DummyStore;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class CLIHelper {

    private void loadStylingProperties(CLIParams params) throws Exception{
        try (InputStream inputStream = Files.newInputStream(params.stylingPropertiesFile.toPath())){
            this.generator.transitionStyler.loadFromXML(inputStream);
        }
    }

    public void allowedActions(CLIParams params,String outputFile) throws Exception {
        out(allowedActions(params),outputFile);
    }

    public String allowedActions(CLIParams params) throws Exception {
        process(params);
        String defaultFlowId = this.stmFlowStore.getDefaultFlow();
        State state = new State(params.stateForAllowedActions, defaultFlowId);
        List<String> allowedActions = this.infoProvider.getAllowedActions(state);
        return allowedActions.toString();
    }
    public void renderStateDiagram(CLIParams params,String outputFile) throws Exception {
        out(renderStateDiagram(params),outputFile);
    }

    public String renderStateDiagram(CLIParams params) throws Exception {
        process(params);
        if (params.stylingPropertiesFile != null){
            loadStylingProperties(params);
        }
        return this.generator.toStateDiagram();
    }


    public void process(CLIParams params) throws Exception {
        if (params.text != null)
            processText(params);
        else if (params.xmlFileName != null)
            processXmlFile(params);
    }

    private void processXmlFile(CLIParams params) throws Exception {
        try (InputStream inputStream = Files.newInputStream(params.xmlFileName.toPath())) {
            processStream(inputStream,params);
        }
    }

    private void processText(CLIParams params) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(params.text.getBytes())) {
            processStream(inputStream,params);
        }
    }

    private void processStream(InputStream inputStream,CLIParams params) throws Exception {
        STMFlowStoreImpl stmFlowStoreImpl = obtainFlowStore(params);
        XmlFlowReader xfr = new XmlFlowReader(stmFlowStoreImpl);
        xfr.parse(inputStream);
        initProcessors(stmFlowStoreImpl);
    }

    private void out(String s, String outputFile) throws IOException{
        if (outputFile != null && !outputFile.isEmpty()){
            writeFile(s,outputFile);
        }else {
            System.out.println(s);
        }
    }

    public void writeFile(String s,String outputFile)
            throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(s);
        printWriter.close();
    }

    private STMFlowStoreImpl obtainFlowStore(CLIParams params) {
        if (params.enablementPropertiesFile == null) return new DummyStore();
        return new DummyStore(){

            @Override
            public EnablementStrategy makeEnablementStrategy(String componentName) throws STMException {
                try{
                    return new ConfigBasedEnablementStrategy(obtainConfigProvider(params),params.prefix);
                }catch(Exception e){
                    throw new STMException("Cannot create enablement strategy", 5000, e);
                }
            }
        };
    }
    private ConfigProvider obtainConfigProvider(CLIParams params) throws Exception{
        try (InputStream inputStream = Files.newInputStream(params.enablementPropertiesFile.toPath())){
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
