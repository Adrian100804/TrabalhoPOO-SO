package mercado;

// Representa a importância do pedido
public enum PrioridadePedido {

    CIDADAO(1),
    COMERCIANTE(3),
    NOBRE(5),
    REALEZA(10);

    private final int valor;

    PrioridadePedido(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return this.valor;
    }
}