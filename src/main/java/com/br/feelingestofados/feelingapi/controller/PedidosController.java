package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.entities.PedidoWrapper;
import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

public class PedidosController extends FeelingController {
    @Autowired
    private WebServiceRequestsService wsRequestsService;

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
}
