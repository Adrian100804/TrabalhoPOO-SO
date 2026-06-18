package mercado;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServidorWeb {

    private static final int PORTA = 8081;
    private static final Object SYSOUT_LOCK = new Object();

    private Mercado mercado = new Mercado();
    private final List<Recurso> recursos = Collections.synchronizedList(new ArrayList<>());
    private final List<Artesao> artesaos = Collections.synchronizedList(new ArrayList<>());
    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ThreadInfo> threads = Collections.synchronizedMap(new LinkedHashMap<>());
    private final AtomicBoolean simulacaoRodando = new AtomicBoolean(false);

    public static void main(String[] args) throws Exception {
        new ServidorWeb().iniciar();
    }

    private void iniciar() throws Exception {
        carregarExemploInicial();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);
        server.createContext("/", this::servirArquivoEstatico);
        server.createContext("/api/status", this::status);
        server.createContext("/api/simular", this::simular);
        server.createContext("/api/demo", this::resetarDemo);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Servidor iniciado em: http://localhost:" + PORTA);
        System.out.println("Abra esse endereço no navegador para ver as threads funcionando.");
    }

    private void carregarExemploInicial() {
        Recurso forno = criarRecurso("Forno Central", TipoRecurso.FORNO);
        Recurso bancada = criarRecurso("Bancada Principal", TipoRecurso.BANCADA);
        Recurso ferramentaRara = criarRecurso("Martelo Raro", TipoRecurso.FERRAMENTA_RARA);

        Artesao ferreiro = criarArtesao("Arthur", TipoArtesao.FERREIRO);
        Artesao carpinteiro = criarArtesao("Bernardo", TipoArtesao.CARPINTEIRO);
        Artesao alquimista = criarArtesao("Merlin", TipoArtesao.ALQUIMISTA);

        ferreiro.adicionarPedido(new Pedido("Espada da Realeza", 1600, PrioridadePedido.REALEZA, List.of(forno, ferramentaRara)));
        ferreiro.adicionarPedido(new Pedido("Escudo para Nobre", 1200, PrioridadePedido.NOBRE, List.of(forno, bancada)));
        carpinteiro.adicionarPedido(new Pedido("Mesa para Comerciante", 1100, PrioridadePedido.COMERCIANTE, List.of(bancada)));
        carpinteiro.adicionarPedido(new Pedido("Carruagem para Nobre", 1500, PrioridadePedido.NOBRE, List.of(bancada, ferramentaRara)));
        alquimista.adicionarPedido(new Pedido("Poção da Realeza", 1400, PrioridadePedido.REALEZA, List.of(forno, bancada)));
        alquimista.adicionarPedido(new Pedido("Elixir para Cidadão", 1000, PrioridadePedido.CIDADAO, List.of(ferramentaRara)));

        prepararThreadsResumo();
        log("Exemplo fixo carregado. A interface está em modo somente leitura.");
    }

    private Recurso criarRecurso(String nome, TipoRecurso tipo) {
        Recurso recurso = new Recurso(nome, tipo);
        recursos.add(recurso);
        mercado.adicionarRecurso(recurso);
        return recurso;
    }

    private Artesao criarArtesao(String nome, TipoArtesao tipo) {
        Artesao artesao = new Artesao(nome, tipo, new ArrayList<>());
        artesaos.add(artesao);
        mercado.adicionarArtesao(artesao);
        return artesao;
    }

    private void status(HttpExchange exchange) throws IOException {
        if (!metodo(exchange, "GET")) return;
        responderJson(exchange, 200, montarJsonStatus());
    }

    private void simular(HttpExchange exchange) throws IOException {
        if (!metodo(exchange, "POST")) return;

        if (!simulacaoRodando.compareAndSet(false, true)) {
            responderErro(exchange, "As threads já estão rodando.");
            return;
        }

        log("Simulação iniciada pela interface.");
        log("O backend vai criar uma Thread para cada artesão.");
        prepararThreadsResumo();

        Thread controlador = new Thread(() -> {
            try {
                capturarSaidaDoConsole(this::executarThreadsDosArtesaos);
                log("Todas as threads terminaram a execução.");
            } catch (Exception e) {
                log("Erro durante a simulação: " + e.getMessage());
            } finally {
                simulacaoRodando.set(false);
            }
        }, "controlador-simulacao-web");

        controlador.start();
        responderJson(exchange, 200, montarJsonStatus());
    }

    private void executarThreadsDosArtesaos() {
        List<Thread> threadsCriadas = new ArrayList<>();

        synchronized (artesaos) {
            for (Artesao artesao : artesaos) {
                String nomeThread = "Thread-" + normalizarNomeThread(artesao.getNome());
                atualizarThread(nomeThread, artesao, "CRIADA");

                Thread thread = new Thread(() -> {
                    atualizarThread(nomeThread, artesao, "RODANDO");
                    log(nomeThread + " iniciou o artesão " + artesao.getNome() + ".");

                    try {
                        artesao.run();
                    } finally {
                        atualizarThread(nomeThread, artesao, "FINALIZADA");
                        log(nomeThread + " finalizou o artesão " + artesao.getNome() + ".");
                    }
                }, nomeThread);

                threadsCriadas.add(thread);
            }
        }

        for (Thread thread : threadsCriadas) {
            thread.start();
        }

        for (Thread thread : threadsCriadas) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("A simulação foi interrompida.");
                return;
            }
        }
    }

    private void resetarDemo(HttpExchange exchange) throws IOException {
        if (!metodo(exchange, "POST")) return;

        if (simulacaoRodando.get()) {
            responderErro(exchange, "Espere as threads terminarem para reiniciar a demonstração.");
            return;
        }

        mercado = new Mercado();
        recursos.clear();
        artesaos.clear();
        logs.clear();
        threads.clear();
        carregarExemploInicial();
        log("Demonstração reiniciada.");
        responderJson(exchange, 200, montarJsonStatus());
    }

    private void prepararThreadsResumo() {
        threads.clear();
        synchronized (artesaos) {
            for (Artesao artesao : artesaos) {
                String nomeThread = "Thread-" + normalizarNomeThread(artesao.getNome());
                atualizarThread(nomeThread, artesao, "AGUARDANDO");
            }
        }
    }

    private void atualizarThread(String nomeThread, Artesao artesao, String status) {
        synchronized (threads) {
            threads.put(nomeThread, new ThreadInfo(nomeThread, artesao.getNome(), String.valueOf(artesao.getTipo()), status));
        }
    }

    private String normalizarNomeThread(String nome) {
        return nome.trim().replaceAll("\\s+", "-");
    }

    private void servirArquivoEstatico(HttpExchange exchange) throws IOException {
        String caminho = exchange.getRequestURI().getPath();
        if (caminho.equals("/")) {
            caminho = "/index.html";
        }

        if (caminho.contains("..")) {
            responderTexto(exchange, 400, "Caminho inválido.", "text/plain; charset=utf-8");
            return;
        }

        String recurso = "/web" + caminho;
        InputStream arquivo = ServidorWeb.class.getResourceAsStream(recurso);

        if (arquivo == null) {
            responderTexto(exchange, 404, "Arquivo não encontrado.", "text/plain; charset=utf-8");
            return;
        }

        byte[] bytes = lerTudo(arquivo);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType(caminho));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private void capturarSaidaDoConsole(Runnable acao) {
        synchronized (SYSOUT_LOCK) {
            PrintStream original = System.out;
            PrintStream capturador = new PrintStream(new OutputStream() {
                private final ByteArrayOutputStream linha = new ByteArrayOutputStream();

                @Override
                public void write(int b) throws IOException {
                    original.write(b);
                    if (b == '\n') {
                        enviarLinha();
                    } else if (b != '\r') {
                        linha.write(b);
                    }
                }

                private void enviarLinha() {
                    String texto = linha.toString(StandardCharsets.UTF_8).trim();
                    if (!texto.isEmpty()) {
                        log(texto);
                    }
                    linha.reset();
                }
            }, true, StandardCharsets.UTF_8);

            try {
                System.setOut(capturador);
                acao.run();
            } finally {
                System.setOut(original);
            }
        }
    }

    private String montarJsonStatus() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"rodando\":").append(simulacaoRodando.get()).append(",");

        json.append("\"threads\":[");
        synchronized (threads) {
            int i = 0;
            for (ThreadInfo info : threads.values()) {
                if (i > 0) json.append(",");
                json.append("{")
                        .append("\"nome\":\"").append(escapar(info.nome)).append("\",")
                        .append("\"artesao\":\"").append(escapar(info.artesao)).append("\",")
                        .append("\"tipo\":\"").append(escapar(info.tipo)).append("\",")
                        .append("\"status\":\"").append(escapar(info.status)).append("\"")
                        .append("}");
                i++;
            }
        }
        json.append("],");

        json.append("\"recursos\":[");
        synchronized (recursos) {
            for (int i = 0; i < recursos.size(); i++) {
                Recurso r = recursos.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                        .append("\"id\":").append(i).append(",")
                        .append("\"nome\":\"").append(escapar(r.getNome())).append("\",")
                        .append("\"tipo\":\"").append(r.getTipo()).append("\",")
                        .append("\"usos\":").append(r.getQuantidadeUsos()).append(",")
                        .append("\"manutencao\":").append(r.isEmManutencao()).append(",")
                        .append("\"disponivel\":").append(r.estaDisponivel())
                        .append("}");
            }
        }
        json.append("],");

        json.append("\"artesaos\":[");
        synchronized (artesaos) {
            for (int i = 0; i < artesaos.size(); i++) {
                Artesao a = artesaos.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                        .append("\"id\":").append(i).append(",")
                        .append("\"nome\":\"").append(escapar(a.getNome())).append("\",")
                        .append("\"tipo\":\"").append(a.getTipo()).append("\",")
                        .append("\"quantidadePedidos\":").append(a.getQuantidadePedidos()).append(",")
                        .append("\"pedidos\":[");

                List<Pedido> pedidos = new ArrayList<>(a.getPedidos());
                for (int j = 0; j < pedidos.size(); j++) {
                    Pedido p = pedidos.get(j);
                    if (j > 0) json.append(",");
                    json.append("{")
                            .append("\"nome\":\"").append(escapar(p.getNome())).append("\",")
                            .append("\"tempo\":").append(p.getTempoExecucao()).append(",")
                            .append("\"prioridade\":\"").append(p.getPrioridade()).append("\",")
                            .append("\"tempoEspera\":").append(p.getTempoEspera()).append(",")
                            .append("\"recursos\":[");

                    List<Recurso> recursosPedido = p.getRecursosNecessarios();
                    for (int k = 0; k < recursosPedido.size(); k++) {
                        if (k > 0) json.append(",");
                        json.append("\"").append(escapar(recursosPedido.get(k).getNome())).append("\"");
                    }

                    json.append("]}");
                }
                json.append("]}");
            }
        }
        json.append("],");

        json.append("\"logs\":[");
        synchronized (logs) {
            int inicio = Math.max(0, logs.size() - 160);
            for (int i = inicio; i < logs.size(); i++) {
                if (i > inicio) json.append(",");
                json.append("\"").append(escapar(logs.get(i))).append("\"");
            }
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }

    private boolean metodo(HttpExchange exchange, String metodo) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase(metodo)) {
            responderTexto(exchange, 405, "Método não permitido.", "text/plain; charset=utf-8");
            return false;
        }
        return true;
    }

    private void responderErro(HttpExchange exchange, String mensagem) throws IOException {
        responderJson(exchange, 400, "{\"erro\":\"" + escapar(mensagem) + "\"}");
    }

    private void responderJson(HttpExchange exchange, int status, String json) throws IOException {
        responderTexto(exchange, status, json, "application/json; charset=utf-8");
    }

    private void responderTexto(HttpExchange exchange, int status, String texto, String contentType) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private byte[] lerTudo(InputStream entrada) throws IOException {
        try (InputStream in = entrada; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloco = new byte[4096];
            int lidos;
            while ((lidos = in.read(bloco)) != -1) {
                buffer.write(bloco, 0, lidos);
            }
            return buffer.toByteArray();
        }
    }

    private String contentType(String caminho) {
        if (caminho.endsWith(".html")) return "text/html; charset=utf-8";
        if (caminho.endsWith(".css")) return "text/css; charset=utf-8";
        if (caminho.endsWith(".js")) return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }

    private void log(String mensagem) {
        String linha = java.time.LocalTime.now().withNano(0) + " - " + mensagem;
        logs.add(linha);
    }

    private String escapar(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private static class ThreadInfo {
        private final String nome;
        private final String artesao;
        private final String tipo;
        private final String status;

        private ThreadInfo(String nome, String artesao, String tipo, String status) {
            this.nome = nome;
            this.artesao = artesao;
            this.tipo = tipo;
            this.status = status;
        }
    }
}
