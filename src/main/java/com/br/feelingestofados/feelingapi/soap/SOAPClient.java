package com.br.feelingestofados.feelingapi.soap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SOAPClient {
    private static final String wsUrl = "http://baseteste.feelingestofados.com.br/g5-senior-services/sapiens_Sync";
    private static final String wsUrlEnd = "?wsdl";

    public static String requestFromSeniorWS(String wsPath, String service, String usr, String pswd, String encryption, HashMap params) throws IOException {
        String xmlBody = prepareXmlBody(service, usr, pswd, encryption, params);
        String url = wsUrl + wsPath + wsUrlEnd;
        String response = postRequest(url, xmlBody);

        return response;
    }

    private static String prepareXmlBody(String service, String usr, String pswd, String encryption, HashMap params) {
        StringBuilder xmlBuilder = new StringBuilder("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\">");
        xmlBuilder.append("<soapenv:Body>");
        xmlBuilder.append("<ser:" + service + ">");
        xmlBuilder.append("<user>" + usr + "</user>");
        xmlBuilder.append("<password>" + pswd + "</password>");
        xmlBuilder.append("<encryption>" + encryption + "</encryption>");
        if(params.isEmpty()) {
            xmlBuilder.append("<parameters/>");
        } else {
            xmlBuilder.append("<parameters>");
            params.forEach((key, value) -> {
                if(value instanceof HashMap) {
                    xmlBuilder.append("<" + key + ">");
                    ((HashMap<?, ?>) value).forEach((key1, value1) -> {
                        if(value1 instanceof ArrayList) {
                            ((ArrayList) value1).forEach(produto -> {
                                xmlBuilder.append("<" + key1 + ">");
                                ((HashMap<?, ?>) produto).forEach((key2, value2) -> {
                                    if(value2 instanceof  ArrayList) {
                                        ((ArrayList) value2).forEach(campo -> {
                                            xmlBuilder.append("<" + key2 + ">");
                                            ((HashMap<?, ?>) campo).forEach((key3, value3) -> {
                                                xmlBuilder.append("<" + key3 + ">" + value3 + "</" + key3 + ">");
                                            });
                                            xmlBuilder.append("</" + key2 + ">");
                                        });
                                    } else {
                                        xmlBuilder.append("<" + key2 + ">" + value2 + "</" + key2 + ">");
                                    }
                                });
                                xmlBuilder.append("</" + key1 + ">");
                            });
                        } else {
                            xmlBuilder.append("<" + key1 + ">" + value1 + "</" + key1+ ">");
                        }
                    });
                    xmlBuilder.append("</" + key + ">");
                }
                else {
                    xmlBuilder.append("<" + key + ">" + value + "</" + key + ">");
                }
            });
            xmlBuilder.append("</parameters>");
        }
        xmlBuilder.append("</ser:" + service + ">");
        xmlBuilder.append("</soapenv:Body>");
        xmlBuilder.append("</soapenv:Envelope>");

        return xmlBuilder.toString();
    }

    private static String postRequest(String url, String xmlBody) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpRequest = new HttpPost(url);
        httpRequest.setHeader("Content-Type", "text/xml");
        StringEntity xmlEntity = new StringEntity(xmlBody);
        httpRequest.setEntity(xmlEntity);
        HttpResponse httpResponse = client.execute(httpRequest);
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
