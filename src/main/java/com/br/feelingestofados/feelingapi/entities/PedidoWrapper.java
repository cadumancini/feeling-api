package com.br.feelingestofados.feelingapi.entities;

import java.util.List;

public class PedidoWrapper {
    private Pedido pedido;
    private List<ItemPedido> itens;

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }
}
