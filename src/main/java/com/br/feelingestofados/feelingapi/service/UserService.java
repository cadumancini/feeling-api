package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.util.Base64Decoder;
import com.br.feelingestofados.feelingapi.util.Forms;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class UserService {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    public String getTelasDisponiveis(String token) throws IOException, ParserConfigurationException, SAXException {
        List<String> userGroups = getUserGroups(token);
        List<Forms> telasDisponiveis = new ArrayList<>();

        for (Forms form : Forms.values()) {
            checkIfUserHasAccess(userGroups, telasDisponiveis, form);
        }

        return createJsonFromForms(telasDisponiveis);
    }

    public String telaDisponivel(String token, String tela) throws IOException, ParserConfigurationException, SAXException {
        List<String> userGroups = getUserGroups(token);
        List<Forms> telasDisponiveis = new ArrayList<>();
        Forms form = Forms.valueOfById(tela);

        if (form == null) return "Tela não encontrada";

        checkIfUserHasAccess(userGroups, telasDisponiveis, form);

        return createJsonFromForms(telasDisponiveis);
    }

    private List<String> getUserGroups(String token) throws IOException, ParserConfigurationException, SAXException {
        String userGroupsXml = wsRequestsService.fetchGruposEncrypted(token);
        String userGroupsEncrypted = getUserGroupsFromXml(userGroupsXml);
        String userGroups = Base64Decoder.decode(userGroupsEncrypted);

        String[] groups = userGroups.split(";");
        List<String> groupsList = Arrays.asList(groups);

        return groupsList;
    }

    private String getUserGroupsFromXml(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("pmGetUserGroupsResult");
        Node node = nodeList.item(0);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) node;
            return eElement.getFirstChild().getNodeValue();
        }
        return "";
    }

    private List<Forms> checkIfUserHasAccess(List<String> userGroups, List<Forms> telasDisponiveis, Forms form) {
        for (String group : form.getGroups()) {
            if (group.equals("ALL") || userGroups.contains(group)) {
                telasDisponiveis.add(form);
                break;
            }
        }
        return telasDisponiveis;
    }

    private String createJsonFromForms(List<Forms> forms) {
        JSONArray jsonArray = new JSONArray();
        for(Forms item : forms) {
            jsonArray.put(item.getId());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("telas", jsonArray);
        return jsonObject.toString();
    }
}
