package com.br.feelingestofados.feelingapi.service;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;

@Component
public class DBQueriesService {
    private SessionFactory sessionFactory;

    @Autowired
    public DBQueriesService(EntityManagerFactory factory) {
        if(factory.unwrap(SessionFactory.class) == null){
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.sessionFactory = factory.unwrap(SessionFactory.class);
    }

    public String findEquivalentes(String modelo, String componente) throws JSONException {
        String sql = "SELECT USU_CMPEQI, USU_DEREQI " +
                       "FROM USU_T075EQI " +
                      "WHERE USU_CODEMP = 1 " +
                        "AND USU_CODMOD = '" + modelo + "' " +
                        "AND USU_CODCMP = '" + componente + "'";

        List<Object> results = listResultsFromSql(sql);
        JSONArray jsonArray = new JSONArray();
        for(Object item : results) {
            Map row = (Map)item;
            String cmpEqi = row.get("USU_CMPEQI").toString();
            String derEqi = row.get("USU_DEREQI").toString();

            JSONObject eqi = new JSONObject();
            eqi.put("cmpEqi", cmpEqi);
            eqi.put("derEqi", derEqi);
            jsonArray.put(eqi);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("equivalentes", jsonArray);
        return jsonObject.toString();
    }

    private List listResultsFromSql(String sql) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        List results = query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
        return results;
    }
}
