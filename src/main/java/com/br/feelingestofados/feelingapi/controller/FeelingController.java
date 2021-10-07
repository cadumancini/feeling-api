package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.service.DBQueriesService;
import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class FeelingController {
    @Autowired
    private DBQueriesService queriesService;

    @Autowired
    private WebServiceRequestsService wsRequestsService;

    @GetMapping("/equivalentes")
    @ResponseBody
    public String getEquivalentes(@RequestParam String modelo, @RequestParam String componente) throws JSONException {
        return queriesService.findEquivalentes(modelo, componente);
    }

    @GetMapping("/estrutura")
    @ResponseBody
    public String getEstrutura(@RequestParam String emp, @RequestParam String fil, @RequestParam String pro,
                               @RequestParam String der, @RequestParam String ped, @RequestParam String ipd) throws IOException {
        return wsRequestsService.fetchEstrutura(emp, fil, pro, der, ped, ipd);
    }
}
