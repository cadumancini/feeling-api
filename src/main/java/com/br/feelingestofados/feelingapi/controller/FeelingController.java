package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.entities.PedidoWrapper;
import com.br.feelingestofados.feelingapi.service.DBQueriesService;
import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class FeelingController {
    protected static final String TOKEN_INVALIDO = "Token inválido.";

    @Autowired
    private WebServiceRequestsService wsRequestsService;
    @Autowired
    private DBQueriesService queriesService;

    @PostMapping("/login")
    @ResponseBody
    public String performLogin(@RequestParam String user, @RequestParam String pswd) throws IOException {
        return wsRequestsService.performLogin(user, pswd);
    }

    @PutMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String createPedido(@RequestBody PedidoWrapper pedidoWrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(pedidoWrapper, "I", "I");
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String editPedido(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "A", "");
        else
            return TOKEN_INVALIDO;
    }

    @DeleteMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String deletePedido(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "E", "");
        else
            return TOKEN_INVALIDO;
    }

    @PutMapping(value = "/pedido/item", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String createItem(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "A", "I");
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/pedido/item", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String editItem(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "A", "A");
        else
            return TOKEN_INVALIDO;
    }

    @DeleteMapping(value = "/pedido/item", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String deleteItem(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "A", "E");
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/estilos", produces = "application/json")
    @ResponseBody
    public String getEstilos(@RequestParam String emp, @RequestParam String token) throws JSONException {
        if(TokensManager.getInstance().isTokenValid(token))
            return queriesService.findEstilos(emp);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/produtosPorEstilo", produces = "application/json")
    @ResponseBody
    public String getProdutosPorEstilo(@RequestParam String emp, @RequestParam String estilo, @RequestParam String token) throws JSONException {
        if(checkToken(token))
            return queriesService.findProdutosPorEstilo(emp, estilo);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/derivacoesPorProduto", produces = "application/json")
    @ResponseBody
    public String getDerivacoesPorProduto(@RequestParam String emp, @RequestParam String produto, @RequestParam String token) throws JSONException {
        if(checkToken(token))
            return queriesService.findDerivacoesPorProduto(emp, produto);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/equivalentes", produces = "application/json")
    @ResponseBody
    public String getEquivalentes(@RequestParam String modelo, @RequestParam String componente, @RequestParam String token) throws JSONException {
        if(checkToken(token))
            return queriesService.findEquivalentes(modelo, componente);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/estrutura", produces = "application/xml")
    @ResponseBody
    public String getEstrutura(@RequestParam String emp, @RequestParam String fil, @RequestParam String pro,
                               @RequestParam String der, @RequestParam String ped, @RequestParam String ipd, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.fetchEstrutura(emp, fil, pro, der, ped, ipd);
        else
            return TOKEN_INVALIDO;
    }

    protected boolean checkToken(String token) {
        return TokensManager.getInstance().isTokenValid(token);
    }
}
