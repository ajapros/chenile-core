package org.chenile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.chenile.stm.cli.CLIHelper;
import org.chenile.stm.cli.CLIParams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-puml", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class StmPumlGenerator extends AbstractMojo {
    @Parameter(property="enablementPropertiesFile")
    String enablementPropertiesFile;
    File enablementProperties;
    @Parameter(property="stylingPropertiesFile")
    String stylingPropertiesFile;
    File stylingProperties;
    @Parameter(property="prefix")
    String prefix;
    @Parameter(property="output",defaultValue = "generated-puml", required = true)
    String output;
    @Parameter(defaultValue = "${project.build.resources[0].directory}", required = true, readonly = true)
    private String resourceDirectory;
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private String buildDir;
    private final CLIHelper cliHelper = new CLIHelper();

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Build dir = " + buildDir + " resource directory = " + resourceDirectory);

        if (stylingPropertiesFile != null)
            stylingProperties = new File(resourceDirectory +
                    File.separator + stylingPropertiesFile);

        if (enablementPropertiesFile != null)
            enablementProperties = new File(resourceDirectory +
                    File.separator + enablementPropertiesFile);
        String outputDir = ensureOutputExists();
        for (File f : findFiles()) {
            try {
                generatePuml(f, outputDir);
            } catch(Exception e){
                getLog().info("Error " + e.getMessage() + " in processing " + f);
            }
        }
    }

    private String ensureOutputExists(){
        File buildDirectory = new File(buildDir);
        if (!buildDirectory.exists())
            buildDirectory.mkdir();
        String outputDir = buildDir + File.separator + output ;
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists())
            outputDirectory.mkdir();
        return outputDir;
    }

    private void generatePuml(File xmlFile, String outputDir) throws Exception{
        CLIParams params = makeCLIParams(xmlFile);
        String outputFile = outputDir + File.separator + basename(xmlFile.getName()) + ".puml";
        getLog().info("Processing States File = " + xmlFile + " Generating output " + outputFile);
        cliHelper.renderStateDiagram(params,outputFile);
    }

    private CLIParams makeCLIParams(File xmlFile){
        CLIParams params = new CLIParams();
        params.xmlFile = xmlFile;
        params.prefix = this.prefix;
        params.stylingPropertiesFile = this.stylingProperties;
        params.enablementPropertiesFile = this.enablementProperties;
        return params;
    }

    private  List<File> findFiles(){
        List<File> list = new ArrayList<>();
        findFiles(resourceDirectory, list);
        return list;
    }

    private  void findFiles(String directoryName, List<File> files)  {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile() && file.getName().endsWith(".xml")) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    findFiles(file.getAbsolutePath(), files);
                }
            }
    }

    private String basename(String filename){
        filename = filename.substring(filename.lastIndexOf(File.separator)+1);
        filename = filename.substring(0,filename.indexOf('.'));
        return filename;
    }
}