package com.chenile.puml.puml;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicForm {

    // One syntax to implement a
    // GET method
    @GetMapping("/")
    public String home() {
        String str
                = "<html><body>"
                + "<h1>WELCOME To STM XML to Plant UML converter</h1>"
                + "<textarea id=\"xmlContent\" name=\"xmlContent\" rows=\"50\" cols=\"50\">" +
                "Paste your xml content here" +
                "</textarea> <button onClick='callBE()'>Click</button>" +
                "<script>" + script() + "</script>" +
                "<img id='output'/>"
                + "</body></html>";
        return str;
    }

    private String script(){
        return """
                function callBE(){
                    const xhr = new XMLHttpRequest();
                    xhr.open("POST", "http://localhost:8080/uml1");
           
                    const body = document.getElementById("xmlContent").value;
                    console.log(body);
                    xhr.onload = () => {
                      if (xhr.readyState == 4 && xhr.status == 200) {
                        updateImage(xhr.responseText);
                      } else {
                        console.log(`Error: ${xhr.status}`);
                      }
                    };
                    xhr.send(body);
                }
                function updateImage(raw_data){
                //const base64 = btoa(new Uint8Array(raw_data.reduce(
                              //(data, byte) => data + String.fromCharCode(byte),
                              //'')
                         // ))
                        const imageUrl =  URL.createObjectURL(raw_data);
                        var img = document.getElementById("output");
                        img.src = imageUrl
                  }
                
                """;
    }
}
