package br.com.arthurhenrique.dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.arthurhenrique.model.Projeto;
import br.com.arthurhenrique.service.ProjetoService;

public class ProjetoCSV {

    private Path caminho;

    public ProjetoCSV() {
        caminho = Path.of("portfolio-academico-java/data/projetos.csv");
    }

    public void salvar(List<Projeto> projetos) throws Exception {
        if (caminho.getParent() != null) {
            Files.createDirectories(caminho.getParent());
        }

        List<String> linhas = new ArrayList<>();
        linhas.add("id;nome;descricao;categoria;status;dataCriacao;prazo");

        for (Projeto projeto : projetos) {
            String dataCriacaoStr = projeto.getDataCriacao() != null ? projeto.getDataCriacao().toString() : "";
            String prazoStr = projeto.getPrazo() != null ? projeto.getPrazo().toString() : "";

            String linha = projeto.getId() + ";"
                    + sanitizarCampo(projeto.getNome()) + ";"
                    + sanitizarCampo(projeto.getDescricao()) + ";"
                    + sanitizarCampo(projeto.getCategoria()) + ";"
                    + sanitizarCampo(projeto.getStatus()) + ";"
                    + dataCriacaoStr + ";"
                    + prazoStr;

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

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha == null || linha.isBlank()) {
                continue;
            }
            String[] dados = linha.split(";", -1);

            int id = Integer.parseInt(dados[0].trim());
            String nome = dados.length > 1 ? dados[1] : "";
            String descricao = dados.length > 2 ? dados[2] : "";
            String categoria = dados.length > 3 ? dados[3] : "";
            String status = dados.length > 4 ? dados[4] : "";

            LocalDate dataCriacao = (dados.length > 5 && !dados[5].isBlank())
                    ? ProjetoService.parseDataFlexivel(dados[5])
                    : LocalDate.now();

            LocalDate prazo = (dados.length > 6 && !dados[6].isBlank())
                    ? ProjetoService.parseDataFlexivel(dados[6])
                    : null;

            Projeto projeto = new Projeto(id, nome, descricao, categoria, status, dataCriacao, prazo);
            projetos.add(projeto);
        }

        return projetos;
    }

    private String sanitizarCampo(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(";", ",").replace("\r", " ").replace("\n", " ");
    }
}
