package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.entities.PedidoWrapper;
import com.br.feelingestofados.feelingapi.soap.SOAPClient;
import com.br.feelingestofados.feelingapi.token.TokensManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.*;

@Component
public class WebServiceRequestsService extends FeelingService{

    public WebServiceRequestsService(EntityManagerFactory factory) {
        super(factory);
    }

    public String fetchEstrutura(String codEmp, String codFil, String codPro,
                                 String codDer, String numPed, String seqIpd) throws IOException {
        HashMap<String, String> params = prepareParamsForEstrutura(codEmp, codFil, codPro, codDer, numPed, seqIpd);
        return SOAPClient.requestFromSeniorWS("customizado", "Estrutura", "heintje", "Mercedes3#", "0", params);
    }

    private HashMap<String, String> prepareParamsForEstrutura(String codEmp, String codFil, String codPro, String codDer, String numPed, String seqIpd) {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("codPro", codPro);
        params.put("codDer", codDer);
        params.put("numPed", numPed);
        params.put("seqIpd", seqIpd);
        return params;
    }

    public String performLogin(String user, String pswd) throws IOException {
        HashMap<String, String> emptyParams = new HashMap<>();
        String response = SOAPClient.requestFromSeniorWS("com_senior_g5_co_ger_sid", "Executar", user, pswd, "0", emptyParams);

        if(response.contains("Credenciais inválidas"))
            return "Credenciais inválidas";
        else {
            Date currentDateTime = Calendar.getInstance().getTime();
            String hash = DigestUtils.sha256Hex(user + pswd + currentDateTime);
            TokensManager.getInstance().addToken(hash);

            return hash;
        }
    }

    public String handlePedido(PedidoWrapper pedidoWrapper, String opePed, String opeIpd) throws IOException {
        HashMap<String, HashMap> params = prepareParamsForPedido(pedidoWrapper, opePed, opeIpd);
        return SOAPClient.requestFromSeniorWS("com_senior_g5_co_mcm_ven_pedidos", "GravarPedidos", "heintje", "Mercedes3#", "0", params);
    }

    private HashMap<String, HashMap> prepareParamsForPedido(PedidoWrapper pedidoWrapper, String opePed, String opeIpd) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("codEmp", pedidoWrapper.getPedido().getCodEmp());
        params.put("codFil", pedidoWrapper.getPedido().getCodFil());
        params.put("numPed", pedidoWrapper.getPedido().getNumPed());
        params.put("codCli", pedidoWrapper.getPedido().getCodCli());
        params.put("pedCli", pedidoWrapper.getPedido().getPedCli());
        params.put("opeExe", opePed);

        if(!pedidoWrapper.getItens().isEmpty()) {
            List<HashMap<String, Object>> listaItens = new ArrayList<>();
            pedidoWrapper.getItens().forEach(itemPedido -> {
                HashMap<String, Object> paramsItem = new HashMap<>();
                paramsItem.put("codPro", itemPedido.getCodPro());
                paramsItem.put("codDer", itemPedido.getCodDer());
                paramsItem.put("seqIpd", itemPedido.getSeqIpd());
                paramsItem.put("qtdPed", itemPedido.getQtdPed());
                paramsItem.put("preUni", String.valueOf(itemPedido.getPreUni()).replace(".", ","));
                paramsItem.put("opeExe", opeIpd);
                listaItens.add(paramsItem);
            });
            params.put("produto", listaItens);
        }

        HashMap<String, HashMap> paramsPedido = new HashMap<>();
        paramsPedido.put("pedido", params);
        return paramsPedido;
    }
}
