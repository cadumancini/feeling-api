package com.br.feelingestofados.feelingapi.service;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class DBQueriesService extends FeelingService{
    @Autowired
    private WebServiceRequestsService wsRequestsService;

    public DBQueriesService(EntityManagerFactory factory) {
        super(factory);
    }

    private static String ANEXOS_PATH = "\\\\feeling.net\\FEELING_DFS\\PUBLIC\\Pedidos\\Anexos\\";
//    private static String ANEXOS_PATH = "/home/cadumancini/Documents/";

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
        String sql = "SELECT PRO.CODPRO, PRO.DESPRO, NVL(PRO.USU_MEDMIN, 0) AS MEDMIN, NVL(PRO.USU_MEDMAX, 0) AS MEDMAX " +
                       "FROM E075PRO PRO, E700MOD MOD " +
                      "WHERE PRO.CODEMP = MOD.CODEMP " +
                        "AND PRO.CODMOD = MOD.CODMOD " +
                        "AND PRO.CODEMP = " + codEmp + " " +
                        "AND PRO.CODPRO LIKE '__" + estilo + "___' " +
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
                             "PED.SITPED, PED.PEDCLI, PED.CODCLI, PED.CODEMP, PED.CODREP, PED.CODTRA, " +
                             "PED.CIFFOB, PED.OBSPED " +
                       "FROM E120PED PED, E028CPG CPG " +
                      "WHERE PED.CODEMP = CPG.CODEMP " +
                        "AND PED.CODCPG = CPG.CODCPG " +
                        "AND PED.CODEMP = " + emp + " " +
                        "AND PED.CODFIL = " + fil + " " +
                        "AND PED.NUMPED = " + ped;
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("DESCPG", "DATENT", "SITPED", "PEDCLI", "CODCLI", "CODEMP",
                "CODREP", "CODTRA", "CIFFOB", "OBSPED");
        return createJsonFromSqlResult(results, fields, "pedido");
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

    public String findPedidosUsuario(String token) throws Exception {
        int codUsu = buscaCodUsuFromToken(token);
        String sql = "SELECT PED.CODEMP, PED.PEDCLI, PED.NUMPED, TO_CHAR(PED.DATEMI, 'DD/MM/YYYY') AS DATEMI, " +
                            "CLI.NOMCLI, REP.NOMREP, TRA.NOMTRA, PED.CODCLI, CLI.INTNET, CLI.FONCLI, CLI.CGCCPF, " +
                            "(CLI.ENDCLI || ' ' || CLI.CPLEND) AS ENDCPL, (CLI.CIDCLI || '/' || CLI.SIGUFS) AS CIDEST, CLI.INSEST " +
                        "FROM E120PED PED, E085CLI CLI, E090REP REP, E073TRA TRA " +
                        "WHERE PED.CODCLI = CLI.CODCLI " +
                        "AND PED.CODREP = REP.CODREP " +
                        "AND PED.CODTRA = TRA.CODTRA " +
                        "AND PED.USUGER = " + codUsu + " " +
                        "ORDER BY PED.NUMPED";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODEMP", "PEDCLI", "NUMPED", "DATEMI", "NOMCLI", "NOMREP", "NOMTRA", "CODCLI",
                                                "INTNET", "FONCLI", "CGCCPF", "ENDCPL", "CIDEST", "INSEST");
        return createJsonFromSqlResult(results, fields, "pedidos");
    }

    public String enviarPedidoEmpresa(String emp, String fil, String ped, String token) throws Exception {
        String pesoCubagem = "";
        Double pesTotBru = 0d;
        Double pesTotLiq = 0d;
        Double volTot = 0d;
        // Checar se na estrutura de algum item existe algum item com CodDer = 'G' ou ProGen = 'S'
        String itensPedido = this.findItensPedido(emp, fil, ped);
        JSONArray itens = new JSONObject(itensPedido).getJSONArray("itens");
        for(int i = 0; i < itens.length(); i++) {
            JSONObject item = itens.getJSONObject(i);
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
                        pesTotBru += Double.valueOf(eElement.getElementsByTagName("pesBru").item(0).getTextContent());
                        pesTotLiq += Double.valueOf(eElement.getElementsByTagName("pesLiq").item(0).getTextContent());
                        volTot += Double.valueOf(eElement.getElementsByTagName("volDer").item(0).getTextContent());

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
                return "O item " + desPro + " " + desDer + " possui as seguintes pendências na estrutura. Verifique!\n\n" + erros;
            }
        }

        String sql = "UPDATE E120PED SET SITPED = 3 WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped;
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            throw new Exception("Nenhuma linha atualizada (E120PED) ao setar campo SITPED com valor 3.");
        }
        JSONObject jObj = new JSONObject();
        jObj.put("pesoTotalBruto", pesTotBru.toString());
        jObj.put("pesoTotalLiq", pesTotLiq.toString());
        jObj.put("volumeTotal", volTot.toString());
        pesoCubagem = jObj.toString();
        return pesoCubagem;
    }

    public String findItensPedido(String emp, String fil, String ped) {
        String sql = "SELECT IPD.SEQIPD, IPD.CODPRO, IPD.CODDER, IPD.QTDPED, (PRO.DESPRO || ' ' || DER.DESDER) AS DSCPRO, " +
                            "PRO.DESPRO, DER.DESDER, IPD.PERDSC, IPD.PERCOM, IPD.OBSIPD, " +
                            "IPD.SEQPCL, TO_CHAR(IPD.DATENT, 'DD/MM/YYYY') AS DATENT, IPD.PREUNI AS VLRIPD, " +
                            "CPR.CODCPR, CPR.DESCPR, NVL(IPD.USU_LARDER, 0) AS LARDER, (DER.PESLIQ * IPD.QTDPED) AS PESIPD, " +
                            "((DER.VOLDER / 100) * IPD.QTDPED) AS VOLIPD, ((IPD.PERIPI / 100) * (IPD.PREUNI * IPD.QTDPED)) AS IPIIPD, " +
                            "((IPD.PERICM / 100) * (IPD.PREUNI * IPD.QTDPED)) AS ICMIPD, " +
                            "(((IPD.PERIPI / 100) * (IPD.PREUNI * IPD.QTDPED)) + (IPD.PREUNI * IPD.QTDPED)) AS NFVIPD, " +
                            "NVL(IPD.USU_MEDESP, 'N') AS CMED, NVL(IPD.USU_DSCESP, 'N') AS CDES, NVL(IPD.USU_PGTESP, 'N') AS CPAG, " +
                            "NVL(IPD.USU_PRZESP, 'N') AS CPRA, NVL(IPD.USU_OUTESP, 'N') AS COUT, " +
                            "NVL(IPD.PERDS1, 0) AS PERDS1, NVL(IPD.PERDS2, 0) AS PERDS2, NVL(IPD.PERDS3, 0) AS PERDS3, " +
                            "NVL(IPD.PERDS4, 0) AS PERDS4, NVL(IPD.PERDS5, 0) AS PERDS5, NVL(IPD.USU_PERGUE, 0) AS PERGUE, " +
                            "NVL(IPD.USU_VLRRET, 0) AS VLRRET, NVL(PRO.USU_MEDMIN, 0) AS MEDMIN, NVL(PRO.USU_MEDMAX, 0) AS MEDMAX, " +
                            "(NVL(IPD.USU_PESLIQ, 0) * IPD.QTDPED) AS PESLIQ, (NVL(IPD.USU_PESBRU, 0) * IPD.QTDPED) AS PESBRU, " +
                            "(NVL(IPD.USU_VOLDER, 0) * IPD.QTDPED) AS VOLDER " +
                       "FROM E120IPD IPD, E075PRO PRO, E075DER DER, E084CPR CPR " +
                      "WHERE IPD.CODEMP = PRO.CODEMP " +
                        "AND IPD.CODPRO = PRO.CODPRO " +
                        "AND IPD.CODEMP = DER.CODEMP " +
                        "AND IPD.CODPRO = DER.CODPRO " +
                        "AND IPD.CODDER = DER.CODDER " +
                        "AND IPD.CODEMP = CPR.CODEMP " +
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
                "PESLIQ", "PESBRU", "VOLDER");
        String itens = createJsonFromSqlResult(results, fields, "itens");

        JSONArray itensJson = new JSONObject(itens).getJSONArray("itens");
        for(int i = 0; i < itensJson.length(); i++) {
            JSONObject item = itensJson.getJSONObject(i);
            String seqIpd = item.getString("SEQIPD");
            // verificar se o item do pedido possui anexo
            File files = new File(ANEXOS_PATH);
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
        String sql = "SELECT DER.CODPRO, DER.CODDER, DER.USU_CODREF AS CODREF, DER.PESBRU, DER.PESLIQ, DER.VOLDER " +
                "FROM E075DER DER " +
                "WHERE DER.CODEMP = " + emp + " " +
                "AND DER.CODPRO = '" + pro + "' " +
                "AND DER.CODDER = '" + der + "'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "CODREF", "PESBRU", "PESLIQ", "VOLDER");
        return createJsonFromSqlResult(results, fields, "dados");
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
                        "AND SEQIPD = " + seqIpd + " " +
                        "AND CODETG = " + codEtg + " " +
                        "AND SEQMOD = " + seqMod + "";

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

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(System.currentTimeMillis());
            String datAlt = formatter.format(date);

            String sql = "INSERT INTO E700PCE (CODEMP,CODFIL,NUMPED,SEQIPD,CODETG,SEQMOD,SEQPCE,CODMOD,CODCMP," +
                                              "DERCMP,QTDUTI,QTDFRQ,PERPRD,PRDQTD,UNIME2,TIPQTD,DESCMP,INDPEP," +
                                              "INDIAE,DATALT,CODCCU,CODUSU,OBSPEC,BXAORP,CMPPEN,CODPRO,CODDER," +
                                              "SBSPRO,CODDEP,CODLOT,SELPRO,SELCUS) " +
                                      "VALUES ("+ emp + "," + fil + "," + ped + "," + ipd + "," + codEtg + "," + seqMod + "," + seqPce + ",'" + mod + "','" + cmpAtu + "'," +
                                               "'" + derCmpAtu + "'," + qtdUti + ", " + qtdFrq + ", " + perPrd + ", " + prdQtd + ",'" + uniMe2 + "','" + tipQtd + "','" + dscCmp + "','I'," +
                                               "'A','" + datAlt + "','" + codCcu + "'," + codUsu + ",' ','S','N','" + codPro + "','" + derMod + "'," +
                                               "' ',' ',' ','S','S')";
            int rowsAffected = executeSqlStatement(sql);
            if (rowsAffected == 0) {
                throw new Exception("Nenhuma linha inserida (E700PCE) ao substituir componente.");
            } else {
                sql = "UPDATE E120IPD SET INDPCE = 'I' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
                rowsAffected = executeSqlStatement(sql);
                if (rowsAffected == 0) {
                    throw new Exception("Nenhuma linha atualizada (E120IPD) ao setar campo INDPCE com valor 'I'.");
                }
            }
        }
        return "OK";
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

    public String marcarDerivacaoEspecial(String emp, String fil, String ped, String ipd, String derEsp) throws Exception {
        String sql = "UPDATE E120IPD SET USU_LARDER = '" + derEsp +"' WHERE CODEMP = " + emp + " AND CODFIL = " + fil + " AND NUMPED = " + ped + " AND SEQIPD = " + ipd;
        System.out.println(sql);
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

    public String uploadArquivo(String emp, String fil, String ped, String ipd, MultipartFile file) throws IOException {
        String destination = ANEXOS_PATH + emp + "-" + fil + "-" + ped + "-" + ipd + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        File dest = new File(destination);
        if(dest.exists()) {
            int index = 1;
            destination = ANEXOS_PATH + emp + "-" + fil + "-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            while(new File(destination).exists()) {
                index++;
                destination = ANEXOS_PATH + emp + "-" + fil + "-" + ped + "-" + ipd + "(" + index + ")" + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            dest = new File(destination);
        }
        file.transferTo(dest);
        return "OK";
    }

    public String[] findArquivos(String emp, String fil, String ped, String ipd) {
        File files = new File(ANEXOS_PATH);
        FilenameFilter filter = (dir, name) -> name.startsWith(emp + "-" + fil + "-" + ped + "-" + ipd);
        String[] fileNames = files.list(filter);
        return fileNames;
    }

    private int buscaCodUsuFromToken(String token) {
        String nomUsu = TokensManager.getInstance().getUserNameFromToken(token);
        JSONObject jObj = new JSONObject(findUsuario(nomUsu));
        return jObj.getJSONArray("usuario").getJSONObject(0).getInt("CODUSU");
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
}
