package com.br.feelingestofados.feelingapi.controller;

import com.br.feelingestofados.feelingapi.entities.PedidoWrapper;
import com.br.feelingestofados.feelingapi.service.DBQueriesService;
import com.br.feelingestofados.feelingapi.service.SIDService;
import com.br.feelingestofados.feelingapi.service.UserService;
import com.br.feelingestofados.feelingapi.service.WebServiceRequestsService;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class FeelingController {
    protected static final String TOKEN_INVALIDO = "Token inválido.";
    private static final String ANEXOS_PATH = "\\\\feeling.net\\FEELING_DFS\\PUBLIC\\%s\\Anexos\\";
    private static final String ANEXOS_PEDIDOS_PATH = String.format(ANEXOS_PATH, "Pedidos");
    private static final String ANEXOS_SGQ_PATH = String.format(ANEXOS_PATH, "SGQ");

    @Autowired
    private WebServiceRequestsService wsRequestsService;
    @Autowired
    private DBQueriesService queriesService;
    @Autowired
    private UserService userService;
    @Autowired
    private SIDService sidService;

    @PostMapping("/login")
    @ResponseBody
    public String performLogin(@RequestParam String user, @RequestParam String pswd) throws IOException {
        return wsRequestsService.performLogin(user, pswd);
    }

    @GetMapping(value = "/itensPedido", produces = "application/json")
    @ResponseBody
    public String getItensPedido(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findItensPedido(emp, fil, ped);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/pedido", produces = "application/json")
    @ResponseBody
    public String getPedido(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String token) {
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

    @GetMapping(value = "/pedidoRepresentante", produces = "application/json")
    @ResponseBody
    public String getPedidoRepresentante(@RequestParam String emp, @RequestParam String fil, @RequestParam String pedRep, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findPedidoRepresentante(emp, fil, pedRep);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/transacoes", produces = "application/json")
    @ResponseBody
    public String getTransacoes(@RequestParam String emp, @RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findTransacoes(emp);
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

    @GetMapping(value = "/cadastro", produces = "application/json")
    @ResponseBody
    public String getCadastro(@RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findCadastro(token);
        else
            return TOKEN_INVALIDO;
    }

    @PutMapping(value = "/pedido", consumes = "application/json", produces = "application/xml")
    @ResponseBody
    public String createPedido(@RequestBody PedidoWrapper pedidoWrapper, @RequestParam String token) throws Exception {
        if(checkToken(token)) {
            String returnPedido = wsRequestsService.handlePedido(pedidoWrapper, "I", "I", token);
            if (!returnPedido.contains("<numPed>0</numPed>") && returnPedido.contains("<numPed>")) {
                int indexStart = returnPedido.indexOf("<numPed>");
                int indexEnd = returnPedido.indexOf("</numPed>");
                String numPed = returnPedido.substring((indexStart + 8), indexEnd);
                if(pedidoWrapper.getPedido().getPedRep() != null && !pedidoWrapper.getPedido().getPedRep().toString().equals("0")) {
                    queriesService.marcarPedidoRep(pedidoWrapper.getPedido().getCodEmp().toString(),
                        pedidoWrapper.getPedido().getCodFil().toString(), numPed, pedidoWrapper.getPedido().getPedRep().toString());
                }
                if(pedidoWrapper.getPedido().getPedFei() != null) {
                    queriesService.marcarPedidoFeira(pedidoWrapper.getPedido().getCodEmp().toString(),
                        pedidoWrapper.getPedido().getCodFil().toString(), numPed, pedidoWrapper.getPedido().getPedFei().toString());
                }
            }
            return returnPedido;
        }
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
            checarSeBloqueiaPedido(wrapper.getPedido().getCodEmp().toString(),
                wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString());

            String itensDoPedido = queriesService.findItensPedido(wrapper.getPedido().getCodEmp().toString(),
                    wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString());
            JSONArray itens = new JSONObject(itensDoPedido).getJSONArray("itens");
            for(int i = 0; i < itens.length(); i++) {
                JSONObject item = itens.getJSONObject(i);
                String seqIpd = item.getString("SEQIPD");
                String sitIpd = item.getString("SITIPD");
                // verificar se o item do pedido do banco existe nos parâmetros, se não existir, excluir
                AtomicBoolean existe = new AtomicBoolean(false);
                wrapper.getItens().forEach(itemPedido -> {
                    if(itemPedido.getSeqIpd().toString().equals(seqIpd)) {
                        existe.set(true);
                        itemPedido.setSitIpd(sitIpd);
                    }
                });
                if(!existe.get()) {
                    // limpando E700PCE
                    queriesService.limparEquivalentes(wrapper.getPedido().getCodEmp().toString(),
                            wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                            seqIpd);
                    // excluindo item do pedido
                    wsRequestsService.handlePedido(wrapper.getPedido().getCodEmp().toString(),
                            wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                            seqIpd, "C", "E", token);
                }

                // Excluir do wrapper se estiver em producao
                String gerNec = item.getString("GERNEC");
                AtomicInteger indiceParaExcluir = new AtomicInteger(-1);
                if(gerNec.equals("2") || gerNec.equals("3") || gerNec.equals("8")) {
                    wrapper.getItens().forEach(itemPedido -> {
                        if(itemPedido.getSeqIpd().toString().equals(seqIpd)) {
                            indiceParaExcluir.set(wrapper.getItens().indexOf(itemPedido));
                        }
                    });

                    if(indiceParaExcluir.get() > -1) {
                        wrapper.getItens().remove(indiceParaExcluir.get());
                    }
                }
            }
            System.out.println("Wrapper: " + wrapper.getItens().toString());
            String returnPedido = wsRequestsService.handlePedido(wrapper, "C", "I", token);
            if (returnPedido.contains("<retorno>OK</retorno>")) {
                AtomicInteger seqIpd = new AtomicInteger();
                wrapper.getItens().forEach(itemPedido -> {
                    seqIpd.getAndIncrement();
                    try {
                        queriesService.marcarCondicaoEspecial(wrapper.getPedido().getCodEmp().toString(),
                                wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                                itemPedido.getSeqIpd() > 0 ? itemPedido.getSeqIpd().toString() : seqIpd.toString(), itemPedido.getMedEsp(),
                                itemPedido.getDesEsp(), itemPedido.getConEsp(), itemPedido.getPraEsp(), itemPedido.getOutEsp());
                        queriesService.marcarParamComerciais(wrapper.getPedido().getCodEmp().toString(),
                                wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                                itemPedido.getSeqIpd() > 0 ? itemPedido.getSeqIpd().toString() : seqIpd.toString(), itemPedido.getPerDs1(),
                                itemPedido.getPerDs2(), itemPedido.getPerDs3(), itemPedido.getPerDs4(), itemPedido.getPerDs5(),
                                itemPedido.getPerGue(), itemPedido.getVlrRet());
                        if(itemPedido.getDerEsp() != null && !itemPedido.getDerEsp().isEmpty()) {
                            queriesService.marcarDerivacaoEspecial(wrapper.getPedido().getCodEmp().toString(),
                                    wrapper.getPedido().getCodFil().toString(), wrapper.getPedido().getNumPed().toString(),
                                    itemPedido.getSeqIpd() > 0 ? itemPedido.getSeqIpd().toString() : seqIpd.toString(), itemPedido.getDerEsp());
                        }
                    } catch (Exception e) {
                       e .printStackTrace();
                    }
                });
            }
            return returnPedido;
        }
        else
            return TOKEN_INVALIDO;
    }

    private void checarSeBloqueiaPedido(String emp, String fil, String ped) throws Exception {
        String pedido = queriesService.findPedido(emp, fil, ped);

        JSONArray pedidoResult = new JSONObject(pedido).getJSONArray("pedido");
        JSONObject pedidoObj = pedidoResult.getJSONObject(0);
        String pedBlo = pedidoObj.getString("PEDBLO");
        String sitPed = pedidoObj.getString("SITPED");

        if ((sitPed.equals("1") || sitPed.equals("2")) && pedBlo.equals("N")) {
            queriesService.bloquearPedido(emp, fil, ped);
        }
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
    public String getEstilos(@RequestParam String emp, @RequestParam String token) {
        if(TokensManager.getInstance().isTokenValid(token))
            return queriesService.findEstilos(emp);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/condicoesPagto", produces = "application/json")
    @ResponseBody
    public String getCondicoesPagto(@RequestParam String emp, @RequestParam String token) {
        if(TokensManager.getInstance().isTokenValid(token))
            return queriesService.findCondicoesPagto(emp);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/produtosPorEstilo", produces = "application/json")
    @ResponseBody
    public String getProdutosPorEstilo(@RequestParam String emp, @RequestParam String estilo, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findProdutosPorEstilo(emp, estilo);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/derivacoesPorProduto", produces = "application/json")
    @ResponseBody
    public String getDerivacoesPorProduto(@RequestParam String emp, @RequestParam String produto, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findDerivacoesPorProduto(emp, produto);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/equivalentes", produces = "application/json")
    @ResponseBody
    public String getEquivalentes(@RequestParam String emp, @RequestParam String modelo, @RequestParam String componente, @RequestParam String derivacao, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findEquivalentes(emp, modelo, componente, derivacao);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/equivalentesAdicionais", produces = "application/json")
    @ResponseBody
    public String getEquivalentesAdicionais(@RequestParam String emp, @RequestParam String modelo, @RequestParam String componente, @RequestParam String der, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findEquivalentesAdicionais(emp, modelo, componente, der);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/derivacoesPossiveis", produces = "application/json")
    @ResponseBody
    public String getDerivacoesPossiveis(@RequestParam String emp, @RequestParam String pro,
                                         @RequestParam String mod, @RequestParam String derMod, @RequestParam String token) {
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

    @GetMapping(value = "/dadosProdutoDerivacao", produces = "application/json")
    @ResponseBody
    public String getDadosProduto(@RequestParam String token, @RequestParam String emp, @RequestParam String pro, @RequestParam String der) throws Exception {
        if(checkToken(token))
            return queriesService.findDadosDerivacao(emp, pro, der);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/depositos", produces = "application/json")
    @ResponseBody
    public String getDepositos(@RequestParam String token, @RequestParam String pro, @RequestParam String der) throws Exception {
        if(checkToken(token))
            return queriesService.findDepositosLigados(pro, der);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/estoque", produces = "application/json")
    @ResponseBody
    public String getQtdeEstoque(@RequestParam String token, @RequestParam String pro, @RequestParam String der, @RequestParam String dep, @RequestParam String lot) throws Exception {
        if(checkToken(token))
            if(!lot.equals("")) {
                return queriesService.findEstoqueLote(lot);
            } else {
                return queriesService.findQtdeEstoque(pro, der, dep);
            }
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/contagem", produces = "application/json")
    @ResponseBody
    public String movimentarEstoque(@RequestParam String token, @RequestParam String pro, @RequestParam String der, @RequestParam String lot, @RequestParam String depOri, @RequestParam String depDes, @RequestParam String qtdMov) throws Exception {
        if(checkToken(token))
            return queriesService.movimentarEstoque(pro, der, lot, depOri, depDes, qtdMov, token);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/dadosLote", produces = "application/json")
    @ResponseBody
    public String getDadosLote(@RequestParam String token, @RequestParam String emp, @RequestParam String lote) throws Exception {
        if(checkToken(token))
            return queriesService.findDadosLote(emp, lote);
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

    @GetMapping(value = "/transportadoras", produces = "application/json")
    @ResponseBody
    public String getTransportadoras(@RequestParam String token) throws Exception {
        if(checkToken(token))
            return queriesService.findTransportadoras();
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/dadosCliente", produces = "application/json")
    @ResponseBody
    public String getDadosCliente(@RequestParam String token, @RequestParam String codCli) {
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
    public String getItensMontagem(@RequestParam String emp, @RequestParam String pro, @RequestParam String der, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findItensMontagem(emp, pro, der);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/uploadArquivo", produces = "application/json")
    @ResponseBody
    public String uploadArquivo(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String ipd,
                                @RequestParam String token, @RequestParam("file") MultipartFile file) throws IOException {
        if(checkToken(token))
            return queriesService.uploadArquivo(emp, fil, ped, ipd, file);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/downloadArquivo", produces = "application/zip")
    public void downloadArquivo(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String ipd,
                                @RequestParam String token, HttpServletResponse response) throws IOException {
        if(checkToken(token)) {
            String[] arquivos = queriesService.findArquivos(emp, fil, ped, ipd);
            if (arquivos.length == 0) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                response.getWriter().write("VAZIO");
                response.getWriter().flush();
            } else {
                ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
                for (String fileName : arquivos) {
                    FileSystemResource resource = new FileSystemResource(ANEXOS_PEDIDOS_PATH + fileName);
                    ZipEntry zipEntry = new ZipEntry(resource.getFilename());
                    zipEntry.setSize(resource.contentLength());
                    zipOut.putNextEntry(zipEntry);
                    StreamUtils.copy(resource.getInputStream(), zipOut);
                    zipOut.closeEntry();
                }
                zipOut.finish();
                zipOut.close();
                response.setStatus(HttpServletResponse.SC_OK);
                response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + emp + "-" + fil + "-" + ped + "-" + ipd + ".zip" + "\"");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(TOKEN_INVALIDO);
            response.getWriter().flush();
        }

    }

    @GetMapping(value = "/trocas", produces = "application/json")
    @ResponseBody
    public String getTrocas(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String ipd, @RequestParam String token) {
        if(checkToken(token))
            return queriesService.findTrocas(emp, fil, ped, ipd);
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/enviarStringExclusivos", produces = "application/json")
    @ResponseBody
    public String enviarStringExclusivos(@RequestParam String emp, @RequestParam String fil, @RequestParam String ped, @RequestParam String ipd,
                                @RequestParam String token, @RequestParam String exclusivos) throws Exception {
        if(checkToken(token))
            return queriesService.enviarStringTrocas(emp, fil, ped, ipd, exclusivos);
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/telasDisponiveis", produces = "application/json")
    @ResponseBody
    public String getTelasDisponiveis(@RequestParam String token, @RequestParam(required = false) String tela) throws IOException, ParserConfigurationException, SAXException {
        if(checkToken(token))
            if (tela == null || tela.isEmpty()) {
                return userService.getTelasDisponiveis(token);
            } else {
                return userService.telaDisponivel(token, tela);
            }
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/separarAlmox", produces = "application/json")
    @ResponseBody
    public String separarAlmoxarifado(@RequestParam String token, @RequestParam String codBar, @RequestParam String qtdSep) throws Exception {
        if(checkToken(token))
            return "OK";
        else
            return TOKEN_INVALIDO;
    }

    @PostMapping(value = "/apontarOP", produces = "application/json")
    @ResponseBody
    public String apontarOP(@RequestParam String token, @RequestParam String codBar) throws Exception {
        if(checkToken(token)) {
            String returnMessage = sidService.runBaixaOP(token, codBar);
            return returnMessage.equals("OK") ? "Apontamento realizado com sucesso!" : returnMessage;
        }
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/origensRnc", produces = "application/json")
    @ResponseBody
    public String getOrigensRnc(@RequestParam String token) {
        if(checkToken(token))
            return queriesService.findOrigensRnc();
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/areasRnc", produces = "application/json")
    @ResponseBody
    public String getAreasRnc(@RequestParam String token) {
        if(checkToken(token))
            return queriesService.findAreasRnc();
        else
            return TOKEN_INVALIDO;
    }

    @GetMapping(value = "/doctosRnc", produces = "application/json")
    @ResponseBody
    public String getDoctosRnc(@RequestParam String token) {
        if(checkToken(token))
            return queriesService.findDoctosRnc();
        else
            return TOKEN_INVALIDO;
    }

    protected boolean checkToken(String token) {
        return TokensManager.getInstance().isTokenValid(token);
    }
}
