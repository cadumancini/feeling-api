package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.entities.PedidoWrapper;
import com.br.feelingestofados.feelingapi.soap.SOAPClient;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManagerFactory;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class WebServiceRequestsService extends FeelingService{
    @Autowired
    private DBQueriesService queriesService;

    public WebServiceRequestsService(EntityManagerFactory factory) {
        super(factory);
    }

    public String fetchEstrutura(String codEmp, String codFil, String codPro,
                                 String codDer, String numPed, String seqIpd, String token) throws Exception {
        HashMap<String, String> params = prepareParamsForEstrutura(codEmp, codFil, codPro, codDer, numPed, seqIpd);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String estruturaXml = SOAPClient.requestFromSeniorWS("customizado", "Estrutura", user, pswd, "0", params);
        estruturaXml = addAditionalFields(codEmp, estruturaXml);
        return estruturaXml;
    }

    private String addAditionalFields(String codEmp, String estruturaXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(estruturaXml.getBytes(StandardCharsets.UTF_8));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("componentes");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String codPro = eElement.getElementsByTagName("codPro").item(0).getTextContent();
                JSONObject jObj = new JSONObject(queriesService.findDadosProduto(codEmp, codPro));

                String exiCmp = jObj.getJSONArray("dados").getJSONObject(0).getString("EXICMP");
                String proGen = jObj.getJSONArray("dados").getJSONObject(0).getString("PROGEN");
                String codFam = jObj.getJSONArray("dados").getJSONObject(0).getString("CODFAM");
                String numOri = jObj.getJSONArray("dados").getJSONObject(0).getString("NUMORI");

                Element eExiCmp = doc.createElement("exiCmp");
                eExiCmp.appendChild(doc.createTextNode(exiCmp));
                Element eProGen = doc.createElement("proGen");
                eProGen.appendChild(doc.createTextNode(proGen));
                Element eCodFam = doc.createElement("codFam");
                eCodFam.appendChild(doc.createTextNode(codFam));
                Element eNumOri = doc.createElement("numOri");
                eNumOri.appendChild(doc.createTextNode(numOri));

                eElement.appendChild(eExiCmp);
                eElement.appendChild(eProGen);
                eElement.appendChild(eCodFam);
                eElement.appendChild(eNumOri);
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    private HashMap<String, String> prepareParamsForEstrutura(String codEmp, String codFil, String codPro, String codDer, String numPed, String seqIpd) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("codPro", codPro);
        params.put("codDer", codDer);
        params.put("numPed", numPed);
        params.put("seqIpd", seqIpd);
        return params;
    }

    public String performLogin(String user, String pswd) throws IOException {
        HashMap<String, String> emptyParams = new HashMap<>();
        String response = SOAPClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams);

        if(response.contains("Credenciais inválidas"))
            return "Credenciais inválidas";
        else {
            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);
            TokensManager.getInstance().addToken(hash, user, pswd);

            return hash;
        }
    }

    public String handlePedido(PedidoWrapper pedidoWrapper, String opePed, String opeIpd, String token) throws IOException {
        HashMap<String, HashMap> params = prepareParamsForPedido(pedidoWrapper, opePed, opeIpd);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        return SOAPClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos", user, pswd, "0", params);
    }

    public String handlePedido(String codEmp, String codFil, String numPed, String seqIpd, String opePed, String opeIpd, String token) throws IOException {
        HashMap<String, HashMap> params = prepareParamsForPedido(codEmp, codFil, numPed, seqIpd, opePed, opeIpd);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        return SOAPClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos", user, pswd, "0", params);
    }

    private HashMap<String, HashMap> prepareParamsForPedido(PedidoWrapper pedidoWrapper, String opePed, String opeIpd) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedidoWrapper.getPedido().getCodEmp());
        params.put("codFil", pedidoWrapper.getPedido().getCodFil());
        params.put("numPed", pedidoWrapper.getPedido().getNumPed());
        if(pedidoWrapper.getPedido().getCodCli() != null)
            params.put("codCli", pedidoWrapper.getPedido().getCodCli());
        if(pedidoWrapper.getPedido().getPedCli() != null)
            params.put("pedCli", pedidoWrapper.getPedido().getPedCli());
        if(pedidoWrapper.getPedido().getCodRep() != null)
            params.put("codRep", pedidoWrapper.getPedido().getCodRep());
        if(pedidoWrapper.getPedido().getCodTra() != null)
            params.put("codTra", pedidoWrapper.getPedido().getCodTra());
        if(pedidoWrapper.getPedido().getCifFob() != null)
            params.put("cifFob", pedidoWrapper.getPedido().getCifFob());
        params.put("opeExe", opePed);

        if(!pedidoWrapper.getItens().isEmpty()) {
            List<HashMap<String, Object>> listaItens = new ArrayList<>();
            pedidoWrapper.getItens().forEach(itemPedido -> {
                HashMap<String, Object> paramsItem = new HashMap<>();
                paramsItem.put("codPro", itemPedido.getCodPro());
                paramsItem.put("codDer", itemPedido.getCodDer());
                paramsItem.put("seqIpd", itemPedido.getSeqIpd());
                paramsItem.put("qtdPed", itemPedido.getQtdPed());
                paramsItem.put("preUni", String.valueOf(itemPedido.getPreUni()).replace(".", ","));
                paramsItem.put("opeExe", opeIpd);
                listaItens.add(paramsItem);
            });
            params.put("produto", listaItens);
        }

        HashMap<String, HashMap> paramsPedido = new HashMap<>();
        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    private HashMap<String, HashMap> prepareParamsForPedido(String codEmp, String codFil, String numPed, String seqIpd, String opePed, String opeIpd) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("numPed", numPed);
        params.put("opeExe", opePed);

        List<HashMap<String, Object>> listaItens = new ArrayList<>();
        HashMap<String, Object> paramsItem = new HashMap<>();
        paramsItem.put("seqIpd", seqIpd);
        paramsItem.put("opeExe", opeIpd);
        listaItens.add(paramsItem);
        params.put("produto", listaItens);

        HashMap<String, HashMap> paramsPedido = new HashMap<>();
        paramsPedido.put("pedido", params);
        return paramsPedido;
    }
}
