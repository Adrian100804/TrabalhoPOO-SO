package mercado;

import java.util.ArrayList;
import java.util.List;

public class Artesao implements Runnable {

    private String nome;
    private TipoArtesao tipo;
    private List<Pedido> pedidos;

    public Artesao(String nome, TipoArtesao tipo, List<Pedido> pedidos) { // construtor completo
        this.nome = nome;
        this.tipo = tipo;
        this.pedidos = pedidos;
    }

    public String getNome() {
        return this.nome;
    }

    public TipoArtesao getTipo() {
        return this.tipo;
    }

    public List<Pedido> getPedidos() {
        return this.pedidos;
    }

    public int getQuantidadePedidos() { // esse metodo retorna quantos pedidos um artesão tem por isso o size, ele conta quantos elementos existe dentro da lista
        return this.pedidos.size();
    }

    public void adicionarPedido(Pedido pedido) { // adiciona um novo pedido na lista do artesão por isso do .add()
        this.pedidos.add(pedido);
    }

    public boolean temPedidos() { // verifica se a lista esta vazia o ! inverte o resultado
        return !this.pedidos.isEmpty();
    }

    public Pedido pegarProximoPedido() { // esse é um metodo importante criado, ele faz duas coisas: verifica se existe pedido e pega o proximo pedido
        if (this.pedidos.isEmpty()) { // verifica se a lista esta vazia, se estiver ele nn tem pedido pra pegar ai ele retorna null: nao tem objeto aqui
            return null;
        }

        Pedido melhorPedido = this.pedidos.get(0); // o codigo ele começa considerando o primeiro pedido como o melhor e depois ele compara com os outros.

        for(Pedido pedido : this.pedidos) {
            int pontuacaoAtual = pedido.getPrioridade().getValor() + pedido.getTempoEspera(); //calcula a pontuação, ele faz com que um pedido que começou tenha vantagem mas o pedido que esperou muito tambem ganha força
            int melhorPontuacao = melhorPedido.getPrioridade().getValor() + melhorPedido.getTempoEspera();

            if (pontuacaoAtual > melhorPontuacao) { // se o pedido atual tiver a pontuação maior, ele vira o melhor pedido
                melhorPedido = pedido;
            }
        }

        this.pedidos.remove(melhorPedido);// o pedido escolhido sai da lista, ele so sai da lista pq ele sera executado
        return melhorPedido; // o artesão recebe o melhor pedido para executar
    }

    public boolean executarPedido(Pedido pedido) {
        if (pedido == null) { // se ele receber um pedido vazio ele simplesmente encerra o metodo
            return false;
        }

        List<Recurso> recursosReservados = new ArrayList<>(); // essa lista guarda os recursos que o artesão conseguiu reservar, se ele nn conseguir reservar ele libera

        boolean conseguiuReservarTodos = true; // assume que todos os recursos foram reservados, mas, se falhar muda para falso

        for (Recurso recurso : pedido.getRecursosNecessarios()) {
            if (recurso.tentarReservar()) {
                recursosReservados.add(recurso);
            } else {
                conseguiuReservarTodos = false;
                break;
            }
        }

        if (!conseguiuReservarTodos) {
            for (Recurso recurso : recursosReservados) {
                recurso.liberar();
            }

            pedido.aumentarTempoEspera();

            System.out.println(this.nome + " não conseguiu executar o pedido agora: " + pedido.getNome());

            return false;
        }

        try {
            System.out.println(this.nome + " iniciou o pedido: " + pedido.getNome());

            Thread.sleep(pedido.getTempoExecucao());

            for (Recurso recurso : recursosReservados) {
                recurso.registrarUso();
            }

            System.out.println(this.nome + " terminou o pedido: " + pedido.getNome());

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.out.println(this.nome + " Execução interrompida para o artesão: " + pedido.getNome());
            return false;

        } finally {
            for (Recurso recurso : recursosReservados) {
                recurso.liberar();
            }
        }
    }

    @Override
    public void run() {
        while (this.temPedidos()) {
            Pedido pedido = this.pegarProximoPedido();

            boolean executado = this.executarPedido(pedido);

            if (!executado && pedido != null) {
                this.adicionarPedido(pedido);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println(this.nome + " Terminou os pedidos.");
    }

    @Override
    public String toString() {
        return "Artesão: " + this.nome // exibe o nome do artesão
                + " | Tipo: " + this.tipo // exibe o tipo do Artesão ex: Ferreiro
                + " | Quantidade de pedidos: " + this.pedidos.size(); // exibe a quantidade de pedidos do artesão
    }
}