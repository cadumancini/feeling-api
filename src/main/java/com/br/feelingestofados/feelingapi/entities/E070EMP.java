package com.br.feelingestofados.feelingapi.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class E070EMP {
    private Long codEmp;
    private String nomEmp;

    public void setCodEmp(Long codEmp) {
        this.codEmp = codEmp;
    }

    @Id
    public Long getCodEmp() {
        return codEmp;
    }

    public void setNomEmp(String nomEmp) {
        this.nomEmp = nomEmp;
    }

    public String getNomEmp() {
        return nomEmp;
    }
}
