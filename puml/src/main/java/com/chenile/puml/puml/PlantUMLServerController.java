package com.chenile.puml.puml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.chenile.stm.cli.CLIHelper;
import org.chenile.stm.cli.CLIParams;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlantUMLServerController {
  private final CLIHelper cliHelper = new CLIHelper();


  @ResponseBody
  @RequestMapping(value = "/uml",
          method = RequestMethod.POST,
          produces = MediaType.IMAGE_PNG_VALUE
  )
  public byte[] generateImageFromUmlScript1(@RequestBody String text)  {
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
      return resource.getByteArray();
    } catch (Exception e) {
      throw new FileProcessingException("Error generating image from UML script.", e);
    }
  }

  private String generatePuml(String text) throws Exception{
    CLIParams params = new CLIParams();
    params.xmlText = text;
    return cliHelper.renderStateDiagram(params);
  }
}