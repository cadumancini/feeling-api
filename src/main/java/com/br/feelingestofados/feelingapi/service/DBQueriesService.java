package com.br.feelingestofados.feelingapi.service;

import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
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
        JSONArray jsonArray = new JSONArray();
        for(Object item : results) {
            Map row = (Map)item;
            String cmpEqi = row.get("USU_CMPEQI").toString();
            String derEqi = row.get("CODDER").toString();
            String dscEqi = row.get("DSCEQI").toString();

            JSONObject eqi = new JSONObject();
            eqi.put("cmpEqi", cmpEqi);
            eqi.put("derEqi", derEqi);
            eqi.put("dscEqi", dscEqi);
            jsonArray.put(eqi);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("equivalentes", jsonArray);
        return jsonObject.toString();
    }

    private List listResultsFromSql(String sql) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        return query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
    }

    public String findEstilos(String codEmp) throws JSONException {
        String sql = "SELECT CODCPR, DESCPR " +
                       "FROM E084CPR " +
                      "WHERE CODEMP = " + codEmp + " " +
                        "AND CODMPR = 'ESTILOS' " +
                        "AND SITCPR = 'A'";

        List<Object> results = listResultsFromSql(sql);
        JSONArray jsonArray = new JSONArray();
        for(Object item : results) {
            Map row = (Map)item;
            String cmpEqi = row.get("CODCPR").toString();
            String derEqi = row.get("DESCPR").toString();

            JSONObject est = new JSONObject();
            est.put("codCpr", cmpEqi);
            est.put("desCpr", derEqi);
            jsonArray.put(est);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("estilos", jsonArray);
        return jsonObject.toString();
    }
}
