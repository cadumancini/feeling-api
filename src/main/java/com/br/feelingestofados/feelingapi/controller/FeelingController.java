package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class FeelingController {
    protected static final String TOKEN_INVALIDO = "Token inválido.";

    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @PostMapping("/login")
    @ResponseBody
    public String performLogin(@RequestParam String user, @RequestParam String pswd) throws IOException {
        return wsRequestsService.performLogin(user, pswd);
    }

    protected boolean checkToken(String token) {
        return TokensManager.getInstance().isTokenValid(token);
    }
}
