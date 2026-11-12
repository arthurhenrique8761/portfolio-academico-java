package br.com.arthurhenrique.client;

import java.util.List;
import br.com.arthurhenrique.model.Projeto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiClient {

    // pq iniciar os objetos no construtor?
    private final String baseUrl = "http://localhost:7070";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ApiClient() {
        client = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
    }

    // GET
    public List<Projeto> listarProjetos() throws Exception {
        // faz a requisição pra api
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos"))
                .GET()
                .build();

        // recebe a requisição
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // retorna a requisição em um Json
        // return response.body();

        // converte a requisição de json para list
        return mapper.readValue(response.body(), new TypeReference<List<Projeto>>() {
        });
    }

    // POST
    public Projeto cadastrarProjeto(Projeto projeto) throws Exception {
        // transforma o projeto do parâmetro em json
        String json = mapper.writeValueAsString(projeto);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/projetos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
            throw new RuntimeException("Erro ao excluir o projeto: " + response.statusCode());
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
            throw new RuntimeException("Erro ao alterar projeto: " + response.statusCode());
        }

        return mapper.readValue(response.body(), Projeto.class);
    }
}