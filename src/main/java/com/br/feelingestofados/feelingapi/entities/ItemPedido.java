package com.br.feelingestofados.feelingapi.entities;

import lombok.Data;

@Data
public class ItemPedido {
    private String codPro;
    private String codDer;
    private Integer seqIpd;
    private Integer qtdPed;
    private Double preUni;
    private String numCnj;
    private String datEnt;
    private String derEsp;
    private String obsIpd;
    private Double perDsc;
    private Double perCom;
    private String cMed;
    private String cDes;
    private String cCon;
    private String cPra;
    private String cOut;
}
