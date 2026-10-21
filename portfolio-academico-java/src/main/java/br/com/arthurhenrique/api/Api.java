package br.com.arthurhenrique.api;

import br.com.arthurhenrique.service.ProjetoService;
import br.com.arthurhenrique.model.Projeto;

import io.javalin.Javalin;

import java.util.List;

public class Api {
    public static void main(String[] args) throws Exception {
        ProjetoService service = new ProjetoService();
        service.carregar();

        var app = Javalin.create(config -> {
            // raiz da api
            config.routes.get("/", ctx -> {
                ctx.result("API de Projetos");
            });

            // retorna todos os projetos em json
            config.routes.get("/api/projetos", ctx -> {
                List<Projeto> projetos = service.listar();

                if (projetos.isEmpty()) {
                    ctx.status(404);
                    return;
                }

                ctx.json(projetos);

            });

            // retorna por id, sendo as chaves um parametro
            config.routes.get("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                Projeto projeto = service.buscaPorId(id);

                if (projeto == null) {
                    ctx.status(404);
                    return;
                }

                ctx.json(projeto);
            });

            // categorias em json
            config.routes.get("/api/categorias", ctx -> {
                List<String> categorias = List.of("Web", "Software", "Mobile", "Outros");
                ctx.json(categorias);
            });

            // status em json
            config.routes.get("/api/status", ctx -> {
                List<String> status = List.of("Em desenvolvimento", "Concluído", "Planejado");
                ctx.json(status);
            });
        });
        app.start(7070);
    }
}