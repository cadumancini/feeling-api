package com.br.feelingestofados.feelingapi.util;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public enum Forms {
    INVENTARIO ("ALM-INV", "Almoxarifado - Inventario", asList("Senior", "ALMOX")),
    SEPARACAO_MATERIAIS ("ALM-SEP", "Almoxarifado - Separacao de Materiais", asList("Senior", "ALMOX")),
    PEDIDO_WEB ("COM-PED", "Comercial - Pedido Web", asList("Senior", "PEDWEB")),
    ASSISTENCIA_TECNICA ("COM-AST", "Comercial - Assistência Técnica", List.of("ALL")),
    APONTAMENTO_PRODUCAO ("PRD-APT", "Producao - Apontamento de Producao", asList("Senior", "AP-PROD")),
    NAO_CONFORMIDADE ("PRD-NCN", "Producao - Nao Conformidade", List.of("ALL"));

    Forms(String id, String description, List<String> groups) {
        this.id = id;
        this.description = description;
        this.groups = groups;
    }

    private final String id;
    private final String description;
    private final List<String> groups;

    public static Forms valueOfById(String formRequestedId) {
        for (Forms form : Forms.values()) {
            if (form.id.equals(formRequestedId)) return form;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public List<String> getGroups() {
        return groups;
    }
}
