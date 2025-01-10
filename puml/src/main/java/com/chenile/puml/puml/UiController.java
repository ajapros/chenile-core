package com.chenile.puml.puml;

import com.chenile.puml.puml.ui.InputModel;
import jakarta.validation.Valid;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.chenile.stm.cli.CLIHelper;
import org.chenile.stm.cli.CLIParams;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Base64;

@Controller
public class UiController {


    private final CLIHelper cliHelper = new CLIHelper();



    @GetMapping("/")
    public String showSignUpForm(@Valid @ModelAttribute("formData") InputModel inputModel, Model model) {
        model.addAttribute("inputModel", inputModel);
        return "index";
    }


    @PostMapping("/convert")
    public String convert(@Valid @ModelAttribute("formData") InputModel inputModel,
                          BindingResult result, Model model) {
        System.out.println(inputModel);
        model.addAttribute("inputModel", inputModel);
        try {
            model.addAttribute("imageData", getImage(inputModel.getStmXml()));
        }
        catch (Exception e){
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }


    private String getImage(String text){
        String script;
        try {
            script = generatePuml(text);
        } catch (Exception e) {
            throw new FileProcessingException("Error generating UML script. Enter valid stm xml", e);
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            SourceStringReader reader = new SourceStringReader(script.replace(';', '\n'));
            reader.outputImage(bos, new FileFormatOption(FileFormat.PNG, false));
            byte[] array = bos.toByteArray();

            ByteArrayResource resource = new ByteArrayResource(array);

            return Base64.getEncoder().encodeToString( resource.getByteArray());
        } catch (Exception e) {
            throw new FileProcessingException("Error generating image from UML script.", e);
        }
    }

    private String generatePuml(String text) throws Exception{
        CLIParams params = new CLIParams();
        params.text = text;
        return cliHelper.renderStateDiagram(params);
    }

}
