package com.chenile.puml.puml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.chenile.stm.cli.CLI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.chenile.puml.puml.validator.XmlValidator.validateXmlFile;

@RestController
public class PlantUMLServerController {

  @Value("${puml.working.dir}")
  String workingdir;

  static CLI cli = new CLI();
  static {
    cli.umlStateDiagram = true;
  }


  @ResponseBody
  @RequestMapping(value = "/uml",
          method = RequestMethod.POST,
          consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  //        produces = MediaType.IMAGE_PNG_VALUE
  )
  public ResponseEntity<Resource> generateImageFromUmlScript(@RequestPart("file") MultipartFile file) throws IOException {
    Path filepath = Paths.get(workingdir, file.getOriginalFilename());

    try (OutputStream os = Files.newOutputStream(filepath)) {
      os.write(file.getBytes());
    } catch (IOException e) {
      throw new FileProcessingException("Error saving uploaded file to disk.", e);
    }

    try {
      validateXmlFile(filepath.toFile());
    } catch (Exception e) {
      throw new FileProcessingException("Invalid file.", e);
    }

    String outfile;
    try {
      outfile = generatePuml(filepath.toFile());
    } catch (Exception e) {
      throw new FileProcessingException("Error generating UML script.", e);
    }

    String script;
    try {
      script = Files.readString(Paths.get(outfile));
    } catch (IOException e) {
      throw new FileProcessingException("Error reading generated UML script.", e);
    }

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      SourceStringReader reader = new SourceStringReader(script.replace(';', '\n'));
      reader.outputImage(bos, new FileFormatOption(FileFormat.PNG, false));
      byte[] array = bos.toByteArray();

      ByteArrayResource resource = new ByteArrayResource(array);
      return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .contentLength(resource.contentLength())
              .header(HttpHeaders.CONTENT_DISPOSITION,
                      ContentDisposition.attachment()
                              .filename("output.png")
                              .build().toString())
              .body(resource);
    } catch (Exception e) {
      throw new FileProcessingException("Error generating image from UML script.", e);
    }
  }

  @ResponseBody
  @RequestMapping(value = "/uml1",
          method = RequestMethod.POST
          //        produces = MediaType.IMAGE_PNG_VALUE
  )
  public ResponseEntity<Resource> generateImageFromUmlScript1(@RequestBody String text) throws IOException {

    String script;
    try {
      script = generatePuml(text);
    } catch (Exception e) {
      throw new FileProcessingException("Error generating UML script.", e);
    }

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      SourceStringReader reader = new SourceStringReader(script.replace(';', '\n'));
      reader.outputImage(bos, new FileFormatOption(FileFormat.PNG, false));
      byte[] array = bos.toByteArray();

      ByteArrayResource resource = new ByteArrayResource(array);
      return ResponseEntity.ok()
              .contentType(MediaType.APPLICATION_OCTET_STREAM)
              .contentLength(resource.contentLength())
              .header(HttpHeaders.CONTENT_DISPOSITION,
                      ContentDisposition.attachment()
                              .filename("output.png")
                              .build().toString())
              .body(resource);
    } catch (Exception e) {
      throw new FileProcessingException("Error generating image from UML script.", e);
    }
  }


  private String generatePuml(File xmlFile){
    cli.xmlFileName = xmlFile;
    cli.outputFile = workingdir + File.separator + basename(xmlFile.getName()) + ".puml";
    System.out.println("Processing States File = " + xmlFile + " Generating output " + cli.outputFile);
    cli.run();
    return cli.outputFile;
  }

  private String generatePuml(String text){
    cli.suppressOutput = true;
    cli.text = text;
    cli.run();
    return cli.text;
  }

  private String basename(String filename){
    filename = filename.substring(filename.lastIndexOf(File.separator)+1);
    filename = filename.substring(0,filename.indexOf('.'));
    return filename;
  }


}