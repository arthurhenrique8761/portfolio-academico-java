package br.com.arthurhenrique.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.arthurhenrique.model.Projeto;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:7070/api/projetos";
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String usuarioLogado;

    public ApiClient() {
        this(System.getProperty("user.name"));
    }

    public ApiClient(String usuarioLogado) {
        this.usuarioLogado = (usuarioLogado != null && !usuarioLogado.isBlank())
                ? usuarioLogado.trim()
                : "";
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    private HttpRequest.Builder novaRequisicao(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (!usuarioLogado.isEmpty()) {
            builder.header("X-Usuario-Logado", usuarioLogado);
        }
        return builder;
    }

    public List<Projeto> listarProjetos() throws Exception {
        HttpRequest request = novaRequisicao(BASE_URL)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validarResposta(response);
        return mapper.readValue(response.body(), new TypeReference<List<Projeto>>() {
        });
    }

    public Projeto cadastrarProjeto(Projeto projeto) throws Exception {
        String json = mapper.writeValueAsString(projeto);
        HttpRequest request = novaRequisicao(BASE_URL)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validarResposta(response);
        return mapper.readValue(response.body(), Projeto.class);
    }

    public Projeto alterarProjeto(int id, Projeto projeto) throws Exception {
        String json = mapper.writeValueAsString(projeto);
        HttpRequest request = novaRequisicao(BASE_URL + "/" + id)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validarResposta(response);
        return mapper.readValue(response.body(), Projeto.class);
    }

    public void excluirProjeto(int id) throws Exception {
        HttpRequest request = novaRequisicao(BASE_URL + "/" + id)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        validarResposta(response);
    }

    private void validarResposta(HttpResponse<String> response) {
        if (response.statusCode() >= 400) {
            throw new RuntimeException(extrairMensagemErro(response.body()));
        }
    }

    private String extrairMensagemErro(String body) {
        try {
            var node = mapper.readTree(body);
            if (node.has("erro")) {
                return node.get("erro").asText();
            }
        } catch (Exception ignored) {
        }
        return body != null && !body.isBlank() ? body : "Erro na requisição HTTP.";
    }
}
