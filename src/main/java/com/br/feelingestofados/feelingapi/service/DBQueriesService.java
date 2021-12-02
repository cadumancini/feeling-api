package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
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
        String sql = "SELECT A.USU_CMPEQI AS CODPRO, C.CODDER, (B.CPLPRO || ' ' || C.DESDER) AS DSCEQI " +
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
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "DSCEQI");
        return createJsonFromSqlResult(results, fields, "equivalentes");
    }

    public String findEquivalentesAdicionais(String modelo, String componente, String der) throws JSONException {
            String sql = "SELECT DISTINCT A.USU_CMPEQI AS CODPRO, C.CODDER, (B.CPLPRO || ' ' || C.DESDER) AS DSCEQI " +
                    "FROM USU_T075EQI A, E075PRO B, E075DER C " +
                    "WHERE A.USU_CODEMP = B.CODEMP " +
                    "AND A.USU_CMPEQI = B.CODPRO " +
                    "AND B.CODEMP = C.CODEMP " +
                    "AND B.CODPRO = C.CODPRO " +
                    "AND A.USU_CODEMP = 1 " +
                    "AND A.USU_CODMOD = '" + modelo + "' " +
                    "AND (A.USU_CMPEQI || C.CODDER) <> '" + componente + der + "' " +
                    "AND A.USU_CODCMP IN (SELECT DISTINCT EQI.USU_CODCMP " +
                                           "FROM USU_T075EQI EQI " +
                                          "WHERE EQI.USU_CODMOD = '" + modelo + "' " +
                                        "AND EQI.USU_CMPEQI = '" + componente + "') " +
                    "AND C.CODDER <> 'G' " +
                    "ORDER BY A.USU_CMPEQI, C.CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "DSCEQI");
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
        String sql = "SELECT DER.CODPRO, DER.CODDER, (PRO.CPLPRO || ' ' || DER.DESDER) AS DSCEQI " +
                       "FROM E075DER DER, E075PRO PRO " +
                      "WHERE DER.CODEMP = PRO.CODEMP " +
                        "AND DER.CODPRO = PRO.CODPRO " +
                        "AND DER.CODEMP = " + emp + " " +
                        "AND DER.CODPRO = '" + pro + "' " +
                        "AND DER.CODDER <> 'G' " +
                        "AND DER.SITDER = 'A' " +
                      "ORDER BY DER.CODDER";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODPRO", "CODDER", "DSCEQI");
        return createJsonFromSqlResult(results, fields, "derivacoes");
    }
    
    private String findDadosEquivalente(String codEmp, String codMod, String derMod, String codCmp, String derCmp) throws JSONException {
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
    
    private String findUsuario(String nomUsu) throws JSONException {
        String sql = "SELECT CODUSU FROM R999USU WHERE UPPER(NOMUSU) = UPPER('" + nomUsu + "')";

        List<Object> results = listResultsFromSql(sql);
        List<String> fields = Arrays.asList("CODUSU");
        return createJsonFromSqlResult(results, fields, "usuario");
    }

    private List<Object> listResultsFromSql(String sql) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sql);
        return query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
    }

    public String insertEquivalente(String emp, String fil, String ped, String ipd, String mod, String derMod, String cmpAnt,
                                    String derCmpAnt, String cmpAtu, String derCmpAtu, String dscCmp, String token) throws JSONException {
        JSONObject jObj = new JSONObject(findDadosEquivalente(emp, mod, derMod, cmpAnt, derCmpAnt));

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
        
        String nomUsu = TokensManager.getInstance().getUserNameFromToken(token);
        jObj = new JSONObject(findUsuario(nomUsu));
        
        int codUsu = jObj.getJSONArray("usuario").getJSONObject(0).getInt("CODUSU");

        //SEQPCE - INCREMENTAL
        //DATALT - DATA ATUAL - TESTAR NULL

        String sql = "INSERT INTO E700PCE (CODEMP,CODFIL,NUMPED,SEQIPD,CODETG,SEQMOD,SEQPCE,CODMOD,CODCMP," +
                                          "DERCMP,QTDUTI,QTDFRQ,PERPRD,PRDQTD,UNIME2,TIPQTD,DESCMP,INDPEP," +
                                          "INDIAE,DATALT,CODCCU,CODUSU,OBSPEC,BXAORP,CMPPEN,CODPRO,CODDER," +
                                          "SBSPRO,CODDEP,CODLOT,SELPRO,SELCUS) " +
                                  "VALUES ("+ emp + "," + fil + "," + ped + "," + ipd + "," + codEtg + "," + seqMod + ",:nSeqPce,'" + mod + "','" + cmpAtu + "'," +
                                           "'" + derCmpAtu + "'," + qtdUti + ", " + qtdFrq + ", " + perPrd + ", " + prdQtd + ",'" + uniMe2 + "','" + tipQtd + "','" + dscCmp + "','I'," +
                                           "'A',null,'" + codCcu + "'," + codUsu + ",' ','S','N','" + codPro + "','" + derMod + "'," +
                                           "' ',' ',' ','S','S')";
        int rowsAffected = executeSqlStatement(sql);
        if (rowsAffected == 0) {
            return "Nenhuma linha inserida";
        } else {
            return "OK";
        }
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
