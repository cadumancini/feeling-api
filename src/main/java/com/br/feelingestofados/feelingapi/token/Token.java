package com.br.feelingestofados.feelingapi.token;

import java.util.Calendar;

public class Token {
    private final String value;
    private final long createdAt;
    private boolean valid;

    public Token (String value) {
        this.value = value;
        this.createdAt = Calendar.getInstance().getTimeInMillis();
        this.valid = true;
    }

    public String getValue() {
        return value;
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
