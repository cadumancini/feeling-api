package com.br.feelingestofados.feelingapi.service;

import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DBQueriesService extends FeelingService{

    public DBQueriesService(EntityManagerFactory factory) {
        super(factory);
    }

    public String findEquivalentes(String modelo, String componente) throws JSONException {
        String sql = "SELECT A.USU_CMPEQI, C.CODDER, (B.CPLPRO || ' ' || C.DESDER) AS DSCEQI " +
                       "FROM USU_T075EQI A, E075PRO B, E075DER C " +
                      "WHERE A.USU_CODEMP = B.CODEMP " +
                        "AND A.USU_CMPEQI = B.CODPRO " +
                        "AND B.CODEMP = C.CODEMP " +
                        "AND B.CODPRO = C.CODPRO " +
                        "AND A.USU_CODEMP = 1 " +
                        "AND A.USU_CODMOD = '" + modelo + "' " +
                        "AND A.USU_CODCMP = '" + componente + "' " +
                        "AND C.CODDER <> 'G' " +
                      "ORDER BY A.USU_CMPEQI, C.CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("USU_CMPEQI", "CODDER", "DSCEQI");
        return createJsonFromSqlResult(results, fields, "equivalentes");
    }

    public String findEstilos(String codEmp) throws JSONException {
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

    public String findProdutosPorEstilo(String codEmp, String estilo) throws JSONException {
        String sql = "SELECT CODPRO, DESPRO " +
                       "FROM E075PRO " +
                      "WHERE CODEMP = " + codEmp + " " +
                        "AND CODPRO LIKE '__" + estilo + "___' " +
                        "AND CODORI = 'ACA'" +
                        "AND SITPRO = 'A' " +
                      "ORDER BY DESPRO";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "DESPRO");
        return createJsonFromSqlResult(results, fields, "produtos");
    }

    public String findDerivacoesPorProduto(String codEmp, String codPro) throws JSONException {
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

    public String findClientes() throws JSONException {
        String sql = "SELECT CODCLI, NOMCLI " +
                       "FROM E085CLI " +
                      "WHERE SITCLI = 'A' " +
                      "ORDER BY CODCLI";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODCLI", "NOMCLI");
        return createJsonFromSqlResult(results, fields, "clientes");
    }

    public String findItensPedido(String emp, String fil, String ped) throws JSONException {
        String sql = "SELECT SEQIPD, CODPRO, CODDER, QTDPED " +
                       "FROM E120IPD " +
                      "WHERE CODEMP = " + emp + " " +
                        "AND CODFIL = " + fil + " " +
                        "AND NUMPED = " + ped;
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("SEQIPD", "CODPRO", "CODDER", "QTDPED");
        return createJsonFromSqlResult(results, fields, "itens");
    }

    public String findDadosProduto(String emp, String pro) throws JSONException {
        String sql = "SELECT NVL(FAM.USU_EXICMP, 'N') AS EXICMP, NVL(PRO.USU_PROGEN, 'N') AS PROGEN, PRO.CODFAM " +
                       "FROM E075PRO PRO, E012FAM FAM " +
                      "WHERE PRO.CODEMP = FAM.CODEMP " +
                        "AND PRO.CODFAM = FAM.CODFAM " +
                        "AND PRO.CODEMP = " + emp + " " +
                        "AND PRO.CODPRO = '" + pro + "'";
        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("EXICMP", "PROGEN", "CODFAM");
        return createJsonFromSqlResult(results, fields, "dados");
    }

    public String findDerivacoesPossiveis(String emp, String pro) throws JSONException {
        String sql = "SELECT DER.CODDER, (PRO.CPLPRO || ' ' || DER.DESDER) AS DESDER " +
                       "FROM E075DER DER, E075PRO PRO" +
                      "WHERE DER.CODEMP = PRO.CODEMP " +
                        "AND DER.CODPRO = PRO.CODPRO " +
                        "AND DER.CODEMP = " + emp + " " +
                        "AND DER.CODPRO = '" + pro + "' " +
                        "AND DER.CODDER <> 'G' " +
                        "AND DER.SITDER = 'A' " +
                      "ORDER BY CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODDER", "DESDER");
        return createJsonFromSqlResult(results, fields, "derivacoes");
    }

    private List<Object> listResultsFromSql(String sql) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        return query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
    }

    private String createJsonFromSqlResult(List<Object> result, List<String> fields, String resultsName) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(Object item : result) {
            Map row = (Map)item;
            JSONObject jsonObj = new JSONObject();
            for(String field : fields) {
                String value = row.get(field).toString();
                jsonObj.put(field, value);
            }
            jsonArray.put(jsonObj);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(resultsName, jsonArray);
        return jsonObject.toString();
    }
}
