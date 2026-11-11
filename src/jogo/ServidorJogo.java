package jogo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Ponto de entrada. Sobe um servidor HTTP que:
 *  - entrega os arquivos da pasta "web" (HTML, CSS e JavaScript);
 *  - expoe uma pequena API para o navegador controlar a partida:
 *      GET  /api/estado        -> estado atual do jogo em JSON
 *      POST /api/iniciar       -> comeca uma nova rodada
 *      POST /api/mover?dir=esq -> move o jogador (esq ou dir)
 */
public class ServidorJogo {

    private static final int INTERVALO_MS = 40;
    private static final long INATIVIDADE_MS = 30 * 60 * 1000L; // descarta sessoes paradas ha 30 min
    private static final String JSON = "application/json; charset=utf-8";

    /** Cada visitante (cookie "sid") tem a sua propria partida e o seu proprio placar. */
    private static final class Sessao {
        final Partida partida;
        volatile long ultimoAcesso = System.currentTimeMillis();

        Sessao(Partida partida) {
            this.partida = partida;
        }
    }

    private static final Map<String, Sessao> SESSOES = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        // Hospedagens informam a porta pela variavel PORT.
        String portaEnv = System.getenv("PORT");
        int porta = args.length > 0 ? Integer.parseInt(args[0])
                : portaEnv != null ? Integer.parseInt(portaEnv) : 8080;
        Path pastaWeb = Path.of("web").toAbsolutePath().normalize();
        String dados = System.getenv("DATA_DIR");
        Path pastaDados = Path.of(dados != null ? dados : "dados").toAbsolutePath().normalize();
        Files.createDirectories(pastaDados);

        // Relogio do jogo: avanca todas as partidas a cada 40 ms e limpa sessoes inativas.
        ScheduledExecutorService relogio = Executors.newSingleThreadScheduledExecutor();
        relogio.scheduleAtFixedRate(() -> {
            try {
                long agora = System.currentTimeMillis();
                SESSOES.entrySet().removeIf(e -> agora - e.getValue().ultimoAcesso > INATIVIDADE_MS);
                for (Sessao s : SESSOES.values()) {
                    s.partida.atualizar();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }, 0, INTERVALO_MS, TimeUnit.MILLISECONDS);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.setExecutor(Executors.newCachedThreadPool());

        servidor.createContext("/api/estado", ex -> {
            Partida partida = partidaDe(ex, pastaDados);
            enviar(ex, 200, JSON, partida.paraJson());
        });

        servidor.createContext("/api/iniciar", ex -> {
            Partida partida = partidaDe(ex, pastaDados);
            partida.iniciar();
            enviar(ex, 200, JSON, partida.paraJson());
        });

        servidor.createContext("/api/mover", ex -> {
            Partida partida = partidaDe(ex, pastaDados);
            String consulta = ex.getRequestURI().getQuery();
            if ("dir=esq".equals(consulta)) {
                partida.moverJogador("esq");
            } else if ("dir=dir".equals(consulta)) {
                partida.moverJogador("dir");
            }
            enviar(ex, 200, JSON, partida.paraJson());
        });

        // Usado pela hospedagem para saber se o servico esta de pe.
        servidor.createContext("/saude", ex -> enviar(ex, 200, "text/plain; charset=utf-8", "ok"));

        servidor.createContext("/", ex -> servirArquivo(ex, pastaWeb));

        servidor.start();
        System.out.println("Jogo rodando na porta " + porta + " (local: http://localhost:" + porta + ")");
    }

    /** Acha (ou cria) a partida do visitante a partir do cookie "sid". */
    private static Partida partidaDe(HttpExchange ex, Path pastaDados) {
        String sid = lerCookie(ex, "sid");
        if (sid == null || !sid.matches("[0-9a-f\\-]{36}")) {
            sid = UUID.randomUUID().toString();
            ex.getResponseHeaders().add("Set-Cookie",
                    "sid=" + sid + "; Path=/; Max-Age=31536000; HttpOnly; SameSite=Lax");
        }
        final String id = sid;
        Sessao sessao = SESSOES.computeIfAbsent(id,
                k -> new Sessao(new Partida(new Placar(pastaDados.resolve(id + ".properties")))));
        sessao.ultimoAcesso = System.currentTimeMillis();
        return sessao.partida;
    }

    private static String lerCookie(HttpExchange ex, String nome) {
        String cabecalho = ex.getRequestHeaders().getFirst("Cookie");
        if (cabecalho == null) {
            return null;
        }
        for (String par : cabecalho.split(";")) {
            String[] kv = par.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(nome)) {
                return kv[1];
            }
        }
        return null;
    }

    private static void servirArquivo(HttpExchange ex, Path pastaWeb) throws IOException {
        String caminho = ex.getRequestURI().getPath();
        if ("/".equals(caminho)) {
            caminho = "/index.html";
        }
        Path arquivo = pastaWeb.resolve(caminho.substring(1)).normalize();

        // Impede acessar arquivos fora da pasta "web".
        if (!arquivo.startsWith(pastaWeb) || !Files.isRegularFile(arquivo)) {
            enviar(ex, 404, "text/plain; charset=utf-8", "Nao encontrado");
            return;
        }
        enviarBytes(ex, 200, tipoDoArquivo(arquivo.getFileName().toString()), Files.readAllBytes(arquivo));
    }

    private static String tipoDoArquivo(String nome) {
        if (nome.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (nome.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (nome.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (nome.endsWith(".gif")) {
            return "image/gif";
        }
        if (nome.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private static void enviar(HttpExchange ex, int codigo, String tipo, String corpo) throws IOException {
        enviarBytes(ex, codigo, tipo, corpo.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviarBytes(HttpExchange ex, int codigo, String tipo, byte[] corpo) throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(codigo, corpo.length);
        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(corpo);
        }
    }
}