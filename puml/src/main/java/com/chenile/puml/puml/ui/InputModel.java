package com.chenile.puml.puml.ui;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InputModel {

    @NotBlank(message = "XML is mandatory")
    private String stmXml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<states>\n" +
            "\t<event-information eventId='close' componentName='org.chenile.stm.test.basicflow.CloseCart'/>\n" +
            "\t<event-information eventId=\"userLogin\" meta-acls=\"USER_MUST_BE_ABLE_TO_LOGIN,USER_CAN_ACCESS_SYSTEM\"/>\n" +
            "\t<flow id='cart-flow' default='true'>\n" +
            "\t\t<security-strategy componentName=\"org.chenile.stm.test.basicflow.MockSecurityStrategy\"/>\n" +
            "\t\t<entry-action componentName=\"org.chenile.stm.test.basicflow.EntryAction\" />\n" +
            "\t\t<exit-action componentName=\"org.chenile.stm.test.basicflow.ExitAction\" />\n" +
            "\t\t\n" +
            "\t\t<manual-state id='CREATED' initialState='true' meta-mainPath=\"true\">\n" +
            "\t\t\t<on eventId='close' newStateId='CLOSED'\n" +
            "\t\t\t    invokableOnlyFromStm='true'/>\n" +
            "\t\t\t<on eventId='addItem' componentName='org.chenile.stm.test.basicflow.AddItem' />\n" +
            "\t\t\t<on eventId='userLogin' componentName='org.chenile.stm.test.basicflow.UserLogin' />\n" +
            "\t\t\t<on eventId='initiatePayment' componentName='org.chenile.stm.test.basicflow.InitiatePayment'\n" +
            "\t\t\t\tnewStateId='PAYMENT_INITIATED' />\n" +
            "\t\t</manual-state>\n" +
            "\n" +
            "\t\t<manual-state id='PAYMENT_INITIATED'  meta-mainPath=\"true\">\n" +
            "\t\t\t<on eventId=\"approve\" componentName=\"org.chenile.stm.test.basicflow.ApproveCart\"   meta-mainPath=\"true\"/>\n" +
            "\t\t\t<on eventId=\"confirmPayment\" componentName='org.chenile.stm.test.basicflow.ConfirmPayment'\n" +
            "\t\t\t\tnewStateId='TEST_STATE'   meta-mainPath=\"true\"/>\n" +
            "\t\t</manual-state>\n" +
            "\t\t\n" +
            "\t\t<if id='TEST_STATE' condition='approved'\n" +
            "\t\t then='confirm' else='reject'>\n" +
            "\t\t<on eventId='confirm' newStateId='PAYMENT_CONFIRMED'  meta-mainPath=\"true\"/>\n" +
            "\t\t<on eventId='reject' newStateId='PAYMENT_INITIATED'/>\n" +
            "\t    </if>\n" +
            "\n" +
            "\t\t<manual-state id='PAYMENT_CONFIRMED'  meta-mainPath=\"true\"/>\n" +
            "\t\t<manual-state id='CLOSED'/>\n" +
            "\t</flow>\n" +
            "\t\n" +
            "</states>";


    public String getEnablementProperties() {
        return enablementProperties;
    }

    public void setEnablementProperties(String enablementProperties) {
        this.enablementProperties = enablementProperties;
    }

    private String enablementProperties;

    public String getStylingProperties() {
        return stylingProperties;
    }

    public void setStylingProperties(String stylingProperties) {
        this.stylingProperties = stylingProperties;
    }

    private String stylingProperties;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String prefix;
    @NotBlank(message = "XML is mandatory")
    @Size(min = 1, max = 100)
    private String svg;

    public String getSvg() {
        return svg;
    }

    public void setSvg(String svg) {
        this.svg = svg;
    }

    public String getStmXml() {
        return stmXml;
    }

    public void setStmXml(String stmXml) {
        this.stmXml = stmXml;
    }

    @Override
    public String toString() {
        return "InputModel{" +
                "stmXml='" + stmXml + '\'' +
                ", svg='" + svg + '\'' +
                '}';
    }
}
