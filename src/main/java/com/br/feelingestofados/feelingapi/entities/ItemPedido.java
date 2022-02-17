package com.br.feelingestofados.feelingapi.entities;

import lombok.Data;

@Data
public class ItemPedido {
    private String codPro;
    private String codDer;
    private Integer seqIpd;
    private Integer qtdPed;
    private Double preUni;
    private Integer numCnj;
    private String datEnt;
    private String conEsp;
    private String derEsp;
    private String obsIpd;
}
