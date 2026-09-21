package dao;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import model.Projeto;

public class ProjetoCSV {
    private Path caminho;

    public ProjetoCSV() {
        caminho = Path.of("dados/projetos.csv");
    }

    public void salvar(List<Projeto> projetos) throws Exception {
        List<String> linhas = new ArrayList<>();
        linhas.add("id;nome;descricao;categoria;status");
        for (Projeto projeto : projetos) {
            String linha = 
            projeto.getId() + ";" +
            projeto.getNome() + ";" +
            projeto.getDescricao() + ";" +
            projeto.getCategoria() + ";" +
            projeto.getStatus();

            linhas.add(linha);
        }
        Files.write(caminho, linhas);
    }

    public List<Projeto> listar() throws Exception {
        List<Projeto> projetos = new ArrayList<>();

        if (!Files.exists(caminho)) {
            return projetos;
        }

        List<String> linhas = Files.readAllLines(caminho);

        for (int i=1; i<linhas.size(); i++) {
            String linha = linhas.get(i);
            String[] dados = linha.split(";");

            int id = Integer.parseInt(dados[0]);
            String nome = dados[1];
            String descricao = dados[2];
            String categoria = dados[3];
            String status = dados[4];

            Projeto projeto = new Projeto(id, nome, descricao, categoria, status);

            projetos.add(projeto);
        }

        return projetos;
    }
}
