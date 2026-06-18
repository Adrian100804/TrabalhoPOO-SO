package mercado;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("=== Sistema do Mercado Medieval iniciado ===");

        Mercado mercado = new Mercado();

        // -------------------------
        // Recursos compartilhados
        // -------------------------

        Recurso forno = new Recurso("Forno Central", TipoRecurso.FORNO);
        Recurso bancada = new Recurso("Bancada Principal", TipoRecurso.BANCADA);
        Recurso ferramentaRara = new Recurso("Martelo Raro", TipoRecurso.FERRAMENTA_RARA);

        mercado.adicionarRecurso(forno);
        mercado.adicionarRecurso(bancada);
        mercado.adicionarRecurso(ferramentaRara);

        // -------------------------
        // Pedidos do Ferreiro
        // -------------------------

        List<Recurso> recursosEspada = new ArrayList<>();
        recursosEspada.add(forno);
        recursosEspada.add(ferramentaRara);

        Pedido espadaReal = new Pedido(
                "Espada da Realeza",
                1000,
                PrioridadePedido.REALEZA,
                recursosEspada
        );

        List<Recurso> recursosEscudo = new ArrayList<>();
        recursosEscudo.add(forno);
        recursosEscudo.add(bancada);

        Pedido escudoNobre = new Pedido(
                "Escudo para Nobre",
                900,
                PrioridadePedido.NOBRE,
                recursosEscudo
        );

        List<Pedido> pedidosFerreiro = new ArrayList<>();
        pedidosFerreiro.add(espadaReal);
        pedidosFerreiro.add(escudoNobre);

        Artesao ferreiro = new Artesao(
                "Arthur",
                TipoArtesao.FERREIRO,
                pedidosFerreiro
        );

        // -------------------------
        // Pedidos do Carpinteiro
        // -------------------------

        List<Recurso> recursosMesa = new ArrayList<>();
        recursosMesa.add(bancada);

        Pedido mesaComerciante = new Pedido(
                "Mesa para Comerciante",
                800,
                PrioridadePedido.COMERCIANTE,
                recursosMesa
        );

        List<Recurso> recursosCarruagem = new ArrayList<>();
        recursosCarruagem.add(bancada);
        recursosCarruagem.add(ferramentaRara);

        Pedido carruagemNobre = new Pedido(
                "Carruagem para Nobre",
                1200,
                PrioridadePedido.NOBRE,
                recursosCarruagem
        );

        List<Pedido> pedidosCarpinteiro = new ArrayList<>();
        pedidosCarpinteiro.add(mesaComerciante);
        pedidosCarpinteiro.add(carruagemNobre);

        Artesao carpinteiro = new Artesao(
                "Bernardo",
                TipoArtesao.CARPINTEIRO,
                pedidosCarpinteiro
        );

        // -------------------------
        // Pedidos do Alquimista
        // -------------------------

        List<Recurso> recursosPocao = new ArrayList<>();
        recursosPocao.add(forno);
        recursosPocao.add(bancada);

        Pedido pocaoReal = new Pedido(
                "Poção da Realeza",
                1100,
                PrioridadePedido.REALEZA,
                recursosPocao
        );

        List<Recurso> recursosElixir = new ArrayList<>();
        recursosElixir.add(ferramentaRara);

        Pedido elixirCidadao = new Pedido(
                "Elixir para Cidadão",
                700,
                PrioridadePedido.CIDADAO,
                recursosElixir
        );

        List<Pedido> pedidosAlquimista = new ArrayList<>();
        pedidosAlquimista.add(pocaoReal);
        pedidosAlquimista.add(elixirCidadao);

        Artesao alquimista = new Artesao(
                "Merlin",
                TipoArtesao.ALQUIMISTA,
                pedidosAlquimista
        );

        // -------------------------
        // Adicionando artesãos ao mercado
        // -------------------------

        mercado.adicionarArtesao(ferreiro);
        mercado.adicionarArtesao(carpinteiro);
        mercado.adicionarArtesao(alquimista);

        // -------------------------
        // Estado inicial
        // -------------------------

        System.out.println("\n--- Estado inicial do mercado ---");

        mercado.mostrarArtesaos();
        mercado.mostrarRecursos();

        // -------------------------
        // Execução concorrente
        // -------------------------

        mercado.iniciarSimulacao();

        // -------------------------
        // Estado final
        // -------------------------

        System.out.println("\n--- Estado final do mercado ---");

        mercado.mostrarArtesaos();
        mercado.mostrarRecursos();

        System.out.println("\n=== Fim da simulação ===");
    }
}