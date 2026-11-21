package br.com.arthurhenrique.api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.arthurhenrique.log.LogManager;
import br.com.arthurhenrique.model.Projeto;
import br.com.arthurhenrique.service.ProjetoService;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

public class Api {

    private static Javalin instancia;

    public static synchronized Javalin iniciar(String usuarioAcesso) {
        if (instancia != null) {
            return instancia;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ProjetoService service = new ProjetoService();
        try {
            service.carregar();
            LogManager.application("INFO", "Sistema iniciado e dados carregados com sucesso.");
        } catch (Exception e) {
            LogManager.application("ERROR", "Falha ao carregar dados iniciais: " + e.getMessage());
            LogManager.error("CSV/LOAD", "Erro leitura CSV: " + e.getMessage());
        }

        LogManager.access(usuarioAcesso, "LOGIN");

        var app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, false));
            config.staticFiles.add("/public");

            config.routes.get("/", ctx -> {
                ctx.redirect("/projetos.html");
                LogManager.api("GET", "/", 302);
            });

            // -- GET / READ --
            config.routes.get("/api/projetos", ctx -> {
                try {
                    List<Projeto> projetos = service.listar();
                    ctx.status(200).json(projetos);
                    LogManager.api("GET", "/api/projetos", 200);
                } catch (Exception e) {
                    ctx.status(500).json(Map.of("erro", "Erro ao listar projetos."));
                    LogManager.error("API_GET_ALL", e.getMessage());
                    LogManager.api("GET", "/api/projetos", 500);
                }
            });

            // busca por id
            config.routes.get("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                try {
                    int id = Integer.parseInt(idParam);
                    if (id <= 0) {
                        ctx.status(400).json(Map.of("erro", "ID deve ser um número inteiro positivo."));
                        LogManager.error("API_GET_ID", "ID inválido: " + idParam);
                        LogManager.api("GET", "/api/projetos/" + idParam, 400);
                        return;
                    }

                    Projeto projeto = service.buscaPorId(id);
                    if (projeto == null) {
                        ctx.status(404).json(Map.of("erro", "Projeto não encontrado."));
                        LogManager.api("GET", "/api/projetos/" + id, 404);
                        return;
                    }

                    ctx.status(200).json(projeto);
                    LogManager.api("GET", "/api/projetos/" + id, 200);
                } catch (NumberFormatException e) {
                    ctx.status(400).json(Map.of("erro", "ID deve ser numérico."));
                    LogManager.error("API_GET_ID", "ID não numérico: " + idParam);
                    LogManager.api("GET", "/api/projetos/" + idParam, 400);
                }
            });

            // -- POST / CREATE --
            config.routes.post("/api/projetos", ctx -> {
                try {
                    Projeto projeto = ctx.bodyAsClass(Projeto.class);

                    if (projeto.getId() == 0) {
                        projeto.setId(service.gerarId());
                    }

                    service.adicionar(projeto);
                    service.salvar();

                    LogManager.audit(projeto.getId(), "CRIADO");
                    LogManager.api("POST", "/api/projetos", 201);
                    ctx.status(201).json(projeto);
                } catch (IllegalArgumentException e) {
                    ctx.status(400).json(Map.of("erro", e.getMessage()));
                    LogManager.error("API_POST_VALIDACAO", "Campo inválido: " + e.getMessage());
                    LogManager.api("POST", "/api/projetos", 400);
                } catch (Exception e) {
                    ctx.status(500).json(Map.of("erro", "Erro ao salvar projeto: " + e.getMessage()));
                    LogManager.error("API_POST_PERSISTENCIA", e.getMessage());
                    LogManager.api("POST", "/api/projetos", 500);
                }
            });

            // -- PUT / UPDATE --
            config.routes.put("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                try {
                    int id = Integer.parseInt(idParam);
                    Projeto projeto = ctx.bodyAsClass(Projeto.class);
                    projeto.setId(id);

                    boolean alterou = service.alterar(projeto);
                    if (!alterou) {
                        ctx.status(404).json(Map.of("erro", "Projeto não encontrado para alteração."));
                        LogManager.api("PUT", "/api/projetos/" + id, 404);
                        return;
                    }

                    service.salvar();
                    LogManager.audit(id, "ALTERADO");
                    LogManager.api("PUT", "/api/projetos/" + id, 200);
                    ctx.status(200).json(projeto);
                } catch (NumberFormatException e) {
                    ctx.status(400).json(Map.of("erro", "ID inválido na URL."));
                    LogManager.error("API_PUT_ID", "ID inválido: " + idParam);
                    LogManager.api("PUT", "/api/projetos/" + idParam, 400);
                } catch (IllegalArgumentException e) {
                    ctx.status(400).json(Map.of("erro", e.getMessage()));
                    LogManager.error("API_PUT_VALIDACAO", "Campo inválido: " + e.getMessage());
                    LogManager.api("PUT", "/api/projetos/" + idParam, 400);
                } catch (Exception e) {
                    ctx.status(500).json(Map.of("erro", "Erro ao alterar projeto: " + e.getMessage()));
                    LogManager.error("API_PUT_PERSISTENCIA", e.getMessage());
                    LogManager.api("PUT", "/api/projetos/" + idParam, 500);
                }
            });

            // -- DELETE --
            config.routes.delete("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                try {
                    int id = Integer.parseInt(idParam);
                    Projeto projeto = service.buscaPorId(id);

                    if (projeto == null) {
                        ctx.status(404).json(Map.of("erro", "Projeto não encontrado para exclusão."));
                        LogManager.api("DELETE", "/api/projetos/" + id, 404);
                        return;
                    }

                    service.removerPorId(id);
                    service.salvar();

                    LogManager.audit(id, "EXCLUIDO");
                    LogManager.api("DELETE", "/api/projetos/" + id, 204);
                    ctx.status(204);
                } catch (NumberFormatException e) {
                    ctx.status(400).json(Map.of("erro", "ID inválido na URL."));
                    LogManager.error("API_DELETE_ID", "ID inválido: " + idParam);
                    LogManager.api("DELETE", "/api/projetos/" + idParam, 400);
                } catch (Exception e) {
                    ctx.status(500).json(Map.of("erro", "Erro ao excluir projeto: " + e.getMessage()));
                    LogManager.error("API_DELETE_PERSISTENCIA", e.getMessage());
                    LogManager.api("DELETE", "/api/projetos/" + idParam, 500);
                }
            });
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogManager.application("INFO", "Sistema encerrado.");
        }));

        app.start(7070);
        instancia = app;
        return app;
    }

    public static void main(String[] args) {
        iniciar(System.getProperty("user.name"));
    }
}
