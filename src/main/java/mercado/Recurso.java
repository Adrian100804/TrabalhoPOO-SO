package mercado;

import java.util.concurrent.locks.ReentrantLock;

public class Recurso extends Thread {

    private String nome;
    private TipoRecurso tipo;
    private ReentrantLock lock; // Garante que só uma thread use o recurso por vez
    private boolean emManutencao;
    private int quantidadeUsos;

    private static final int LIMITE_USOS_MANUTENCAO = 3; // define que o limite de uso para a manutenção seja 3 
    private static final int TEMPO_MANUTENCAO = 1000; // define um tempo de espera para menuteção de 1 segundo

    public Recurso(String nome, TipoRecurso tipo) {
        this.nome = nome;
        this.tipo = tipo;
        this.lock = new ReentrantLock(true); // o reentrandolock ele trata a parte de sincronização, faz com que apenas uma thread por vez reserve o recurso necessario
        this.emManutencao = false;
        this.quantidadeUsos = 0;
    }

    public String getNome() {
        return this.nome;
    }

    public TipoRecurso getTipo() {
        return this.tipo;
    }

    public boolean isEmManutencao() {
        return this.emManutencao;
    }

    public int getQuantidadeUsos() {
        return this.quantidadeUsos;
    }

    public void setEmManutencao(boolean emManutencao) {
        this.emManutencao = emManutencao;
    }

    public boolean tentarReservar() { 
        if (this.emManutencao) { // ele tenta reservar o recurso se ele estiver manuteção ele quebra o laço 
            return false;
        }

        return this.lock.tryLock(); // o trylock serve para reservar o recurso sem travar a thread para sempre 
    }

    public void liberar() {
        if (this.lock.isHeldByCurrentThread()) {
            this.lock.unlock(); // o unlock libera o recurso para a fila novamente
        }
    }

    private void realizarManutencaoAutomatica() { // realiza a manutenção automatica quando atinge uma certa quantidade uso
        this.emManutencao = true;

        System.out.println(this.nome + " entrou em manutenção.");

        try {
            Thread.sleep(TEMPO_MANUTENCAO); // a thread para a execução com o tempo definido em 1 segundo 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(" Manutenção interrompida em: " + this.nome);
        }

        this.quantidadeUsos = 0; // a quantidade é zerada novamente
        this.emManutencao = false;

        System.out.println(this.nome + " saiu da manutenção.");
    }

    public void registrarUso() { // toda vez que o recurso é usado ele adiciona 1 na quantidade de uso 
        this.quantidadeUsos = this.quantidadeUsos + 1;

        if (this.quantidadeUsos >= LIMITE_USOS_MANUTENCAO) { // se a quantidade de uso chegar o limite ele entra em manutenção automatica
            this.realizarManutencaoAutomatica();
        }
    }

    public boolean estaDisponivel() { // ele verifica se o recurso esta disponivel para uso
        return !this.emManutencao && !this.lock.isLocked(); // essa linha quer dizer que o codigo não esta em manutenção e se não estiver sendo usado por outra thread
    }

    @Override
    public String toString() {
        return "Recurso: " + this.nome
                + " | Tipo: " + this.tipo // Tipo Enum que vai ser passado.
                + " | Em manutenção: " + this.emManutencao //Indica se o recurso esta disponivel ou maanutenção.
                + " | Quantidade de usos: " + this.quantidadeUsos; // Mostra a quantidade de usos dos recursos para ser reparados futuramente.
    }
}