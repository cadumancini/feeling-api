package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.service.E070EMPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {
    @Autowired
    private E070EMPService empService;

    @GetMapping("/hello")
    public List hello() {
        return empService.findAll();
    }
}
