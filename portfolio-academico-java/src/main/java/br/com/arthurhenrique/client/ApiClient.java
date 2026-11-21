package br.com.arthurhenrique.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.arthurhenrique.model.Projeto;

public class ApiClient {

    private final String baseUrl = "http://localhost:7070";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ApiClient() {
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private String extrairMensagemErro(String body, int statusCode) {
        if (body != null && !body.isBlank()) {
            try {
                Map<String, Object> mapa = mapper.readValue(body, new TypeReference<Map<String, Object>>() {
                });
                if (mapa.get("erro") != null) {
                    return mapa.get("erro").toString();
                }
            } catch (Exception ignored) {
            }
            return body;
        }
        return "Erro HTTP " + statusCode;
    }

    // GET
    public List<Projeto> listarProjetos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(extrairMensagemErro(response.body(), response.statusCode()));
        }

        return mapper.readValue(response.body(), new TypeReference<List<Projeto>>() {
        });
    }

    // POST
    public Projeto cadastrarProjeto(Projeto projeto) throws Exception {
        String json = mapper.writeValueAsString(projeto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(extrairMensagemErro(response.body(), response.statusCode()));
        }

        return mapper.readValue(response.body(), Projeto.class);
    }

    // DELETE
    public void excluirProjeto(int id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new RuntimeException(extrairMensagemErro(response.body(), response.statusCode()));
        }
    }

    // PUT
    public Projeto alterarProjeto(int id, Projeto projeto) throws Exception {
        String json = mapper.writeValueAsString(projeto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(extrairMensagemErro(response.body(), response.statusCode()));
        }

        return mapper.readValue(response.body(), Projeto.class);
    }
}
