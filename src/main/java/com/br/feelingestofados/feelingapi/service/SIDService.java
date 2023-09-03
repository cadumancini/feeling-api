package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class SIDService {
//    01.14001.00038455.002.01
//    01.13001.00038455.002.01
//    01.11001.00038455.002.01
//    01.11.00038455.002.01
//    01.10001.00038455.002.01
//    01.09001.00038455.002.01
//    01.08001.00038455.002.01
//    01.07001.00038455.002.01
//    01.06001.00038455.002.01
//    01.04001.00038455.002.01

    private final String feelingUrl;

    private final String processBaixaOP = "970";

    public SIDService(Environment env) {
        String envValue = env.getProperty("env");
        String domain = envValue.equals("prod") ? "sapiensweb" : "baseteste";
        feelingUrl = String.format("http://%s.feelingestofados.com.br/sapiensweb/conector?SIS=CO&LOGIN=SID&ACAO=EXESENHA", domain);
    }

    public String runBaixaOP(String token, String aCodBar) throws IOException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);

        String url = String.format("%s&NOMUSU=%s&SENUSU=%s&PROXACAO=SID.Srv.Regra&NumReg=%s&aCodBar=%s", feelingUrl,  URLEncoder.encode(user, StandardCharsets.UTF_8),  URLEncoder.encode(pswd, StandardCharsets.UTF_8), processBaixaOP, aCodBar);
        return getRequest(url);
    }
    private String getRequest(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpRequest = new HttpGet(url);
        HttpResponse httpResponse = client.execute(httpRequest);
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
