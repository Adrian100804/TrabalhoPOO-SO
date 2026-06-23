package mercado;

import java.util.ArrayList;
import java.util.List;

public class Mercado {

    private List<Artesao> artesaos; //lista de artesãos
    private List<Recurso> recursos; //lista de recursos

    public Mercado() { //construtor completo onde vai começar com duas listas vazias
        this.artesaos = new ArrayList<>();
        this.recursos = new ArrayList<>();
    }

    public void adicionarArtesao(Artesao artesao) { //coloca um artesão dentro da lista mercado
        this.artesaos.add(artesao);
    }

    public void adicionarRecurso(Recurso recurso) { //coloca um recurso dentro da lista mercado
        this.recursos.add(recurso);
    }

    public void mostrarArtesaos() { //metodo percorre a lista artesão e imprime cada um
        System.out.println("\n--- Artesãos do Mercado ---");

        for (Artesao artesao : artesaos) {
            System.out.println(artesao);
        }
    }

    public void mostrarRecursos() { //metodo percorre a lista recurso e imprime cada um
        System.out.println("\n--- Recursos do Mercado ---");

        for (Recurso recurso : recursos) { // percorre o vetor de recursos e mostra os recursos
            System.out.println(recurso);
        }
    }

    public void iniciarSimulacao(){
        System.out.println("\n--- Iniciando a Simulação ---");

        List<Thread> threads = new ArrayList<>(); //lista guarda que guarda as threads

        for (Artesao artesao : this.artesaos) {
            Thread thread = new Thread(artesao); //cria a thread para cada artesão
            threads.add(thread); //guarda essa Thread na lista
            thread.start(); //inicia essa Thread
        }

        for(Thread thread : threads){
        try {
                thread.join(); //espera cada thread terminar a execução para depois finalizar o processo
            } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(" A simulação foi interrompida.");
            }
        }
        System.out.println("\n--- Simulação finalizada ---");
    }
}
