package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.service.DBQueriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FeelingController {
    @Autowired
    private DBQueriesService queriesService;

    @GetMapping("/equivalentes")
    @ResponseBody
    public String getEquivalentes(@RequestParam String modelo, @RequestParam String componente) throws JSONException {
        return queriesService.findEquivalentes(modelo, componente);
    }
}
