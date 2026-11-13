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

            config.staticFiles.add("/public");

            // Raiz da API
            config.routes.get("/", ctx -> {
                ctx.redirect("/api/projetos");
            });

            // -- GET / READ --
            // retorna todos os projetos
            config.routes.get("/api/projetos", ctx -> {
                List<Projeto> projetos = service.listar();

                if (projetos.isEmpty()) {
                    ctx.status(404);
                    return;
                }

                ctx.json(projetos);
            });

            // busca por id
            config.routes.get("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                Projeto projeto = service.buscaPorId(id);

                if (projeto == null) {
                    ctx.status(404);
                    return;
                }

                ctx.json(projeto);
            });

            /*
             * // categorias em json
             * config.routes.get("/api/categorias", ctx -> {
             * List<String> categorias = List.of("Web", "Software", "Mobile", "Outros");
             * ctx.json(categorias);
             * });
             * 
             * // status em json
             * config.routes.get("/api/status", ctx -> {
             * List<String> status = List.of("Em desenvolvimento", "Concluído",
             * "Planejado");
             * ctx.json(status);
             * });
             */

            // -- POST / CREATE --
            config.routes.post("/api/projetos", ctx -> {
                Projeto projeto = ctx.bodyAsClass(Projeto.class);

                if (projeto.getId() == 0)
                    projeto.setId(service.gerarId());

                if (projeto.getNome() == null || projeto.getNome().isBlank()) {
                    ctx.json("{\"erro\":\"Nome é obrigatório\"}");
                    return;
                }

                service.adicionar(projeto);
                service.salvar();

                ctx.status(201);
                ctx.json(projeto);
            });

            // -- PUT / UPDATE --
            config.routes.put("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                Projeto projeto = ctx.bodyAsClass(Projeto.class);

                projeto.setId(id);

                boolean alterou = service.alterar(projeto);

                if (!alterou) {
                    ctx.status(404);
                    return;
                }

                service.salvar();
                ctx.json(projeto);
            });

            // -- DELETE --
            config.routes.delete("/api/projetos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));

                Projeto projeto = service.buscaPorId(id);

                if (projeto == null) {
                    ctx.status(404);
                    return;
                }

                service.removerPorId(id);
                service.salvar();

                ctx.status(204);
            });
        });
        app.start(7070);
    }
}