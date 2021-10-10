package com.br.feelingestofados.feelingapi.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokensManager {
    private static TokensManager instance = null;
    private List<Token> validTokens;

    private TokensManager() {
        validTokens = new ArrayList<>();
    }

    public static TokensManager getInstance() {
        if(instance == null)
            instance = new TokensManager();

        return instance;
    }

    public List<Token> getValidTokens() {
        return validTokens;
    }

    public void addToken(String tokenValue) {
        validTokens.add(new Token(tokenValue));
    }

    public void removeInvalidTokens() {
        Iterator<Token> it = validTokens.iterator();
        while (it.hasNext()) {
            Token token = it.next();
            if(!token.isValid())
                it.remove();
        }
    }
}
