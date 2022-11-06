package com.br.feelingestofados.feelingapi.token;

import java.util.Calendar;

public class Token {
    private final String value;
    private final String nomUsu;
    private final String senUsu;
    private final String numCad;
    private final String nomOpe;
    private final long createdAt;
    private boolean valid;

    public Token (String value, String nomUsu, String senUsu, String numCad, String nomOpe) {
        this.value = value;
        this.nomUsu = nomUsu;
        this.senUsu = senUsu;
        this.numCad = numCad;
        this.nomOpe = nomOpe;
        this.createdAt = Calendar.getInstance().getTimeInMillis();
        this.valid = true;
    }

    public String getValue() {
        return value;
    }

    public String getUserName() {
        return nomUsu;
    }

    public String getPassword() {
        return senUsu;
    }

    public String getNumCad() {
        return numCad;
    }

    public String getNomOpe() {
        return nomOpe;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void invalidateToken() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid;
    }
}
