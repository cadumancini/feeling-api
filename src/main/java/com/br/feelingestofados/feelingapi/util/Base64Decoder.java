package com.br.feelingestofados.feelingapi.util;

import java.util.Base64;

public class Base64Decoder {
    public static String decode(String input) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(input);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }
}
