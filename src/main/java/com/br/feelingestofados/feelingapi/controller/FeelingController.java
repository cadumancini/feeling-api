package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.entities.PedidoWrapper;
import com.br.feelingestofados.feelingapi.service.DBQueriesService;
import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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

    @GetMapping(value = "/itensPedido", produces = "application/json")
    @ResponseBody
    public String getItensPedido(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findItensPedido(emp, fil, ped);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/pedido", produces = "application/json")
    @ResponseBody
    public String getPedido(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findPedido(emp, fil, ped);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/pedidosCliente", produces = "application/json")
    @ResponseBody
    public String getPedidosCliente(@RequestParam String cli, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findPedidosClientes(cli);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/pedidos", produces = "application/json")
    @ResponseBody
    public String getPedidosUsuario(@RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findPedidosUsuario(token);
        else
            return TOKEN_INVALIDO;
    }

    @PutMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String createPedido(@RequestBody PedidoWrapper pedidoWrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(pedidoWrapper, "I", "I", token);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String editPedido(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "C", "", token);
        else
            return TOKEN_INVALIDO;
    }

    @DeleteMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String deletePedido(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "E", "", token);
        else
            return TOKEN_INVALIDO;
    }

    @PutMapping(value = "/pedido/item", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String createItem(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "C", "I", token);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/pedido/item", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String editItem(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "C", "C", token);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/pedido/itens", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String includeItems(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws Exception {
        if(checkToken(token)) {
            String itensDoPedido = queriesService.findItensPedido(wrapper.getPedido().getCodEmp().toString(),
                    wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString());
            JSONArray itens = new JSONObject(itensDoPedido).getJSONArray("itens");
            for(int i = 0; i < itens.length(); i++) {
                JSONObject item = itens.getJSONObject(i);
                String seqIpd = item.getString("SEQIPD");
                // limpando E700PCE
                queriesService.limparEquivalentes(wrapper.getPedido().getCodEmp().toString(),
                        wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                        seqIpd);
                // excluindo item do pedido
                wsRequestsService.handlePedido(wrapper.getPedido().getCodEmp().toString(),
                        wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                        seqIpd, "C", "E", token);
            }
            String returnPedido = wsRequestsService.handlePedido(wrapper, "C", "I", token);
            if (returnPedido.contains("<retorno>OK</retorno>")) {
                AtomicInteger seqIpd = new AtomicInteger();
                wrapper.getItens().forEach(itemPedido -> {
                    seqIpd.getAndIncrement();
                    if(itemPedido.getConEsp() != null) {
                        try {
                            queriesService.marcarCondicaoEspecial(wrapper.getPedido().getCodEmp().toString(),
                                    wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(), seqIpd.toString(), itemPedido.getConEsp());
                            if(itemPedido.getDerEsp() != null && !itemPedido.getDerEsp().isEmpty()) {
                                queriesService.marcarDerivacaoEspecial(wrapper.getPedido().getCodEmp().toString(),
                                        wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(), seqIpd.toString(), itemPedido.getDerEsp());
                            }
                        } catch (Exception e) {
                           e .printStackTrace();
                        }
                    }
                });
            }
            return returnPedido;
        }
        else
            return TOKEN_INVALIDO;
    }

    @DeleteMapping(value = "/pedido/item", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String deleteItem(@RequestBody PedidoWrapper wrapper, @RequestParam String token) throws IOException {
        if(checkToken(token))
            return wsRequestsService.handlePedido(wrapper, "C", "E", token);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/estilos", produces = "application/json")
    @ResponseBody
    public String getEstilos(@RequestParam String emp, @RequestParam String token) throws Exception {
        if(TokensManager.getInstance().isTokenValid(token))
            return queriesService.findEstilos(emp);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/condicoesPagto", produces = "application/json")
    @ResponseBody
    public String getCondicoesPagto(@RequestParam String emp, @RequestParam String token) throws Exception {
        if(TokensManager.getInstance().isTokenValid(token))
            return queriesService.findCondicoesPagto(emp);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/produtosPorEstilo", produces = "application/json")
    @ResponseBody
    public String getProdutosPorEstilo(@RequestParam String emp, @RequestParam String estilo, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findProdutosPorEstilo(emp, estilo);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/derivacoesPorProduto", produces = "application/json")
    @ResponseBody
    public String getDerivacoesPorProduto(@RequestParam String emp, @RequestParam String produto, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findDerivacoesPorProduto(emp, produto);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/equivalentes", produces = "application/json")
    @ResponseBody
    public String getEquivalentes(@RequestParam String emp, @RequestParam String modelo, @RequestParam String componente, @RequestParam String derivacao, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findEquivalentes(emp, modelo, componente, derivacao);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/equivalentesAdicionais", produces = "application/json")
    @ResponseBody
    public String getEquivalentesAdicionais(@RequestParam String emp, @RequestParam String modelo, @RequestParam String componente, @RequestParam String der, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findEquivalentesAdicionais(emp, modelo, componente, der);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/derivacoesPossiveis", produces = "application/json")
    @ResponseBody
    public String getDerivacoesPossiveis(@RequestParam String emp, @RequestParam String pro,
                                         @RequestParam String mod, @RequestParam String derMod, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findDerivacoesPossiveis(emp, pro, mod, derMod);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/estrutura", produces = "application/xml")
    @ResponseBody
    public String getEstrutura(@RequestParam String emp, @RequestParam String fil, @RequestParam String pro,
                               @RequestParam String der, @RequestParam String ped, @RequestParam String ipd, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return wsRequestsService.fetchEstrutura(emp, fil, pro, der, ped, ipd, token);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/clientes", produces = "application/json")
    @ResponseBody
    public String getClientes(@RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findClientes(token);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/dadosCliente", produces = "application/json")
    @ResponseBody
    public String getDadosCliente(@RequestParam String token, @RequestParam String codCli) throws Exception {
        if(checkToken(token))
            return queriesService.findDadosCliente(codCli);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/equivalente", produces = "application/json")
    @ResponseBody
    public String putEquivalente(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped,
                                 @RequestParam String ipd, @RequestBody String trocas, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.insertEquivalente(emp, fil, ped, ipd, trocas, token);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/enviarPedido", produces = "application/json")
    @ResponseBody
    public String enviarPedido(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped,
                               @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.enviarPedidoEmpresa(emp, fil, ped, token);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/itensMontagem", produces = "application/json")
    @ResponseBody
    public String getItensMontagem(@RequestParam String emp, @RequestParam String pro, @RequestParam String der, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findItensMontagem(emp, pro, der);
        else
            return TOKEN_INVALIDO;
    }

    protected boolean checkToken(String token) {
        return TokensManager.getInstance().isTokenValid(token);
    }
}
