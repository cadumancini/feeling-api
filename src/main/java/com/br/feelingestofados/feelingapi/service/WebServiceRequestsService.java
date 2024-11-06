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
    @Autowired
    private SOAPClient soapClient;

    public WebServiceRequestsService(EntityManagerFactory factory) {
        super(factory);
    }

    public String fetchEstrutura(String codEmp, String codFil, String codPro,
                                 String codDer, String numPed, String seqIpd, String token) throws Exception {
        HashMap<String, String> params = prepareParamsForEstrutura(codEmp, codFil, codPro, codDer, numPed, seqIpd);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String estruturaXml = soapClient.requestFromSeniorWS("customizado", "Estrutura", user, pswd, "0", params);
        estruturaXml = addAditionalFields(codEmp, numPed, estruturaXml);
        return estruturaXml;
    }

    private String addAditionalFields(String codEmp, String numPed, String estruturaXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
                String codDer = eElement.getElementsByTagName("codDer").item(0).getTextContent();
                JSONObject jObj = new JSONObject(queriesService.findDadosProduto(codEmp, codPro));
                JSONObject jObjDer = new JSONObject(queriesService.findDadosDerivacao(codEmp, codPro, codDer));
                JSONObject jObjProCli = new JSONObject(queriesService.findDescricaoProdCliente(codEmp, codPro, numPed));

                String exiCmp = jObj.getJSONArray("dados").getJSONObject(0).getString("EXICMP");
                String proGen = jObj.getJSONArray("dados").getJSONObject(0).getString("PROGEN");
                String codFam = jObj.getJSONArray("dados").getJSONObject(0).getString("CODFAM");
                String numOri = jObj.getJSONArray("dados").getJSONObject(0).getString("NUMORI");
                String codAgp = jObj.getJSONArray("dados").getJSONObject(0).getString("CODAGP");
                String desNfv = jObj.getJSONArray("dados").getJSONObject(0).getString("DESNFV");
                String codRef = jObjDer.getJSONArray("dados").getJSONObject(0).getString("CODREF");
                String pesBru = jObjDer.getJSONArray("dados").getJSONObject(0).getString("PESBRU");
                String pesLiq = jObjDer.getJSONArray("dados").getJSONObject(0).getString("PESLIQ");
                String volDer = jObjDer.getJSONArray("dados").getJSONObject(0).getString("VOLDER");
                String desCpl = jObjDer.getJSONArray("dados").getJSONObject(0).getString("DESCPL");
                String larDer = jObjDer.getJSONArray("dados").getJSONObject(0).getString("LARDER");
                String altDer = jObjDer.getJSONArray("dados").getJSONObject(0).getString("ALTDER");
                String comDer = jObjDer.getJSONArray("dados").getJSONObject(0).getString("COMDER");
                String desPpc = jObjProCli.getJSONArray("produto").length() > 0 ?
                    jObjProCli.getJSONArray("produto").getJSONObject(0).getString("DESNFV") : desNfv;

                Element eExiCmp = doc.createElement("exiCmp");
                eExiCmp.appendChild(doc.createTextNode(exiCmp));
                Element eProGen = doc.createElement("proGen");
                eProGen.appendChild(doc.createTextNode(proGen));
                Element eCodFam = doc.createElement("codFam");
                eCodFam.appendChild(doc.createTextNode(codFam));
                Element eNumOri = doc.createElement("numOri");
                eNumOri.appendChild(doc.createTextNode(numOri));
                Element eCodAgp = doc.createElement("codAgp");
                eCodAgp.appendChild(doc.createTextNode(codAgp));
                Element eDesNfv = doc.createElement("desNfv");
                eDesNfv.appendChild(doc.createTextNode(desNfv));
                Element eCodRef = doc.createElement("codRef");
                eCodRef.appendChild(doc.createTextNode(codRef));
                Element ePesBru = doc.createElement("pesBru");
                ePesBru.appendChild(doc.createTextNode(pesBru));
                Element ePesLiq = doc.createElement("pesLiq");
                ePesLiq.appendChild(doc.createTextNode(pesLiq));
                Element eLarDer = doc.createElement("larDer");
                eLarDer.appendChild(doc.createTextNode(larDer));
                Element eAltDer = doc.createElement("altDer");
                eAltDer.appendChild(doc.createTextNode(altDer));
                Element eComDer = doc.createElement("comDer");
                eComDer.appendChild(doc.createTextNode(comDer));
                Element eVolDer = doc.createElement("volDer");
                eVolDer.appendChild(doc.createTextNode(volDer));
                Element eDesCpl = doc.createElement("desCpl");
                eDesCpl.appendChild(doc.createTextNode(desCpl));
                Element eDesPpc = doc.createElement("desPpc");
                eDesPpc.appendChild(doc.createTextNode(desPpc));

                eElement.appendChild(eExiCmp);
                eElement.appendChild(eProGen);
                eElement.appendChild(eCodFam);
                eElement.appendChild(eNumOri);
                eElement.appendChild(eCodAgp);
                eElement.appendChild(eDesNfv);
                eElement.appendChild(eCodRef);
                eElement.appendChild(ePesBru);
                eElement.appendChild(ePesLiq);
                eElement.appendChild(eLarDer);
                eElement.appendChild(eAltDer);
                eElement.appendChild(eComDer);
                eElement.appendChild(eVolDer);
                eElement.appendChild(eDesCpl);
                eElement.appendChild(eDesPpc);
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
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
        String response = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams);

        if(response.contains("Credenciais inválidas"))
            return "Credenciais inválidas";
        else {
            String numCad = "";
            String nomOpe = "";
            JSONObject jObj = new JSONObject(queriesService.findOperador(user));
            if(jObj.getJSONArray("operador").length() > 0) {
                numCad = jObj.getJSONArray("operador").getJSONObject(0).getString("NUMCAD");
                nomOpe = jObj.getJSONArray("operador").getJSONObject(0).getString("NOMOPE");
            }

            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);
            TokensManager.getInstance().addToken(hash, user, pswd, numCad, nomOpe);

            return hash;
        }
    }

    public String handlePedido(PedidoWrapper pedidoWrapper, String opePed, String opeIpd, String token) throws IOException {
        HashMap<String, HashMap> params = prepareParamsForPedido(pedidoWrapper, opePed, opeIpd);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        return soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", user, pswd, "0", params);
    }

    public String handlePedido(String codEmp, String codFil, String numPed, String seqIpd, String opePed, String opeIpd, String token) throws IOException {
        HashMap<String, HashMap> params = prepareParamsForPedido(codEmp, codFil, numPed, seqIpd, opePed, opeIpd);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        return soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", user, pswd, "0", params);
    }

    public String updateSitPedido(String codEmp, String codFil, String numPed, String sitPed, String opePed, String token) throws IOException {
        HashMap<String, HashMap> params = prepareParamsForSitPedido(codEmp, codFil, numPed, sitPed, opePed);
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        return soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos_13", user, pswd, "0", params);
    }

    private HashMap<String, HashMap> prepareParamsForPedido(PedidoWrapper pedidoWrapper, String opePed, String opeIpd) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedidoWrapper.getPedido().getCodEmp());
        params.put("codFil", pedidoWrapper.getPedido().getCodFil());
        params.put("numPed", pedidoWrapper.getPedido().getNumPed());
        if(pedidoWrapper.getPedido().getCodCli() != null)
            params.put("codCli", pedidoWrapper.getPedido().getCodCli());
        if(pedidoWrapper.getPedido().getPedCli() != null)
            params.put("pedCli", pedidoWrapper.getPedido().getPedCli().equals(" ") ? "" : pedidoWrapper.getPedido().getPedCli());
        if(pedidoWrapper.getPedido().getCodRep() != null)
            params.put("codRep", pedidoWrapper.getPedido().getCodRep());
        if(pedidoWrapper.getPedido().getCodTra() != null)
            params.put("codTra", pedidoWrapper.getPedido().getCodTra());
        if(pedidoWrapper.getPedido().getCifFob() != null)
            params.put("cifFob", pedidoWrapper.getPedido().getCifFob());
        if(pedidoWrapper.getPedido().getObsPed() != null)
            params.put("obsPed", pedidoWrapper.getPedido().getObsPed());
        if(pedidoWrapper.getPedido().getCodCpg() != null)
            params.put("codCpg", pedidoWrapper.getPedido().getCodCpg());
        if(pedidoWrapper.getPedido().getTnsPro() != null)
            params.put("tnsPro", pedidoWrapper.getPedido().getTnsPro());
        if(pedidoWrapper.getPedido().getNumPed() > 0)
            params.put("opeExe", "C");
        else
            params.put("opeExe", "I");

        if(!pedidoWrapper.getItens().isEmpty()) {
            List<HashMap<String, Object>> listaItens = new ArrayList<>();
            pedidoWrapper.getItens().forEach(itemPedido -> {
                if (itemPedido.getSeqIpd() == 0 || (!itemPedido.getSitIpd().equals("4") && !itemPedido.getSitIpd().equals("5"))) {
                    HashMap<String, Object> paramsItem = new HashMap<>();
                    paramsItem.put("codPro", itemPedido.getCodPro());
                    paramsItem.put("codDer", itemPedido.getCodDer());
                    paramsItem.put("seqIpd", itemPedido.getSeqIpd());
                    paramsItem.put("cplIpd", itemPedido.getCplIpd());
                    paramsItem.put("qtdPed", itemPedido.getQtdPed());
                    paramsItem.put("preUni", String.valueOf(itemPedido.getPreUni()).replace(".", ","));
                    paramsItem.put("seqPcl", itemPedido.getNumCnj());
                    paramsItem.put("datEnt", itemPedido.getDatEnt());
                    paramsItem.put("obsIpd", itemPedido.getObsIpd());
                    paramsItem.put("tnsPro", itemPedido.getTnsPro());
                    paramsItem.put("resEst", "N");
                    paramsItem.put("perCom", String.valueOf(itemPedido.getPerCom()).replace(".", ","));
                    if(itemPedido.getSeqIpd() > 0)
                        paramsItem.put("opeExe", "A");
                    else
                        paramsItem.put("opeExe", "I");
                    listaItens.add(paramsItem);
                }
                else {
                    System.out.println("Item " + itemPedido.getSeqIpd() + " não incluido");
                }
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

    private HashMap<String, HashMap> prepareParamsForSitPedido(String codEmp, String codFil, String numPed, String sitPed, String opePed) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("numPed", numPed);
        params.put("fecPed", sitPed);
        params.put("sitPed", "3");
        params.put("opeExe", opePed);

        HashMap<String, HashMap> paramsPedido = new HashMap<>();
        paramsPedido.put("pedido", params);
        return paramsPedido;
    }

    public String handleContagem(String codEmp, String codPro, String codDer, String codDep, 
                                    String codLot, String qtdMov, String codTns, String token) throws IOException {
        qtdMov = qtdMov.replace('.', ',');
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String retorno = "";

        // mudar empresa
        String params = prepareParamsForMudarEmpresa(codEmp);
        soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", params);
        // realizar contagem
        HashMap<String, HashMap> paramsCont = prepareParamsForContagem(codEmp, codPro, codDer, codDep, codLot, qtdMov, codTns);
        retorno = soapClient.requestFromSeniorWS("com_senior_g5_co_mcm_est_estoques", "MovimentarEstoque", user, pswd, "0", paramsCont);
        
        if (!codEmp.equals("1")) {
            // voltar para empresa 1
            params = prepareParamsForMudarEmpresa("1");
            soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", params);
        }

        return retorno;
    }

    public String executarRelatorio(String codEmp, String codPro, String codDer, String codLot, String qtdCon, String token) throws IOException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);

        // mudar empresa
        String params = prepareParamsForMudarEmpresa(codEmp);
        soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", params);

        // executar relatorio
        params = prepareParamsForRelatorio(codPro, codDer, codLot, qtdCon);
        String retorno = soapClient.requestFromSeniorWS("com_senior_g5_co_ger_relatorio", "Executar", user, pswd, "0", params);
        
        if (!codEmp.equals("1")) {
            // voltar para empresa 1
            params = prepareParamsForMudarEmpresa("1");
            soapClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", params);
        }
        System.out.println("Retorno relatório:");
        System.out.println(retorno);
        return retorno;
    }

    private String prepareParamsForMudarEmpresa(String codEmp) {
        String paramSid = "<SID><param>acao=sid.srv.altempfil</param></SID><SID><param>CODEMP=" + codEmp + "</param></SID>";
        return paramSid;
    }

    private HashMap<String, HashMap> prepareParamsForContagem(String codEmp, String codPro, String codDer, String codDep, 
                                            String codLot, String qtdMov, String codTns) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codPro", codPro);
        params.put("codDer", codDer);
        params.put("codDep", codDep);
        params.put("codLot", codLot);
        params.put("qtdMov", qtdMov);
        params.put("codTns", codTns);

        HashMap<String, HashMap> paramsPedido = new HashMap<>();
        paramsPedido.put("dadosGerais", params);
        return paramsPedido;
    }

    private String prepareParamsForRelatorio(String codPro, String codDer, String codLot, String qtdCon) {
        String paramSid = "<prEntrada><![CDATA[<ECodPro=" + codPro + "><ECodDer=" + codDer + ">" + (!codLot.equals("") ? ("<ECodLot=" + codLot + ">") : ("<EQtdUni=" + qtdCon + "><EQtdImp=1>")) + "]]></prEntrada>" + 
                          "<prEntranceIsXML>F</prEntranceIsXML>" + 
                          "<prRelatorio>" + (!codLot.equals("") ? "MPOP400.GER" : "SEIV300.GER") + "</prRelatorio>" +
                          "<prSaveFormat>tsfPDF</prSaveFormat>" +
                          "<prExecFmt>tefFile</prExecFmt>";
        return paramSid;
    }

    public String fetchGruposEncrypted(String token) throws IOException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);
        String params = "<pmGetUserGroupsUserName>" + user + "</pmGetUserGroupsUserName>";

        String retorno = soapClient.requestFromSeniorWS("MCWFUsers", "GetUserGroups", user, pswd, "0", params);
        return retorno;
    }
}
