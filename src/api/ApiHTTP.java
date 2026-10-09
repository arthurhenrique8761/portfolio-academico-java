package api;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class ApiHTTP {
    public static void main(String[] args)
            throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(7070),0);
        server.start();
        System.out.println("Servidor iniciado");

        server.createContext("/", exchange -> {
            String resposta = "API Sistema de Projetos";

            exchange.sendResponseHeaders(200, resposta.getBytes().length);
            exchange.getResponseBody().write(resposta.getBytes());
            exchange.close();
        });
    }

}
