package com.br.feelingestofados.feelingapi.entities;

import java.util.List;

public class PedidoWrapper {
    private List<Pedido> pedidos;

    public Pedido getPedido() {
        return pedidos.get(0);
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}
