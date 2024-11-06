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
    private Double perDs1;
    private Double perDs2;
    private Double perDs3;
    private Double perDs4;
    private Double perDs5;
    private Double perGue;
    private Double vlrRet;
    private Double perCom;
    private String medEsp;
    private String desEsp;
    private String conEsp;
    private String praEsp;
    private String outEsp;
    private String tnsPro;
    private String sitIpd = "";
    private String cplIpd;
}
