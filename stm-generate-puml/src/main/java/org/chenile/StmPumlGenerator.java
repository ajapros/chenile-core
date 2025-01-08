package org.chenile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.chenile.stm.cli.CLI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-puml", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class StmPumlGenerator extends AbstractMojo {
    @Parameter(property="enablementPropertiesFile")
    String enablementPropertiesFile;
    @Parameter(property="stylingPropertiesFile")
    String stylingPropertiesFile;
    @Parameter(property="prefix")
    String prefix;
    @Parameter(property="output",defaultValue = "generated-puml", required = true)
    String output;
    @Parameter(defaultValue = "${project.build.resources[0].directory}", required = true, readonly = true)
    private String resourceDirectory;
    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private String buildDir;;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Build dir = " + buildDir + " resource directory = " + resourceDirectory);
        CLI cli = new CLI();
        cli.umlStateDiagram = true;
        if(stylingPropertiesFile != null)
            cli.stylingPropertiesFile = new File(resourceDirectory +
                    File.separator + stylingPropertiesFile);
        cli.prefix = prefix;
        if (enablementPropertiesFile != null)
            cli.enablementPropertiesFile = new File(resourceDirectory +
                    File.separator + enablementPropertiesFile);

        String outputDir = ensureOutputExists();
        for (File f: findFiles()){
            generatePuml(f,cli,outputDir);
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

    private void generatePuml(File xmlFile, CLI cli, String outputDir){
        cli.xmlFileName = xmlFile;
        cli.outputFile = outputDir + File.separator + basename(xmlFile.getName()) + ".puml";
        getLog().info("Processing States File = " + xmlFile + " Generating output " + cli.outputFile);
        cli.run();
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