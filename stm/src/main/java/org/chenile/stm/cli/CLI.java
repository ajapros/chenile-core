package org.chenile.stm.cli;

import picocli.CommandLine;

import java.io.File;

import static picocli.CommandLine.*;

@Command(name = "stm-cli", mixinStandardHelpOptions = true, version = "stm-cli 1.0",
        description = "Reads a State Definition file and allows a few operations on it. STM is not created. Hence components don't have to be in the class path.")
public class CLI implements Runnable {
    @Parameters(index = "0", paramLabel = "<XML File name>", description = "The XML filename to read. Must be a valid states XML. Component names in file will be ignored.")
    private File xmlFileName;
    @Option(names = {"-s", "--uml-state-diagram"}, description = "Generate a UML state diagram")
    private boolean umlStateDiagram;
    @Option(names = {"-a", "--allowed-actions"},paramLabel = "state", description = "Return allowed actions for a state")
    private String stateForAllowedActions;
    @Option(names = {"-j", "--render-json"},paramLabel = "render-json", description = "Return a JSON representation of the XML file")
    private boolean toJson;
    @Option(names = {"-t", "--generate-test-cases"},paramLabel = "render-json", description = "Return a JSON representation of the XML file")
    private boolean genTestCase;
    @Option(names = {"-o", "--output"},paramLabel = "output-file", description = "Writes output to the specified file")
    private String outputFile;
    @Option(names = {"-S", "--styling-properties-file"},paramLabel = "Styling-properties-file", description = "Use the properties file for setting styles according to metadata in states and transitions")
    private File stylingPropertiesFile;
    @Option(names = {"-e", "--enablement-properties-file"},paramLabel = "enablement-properties-file", description = "Use the properties file for enablement properties")
    private File enablementPropertiesFile;
    @Option(names = {"-p", "--prefix"},paramLabel = "prefix", description = "The prefix for all properties")
    private String prefix;
    private final CLIHelper cliHelper = new CLIHelper();
    @Spec
    Model.CommandSpec spec;

    public static void main(String... args) {
        System.exit(new CommandLine(new CLI()).execute(args));
    }
    @Override
    public void run() {
        CLIParams params = makeCLIParams();
        try {
            if (umlStateDiagram) {
                cliHelper.renderStateDiagram(params,outputFile);
            } else if (stateForAllowedActions != null && !stateForAllowedActions.isEmpty()) {
                cliHelper.allowedActions(params,outputFile);
            } else if (toJson) {
                cliHelper.toJson(params,outputFile);
            } else if (genTestCase) {
                cliHelper.renderTestCases(params,outputFile);
            }else {
                System.err.println("Missing option: at least one of the " +
                        "-s or -a options must be specified");
                spec.commandLine().usage(System.err);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CLIParams makeCLIParams() {
        CLIParams params = new CLIParams();
        params.enablementPropertiesFile = this.enablementPropertiesFile;
        params.prefix = this.prefix;
        params.stateForAllowedActions = this.stateForAllowedActions;
        params.stylingPropertiesFile = this.stylingPropertiesFile;
        params.xmlFile = xmlFileName;
        return params;
    }
}
