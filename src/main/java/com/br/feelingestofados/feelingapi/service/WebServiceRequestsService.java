package com.br.feelingestofados.feelingapi.service;

import com.br.feelingestofados.feelingapi.soap.SOAPClient;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.util.HashMap;

@Component
public class WebServiceRequestsService extends FeelingService{

    public WebServiceRequestsService(EntityManagerFactory factory) {
        super(factory);
    }

    public String fetchEstrutura(String codEmp, String codFil, String codPro,
                                 String codDer, String numPed, String seqIpd) throws IOException {
        HashMap<String, String> params = new HashMap<>();
        params.put("codEmp", codEmp);
        params.put("codFil", codFil);
        params.put("codPro", codPro);
        params.put("codDer", codDer);
        params.put("numPed", numPed);
        params.put("seqIpd", seqIpd);
        return SOAPClient.requestFromSeniorWS("Estrutura", "heintje", "Mercedes3#", "0", params);
    }
}
