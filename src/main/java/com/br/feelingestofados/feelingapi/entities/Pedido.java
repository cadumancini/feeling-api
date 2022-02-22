package com.br.feelingestofados.feelingapi.entities;

import lombok.Data;

@Data
public class Pedido {
    private Integer codEmp;
    private Integer codFil;
    private Integer numPed;
    private Integer codCli;
    private String pedCli;
    private Integer codRep;
    private Integer codTra;
    private String cifFob;
    private String obsPed;
}
