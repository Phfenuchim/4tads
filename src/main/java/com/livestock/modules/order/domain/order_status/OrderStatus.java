package com.livestock.modules.order.domain.order_status;

public enum OrderStatus {
    AGUARDANDO_PAGAMENTO("Aguardando Pagamento"),
    PAGAMENTO_REJEITADO("Pagamento Rejeitado"), // NOVO
    PAGAMENTO_COM_SUCESSO("Pagamento Aprovado"), // NOVO (ou renomear PAGO)
    AGUARDANDO_RETIRADA("Aguardando Retirada"), // NOVO
    EM_TRANSITO("Em Trânsito"),                 // NOVO
    ENTREGUE("Entregue");

    private final String descricao;

    OrderStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    // Opcional: para facilitar a obtenção do enum pelo nome se necessário
    public static OrderStatus fromString(String text) {
        for (OrderStatus b : OrderStatus.values()) {
            if (b.name().equalsIgnoreCase(text) || b.descricao.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null; // ou lançar exceção
    }
}
