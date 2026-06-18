package mercado;

import java.util.List;

public class Pedido {

    private String nome;
    private long tempoExecucao;
    private PrioridadePedido prioridade;
    private List<Recurso> recursosNecessarios;
    private int tempoEspera;

    public Pedido(String nome, long tempoExecucao, PrioridadePedido prioridade, List<Recurso> recursosNecessarios) {
        this.nome = nome;
        this.tempoExecucao = tempoExecucao;
        this.prioridade = prioridade;
        this.recursosNecessarios = recursosNecessarios;
        this.tempoEspera = 0;
    }

    public String getNome() {
        return this.nome;
    }

    public long getTempoExecucao() {
        return this.tempoExecucao;
    }

    public PrioridadePedido getPrioridade() {
        return this.prioridade;
    }

    public List<Recurso> getRecursosNecessarios() {
        return this.recursosNecessarios;
    }

    public int getTempoEspera() {
        return this.tempoEspera;
    }

    public void setTempoEspera(int tempoEspera) {
        this.tempoEspera = tempoEspera;
    }

    public void aumentarTempoEspera() {
        this.tempoEspera = this.tempoEspera + 1;
    }

    @Override
    public String toString() {
        return "Nome: " + this.nome
                + " | Tempo: " + this.tempoExecucao
                + " | Prioridade: " + this.prioridade
                + " | Tempo de espera: " + this.tempoEspera;
    }
}