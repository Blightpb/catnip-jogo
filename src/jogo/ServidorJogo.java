package jogo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Path pastaWeb = Path.of("web").toAbsolutePath().normalize();

        Placar placar = new Placar(Path.of("placar.properties"));
        Partida partida = new Partida(placar);

        // Relogio do jogo: avanca a partida a cada 40 ms.
        ScheduledExecutorService relogio = Executors.newSingleThreadScheduledExecutor();
        relogio.scheduleAtFixedRate(() -> {
            try {
                partida.atualizar();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }, 0, INTERVALO_MS, TimeUnit.MILLISECONDS);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);

        servidor.createContext("/api/estado", ex ->
                enviar(ex, 200, "application/json; charset=utf-8", partida.paraJson()));

        servidor.createContext("/api/iniciar", ex -> {
            partida.iniciar();
            enviar(ex, 200, "application/json; charset=utf-8", partida.paraJson());
        });

        servidor.createContext("/api/mover", ex -> {
            String consulta = ex.getRequestURI().getQuery();
            if ("dir=esq".equals(consulta)) {
                partida.moverJogador("esq");
            } else if ("dir=dir".equals(consulta)) {
                partida.moverJogador("dir");
            }
            enviar(ex, 200, "application/json; charset=utf-8", partida.paraJson());
        });

        servidor.createContext("/", ex -> servirArquivo(ex, pastaWeb));

        servidor.start();
        System.out.println("Jogo rodando! Abra no navegador: http://localhost:" + porta);
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
