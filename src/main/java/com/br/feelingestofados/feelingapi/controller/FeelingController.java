package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.service.DBQueriesService;
import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class FeelingController {
    @Autowired
    private DBQueriesService queriesService;

    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @GetMapping("/estilos")
    @ResponseBody
    public String getEstilos(@RequestParam String emp, @RequestParam String token) throws JSONException {
        if(TokensManager.getInstance().isTokenValid(token))
            return queriesService.findEstilos(emp);
        else
            return "Token inválido.";
    }

    @GetMapping("/produtosPorEstilo")
    @ResponseBody
    public String getProdutosPorEstilo(@RequestParam String emp, @RequestParam String estilo, @RequestParam String token) throws JSONException {
        if(checkToken(token))
            return queriesService.findProdutosPorEstilo(emp, estilo);
        else
            return "Token inválido.";
    }

    @GetMapping("/derivacoesPorProduto")
    @ResponseBody
    public String getDerivacoesPorProduto(@RequestParam String emp, @RequestParam String produto, @RequestParam String token) throws JSONException {
        if(checkToken(token))
            return queriesService.findDerivacoesPorProduto(emp, produto);
        else
            return "Token inválido.";
    }

    @GetMapping("/equivalentes")
    @ResponseBody
    public String getEquivalentes(@RequestParam String modelo, @RequestParam String componente, @RequestParam String token) throws JSONException {
        if(checkToken(token))
            return queriesService.findEquivalentes(modelo, componente);
        else
            return "Token inválido.";
    }

    @GetMapping("/estrutura")
    @ResponseBody
    public String getEstrutura(@RequestParam String emp, @RequestParam String fil, @RequestParam String pro,
                               @RequestParam String der, @RequestParam String ped, @RequestParam String ipd, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.fetchEstrutura(emp, fil, pro, der, ped, ipd);
        else
            return "Token inválido.";
    }

    @PostMapping("/login")
    @ResponseBody
    public String performLogin(@RequestParam String user, @RequestParam String pswd) throws IOException {
        return wsRequestsService.performLogin(user, pswd);
    }

    private boolean checkToken(String token) {
        if(TokensManager.getInstance().isTokenValid(token))
            return true;
        else
            return false;
    }
}
