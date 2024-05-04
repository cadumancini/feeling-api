package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.entities.AssistenciaTecnica;
import com.br.feelingestofados.feelingapi.entities.RNC;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DBQueriesService extends FeelingService{
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    public DBQueriesService(EntityManagerFactory factory) {
        super(factory);
    }

    private static final String ANEXOS_PATH = "\\\\feeling.net\\FEELING_DFS\\PUBLIC\\%s\\Anexos\\";
    private static final String ANEXOS_PEDIDOS_PATH = String.format(ANEXOS_PATH, "Pedidos");
    private static final String ANEXOS_SGQ_PATH = String.format(ANEXOS_PATH, "SGQ");
    private static final String ANEXOS_ASS_PATH = String.format(ANEXOS_PATH, "ASS");

    public String findEquivalentes(String emp, String modelo, String componente, String der) {
        String sql = "SELECT DISTINCT A.USU_CMPEQI AS CODPRO, C.CODDER, (B.DESNFV || ' ' || C.DESDER) AS DSCEQI, C.USU_CODREF AS CODREF " + // EQUIVALENTES
                        "FROM USU_T075EQI A, E075PRO B, E075DER C " +
                        "WHERE A.USU_CODEMP = B.CODEMP " +
                        "AND A.USU_CMPEQI = B.CODPRO " +
                        "AND B.CODEMP = C.CODEMP " +
                        "AND B.CODPRO = C.CODPRO " +
                        "AND A.USU_DEREQI = C.CODDER " +
                        "AND A.USU_CODEMP = " + emp + " " +
                        "AND A.USU_CODMOD = '" + modelo + "' " +
                        "AND A.USU_CODCMP = '" + componente + "' " +
                        "AND A.USU_DERCMP = '" + der + "' " +
                        "AND B.SITPRO = 'A' " +
                        "AND C.SITDER = 'A' " +
                        "AND C.CODDER <> 'G' " +
                    "UNION " +
                    "SELECT DISTINCT A.USU_CODCMP AS CODPRO, C.CODDER, (B.DESNFV || ' ' || C.DESDER) AS DSCEQI, C.USU_CODREF AS CODREF " + // TITULAR
                        "FROM USU_T075EQI A, E075PRO B, E075DER C " +
                        "WHERE A.USU_CODEMP = B.CODEMP " +
                        "AND A.USU_CODCMP = B.CODPRO " +
                        "AND B.CODEMP = C.CODEMP " +
                        "AND B.CODPRO = C.CODPRO " +
                        "AND A.USU_DERCMP = C.CODDER " +
                        "AND A.USU_CODEMP = " + emp + " " +
                        "AND A.USU_CODMOD = '" + modelo + "' " +
                        "AND A.USU_CMPEQI = '" + componente + "' " +
                        "AND A.USU_DEREQI = '" + der + "' " +
                        "AND B.SITPRO = 'A' " +
                        "AND C.SITDER = 'A' " +
                        "AND C.CODDER <> 'G' " +
                    "UNION " +
                    "SELECT DISTINCT A.USU_CMPEQI AS CODPRO, C.CODDER, (B.DESNFV || ' ' || C.DESDER) AS DSCEQI, C.USU_CODREF AS CODREF " + // OUTROS EQUIVALENTES CASO JA TENHA ESCOLHIDO UM EQUIVALENTE
                        "FROM USU_T075EQI A, E075PRO B, E075DER C " +
                        "WHERE A.USU_CODEMP = B.CODEMP " +
                        "AND A.USU_CMPEQI = B.CODPRO " +
                        "AND B.CODEMP = C.CODEMP " +
                        "AND B.CODPRO = C.CODPRO " +
                        "AND A.USU_DEREQI = C.CODDER " +
                        "AND A.USU_CODEMP = " + emp + " " +
                        "AND A.USU_CODMOD = '" + modelo + "' " +
                        "AND (A.USU_CODCMP || A.USU_DERCMP) IN (SELECT DISTINCT (EQI.USU_CODCMP || DER.CODDER) " +
                                                                "FROM USU_T075EQI EQI, E075PRO PRO, E075DER DER " +
                                                                "WHERE EQI.USU_CODEMP = PRO.CODEMP " +
                                                                "AND EQI.USU_CODCMP = PRO.CODPRO " +
                                                                "AND PRO.CODEMP = DER.CODEMP " +
                                                                "AND PRO.CODPRO = DER.CODPRO " +
                                                                "AND EQI.USU_DERCMP = DER.CODDER " +
                                                                "AND EQI.USU_CODEMP = " + emp + " " +
                                                                "AND EQI.USU_CODMOD = '" + modelo + "' " +
                                                                "AND EQI.USU_CMPEQI = '" + componente + "' " +
                                                                "AND EQI.USU_DEREQI = '" + der + "') " +
                        "AND (A.USU_CMPEQI || A.USU_DEREQI) <> '" + componente + der + "' " +
                        "AND C.CODDER <> 'G' " +
                        "AND B.SITPRO = 'A' " +
                        "AND C.SITDER = 'A' " +
                    "ORDER BY CODPRO, CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "DSCEQI", "CODREF");
        return createJsonFromSqlResult(results, fields, "equivalentes");
    }

    public String findEquivalentesAdicionais(String emp, String modelo, String componente, String der) {
            String sql = "SELECT DISTINCT A.USU_CODCMP AS CODPRO, C.CODDER, (B.DESNFV || ' ' || C.DESDER) AS DSCEQI, C.USU_CODREF AS CODREF " +
                        "FROM USU_T075EQI A, E075PRO B, E075DER C " +
                        "WHERE A.USU_CODEMP = B.CODEMP " +
                        "AND A.USU_CODCMP = B.CODPRO " +
                        "AND B.CODEMP = C.CODEMP " +
                        "AND B.CODPRO = C.CODPRO " +
                        "AND A.USU_DERCMP = C.CODDER " +
                        "AND A.USU_CODEMP = " + emp + " " +
                        "AND A.USU_CODMOD = '" + modelo + "' " +
                        "AND A.USU_CMPEQI = '" + componente + "' " +
                        "AND A.USU_DEREQI = '" + der + "' " +
                        "AND C.CODDER <> 'G' " +
                        "ORDER BY A.USU_CODCMP, C.CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "DSCEQI", "CODREF");
        return createJsonFromSqlResult(results, fields, "equivalentes");
    }

    public String findEstilos(String codEmp) {
        String sql = "SELECT CODCPR, DESCPR " +
                       "FROM E084CPR " +
                       "WHERE CODEMP = " + codEmp + " " +
                         "AND CODMPR = 'ESTILOS' " +
                         "AND SITCPR = 'A'" +
                       "ORDER BY DESCPR";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODCPR", "DESCPR");
        return createJsonFromSqlResult(results, fields, "estilos");
    }

    public String findProdutosPorEstilo(String codEmp, String estilo) {
        String sql = "SELECT PRO.CODPRO, PRO.DESNFV AS DESPRO, NVL(PRO.USU_MEDMIN, 0) AS MEDMIN, NVL(PRO.USU_MEDMAX, 0) AS MEDMAX " +
                       "FROM E075PRO PRO, E700MOD MOD " +
                      "WHERE PRO.CODEMP = MOD.CODEMP " +
                        "AND PRO.CODMOD = MOD.CODMOD " +
                        "AND PRO.CODEMP = " + codEmp + " " +
                        "AND PRO.CODPRO LIKE '__" + estilo + "%' " +
                        "AND PRO.CODORI = 'ACA'" +
                        "AND PRO.SITPRO = 'A' " +
                        "AND MOD.SITMOD = 'A' " +
                      "ORDER BY DESPRO";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "DESPRO", "MEDMIN", "MEDMAX");
        return createJsonFromSqlResult(results, fields, "produtos");
    }

    public String findDerivacoesPorProduto(String codEmp, String codPro) {
        String sql = "SELECT CODDER, DESDER " +
                       "FROM E075DER " +
                       "WHERE CODEMP = " + codEmp + " " +
                         "AND CODPRO = '" + codPro + "' " +
                         "AND SITDER = 'A' " +
                       "ORDER BY CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODDER", "DESDER");
        return createJsonFromSqlResult(results, fields, "derivacoes");
    }

    public String findClientes(String token) throws Exception {
        int codRep = findRepresentante(token);

        String sql = "SELECT CLI.CODCLI, CLI.NOMCLI, CLI.INTNET, CLI.FONCLI, CLI.CGCCPF, " +
                "(CLI.ENDCLI || ' ' || CLI.CPLEND) AS ENDCPL, (CLI.CIDCLI || '/' || CLI.SIGUFS) AS CIDEST, " +
                "CLI.INSEST " +
                "FROM E085CLI CLI " +
                "WHERE CLI.SITCLI = 'A' ";
        if(codRep > 0)
            sql += "AND EXISTS (SELECT 1 FROM E085HCL HCL WHERE HCL.CODCLI = CLI.CODCLI AND HCL.CODREP = " + codRep + ") ";
        sql += "ORDER BY CLI.CODCLI";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODCLI", "NOMCLI", "INTNET", "FONCLI", "CGCCPF", "ENDCPL", "CIDEST", "INSEST");
        return createJsonFromSqlResult(results, fields, "clientes");
    }

    public String findTransportadoras() {
        String sql = "SELECT CODTRA, NOMTRA FROM E073TRA WHERE SITTRA = 'A' ORDER BY CODTRA";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODTRA", "NOMTRA");
        return createJsonFromSqlResult(results, fields, "transportadoras");
    }

    public String findDadosCliente(String codCli) {
        String sql = "SELECT HCL.CODEMP, HCL.CODREP, HCL.CODTRA, EMP.NOMEMP, REP.NOMREP, TRA.NOMTRA, HRP.PERCOM, HCL.CIFFOB, " +
                            "HCL.PERDS1, HCL.PERDS2, HCL.PERDS3, HCL.PERDS4, HCL.PERDS5, CLI.USU_PERGUE AS PERGUE, HCL.CODCPG, CPG.DESCPG " +
                       "FROM E085HCL HCL, E070EMP EMP, E090REP REP, E073TRA TRA, E085CLI CLI, E090HRP HRP, E028CPG CPG " +
                      "WHERE HCL.CODEMP = EMP.CODEMP " +
                        "AND HCL.CODREP = REP.CODREP " +
                        "AND HCL.CODTRA = TRA.CODTRA " +
                        "AND HCL.CODCLI = CLI.CODCLI " +
                        "AND HCL.CODEMP = HRP.CODEMP " +
                        "AND HCL.CODREP = HRP.CODREP " +
                        "AND HCL.CODEMP = CPG.CODEMP " +
                        "AND HCL.CODCPG = CPG.CODCPG " +
                        "AND HCL.CODFIL = 1 " +
                        "AND HCL.CODCLI = " + codCli + " " +
                      "ORDER BY HCL.CODEMP";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "CODREP", "CODTRA", "NOMEMP", "NOMREP", "NOMTRA", "PERCOM",
                "PERDS1", "PERDS2", "PERDS3", "PERDS4", "PERDS5", "PERGUE", "CIFFOB", "CODCPG", "DESCPG");
        return createJsonFromSqlResult(results, fields, "dadosCliente");
    }

    public String findPedido(String emp, String fil, String ped) {
        String sql = "SELECT CPG.DESCPG, TO_CHAR((SELECT MAX(IPD.DATENT) " +
                                                   "FROM E120IPD IPD " +
                                                   "WHERE IPD.CODEMP = PED.CODEMP " +
                                                   "AND IPD.CODFIL = PED.CODFIL " +
                                                   "AND IPD.NUMPED = PED.NUMPED), 'DD/MM/YYYY') AS DATENT, " +
                             "PED.SITPED, PED.PEDCLI, PED.USU_PEDREP AS PEDREP, PED.CODCLI, PED.CODEMP, PED.CODREP, PED.CODTRA, " +
                             "PED.CIFFOB, PED.OBSPED, PED.TNSPRO, TNS.VENIPI, PED.CODMOT, NVL(PED.USU_PEDFEI, 'N') AS PEDFEI, NVL(PED.PEDBLO, 'N') AS PEDBLO, " +
                             "CASE WHEN EXISTS (SELECT 1 FROM E120IPD IPD WHERE IPD.CODEMP = PED.CODEMP AND IPD.CODFIL = PED.CODFIL " +
                                                   "AND IPD.NUMPED = PED.NUMPED AND NVL(IPD.USU_ENVEMP, 'N') = 'S') " +
                             "THEN 'S' ELSE 'N' END AS PEDENV, " +
                             "CASE WHEN EXISTS (SELECT 1 FROM E120IPD IPD WHERE IPD.CODEMP = PED.CODEMP AND IPD.CODFIL = PED.CODFIL " +
                                                   "AND IPD.NUMPED = PED.NUMPED AND NVL(IPD.USU_ENVEMP, 'N') IN ('N', ' ')) " +
                             "THEN 'S' ELSE 'N' END AS PEDABE " +                           
                       "FROM E120PED PED, E028CPG CPG, E001TNS TNS " +
                      "WHERE PED.CODEMP = CPG.CODEMP " +
                        "AND PED.CODCPG = CPG.CODCPG " +
                        "AND PED.CODEMP = TNS.CODEMP " +
                        "AND PED.TNSPRO = TNS.CODTNS " +
                        "AND PED.CODEMP = " + emp + " " +
                        "AND PED.CODFIL = " + fil + " " +
                        "AND PED.NUMPED = " + ped;
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("DESCPG", "DATENT", "SITPED", "PEDCLI", "PEDREP", "CODCLI", "CODEMP",
                "CODREP", "CODTRA", "CIFFOB", "OBSPED", "TNSPRO", "VENIPI", "CODMOT", "PEDFEI", "PEDENV", "PEDABE", "PEDBLO");
        return createJsonFromSqlResult(results, fields, "pedido");
    }

    public void bloquearPedido(String emp, String fil, String ped) throws Exception {
        String sql = "UPDATE E120PED SET PEDBLO = 'S', OBSPED = 'Bloqueado antes da inserção de novo item' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120PED) ao setar campos PEDBLO e OBSPED.");
        }
    }

    public String findDescricaoProdCliente(String emp, String pro, String ped) {
        String sql = "SELECT PPC.DESNFV FROM E075PPC PPC, E120PED PED " +
                      "WHERE PPC.CODEMP = PED.CODEMP " +
                        "AND PPC.CODCLI = PED.CODCLI " +
                        "AND PPC.CODEMP = " + emp + " " +
                        "AND PED.NUMPED = " + ped + " " +
                        "AND PPC.CODPRO = '" + pro + "'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = List.of("DESNFV");
        return createJsonFromSqlResult(results, fields, "produto");
    }

    public String findCondicoesPagto(String emp) {
        String sql = "SELECT CPG.CODCPG, CPG.DESCPG FROM E028CPG CPG WHERE CODEMP = " + emp + " ORDER BY CPG.DESCPG";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODCPG", "DESCPG");
        return createJsonFromSqlResult(results, fields, "condicoes");
    }

    public String findPedidosClientes(String codCli) {
        String sql = "SELECT PED.CODEMP, PED.PEDCLI, PED.NUMPED, TO_CHAR(PED.DATEMI, 'DD/MM/YYYY') AS DATEMI, " +
                            "CLI.NOMCLI, REP.NOMREP " +
                        "FROM E120PED PED, E085CLI CLI, E090REP REP " +
                        "WHERE PED.CODCLI = CLI.CODCLI " +
                        "AND PED.CODREP = REP.CODREP " +
                        "AND PED.CODCLI = " + codCli + " " +
                        "ORDER BY PED.NUMPED";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "PEDCLI", "NUMPED", "DATEMI", "NOMCLI", "NOMREP");
        return createJsonFromSqlResult(results, fields, "pedidos");
    }

    public String findPedidosUsuario() {
        String sql = "SELECT PED.CODEMP, PED.PEDCLI, PED.USU_PEDREP AS PEDREP, PED.NUMPED, TO_CHAR(PED.DATEMI, 'DD/MM/YYYY') AS DATEMI, " +
                            "CLI.NOMCLI, REP.NOMREP, TRA.NOMTRA, PED.CODCLI, CLI.INTNET, CLI.FONCLI, CLI.CGCCPF, " +
                            "(CLI.ENDCLI || ' ' || CLI.CPLEND) AS ENDCPL, (CLI.CIDCLI || '/' || CLI.SIGUFS) AS CIDEST, CLI.INSEST, PED.TNSPRO, TNS.VENIPI, PED.CODFIL " +
                        "FROM E120PED PED, E085CLI CLI, E090REP REP, E073TRA TRA, E001TNS TNS " +
                        "WHERE PED.CODCLI = CLI.CODCLI " +
                        "AND PED.CODREP = REP.CODREP " +
                        "AND PED.CODTRA = TRA.CODTRA " +
                        "AND PED.CODEMP = TNS.CODEMP " +
                        "AND PED.TNSPRO = TNS.CODTNS " +
                        "AND PED.SITPED IN (1,2,3,9) " +
                        "ORDER BY PED.NUMPED";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "CODFIL", "PEDCLI", "PEDREP", "NUMPED", "DATEMI", "NOMCLI", "NOMREP", "NOMTRA", "CODCLI",
                                                "INTNET", "FONCLI", "CGCCPF", "ENDCPL", "CIDEST", "INSEST", "TNSPRO", "VENIPI");
        return createJsonFromSqlResult(results, fields, "pedidos");
    }

    public String findTransacoes(String emp) {
        String sql = "SELECT CODTNS, DESTNS, DETTNS, VENIPI FROM E001TNS WHERE CODEMP = " + emp + " AND SITTNS = 'A' AND LISMOD = 'VEP' ORDER BY CODTNS";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODTNS", "DESTNS", "DETTNS", "VENIPI");
        return createJsonFromSqlResult(results, fields, "transacoes");
    }

    public String enviarPedidoEmpresa(String emp, String fil, String ped, String token) throws Exception {
        String pesoCubagem = "";
        double pesTotBru = 0d;
        double pesTotLiq = 0d;
        double volTot = 0d;
        // Checar se na estrutura de algum item existe algum item com CodDer = 'G' ou ProGen = 'S'
        String itensPedido = this.findItensPedido(emp, fil, ped);
        JSONArray itens = new JSONObject(itensPedido).getJSONArray("itens");
        for(int i = 0; i < itens.length(); i++) {
            JSONObject item = itens.getJSONObject(i);
            if (!item.getString("SITIPD").equals("5") && item.getString("TEMORP").equals("N")) {
                String seqIpd = item.getString("SEQIPD");
                String codPro = item.getString("CODPRO");
                String codDer = item.getString("CODDER");
                String desPro = item.getString("DESPRO");
                String desDer = item.getString("DESDER");

                // Buscando estrutura
                String estrutura = wsRequestsService.fetchEstrutura(emp, fil, codPro, codDer, ped, seqIpd, token);

                boolean temErro = false;
                StringBuilder erros = new StringBuilder();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                ByteArrayInputStream input = new ByteArrayInputStream(estrutura.getBytes(StandardCharsets.UTF_8));
                Document doc = builder.parse(input);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("componentes");
                for (int cmp = 0; cmp < nList.getLength(); cmp++) {
                    Node nNode = nList.item(cmp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        String codFam = eElement.getElementsByTagName("codFam").item(0).getTextContent();
                        String proGen = eElement.getElementsByTagName("proGen").item(0).getTextContent();
                        String desProEst = eElement.getElementsByTagName("desPro").item(0).getTextContent();
                        String codProEst = eElement.getElementsByTagName("codPro").item(0).getTextContent();
                        String codDerEst = eElement.getElementsByTagName("codDer").item(0).getTextContent();
                        if(codFam.equals("15001")) {
                            pesTotBru += Double.parseDouble(eElement.getElementsByTagName("pesBru").item(0).getTextContent());
                            pesTotLiq += Double.parseDouble(eElement.getElementsByTagName("pesLiq").item(0).getTextContent());
                            volTot += Double.parseDouble(eElement.getElementsByTagName("volDer").item(0).getTextContent());

                            String sql = "UPDATE E120IPD SET USU_PESBRU = " + Double.valueOf(eElement.getElementsByTagName("pesBru").item(0).getTextContent()) +
                                    ", USU_PESLIQ = " + Double.valueOf(eElement.getElementsByTagName("pesLiq").item(0).getTextContent()) +
                                    ", USU_VOLDER = " + Double.valueOf(eElement.getElementsByTagName("volDer").item(0).getTextContent()) +
                                    " WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + seqIpd;
                            int rowsAffected = executeSqlStatement(sql);
                            if (rowsAffected == 0) {
                                throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar os valores em USU_PESLIQ, USU_PESBRU e USU_VOLDER.");
                            }
                        }
                        if(proGen.equals("S") || codDerEst.equals("G") || codDerEst.equals("GM")) {
                            temErro = true;
                            erros.append("-> ").append(codProEst).append(" (").append(desProEst).append(") - der. ").append(codDerEst).append("\n");
                        }
                    }
                }
                if(temErro) {
                    return "O item (seq. " + seqIpd + ")" + desPro + " " + desDer + " possui as seguintes pendências na estrutura. Verifique!\n\n" + erros;
                }
            }
        }

        String sql = "UPDATE E120IPD SET USU_ENVEMP = 'S' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120PED) ao setar campo CODMOT com valor 75.");
        }

        JSONObject jObj = new JSONObject();
        jObj.put("pesoTotalBruto", Double.toString(pesTotBru));
        jObj.put("pesoTotalLiq", Double.toString(pesTotLiq));
        jObj.put("volumeTotal", Double.toString(volTot));
        pesoCubagem = jObj.toString();
        return pesoCubagem;
    }

    public String findItensPedido(String emp, String fil, String ped) {
        String sql = "SELECT IPD.SEQIPD, IPD.CODPRO, IPD.CODDER, IPD.QTDPED, (PRO.DESNFV || ' ' || DER.DESDER) AS DSCPRO, " +
                            "PRO.DESPRO, DER.DESDER, IPD.PERDSC, IPD.PERCOM, IPD.OBSIPD, IPD.GERNEC, " +
                            "IPD.SEQPCL, TO_CHAR(IPD.DATENT, 'DD/MM/YYYY') AS DATENT, IPD.PREUNI AS VLRIPD, " +
                            "CPR.CODCPR, CPR.DESCPR, NVL(IPD.USU_LARDER, 0) AS LARDER, (DER.PESLIQ * IPD.QTDPED) AS PESIPD, " +
                            "((DER.VOLDER / 100) * IPD.QTDPED) AS VOLIPD, " +
                            "CASE WHEN TNS.VENIPI = 'S' THEN 0 ELSE ((PRO.PERIPI / 100) * (IPD.PREUNI * IPD.QTDPED)) END AS IPIIPD, " +
                            "((IPD.PERICM / 100) * (IPD.PREUNI * IPD.QTDPED)) AS ICMIPD, " +
                            "(((IPD.PERIPI / 100) * (IPD.PREUNI * IPD.QTDPED)) + (IPD.PREUNI * IPD.QTDPED)) AS NFVIPD, " +
                            "NVL(IPD.USU_MEDESP, 'N') AS CMED, NVL(IPD.USU_DSCESP, 'N') AS CDES, NVL(IPD.USU_PGTESP, 'N') AS CPAG, " +
                            "NVL(IPD.USU_PRZESP, 'N') AS CPRA, NVL(IPD.USU_OUTESP, 'N') AS COUT, " +
                            "NVL(IPD.PERDS1, 0) AS PERDS1, NVL(IPD.PERDS2, 0) AS PERDS2, NVL(IPD.PERDS3, 0) AS PERDS3, " +
                            "NVL(IPD.PERDS4, 0) AS PERDS4, NVL(IPD.PERDS5, 0) AS PERDS5, NVL(IPD.USU_PERGUE, 0) AS PERGUE, " +
                            "NVL(IPD.USU_VLRRET, 0) AS VLRRET, NVL(PRO.USU_MEDMIN, 0) AS MEDMIN, NVL(PRO.USU_MEDMAX, 0) AS MEDMAX, " +
                            "(NVL(IPD.USU_PESLIQ, 0) * IPD.QTDPED) AS PESLIQ, (NVL(IPD.USU_PESBRU, 0) * IPD.QTDPED) AS PESBRU, " +
                            "(NVL(IPD.USU_VOLDER, 0) * IPD.QTDPED) AS VOLDER, IPD.TNSPRO, TNS.VENIPI, IPD.SITIPD, " +
                            "(CASE WHEN EXISTS (SELECT 1 FROM E900COP COP, E900QDO QDO " + 
                                                "WHERE COP.CODEMP = QDO.CODEMP " +
                                                  "AND COP.CODFIL = IPD.CODFIL " + 
                                                  "AND COP.CODORI = QDO.CODORI " +
                                                  "AND COP.NUMORP = QDO.NUMORP " +
                                                  "AND COP.CODEMP = IPD.CODEMP " +
                                                  "AND COP.NUMPED = IPD.NUMPED " +
                                                  "AND QDO.SEQIPD = IPD.SEQIPD " +
                                                  "AND COP.SITORP IN ('A','L','E','F')) THEN 'S' ELSE 'N' END) AS TEMORP, " +
                            "(CASE WHEN NVL(IPD.USU_ENVEMP, 'N') IN ('N', ' ') THEN 'N' ELSE 'S' END) AS IPDENV " + 
                       "FROM E120IPD IPD, E075PRO PRO, E075DER DER, E084CPR CPR, E001TNS TNS " +
                      "WHERE IPD.CODEMP = PRO.CODEMP " +
                        "AND IPD.CODPRO = PRO.CODPRO " +
                        "AND IPD.CODEMP = DER.CODEMP " +
                        "AND IPD.CODPRO = DER.CODPRO " +
                        "AND IPD.CODDER = DER.CODDER " +
                        "AND IPD.CODEMP = CPR.CODEMP " +
                        "AND IPD.CODEMP = TNS.CODEMP " +
                        "AND IPD.TNSPRO = TNS.CODTNS " +
                        "AND CPR.CODCPR = SUBSTR(IPD.CODPRO, 3, 4) " +
                        "AND CPR.CODMPR = 'ESTILOS' " +
                        "AND IPD.CODEMP = " + emp + " " +
                        "AND IPD.CODFIL = " + fil + " " +
                        "AND IPD.NUMPED = " + ped + " " +
                      "ORDER BY IPD.SEQIPD";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("SEQIPD", "CODPRO", "CODDER", "QTDPED", "DSCPRO", "DESPRO", "DESDER",
                "PERDSC", "PERCOM", "OBSIPD", "SEQPCL", "DATENT", "VLRIPD", "CODCPR", "DESCPR", "LARDER",
                "PESIPD", "VOLIPD", "IPIIPD", "ICMIPD", "NFVIPD", "CMED", "CDES", "CPAG", "CPRA", "COUT",
                "PERDS1", "PERDS2", "PERDS3", "PERDS4", "PERDS5", "PERGUE", "VLRRET", "MEDMIN", "MEDMAX",
                "PESLIQ", "PESBRU", "VOLDER", "TNSPRO", "VENIPI", "SITIPD", "TEMORP", "IPDENV", "GERNEC");
        String itens = createJsonFromSqlResult(results, fields, "itens");

        JSONArray itensJson = new JSONObject(itens).getJSONArray("itens");
        for(int i = 0; i < itensJson.length(); i++) {
            JSONObject item = itensJson.getJSONObject(i);
            String seqIpd = item.getString("SEQIPD");
            // verificar se o item do pedido possui anexo
            File files = new File(ANEXOS_PEDIDOS_PATH);
            FilenameFilter filter = (dir, name) -> name.startsWith(emp + "-" + fil + "-" + ped + "-" + seqIpd);
            String[] fileNames = files.list(filter);
            String temAnexo = "N";
            if(fileNames != null) {
                if (files.list(filter).length > 0) {
                    temAnexo = "S";
                }
            }
            itensJson.getJSONObject(i).put("TEMANX", temAnexo);
        }
        JSONObject itensRetornar = new JSONObject();
        itensRetornar.put("itens", itensJson);
        return itensRetornar.toString();
    }

    public String findDadosProduto(String emp, String pro) {
        String sql = "SELECT NVL(FAM.USU_EXICMP, 'N') AS EXICMP, NVL(PRO.USU_PROGEN, 'N') AS PROGEN, PRO.CODFAM, PRO.NUMORI, PRO.CODAGP, PRO.DESNFV " +
                "FROM E075PRO PRO, E012FAM FAM " +
                "WHERE PRO.CODEMP = FAM.CODEMP " +
                "AND PRO.CODFAM = FAM.CODFAM " +
                "AND PRO.CODEMP = " + emp + " " +
                "AND PRO.CODPRO = '" + pro + "'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("EXICMP", "PROGEN", "CODFAM", "NUMORI", "CODAGP", "DESNFV");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String findDadosDerivacao(String emp, String pro, String der) {
        String sql = "SELECT DER.CODPRO, DER.CODDER, DER.USU_CODREF AS CODREF, DER.PESBRU, DER.PESLIQ, DER.VOLDER, DER.DESCPL, " +
                            "PRO.CPLPRO, PRO.DESNFV, DER.DESDER, DER.DEPPAD AS DEPDER, PRO.DEPPAD AS DEPPRO, FAM.DEPPAD AS DEPFAM, " +
                            "ORI.DEPPAD AS DEPORI, PRO.UNIMED " +
                "FROM E075DER DER, E075PRO PRO, E012FAM FAM, E083ORI ORI " +
                "WHERE DER.CODEMP = PRO.CODEMP " +
                "AND DER.CODPRO = PRO.CODPRO " +
                "AND PRO.CODEMP = FAM.CODEMP " +
                "AND PRO.CODFAM = FAM.CODFAM " +
                "AND PRO.CODEMP = ORI.CODEMP " +
                "AND PRO.CODORI = ORI.CODORI " +
                "AND DER.CODEMP = " + emp + " " +
                "AND DER.CODPRO = '" + pro + "' " +
                "AND DER.CODDER = '" + der + "'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "CODREF", "PESBRU", "PESLIQ", "VOLDER", "DESCPL",
                                    "CPLPRO", "DESNFV", "DESDER", "DEPDER", "DEPPRO", "DEPFAM", "DEPORI", "UNIMED");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String findDadosLote(String emp, String lote) {
        String sql = "SELECT DLS.CODPRO, DLS.CODDER " +
                "FROM E210DLS DLS " +
                "WHERE DLS.CODLOT = '" + lote + "' " +
                "AND ROWNUM = 1";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER");
        String resultString = createJsonFromSqlResult(results, fields, "dados");

        JSONObject jObj = new JSONObject(resultString);
        if(jObj.getJSONArray("dados").length() > 0) {
            String codPro = jObj.getJSONArray("dados").getJSONObject(0).getString("CODPRO");
            String codDer = jObj.getJSONArray("dados").getJSONObject(0).getString("CODDER");
            return this.findDadosDerivacao(emp, codPro, codDer);
        } else {
            return resultString;
        }
    }

    public String findDepositosLigados(String pro, String der) {
        String sql = "SELECT DISTINCT CODDEP FROM E210EST WHERE CODPRO = '" + pro + "' AND CODDER = '" + der + "' AND SITEST = 'A' ORDER BY CODDEP";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODDEP");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String findQtdeEstoque(String pro, String der, String dep) {
        String sql = "SELECT NVL(SUM(QTDEST), 0) AS QTDEST FROM E210EST WHERE CODPRO = '" + pro + "' AND CODDER = '" + der + "' AND CODDEP = '" + dep + "' AND SITEST = 'A'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("QTDEST");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String findEstoqueLote(String lot) {
        String sql = "SELECT QTDEST, CODEMP FROM E210DLS WHERE CODLOT = '" + lot + "'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("QTDEST", "CODEMP");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String findQtdeEstoque(String emp, String pro, String der, String dep) {
        String sql = "SELECT NVL(SUM(QTDEST), 0) AS QTDEST FROM E210EST WHERE CODEMP = " + emp + " AND CODPRO = '" + pro + "' AND CODDER = '" + der + "' AND CODDEP = '" + dep + "' AND SITEST = 'A'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("QTDEST");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String movimentarEstoque(String pro, String der, String codLot, String depOri, String depDes, String qtdCon, String token) throws IOException {
        String retorno = "";
        if(!codLot.equals("")) {
            String resultLote = this.findEstoqueLote(codLot);

            // ver em qual empresa está o lote
            String qtdEstLote = new JSONObject(resultLote).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
            String empLote = new JSONObject(resultLote).getJSONArray("dados").getJSONObject(0).getString("CODEMP");

            if (depOri.equals(depDes)) {
                if(empLote.equals("1")) {
                    // movientar emp1
                    qtdCon = qtdCon.replace(',', '.');
                    Double qtdMov = Double.parseDouble(qtdEstLote) - Double.parseDouble(qtdCon);
                    String codTns = "";
                    if (qtdMov != 0.0) {
                        codTns = qtdMov > 0.0 ? "90255" : "90205";
                        qtdMov = Math.abs(qtdMov);
                    }
                    retorno = wsRequestsService.handleContagem("1", pro, der, depOri, codLot, qtdMov.toString(), codTns, token);

                } else {
                    //   zerar empresa atual do lote com qtd atual
                    retorno = wsRequestsService.handleContagem(empLote, pro, der, depOri, codLot, qtdEstLote, "90255", token);

                    //   movimentar emp1 com lote com qtd contada
                    qtdCon = qtdCon.replace(',', '.');
                    Double qtdMov = Double.parseDouble(qtdCon);
                    String codTns = "90205";
                    retorno = wsRequestsService.handleContagem("1", pro, der, depOri, codLot, qtdMov.toString(), codTns, token);
                }
            } else {
                // zerar empresa atual no depOri
                retorno = wsRequestsService.handleContagem(empLote, pro, der, depOri, codLot, qtdEstLote, "90255", token);

                // movientar emp1 no depDes
                qtdCon = qtdCon.replace(',', '.');
                Double qtdMov = Double.parseDouble(qtdCon);
                String codTns = "90205";
                retorno = wsRequestsService.handleContagem("1", pro, der, depDes, codLot, qtdMov.toString(), codTns, token);
            }

        } else {
            String estEmp1Json = this.findQtdeEstoque("1", pro, der, depOri);
            String estEmp2Json = this.findQtdeEstoque("2", pro, der, depOri);
            String estEmp3Json = this.findQtdeEstoque("3", pro, der, depOri);
            String estEmp5Json = this.findQtdeEstoque("5", pro, der, depOri);

            String qtdEstEmp1 = new JSONObject(estEmp1Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
            String qtdEstEmp2 = new JSONObject(estEmp2Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
            String qtdEstEmp3 = new JSONObject(estEmp3Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
            String qtdEstEmp5 = new JSONObject(estEmp5Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");

            if (depOri.equals(depDes)) {
                // zerar estoque emp2
                if (!qtdEstEmp2.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp2);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("2", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp3
                if (!qtdEstEmp3.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp3);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("3", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp5
                if (!qtdEstEmp5.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp5);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("5", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }
                
                // movimentar emp1
                qtdCon = qtdCon.replace(',', '.');
                Double qtdMov = Double.parseDouble(qtdEstEmp1) - Double.parseDouble(qtdCon);
                String codTns = "";
                if (qtdMov != 0.0) {
                    codTns = qtdMov > 0.0 ? "90255" : "90205";
                    qtdMov = Math.abs(qtdMov);
                }
                retorno = wsRequestsService.handleContagem("1", pro, der, depOri, codLot, qtdMov.toString(), codTns, token);
                
            } else {
                // zerar estoque emp1 no depOri
                if (!qtdEstEmp1.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp1);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("1", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp2 no depOri
                if (!qtdEstEmp2.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp2);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("2", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp3 no depOri
                if (!qtdEstEmp3.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp3);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("3", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp5 no depOri
                if (!qtdEstEmp5.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp5);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("5", pro, der, depOri, codLot, qtdMov.toString(), tnsEst, token);
                }
                
                estEmp1Json = this.findQtdeEstoque("1", pro, der, depDes);
                estEmp2Json = this.findQtdeEstoque("2", pro, der, depDes);
                estEmp3Json = this.findQtdeEstoque("3", pro, der, depDes);
                estEmp5Json = this.findQtdeEstoque("5", pro, der, depDes);

                qtdEstEmp1 = new JSONObject(estEmp1Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
                qtdEstEmp2 = new JSONObject(estEmp2Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
                qtdEstEmp3 = new JSONObject(estEmp3Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");
                qtdEstEmp5 = new JSONObject(estEmp5Json).getJSONArray("dados").getJSONObject(0).getString("QTDEST");

                // zerar estoque emp2 no depDes
                if (!qtdEstEmp2.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp2);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("2", pro, der, depDes, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp3 no depDes
                if (!qtdEstEmp3.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp3);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("3", pro, der, depDes, codLot, qtdMov.toString(), tnsEst, token);
                }

                // zerar estoque emp5 no depDes
                if (!qtdEstEmp5.equals("0")) {
                    String tnsEst = "90255";
                    Double qtdMov = Double.parseDouble(qtdEstEmp5);
                    if (qtdMov < 0.0) {
                        qtdMov = Math.abs(qtdMov);
                        tnsEst = "90205";
                    }
                    retorno = wsRequestsService.handleContagem("5", pro, der, depDes, codLot, qtdMov.toString(), tnsEst, token);
                }
                
                // movimentar emp1 no depDes:
                qtdCon = qtdCon.replace(',', '.');
                Double qtdMov = Double.parseDouble(qtdEstEmp1) - Double.parseDouble(qtdCon);
                String codTns = "";
                if (qtdMov != 0.0) {
                    codTns = qtdMov > 0.0 ? "90255" : "90205";
                    qtdMov = Math.abs(qtdMov);
                }
                retorno = wsRequestsService.handleContagem("1", pro, der, depDes, codLot, qtdMov.toString(), codTns, token);
                
            }
        }

        if (retorno.contains("<mensagemRetorno>Processado com Sucesso.</mensagemRetorno>") && !codLot.equals("")) {
            // chamar impressao de etiqueta
            wsRequestsService.executarRelatorio("1", pro, der, codLot, qtdCon, token);
        }
        return retorno;
    }

    public String findDerivacoesPossiveis(String emp, String pro, String mod, String derMod) {
        String sql = "SELECT DER.CODPRO, DER.CODDER, (PRO.DESNFV || ' ' || DER.DESDER) AS DSCEQI, DER.USU_CODREF AS CODREF " +
                       "FROM E075DER DER, E075PRO PRO " +
                      "WHERE DER.CODEMP = PRO.CODEMP " +
                        "AND DER.CODPRO = PRO.CODPRO " +
                        "AND DER.CODEMP = " + emp + " " +
                        "AND DER.CODPRO = '" + pro + "' " +
                        "AND DER.CODDER <> 'G' " +
                        "AND DER.SITDER = 'A' " +
                        "AND EXISTS (SELECT 1 FROM E700CTM CTM " +
                                     "WHERE CTM.CODEMP = DER.CODEMP " +
                                       "AND CTM.CODMOD = '" + mod + "' " +
                                       "AND CTM.CODDER = '" + derMod + "' " +
                                       "AND CTM.CODCMP = DER.CODPRO " +
                                       "AND CTM.DERCMP = 'G') " +
                      "ORDER BY DER.CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "DSCEQI", "CODREF");
        return createJsonFromSqlResult(results, fields, "derivacoes");
    }

    private String findDadosEquivalente(String codEmp, String codMod, String derMod, String codCmp, String derCmp) {
        String sql = "SELECT CTM.CODETG, CTM.SEQMOD, CTM.QTDUTI, CTM.QTDFRQ, " +
                            "CTM.PERPRD, CTM.PRDQTD, CTM.UNIME2, CMM.TIPQTD, " +
                            "CMM.CODCCU, PRO.CODPRO " +
                       "FROM E700CMM CMM, E700CTM CTM, E075PRO PRO " +
                      "WHERE CMM.CODEMP = CTM.CODEMP " +
                        "AND CMM.CODMOD = CTM.CODMOD " +
                        "AND CMM.CODETG = CTM.CODETG " +
                        "AND CMM.SEQMOD = CTM.SEQMOD " +
                        "AND CMM.CODCMP = CTM.CODCMP " +
                        "AND CTM.CODEMP = PRO.CODEMP " +
                        "AND CTM.CODMOD = PRO.CODMOD " +
                        "AND CTM.CODEMP = " + codEmp + " " +
                        "AND CTM.CODMOD = '" + codMod + "' " +
                        "AND CTM.CODDER = '" + derMod + "' " +
                        "AND CTM.CODCMP = '" + codCmp + "' " +
                        "AND CTM.DERCMP = '" + derCmp + "'";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODETG", "SEQMOD", "QTDUTI", "QTDFRQ", "PERPRD", "PRDQTD",
                "UNIME2", "TIPQTD", "CODCCU", "CODPRO");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    private String findDadosEquivalenteBySeqMod(String codEmp, String codMod, String derMod, int seqMod) {
        String sql = "SELECT CTM.CODETG, CTM.SEQMOD, CTM.QTDUTI, CTM.QTDFRQ, " +
                "CTM.PERPRD, CTM.PRDQTD, CTM.UNIME2, CMM.TIPQTD, " +
                "CMM.CODCCU, PRO.CODPRO " +
                "FROM E700CMM CMM, E700CTM CTM, E075PRO PRO " +
                "WHERE CMM.CODEMP = CTM.CODEMP " +
                "AND CMM.CODMOD = CTM.CODMOD " +
                "AND CMM.CODETG = CTM.CODETG " +
                "AND CMM.SEQMOD = CTM.SEQMOD " +
                "AND CMM.CODCMP = CTM.CODCMP " +
                "AND CTM.CODEMP = PRO.CODEMP " +
                "AND CTM.CODMOD = PRO.CODMOD " +
                "AND CTM.CODEMP = " + codEmp + " " +
                "AND CTM.CODMOD = '" + codMod + "' " +
                "AND CTM.CODDER = '" + derMod + "' " +
                "AND CTM.SEQMOD = " + seqMod;

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODETG", "SEQMOD", "QTDUTI", "QTDFRQ", "PERPRD", "PRDQTD",
                "UNIME2", "TIPQTD", "CODCCU", "CODPRO");
        return createJsonFromSqlResult(results, fields, "dados");
    }
    
    private String findUsuario(String nomUsu) {
        String sql = "SELECT CODUSU FROM R999USU WHERE UPPER(NOMUSU) = UPPER('" + nomUsu + "')";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODUSU");
        return createJsonFromSqlResult(results, fields, "usuario");
    }
    
    public String findOperador(String nomUsu) {
        String sql = "SELECT E906.NUMCAD, E906.NOMOPE " +
                       "FROM R999USU R999, E099USU E099, E906OPE E906 " +
                      "WHERE R999.CODUSU = E099.CODUSU " +
                        "AND E099.NUMCAD = E906.NUMCAD " +
                        "AND UPPER(R999.NOMUSU) = UPPER('" + nomUsu + "') " +
                        "AND E906.CODEMP = 1 " +
                        "AND ROWNUM = 1";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("NUMCAD", "NOMOPE");
        return createJsonFromSqlResult(results, fields, "operador");
    }

    public String findCadastro(String token) {
        String numCad = TokensManager.getInstance().getNumCadFromToken(token);
        String nomOpe = TokensManager.getInstance().getNomOpeFromToken(token);
        
        JSONObject objCadastro = new JSONObject();
        objCadastro.put("NUMCAD", numCad);
        objCadastro.put("NOMOPE", nomOpe);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(objCadastro);

        JSONObject objReturn = new JSONObject();
        objReturn.put("operador", jsonArray);
        return objReturn.toString();
    }

    private int findRepresentante(String token) throws Exception {
        int codUsu = buscaCodUsuFromToken(token);
        String sql = "SELECT CODREP FROM E099USU WHERE CODUSU = " + codUsu + " AND CODREP <> 0 AND ROWNUM = 1";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODREP");
        String representante = createJsonFromSqlResult(results, fields, "representante");
        JSONObject jObj = new JSONObject(representante);
        if(jObj.getJSONArray("representante").length() > 0)
            return jObj.getJSONArray("representante").getJSONObject(0).getInt("CODREP");
        return 0;
    }

    private String findUltimoSeqPce(String codEmp, String codFil, String numPed,
                                    String seqIpd, int codEtg, int seqMod) {
        String sql = "SELECT NVL(MAX(SEQPCE), 0) AS SEQPCE " +
                       "FROM E700PCE " +
                      "WHERE CODEMP = " + codEmp + " " +
                        "AND CODFIL = " + codFil + " " +
                        "AND NUMPED = " + numPed + " " +
                        "AND SEQIPD = " + seqIpd; /* + " " +
                        "AND CODETG = " + codEtg + " " +
                        "AND SEQMOD = " + seqMod + ""; */

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("SEQPCE");
        return createJsonFromSqlResult(results, fields, "pce");
    }

    private String findEquivalenteExistente(String codEmp, String codFil, String numPed,
                                            String seqIpd, String codMod, String codCmp,
                                            String derCmp) {
        String sql = "SELECT SEQMOD " +
                       "FROM E700PCE " +
                      "WHERE CODEMP = " + codEmp + " " +
                        "AND CODFIL = " + codFil + " " +
                        "AND NUMPED = " + numPed + " " +
                        "AND SEQIPD = " + seqIpd + " " +
                        "AND CODMOD = '" + codMod + "' " +
                        "AND CODCMP = '" + codCmp + "' " +
                        "AND DERCMP = '" + derCmp + "'";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("SEQMOD");
        return createJsonFromSqlResult(results, fields, "seqMod");
    }

    public String findItensMontagem(String emp, String pro, String der) {
        String sql = "SELECT CTM.SEQMOD, CTM.CODCMP, CTM.DERCMP, (PRO.CPLPRO || DER.DESCPL) AS DSCCMP " +
                       "FROM E700CTM CTM, E075PRO PRO, E075DER DER " +
                      "WHERE CTM.CODEMP = PRO.CODEMP " +
                        "AND CTM.CODCMP = PRO.CODPRO " +
                        "AND CTM.CODEMP = DER.CODEMP " +
                        "AND CTM.CODCMP = DER.CODPRO " +
                        "AND CTM.DERCMP = DER.CODDER " +
                        "AND CTM.CODEMP = " + emp + " " +
                        "AND CTM.CODMOD = 'M" + pro + "' " +
                        "AND CTM.CODDER = '" + der + "' " +
                      "ORDER BY CTM.SEQMOD";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("SEQMOD", "CODCMP", "DERCMP", "DSCCMP");
        return createJsonFromSqlResult(results, fields, "itensMontagem");
    }

    public String findTrocas(String emp, String fil, String ped, String ipd) {
        String sql = "SELECT PCE.CODETG, PCE.SEQMOD, PCE.CODPRO, PCE.CODDER, PCE.CODCMP, PCE.DERCMP, PRO.CODFAM "+
                       "FROM E700PCE PCE, E075PRO PRO " +
                      "WHERE PCE.CODEMP = PRO.CODEMP " +
                        "AND PCE.CODCMP = PRO.CODPRO " +
                        "AND PCE.CODEMP = " + emp + " " +
                        "AND PCE.NUMPED = " + ped + " " +
                        "AND PCE.CODFIL = " + fil + " " +
                        "AND PCE.SEQIPD = " + ipd + " " +
                        "AND PRO.CODFAM <> '01034' " +
                        "AND NOT EXISTS (SELECT 1 FROM E700CTM CTM " +
                                         "WHERE PRO.CODEMP = CTM.CODEMP " +
                                           "AND PRO.CODPRO = CTM.CODCMP " +
                                           "AND CTM.CODMOD LIKE 'M%') " +
                        "AND PCE.SEQPCE = (SELECT MAX(X.SEQPCE) " +
                                            "FROM E700PCE X " +
                                           "WHERE PCE.CODEMP = X.CODEMP " +
                                             "AND PCE.CODFIL = X.CODFIL " +
                                             "AND PCE.NUMPED = X.NUMPED " +
                                             "AND PCE.SEQIPD = X.SEQIPD " +
                                             "AND PCE.CODETG = X.CODETG " +
                                             "AND PCE.SEQMOD = X.SEQMOD " +
                                             "AND PCE.CODMOD = X.CODMOD)";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODETG", "SEQMOD", "CODPRO", "CODDER", "CODCMP", "DERCMP", "CODFAM");
        return createJsonFromSqlResult(results, fields, "trocas");
    }

    public String findOrigensRnc() {
        String sql = "SELECT CODRGQ, DESRGQ FROM E104ORG ORDER BY CODRGQ";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODRGQ", "DESRGQ");
        return createJsonFromSqlResult(results, fields, "origens");
    }

    public String findAreasRnc() {
        String sql = "SELECT CODARE, NOMARE FROM E079ARE ORDER BY CODARE";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODARE", "NOMARE");
        return createJsonFromSqlResult(results, fields, "areas");
    }

    public String findDoctosRnc() {
        String sql = "SELECT CODDOC, DESDOC, SITDOC FROM E100DOC WHERE SITDOC = 'A' ORDER BY CODDOC";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODDOC", "DESDOC", "SITDOC");
        return createJsonFromSqlResult(results, fields, "doctos");
    }

    public String findTiposAcaoRnc() {
        String sql = "SELECT USU_CODACI AS CODACI, USU_DESACI DESACI FROM USU_T104ACI ORDER BY USU_CODACI";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODACI", "DESACI");
        return createJsonFromSqlResult(results, fields, "tiposAcao");
    }

    public String findRequisitosIso() {
        String sql = "SELECT REQISO, DESREQ FROM E104RIS ORDER BY REQISO";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("REQISO", "DESREQ");
        return createJsonFromSqlResult(results, fields, "requisitos");
    }

    public String enviarStringTrocas(String emp, String fil, String ped, String ipd, String trocas) throws Exception {
        String sql = "UPDATE E120IPD SET USU_DESCPL = '" + trocas + "' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar campo USU_DESCPL.");
        }
        return "OK";
    }

    private List<Object> listResultsFromSql(String sql) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        return query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
    }

    public String insertEquivalente(String emp, String fil, String ped, String ipd, String trocas, String token) throws Exception {
        JSONArray arrayTrocas = new JSONArray(trocas);
        for (int i = 0; i < arrayTrocas.length(); i++) {
            JSONObject objTroca = arrayTrocas.getJSONObject(i);
            String mod = objTroca.getString("codMod");
            String derMod = objTroca.getString("derMod");
            String cmpAnt = objTroca.getString("cmpAnt");
            String derCmpAnt = objTroca.getString("derAnt");
            String cmpAtu = objTroca.getString("cmpAtu");
            String derCmpAtu = objTroca.getString("derAtu");
            String dscCmp = objTroca.getString("dscCmp");
            String codNiv = objTroca.getString("codNiv");

            JSONObject jObj = new JSONObject(findDadosEquivalente(emp, mod, derMod, cmpAnt, derCmpAnt));
            if (jObj.getJSONArray("dados").length() == 0) {
                jObj = new JSONObject(findEquivalenteExistente(emp, fil, ped, ipd, mod, cmpAnt, derCmpAnt));
                int seqMod = jObj.getJSONArray("seqMod").getJSONObject(0).getInt("SEQMOD");
                jObj = new JSONObject(findDadosEquivalenteBySeqMod(emp, mod, derMod, seqMod));
            }
            int codEtg = jObj.getJSONArray("dados").getJSONObject(0).getInt("CODETG");
            int seqMod = jObj.getJSONArray("dados").getJSONObject(0).getInt("SEQMOD");
            double qtdUti = jObj.getJSONArray("dados").getJSONObject(0).getDouble("QTDUTI");
            double qtdFrq = jObj.getJSONArray("dados").getJSONObject(0).getDouble("QTDFRQ");
            double perPrd = jObj.getJSONArray("dados").getJSONObject(0).getDouble("PERPRD");
            double prdQtd = jObj.getJSONArray("dados").getJSONObject(0).getDouble("PRDQTD");
            String uniMe2 = jObj.getJSONArray("dados").getJSONObject(0).getString("UNIME2");
            String tipQtd = jObj.getJSONArray("dados").getJSONObject(0).getString("TIPQTD");
            String codCcu = jObj.getJSONArray("dados").getJSONObject(0).getString("CODCCU");
            String codPro = jObj.getJSONArray("dados").getJSONObject(0).getString("CODPRO");

            int codUsu = buscaCodUsuFromToken(token);

            jObj = new JSONObject(findUltimoSeqPce(emp, fil, ped, ipd, codEtg, seqMod));
            int seqPce = jObj.getJSONArray("pce").getJSONObject(0).getInt("SEQPCE");
            seqPce += 1;

            String datAlt = getDataAtual();

            String sql = "INSERT INTO E700PCE (CODEMP,CODFIL,NUMPED,SEQIPD,CODETG,SEQMOD,SEQPCE,CODMOD,CODCMP," +
                                              "DERCMP,QTDUTI,QTDFRQ,PERPRD,PRDQTD,UNIME2,TIPQTD,DESCMP,INDPEP," +
                                              "INDIAE,DATALT,CODCCU,CODUSU,OBSPEC,BXAORP,CMPPEN,CODPRO,CODDER," +
                                              "SBSPRO,CODDEP,CODLOT,SELPRO,SELCUS) " +
                                      "VALUES ("+ emp + "," + fil + "," + ped + "," + ipd + "," + codEtg + "," + seqMod + "," + seqPce + ",'" + mod + "','" + cmpAtu + "'," +
                                               "'" + derCmpAtu + "'," + qtdUti + ", " + qtdFrq + ", " + perPrd + ", " + prdQtd + ",'" + uniMe2 + "','" + tipQtd + "','" + dscCmp + "','I'," +
                                               "'A',TO_DATE('" + datAlt + "', 'DD/MM/YYYY'),'" + codCcu + "'," + codUsu + ",'" + codNiv + "','S','N','" + codPro + "','" + derMod + "'," +
                                               "' ',' ',' ','S','S')";

            if (codPro.equals("") || codPro.equals(" ") || sql.contains(",' ',' ',' ',' ',' ',")) {
                System.out.println("E700PCE COM CAMPO VAZIO");
                System.out.println(sql);
            }
            int rowsAffected = executeSqlStatement(sql);
            if (rowsAffected == 0) {
                throw new Exception("Nenhuma linha inserida (E700PCE) ao substituir componente.");
            } else {
                sql = "UPDATE E120IPD SET INDPCE = 'I' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
                rowsAffected = executeSqlStatement(sql);
                if (rowsAffected == 0) {
                    throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar campo INDPCE com valor 'I'.");
                }
                if (!codNiv.isEmpty()) {
                    sql = "DELETE FROM E700PCE WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd + " AND OBSPEC LIKE '" + codNiv + ".%'";
                    executeSqlStatement(sql);
                }
            }
        }
        return "OK";
    }

    private String getDataAtual() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private int getHoraAtualEmMinutos() {
        Calendar rightNow = Calendar.getInstance();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        return (hour * 60) + minute;
    }

    public void marcarCondicaoEspecial(String emp, String fil, String ped, String ipd, String cMed, String cDes,
                                       String cCon, String cPra, String cOut) throws Exception {
        String sql = "UPDATE E120IPD SET USU_MEDESP = '" + cMed +"', USU_DSCESP = '" + cDes +"', USU_PGTESP = '" + cCon +"', " +
                "USU_PRZESP = '" + cPra +"', USU_OUTESP = '" + cOut +"' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " " +
                "AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar condições especiais.");
        }
    }

    public String findPedidoRepresentante(String emp, String fil, String pedRep) {
        String sql = "SELECT NUMPED FROM E120PED WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND USU_PEDREP = '" + pedRep + "'";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("NUMPED");
        return createJsonFromSqlResult(results, fields, "pedidos");
    }

    public void marcarPedidoRep(String emp, String fil, String ped, String pedRep) throws Exception {
        String sql = "UPDATE E120PED SET USU_PEDREP = '" + pedRep +"' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120PED) ao setar pedido representante. Comando: " + sql);
        }
    }

    public void marcarPedidoFeira(String emp, String fil, String ped, String pedFei) throws Exception {
        String sql = "UPDATE E120PED SET USU_PEDFEI = '" + pedFei +"' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120PED) ao setar pedido feira. Comando: " + sql);
        }
    }

    public String marcarDerivacaoEspecial(String emp, String fil, String ped, String ipd, String derEsp) throws Exception {
        String sql = "UPDATE E120IPD SET USU_LARDER = '" + derEsp +"' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar campo USU_LARDER com valor '" + derEsp + "'.");
        }
        return "OK";
    }

    public String marcarParamComerciais(String emp, String fil, String ped, String ipd, Double ds1, Double ds2, Double ds3, Double ds4, Double ds5, Double guelta, Double rt) throws Exception {
        String sql = "UPDATE E120IPD SET PERDS1 = " + ds1 + ", PERDS2 = " + ds2 + ", PERDS3 = " + ds3 + ", PERDS4 = " + ds4 +
                ", PERDS5 = " + ds5 + ", USU_PERGUE = " + guelta +", USU_VLRRET = " + rt + ", USU_PREOLD = E120IPD.PREUNI WHERE CODEMP = " + emp +
                " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar campos com parâmetros comerciais");
        }
        return "OK";
    }

    public String limparEquivalentes(String emp, String fil, String ped, String ipd) {
        String sql = "DELETE FROM E700PCE " +
                      "WHERE CODEMP = " + emp + " " +
                        "AND CODFIL = " + fil + " " +
                        "AND NUMPED = " + ped + " " +
                        "AND SEQIPD = " + ipd;
        executeSqlStatement(sql);
        return "OK";
    }

    public String insertRnc(RNC rnc, String token) throws Exception {
        if (rncExists(rnc)) {
            return updateRnc(rnc);
        } else {
            int codUsu = buscaCodUsuFromToken(token);
            String datAtu = getDataAtual();
            int horaAtu = getHoraAtualEmMinutos();

            return insertRnc(rnc, codUsu, datAtu, horaAtu);
        }
    }

    public String insertOrUpdateAssistencia(AssistenciaTecnica assistencia) throws Exception {
        if (assistenciaExists(assistencia)) {
            return updateAssistencia(assistencia);
        } else {
            return insertAssistencia(assistencia);
        }
    }

    private String updateAssistencia(AssistenciaTecnica assistencia) throws Exception {
        String sql = "UPDATE USU_TASSIST SET USU_SNFNFE = '" + assistencia.getSnfNfe() + "', USU_CODFOR = " + assistencia.getCodFor() + ", " +
                "USU_CODFIL = " + assistencia.getCodFil() + ", USU_NUMNFC = " + assistencia.getNumNfc() + ", USU_SEQIPC = " + assistencia.getSeqIpc() + ", " +
                "USU_NUMPED = " + assistencia.getNumPed() + ", USU_SEQIPD = " + assistencia.getSeqIpd() + ", USU_SEQIPE = " + assistencia.getSeqIpe() + ", " +
                "USU_NUMSEP = '" + assistencia.getNumSep() + "', USU_DATGER = to_date('" + assistencia.getDatGer() + "', 'DD/MM/YYYY'), USU_DATENT = to_date('" + assistencia.getDatEnt() + "', 'DD/MM/YYYY'), " +
                "USU_DATFEC = to_date('" + assistencia.getDatFec() + "', 'DD/MM/YYYY'), USU_RECCLI = '" + assistencia.getRecCli() + "', USU_AVAASS = '" + assistencia.getAvaAss() + "', " +
                "USU_OUTOBS = '" + assistencia.getOutObs() + "', USU_ASSPRC '" + assistencia.getAssPrc() + "'= , USU_TIPFRE = '" + assistencia.getTipFre() + "', " +
                "USU_NUMORI = " + assistencia.getNumOri() + ", USU_TIPSOL = '" + assistencia.getTipSol() + "', USU_DSCCRT = '" + assistencia.getDscCrt() + "', USU_NUMRNC = " + assistencia.getNumRnc() + " " +
             "WHERE USU_CODEMP = " + assistencia.getCodEmp() + " AND USU_NUMASS = " + assistencia.getNumAss();

        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0)  throw new Exception("Nenhuma linha atualizada (USU_TASSIST) ao editar registro de Assistência Técnica. Comando: " + sql);

        return "OK";
    }

    private String insertAssistencia(AssistenciaTecnica assistencia) throws Exception {
        String sql = "INSERT INTO USU_TASSIST (USU_CODEMP, USU_NUMASS, USU_SNFNFE, USU_CODFOR, USU_CODFIL, USU_NUMNFC, " +
                "USU_SEQIPC, USU_NUMPED, USU_SEQIPD, USU_SEQIPE, USU_NUMSEP, USU_DATGER, USU_DATENT, USU_DATFEC, USU_RECCLI, " +
                "USU_AVAASS, USU_OUTOBS, USU_ASSPRC, USU_TIPFRE, USU_NUMORI, USU_TIPSOL, USU_DSCCRT, USU_NUMRNC) VALUES " +
                "(" + assistencia.getCodEmp() + ", " + assistencia.getNumAss() + ", '" + assistencia.getSnfNfe() + "', " + assistencia.getCodFor() + ", " +
                assistencia.getCodFil() + ", " + assistencia.getNumNfc() + ", " + assistencia.getSeqIpc() + ", " + assistencia.getNumPed() + ", " +
                assistencia.getSeqIpd() + ", " + assistencia.getSeqIpe() + ", '" + assistencia.getNumSep() + "', to_date('" + assistencia.getDatGer() + "', 'DD/MM/YYYY'), " +
                "to_date('" + assistencia.getDatEnt() + "', 'DD/MM/YYYY'), to_date('" + assistencia.getDatFec() + "', 'DD/MM/YYYY'), '" + assistencia.getRecCli() + "', '" + assistencia.getAvaAss() + "', " +
                "'" + assistencia.getOutObs() + "', '" + assistencia.getAssPrc() + "', '" + assistencia.getTipFre() + "', " + assistencia.getNumOri() + ", " +
                "'" + assistencia.getTipSol() + "', '" + assistencia.getDscCrt() + "', " + assistencia.getNumRnc() + ")";

        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0)  throw new Exception("Nenhuma linha inserida (USU_TASSIST) ao inserir registro de Assistência Técnica. Comando: " + sql);

        return "OK";
    }

    private boolean assistenciaExists(AssistenciaTecnica assistencia) {
        String sql = "SELECT 1 FROM USU_TASSIST WHERE USU_CODEMP = " + assistencia.getCodEmp() + " AND USU_NUMASS = " + assistencia.getNumAss();
        List<Object> results = listResultsFromSql(sql);
        return (results.size() > 0);
    }

    private boolean rncExists(RNC rnc) {
        String sql = "SELECT 1 FROM E104RMC WHERE CODEMP = " + rnc.getCodEmp() + " AND TIPRMC = '" + rnc.getTipRmc() + "' AND NUMRMC = " + rnc.getNumRmc();
        List<Object> results = listResultsFromSql(sql);
        return (results.size() > 0);
    }

    private String updateRnc(RNC rnc) throws Exception {
        String sql = "UPDATE E104RMC SET ORIRMC = " + rnc.getOriRmc() + ", AREAPL = '" + rnc.getAreApl() + "', " +
                "DATAUD = to_date('" + rnc.getDatAud() + "','DD/MM/YYYY'), DESNCF = '" + rnc.getDesNcf() + "', " +
                "USU_NUMPED = " + rnc.getNumPed() + ", USU_SEQIPD = " + rnc.getSeqIpd() + ", USU_SEQIPE = " + rnc.getSeqIte() + " " +
                "WHERE CODEMP = " + rnc.getCodEmp() + " AND TIPRMC = '" + rnc.getTipRmc() + "' AND NUMRMC = " + rnc.getNumRmc();

        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0)  throw new Exception("Nenhuma linha atualizada (E104RMC) ao editar RNC. Comando: " + sql);

        if (acaoExists(rnc)) updateAcaoCorretiva(rnc);
        else insertAcaoCorretiva(rnc);

        return "OK";
    }

    private String insertRnc(RNC rnc, int codUsu, String datAtu, int horaAtu) throws Exception {
        String sql = "INSERT INTO E104RMC (CODEMP,TIPRMC,NUMRMC,ASSRMC,ORIRMC,REQISO,AREAPL,CODCLI,CODFOR,DATAUD,AUDLID,USUGER,DATGER,HORGER,DESNCF,CODDOC,NUMEPI,ROTANX," +
                                            "NUMANX,USU_USOMET,USU_DESMET,USU_EMAENV, USU_NUMPED, USU_SEQIPD, USU_SEQIPE) " +
                "VALUES (" + rnc.getCodEmp() + ",'" + rnc.getTipRmc() + "'," + rnc.getNumRmc() + ",''," + rnc.getOriRmc() + ",''," +
                        "'" + rnc.getAreApl() + "',0,0,to_date('" + rnc.getDatAud() + "','DD/MM/YYYY'),' '," + codUsu + ",to_date('" + datAtu + "','DD/MM/YYYY')," +
                        horaAtu + ",'" + rnc.getDesNcf() + "','',0,0,0,'','',null," + rnc.getNumPed() + "," + rnc.getSeqIpd() + "," + rnc.getSeqIte() + ")";

        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0)  throw new Exception("Nenhuma linha inserida (E104RMC) ao inserir RNC. Comando: " + sql);

        if (!(rnc.getAcaRnc() == null) && !rnc.getAcaRnc().isEmpty()) {
            insertAcaoCorretiva(rnc);
        }

        return "OK";
    }

    private boolean acaoExists(RNC rnc) {
        String sql = "SELECT 1 FROM USU_T104AAI WHERE USU_CODEMP = " + rnc.getCodEmp() + " AND USU_TIPRMC = '" + rnc.getTipRmc() + "' AND USU_NUMRMC = " + rnc.getNumRmc();
        List<Object> results = listResultsFromSql(sql);
        return (results.size() > 0);
    }

    private void insertAcaoCorretiva(RNC rnc) throws Exception {
        String sql = "INSERT INTO USU_T104AAI (USU_CODEMP,USU_TIPRMC,USU_NUMRMC,USU_SEQAAI,USU_CODACI,USU_ACITOM) " +
                "VALUES (" + rnc.getCodEmp() + ",'" + rnc.getTipRmc() + "'," + rnc.getNumRmc() + ", 1, '" + rnc.getTipAca() + "','" + rnc.getAcaRnc() + "')";

        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0)  throw new Exception("Nenhuma linha inserida (USU_T104AAI) ao inserir ação corretiva da RNC. Comando: " + sql);
    }

    private void updateAcaoCorretiva(RNC rnc) throws Exception {
        String sql = "UPDATE USU_T104AAI SET USU_CODACI = '" + rnc.getTipAca() + "', USU_ACITOM = '" + rnc.getAcaRnc() + "' " +
                "WHERE USU_CODEMP = " + rnc.getCodEmp() + " " +
                "AND USU_TIPRMC = '" + rnc.getTipRmc() + "' " +
                "AND USU_NUMRMC = " + rnc.getNumRmc() + " " +
                "AND USU_SEQAAI = 1";

        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0)  throw new Exception("Nenhuma linha inserida (USU_T104AAI) ao inserir ação corretiva da RNC. Comando: " + sql);
    }

    public String insertTipoAcao(String codAcao, String desAcao) throws Exception {
        if (acaoExists(codAcao)) {
            return "Código de Tipo de Ação já existe";
        }
        String sql = "INSERT INTO USU_T104ACI (USU_CODACI, USU_DESACI) VALUES ('" + codAcao + "', '" + desAcao + "')";
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha inserida (USU_T104ACI) ao inserir tipo de ação.");
        }
        return "OK";
    }

    private boolean acaoExists(String codAcao) {
        String sql = "SELECT 1 FROM USU_T104ACI WHERE USU_CODACI = '" + codAcao + "'";
        List<Object> results = listResultsFromSql(sql);
        return (results.size() > 0);
    }

    public String getAcaoRnc(String codEmp, String tipRmc, String numRmc) {
        String sql = "SELECT AAI.USU_CODACI CODACI, ACI.USU_DESACI DESACI, AAI.USU_ACITOM ACITOM " +
                "FROM USU_T104AAI AAI, USU_T104ACI ACI " +
                "WHERE AAI.USU_CODACI = ACI.USU_CODACI " +
                "AND AAI.USU_CODEMP = " + codEmp + " " +
                "AND AAI.USU_TIPRMC = '" + tipRmc + "' " +
                "AND AAI.USU_NUMRMC = " + numRmc + " " +
                "AND AAI.USU_SEQAAI = 1";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODACI", "DESACI", "ACITOM");
        return createJsonFromSqlResult(results, fields, "acaoRnc");
    }

    public String uploadArquivo(String emp, String fil, String ped, String ipd, MultipartFile file) throws IOException {
        String destination = ANEXOS_PEDIDOS_PATH + emp + "-" + fil + "-" + ped + "-" + ipd + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        File dest = new File(destination);
        if(dest.exists()) {
            int index = 1;
            destination = ANEXOS_PEDIDOS_PATH + emp + "-" + fil + "-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            while(new File(destination).exists()) {
                index++;
                destination = ANEXOS_PEDIDOS_PATH + emp + "-" + fil + "-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            dest = new File(destination);
        }
        file.transferTo(dest);
        return "OK";
    }

    public String listRncs() {
        String sql = "SELECT RMC.NUMRMC, RMC.ASSRMC, RMC.ORIRMC, RMC.REQISO, RMC.AREAPL, TO_CHAR(RMC.DATAUD, 'DD/MM/YYYY') AS DATAUD, " +
                "RMC.DESNCF, RMC.CODDOC, ORG.DESRGQ, ARE.NOMARE, UPPER(USU.NOMUSU) AS USERNAME, " +
                "RMC.USU_NUMPED AS NUMPED, RMC.USU_SEQIPD AS SEQIPD, RMC.USU_SEQIPE AS SEQITE " +
                "FROM E104RMC RMC, E104ORG ORG, E079ARE ARE, R999USU USU " +
                "WHERE RMC.ORIRMC = ORG.CODRGQ " +
                "AND RMC.AREAPL = ARE.CODARE " +
                "AND RMC.USUGER = USU.CODUSU " +
                "ORDER BY NUMRMC";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("NUMRMC", "ASSRMC", "ORIRMC", "AREAPL", "DATAUD", "DESNCF",
                "DESRGQ", "NOMARE", "USERNAME", "NUMPED", "SEQIPD", "SEQITE");
        return createJsonFromSqlResult(results, fields, "rnc");
    }

    public String listRncsPorPedido(String numPed, String seqIpd) {
        String sql = "SELECT RMC.NUMRMC, RMC.ASSRMC, RMC.ORIRMC, RMC.REQISO, RMC.AREAPL, TO_CHAR(RMC.DATAUD, 'DD/MM/YYYY') AS DATAUD, " +
                "RMC.DESNCF, RMC.CODDOC, ORG.DESRGQ, ARE.NOMARE, UPPER(USU.NOMUSU) AS USERNAME, " +
                "RMC.USU_NUMPED AS NUMPED, RMC.USU_SEQIPD AS SEQIPD, RMC.USU_SEQIPE AS SEQITE " +
                "FROM E104RMC RMC, E104ORG ORG, E079ARE ARE, R999USU USU " +
                "WHERE RMC.ORIRMC = ORG.CODRGQ " +
                "AND RMC.AREAPL = ARE.CODARE " +
                "AND RMC.USUGER = USU.CODUSU " +
                "AND RMC.USU_NUMPED = " + numPed + " " +
                "AND RMC.USU_SEQIPD = " + seqIpd + " " +
                "ORDER BY NUMRMC";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("NUMRMC", "ASSRMC", "ORIRMC", "AREAPL", "DATAUD", "DESNCF",
                "DESRGQ", "NOMARE", "USERNAME", "NUMPED", "SEQIPD", "SEQITE");
        return createJsonFromSqlResult(results, fields, "rnc");
    }

    public String listAssistencias() {
        String sql = "SELECT ASS.USU_NUMASS NUMASS, ASS.USU_CODEMP CODEMP, ASS.USU_SNFNFE CODSNF, ASS.USU_CODFOR CODFOR, " +
                "ASS.USU_CODFIL CODFIL, ASS.USU_NUMNFC NUMNFV, ASS.USU_NUMPED NUMPED, ASS.USU_SEQIPD SEQIPD, ASS.USU_SEQIPE SEQIPE, " +
                "ASS.USU_NUMSEP NUMSEP, TO_CHAR(ASS.USU_DATGER, 'DD/MM/YYYY') AS DATGER, TO_CHAR(ASS.USU_DATENT, 'DD/MM/YYYY') AS DATENT, " +
                "TO_CHAR(ASS.USU_DATFEC, 'DD/MM/YYYY') AS DATFEC, ASS.USU_RECCLI RECCLI, ORG.DESRGQ, " +
                "ASS.USU_AVAASS AVAASS, ASS.USU_OUTOBS OUTOBS, ASS.USU_ASSPRC ASSPRC, ASS.USU_TIPFRE TIPFRE, ASS.USU_NUMORI NUMORI, " +
                "ASS.USU_TIPSOL TIPSOL, ASS.USU_DSCCRT DSCCRT, ASS.USU_SEQIPC SEQIPV, CLI.NOMCLI, REP.NOMREP, (PRO.DESNFV || ' ' || DER.DESDER) AS DSCPRO, " +
                "IPD.QTDPED, IPV.CPLIPV, ASS.USU_NUMRNC NUMRNC " +
                "FROM USU_TASSIST ASS, E104ORG ORG, E120PED PED, E085CLI CLI, E090REP REP, E120IPD IPD, E075PRO PRO, E075DER DER, E140IPV IPV " +
                "WHERE ASS.USU_NUMORI = ORG.CODRGQ " +
                "AND ASS.USU_CODEMP = PED.CODEMP " +
                "AND ASS.USU_CODFIL = PED.CODFIL " +
                "AND ASS.USU_NUMPED = PED.NUMPED " +
                "AND PED.CODEMP = IPD.CODEMP " +
                "AND PED.CODFIL = IPD.CODFIL " +
                "AND PED.NUMPED = IPD.NUMPED " +
                "AND ASS.USU_SEQIPD = IPD.SEQIPD " +
                "AND IPD.CODEMP = PRO.CODEMP " +
                "AND IPD.CODPRO = PRO.CODPRO " +
                "AND IPD.CODEMP = DER.CODEMP " +
                "AND IPD.CODPRO = DER.CODPRO " +
                "AND IPD.CODDER = DER.CODDER " +
                "AND PED.CODCLI = CLI.CODCLI " +
                "AND PED.CODREP = REP.CODREP " +
                "AND ASS.USU_CODEMP = IPV.CODEMP " +
                "AND ASS.USU_CODFIL = IPV.CODFIL " +
                "AND ASS.USU_NUMNFC = IPV.NUMNFV " +
                "AND ASS.USU_SNFNFE = IPV.CODSNF " +
                "AND ASS.USU_SEQIPC = IPV.SEQIPV " +
                "ORDER BY ASS.USU_NUMASS";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("NUMASS", "CODEMP", "CODSNF", "CODFOR", "CODFIL", "NUMNFV",
                "NUMPED", "SEQIPD", "SEQIPE", "NUMSEP", "DATGER", "DATENT", "DATFEC", "RECCLI", "AVAASS",
                "OUTOBS", "ASSPRC", "TIPFRE", "NUMORI", "TIPSOL", "DSCCRT", "SEQIPV", "DESRGQ", "NOMCLI",
                "NOMREP", "DSCPRO", "QTDPED", "NUMRNC");
        return createJsonFromSqlResult(results, fields, "assistencias");
    }

    public String getNextRnc(String token) {
        String username = TokensManager.getInstance().getUserNameFromToken(token);

        String sql = "SELECT (NUMRMC + 1) AS NUMRMC, UPPER('" + username + "') AS USERNAME FROM E104RMC ORDER BY NUMRMC DESC";

        List<Object> results = listResultsFromSql(sql);
        results = List.of(results.get(0));
        List<String> fields = Arrays.asList("NUMRMC", "USERNAME");
        return createJsonFromSqlResult(results, fields, "rnc");
    }

    public String getNextAssistencia() {
        String sql = "SELECT (USU_NUMASS + 1) AS NUMASS FROM USU_TASSIST ORDER BY USU_NUMASS DESC";

        List<Object> results = listResultsFromSql(sql);
        if(results.isEmpty()) results = listResultsFromSql("SELECT 1 AS NUMASS FROM DUAL");
        results = List.of(results.get(0));
        List<String> fields = Arrays.asList("NUMASS");
        return createJsonFromSqlResult(results, fields, "assistencia");
    }

    public String getOPsAcabado(String codEmp, String numPed, String seqIpd, String codFam) {
        String sql = "SELECT COP.CODORI, COP.NUMORP, COP.NUMPED, QDO.SEQIPD, QDO.CODPRO, QDO.CODDER, (PRO.DESNFV || ' ' || DER.DESCPL) AS DESPRO, FAM.DESFAM " +
                "FROM E900COP COP, E900QDO QDO, E075PRO PRO, E075DER DER, E012FAM FAM " +
                "WHERE COP.CODEMP = QDO.CODEMP " +
                "AND COP.CODORI = QDO.CODORI " +
                "AND COP.NUMORP = QDO.NUMORP " +
                "AND COP.CODEMP = PRO.CODEMP " +
                "AND COP.CODPRO = PRO.CODPRO " +
                "AND QDO.CODEMP = DER.CODEMP " +
                "AND QDO.CODPRO = DER.CODPRO " +
                "AND QDO.CODDER = DER.CODDER " +
                "AND PRO.CODEMP = DER.CODEMP " +
                "AND PRO.CODPRO = DER.CODPRO " +
                "AND COP.CODEMP = FAM.CODEMP " +
                "AND FAM.CODFAM = '" + codFam + "' " +
                "AND COP.CODEMP = " + codEmp + " " +
                "AND COP.NUMPED = " + numPed + " " +
                "AND QDO.SEQIPD = " + seqIpd + " " +
                "AND COP.CODORI LIKE 'ACA'";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODORI", "NUMORP", "CODPRO", "CODDER", "DESPRO", "NUMPED", "SEQIPD", "DESFAM");
        return createJsonFromSqlResult(results, fields, "ops");
    }

    public String getEmpresas() {
        String sql = "SELECT EMP.CODEMP, EMP.NOMEMP FROM E070EMP EMP WHERE EMP.CODEMP IN (SELECT DISTINCT PED.CODEMP FROM E120PED PED) ORDER BY EMP.CODEMP";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "NOMEMP");
        return createJsonFromSqlResult(results, fields, "empresas");
    }

    public String[] findArquivos(String emp, String fil, String ped, String ipd) {
        File files = new File(ANEXOS_PEDIDOS_PATH);
        FilenameFilter filter = (dir, name) -> name.startsWith(emp + "-" + fil + "-" + ped + "-" + ipd);
        return files.list(filter);
    }

    public String[] findArquivosRnc(String ped, String ipd) {
        File files = new File(ANEXOS_SGQ_PATH);
        FilenameFilter filter = (dir, name) -> name.startsWith("NC-" + ped + "-" + ipd);
        return files.list(filter);
    }

    public String[] findArquivosAss(String ped, String ipd) {
        File files = new File(ANEXOS_ASS_PATH);
        FilenameFilter filter = (dir, name) -> name.startsWith("ASS-" + ped + "-" + ipd);
        return files.list(filter);
    }

    private int buscaCodUsuFromToken(String token) {
        String nomUsu = TokensManager.getInstance().getUserNameFromToken(token);
        JSONObject jObj = new JSONObject(findUsuario(nomUsu));
        return jObj.getJSONArray("usuario").getJSONObject(0).getInt("CODUSU");
    }

    public String getNotasFiscais() {
        String sql = "SELECT NFV.CODEMP, NFV.CODFIL, NFV.NUMNFV, NFV.CODSNF, TO_CHAR(NFV.DATEMI, 'DD/MM/YYYY') AS DATEMI " +
                       "FROM E140NFV NFV " +
                      "WHERE NFV.TNSPRO IN ('6101x','5101x','5949F','6949F') " +
                   "ORDER BY NFV.DATEMI DESC";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "CODFIL", "NUMNFV", "CODSNF", "DATEMI");
        return createJsonFromSqlResult(results, fields, "notas");
    }

    public String getItensNota(String codEmp, String codFil, String numNfv, String codSnf) {
        String sql = "SELECT SEQIPV, CPLIPV " +
                       "FROM E140IPV " +
                      "WHERE CODEMP = " + codEmp + " " +
                        "AND CODFIL = " + codFil + " " +
                        "AND NUMNFV = " + numNfv + " " +
                        "AND CODSNF = '" + codSnf + "' " +
                      "ORDER BY SEQIPV";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("SEQIPV", "CPLIPV");
        return createJsonFromSqlResult(results, fields, "itens");
    }

    public String getPedidoItemPorNota(String codEmp, String codFil, String snfNfv, String numNfv, String seqIpv) {
        String sql = "SELECT IPV.NUMPED, IPV.SEQIPD, IPD.QTDPED, (PRO.DESNFV || ' ' || DER.DESDER) AS DSCPRO, CLI.NOMCLI, REP.NOMREP " +
                      "FROM E140IPV IPV, E075PRO PRO, E075DER DER, E120PED PED, E085CLI CLI, E090REP REP, E120IPD IPD " +
                     "WHERE IPV.CODEMP = PRO.CODEMP " +
                       "AND IPV.CODPRO = PRO.CODPRO " +
                       "AND IPV.CODEMP = DER.CODEMP " +
                       "AND IPV.CODPRO = DER.CODPRO " +
                       "AND IPV.CODDER = DER.CODDER " +
                       "AND IPV.CODEMP = PED.CODEMP " +
                       "AND IPV.CODFIL = PED.CODFIL " +
                       "AND IPV.NUMPED = PED.NUMPED " +
                       "AND IPV.CODEMP = IPD.CODEMP " +
                       "AND IPV.CODFIL = IPD.CODFIL " +
                       "AND IPV.NUMPED = IPD.NUMPED " +
                       "AND IPV.SEQIPD = IPD.SEQIPD " +
                       "AND PED.CODCLI = CLI.CODCLI " +
                       "AND PED.CODREP = REP.CODREP " +
                       "AND IPV.CODEMP = " + codEmp + " " +
                       "AND IPV.CODFIL = " + codFil + " " +
                       "AND IPV.CODSNF = '" + snfNfv + "' " +
                       "AND IPV.NUMNFV = " + numNfv + " " +
                       "AND IPV.SEQIPV = " + seqIpv;

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("NUMPED", "SEQIPD", "QTDPED", "DSCPRO", "NOMCLI", "NOMREP");
        return createJsonFromSqlResult(results, fields, "pedido");
    }

    public String getNotaPorPedido(String codEmp, String codFil, String numPed, String seqIpd) {
        String sql = "SELECT IPV.CODEMP, IPV.CODFIL, IPV.NUMNFV, IPV.CODSNF, IPV.SEQIPV, IPV.CPLIPV " +
                       "FROM E140IPV IPV " +
                      "WHERE IPV.CODEMP = " + codEmp + " " +
                        "AND IPV.FILPED = " + codFil + " " +
                        "AND IPV.NUMPED = " + numPed + " " +
                        "AND IPV.SEQIPD = " + seqIpd;

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "CODFIL", "NUMNFV", "CODSNF", "SEQIPV", "CPLIPV");
        return createJsonFromSqlResult(results, fields, "nota");
    }

    public String getFornecedoresPorPedido(String codEmp, String codPro, String codDer) {
        String sql = "SELECT FORN.CODFOR, FORN.NOMFOR, FORN.APEFOR " +
                "FROM E403FPR FPR, E095FOR FORN " +
                "WHERE FPR.CODFOR = FORN.CODFOR " +
                "AND FPR.CODEMP = " + codEmp + " " +
                "AND FPR.CODPRO = '" + codPro + "' " +
                "AND FPR.CODDER = '" + codDer + "'";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODFOR", "NOMFOR", "APEFOR");
        return createJsonFromSqlResult(results, fields, "fornecedores");
    }

    private int executeSqlStatement(String sql) {
        Transaction transaction = null;
        int rowsAffected = 0;
        try {
            // start a transaction
            transaction = this.sessionFactory.getCurrentSession().beginTransaction();
            Query statement = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
            rowsAffected = statement.executeUpdate();
            transaction.commit();
            return rowsAffected;
        } catch (Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return rowsAffected;
    }

    private String createJsonFromSqlResult(List<Object> result, List<String> fields, String resultsName) {
        JSONArray jsonArray = new JSONArray();
        for(Object item : result) {
            Map row = (Map)item;
            JSONObject jsonObj = new JSONObject();
            for(String field : fields) {
                String value = row.get(field) == null ? "" : row.get(field).toString();
                jsonObj.put(field, value);
            }
            jsonArray.put(jsonObj);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(resultsName, jsonArray);
        return jsonObject.toString();
    }

    public String uploadArquivoRnc(String ped, String ipd, MultipartFile file) throws IOException {
        String destination = ANEXOS_SGQ_PATH + "NC-" + ped + "-" + ipd + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        File dest = new File(destination);
        if(dest.exists()) {
            int index = 1;
            destination = ANEXOS_SGQ_PATH + "NC-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            while(new File(destination).exists()) {
                index++;
                destination = ANEXOS_SGQ_PATH + "NC-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            dest = new File(destination);
        }
        file.transferTo(dest);
        return "OK";
    }

    public String uploadArquivoAss(String ped, String ipd, MultipartFile file) throws IOException {
        String destination = ANEXOS_ASS_PATH + "ASS-" + ped + "-" + ipd + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        File dest = new File(destination);
        if(dest.exists()) {
            int index = 1;
            destination = ANEXOS_ASS_PATH + "ASS-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            while(new File(destination).exists()) {
                index++;
                destination = ANEXOS_ASS_PATH + "ASS-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            dest = new File(destination);
        }
        file.transferTo(dest);
        return "OK";
    }

    public String consultarOPAlmox(String codBar) {
        String[] params = codBar.split("\\.");
        String sql = "SELECT E900CMO.QTDREQ " +
                       "FROM E900COP, E900CMO " +
                      "WHERE E900COP.CODEMP = E900CMO.CODEMP " +
                        "AND E900COP.CODORI = E900CMO.CODORI " +
                        "AND E900COP.NUMORP = E900CMO.NUMORP " +
                        "AND E900COP.CODEMP = " + Integer.parseInt(params[0]) + " " +
                        "AND E900COP.CODORI = '" + params[1] + "' " +
                        "AND E900COP.NUMORP = " +  Integer.parseInt(params[2]) + " " +
                        "AND E900CMO.CODCMP = '" + params[3] + "' " +
                        "AND E900CMO.CODDER = '" + defineCodDer(params) + "'";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("QTDREQ");
        return createJsonFromSqlResult(results, fields, "OP");
    }

    private String defineCodDer(String[] params) {
        try {
            return params[4];
        } catch (Exception e) {
            return " ";
        }
    }
}
