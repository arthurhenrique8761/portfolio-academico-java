package br.com.arthurhenrique.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {

    private static final Path PASTA_LOGS = Path.of("logs");
    private static final DateTimeFormatter FORMATO_DATA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LogManager() {
    }

    private static void escrever(String nomeArquivo, String linha) {
        try {
            Files.createDirectories(PASTA_LOGS);
            Path arquivo = PASTA_LOGS.resolve(nomeArquivo);
            String timestamp = LocalDateTime.now().format(FORMATO_DATA_HORA);
            String registro = timestamp + " " + linha + System.lineSeparator();
            Files.writeString(
                    arquivo,
                    registro,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Falha ao escrever log em " + nomeArquivo + ": " + e.getMessage());
        }
    }

    public static void application(String nivel, String mensagem) {
        escrever("application.log", nivel + " " + mensagem);
    }

    public static void access(String usuario, String acao) {
        String usuarioLimpo = (usuario == null || usuario.isBlank()) ? "anonimo" : usuario.trim();
        escrever("access.log", "USUARIO=" + usuarioLimpo + " " + acao);
    }

    public static void audit(int idProjeto, String acao) {
        escrever("audit.log", "PROJETO " + idProjeto + " " + acao);
    }

    public static void error(String origem, String detalhe) {
        escrever("error.log", "ERROR [" + origem + "] " + detalhe);
    }

    public static void api(String metodo, String rota, int statusHttp) {
        escrever("api.log", metodo + " " + rota + " -> HTTP " + statusHttp);
    }
}
