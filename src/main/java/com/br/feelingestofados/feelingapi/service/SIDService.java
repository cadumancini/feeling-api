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
    private final String feelingUrl;

    private final String processBaixaOP = "972";
    private final String processSepararAlmox = "973";

    public SIDService(Environment env) {
        String envValue = env.getProperty("env");
        String domain = envValue.equals("prod") ? "sapiensweb.feelingestofados.com.br" : "baseteste.feelingestofados.com.br:28080";
        feelingUrl = String.format("http://%s/sapiensweb/conector?SIS=CO&LOGIN=SID&ACAO=EXESENHA", domain);
    }

    public String runBaixaOP(String token, String aCodBar, String aTipOpe, String aCodFor) throws IOException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);

        String url = String.format("%s&NOMUSU=%s&SENUSU=%s&PROXACAO=SID.Srv.Regra&NumReg=%s&aCodBar=%s&aTipOpe=%s&aCodFor=%s",
                feelingUrl,  URLEncoder.encode(user, StandardCharsets.UTF_8),  URLEncoder.encode(pswd, StandardCharsets.UTF_8),
                processBaixaOP, aCodBar, aTipOpe, aCodFor);
        System.out.println("Regra: " + url);

        String resposta = getRequest(url);
        System.out.println("Resposta: " + resposta);
        return resposta;
    }

    public String runSeparacaoAlmox(String token, String aCodBar) throws IOException {
        String user = TokensManager.getInstance().getUserNameFromToken(token);
        String pswd = TokensManager.getInstance().getPasswordFromToken(token);

        String url = String.format("%s&NOMUSU=%s&SENUSU=%s&PROXACAO=SID.Srv.Regra&NumReg=%s&aCodBar=%s",
                feelingUrl,  URLEncoder.encode(user, StandardCharsets.UTF_8),  URLEncoder.encode(pswd, StandardCharsets.UTF_8),
                processSepararAlmox, aCodBar);
        System.out.println("Regra: " + url);

        String resposta = getRequest(url);
        System.out.println("Resposta: " + resposta);
        return resposta;
    }

    private String getRequest(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpRequest = new HttpGet(url);
        HttpResponse httpResponse = client.execute(httpRequest);
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
