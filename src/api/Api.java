package api;

import io.javalin.Javalin;
import java.util.List;

import model.Projeto;
import service.ProjetoService;

public class Api {

    // TO DO
    // fazer isso funcionar

    public static void main(String[] args) {

        var app = Javalin.create(config -> {

            config.routes.get("/", ctx -> {
                ctx.result("API Sistema de Projetos");
            });

        }).start(7070);
    }
}