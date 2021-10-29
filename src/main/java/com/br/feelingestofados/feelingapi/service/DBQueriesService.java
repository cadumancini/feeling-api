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
