package br.com.arthurhenrique.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.arthurhenrique.dao.ProjetoCSV;
import br.com.arthurhenrique.model.Projeto;

public class ProjetoService {

    public static final Set<String> CATEGORIAS_VALIDAS = Set.of(
            "Software", "Mobile", "Web", "Desktop"
    );

    public static final Set<String> STATUS_VALIDOS = Set.of(
            "Planejado", "Em Desenvolvimento", "Concluído", "Cancelado"
    );

    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private List<Projeto> projetos;
    private ProjetoCSV dao;

    public ProjetoService() {
        projetos = new ArrayList<>();
        dao = new ProjetoCSV();
    }

    public void carregar() throws Exception {
        projetos = dao.listar();
    }

    public void salvar() throws Exception {
        dao.salvar(projetos);
    }

    public static LocalDate parseDataFlexivel(String textoData) {
        if (textoData == null || textoData.isBlank()) {
            return null;
        }
        String limpa = textoData.trim();
        try {
            if (limpa.contains("/")) {
                return LocalDate.parse(limpa, FORMATO_BR);
            }
            return LocalDate.parse(limpa, FORMATO_ISO);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida: " + textoData);
        }
    }

    public void validarProjeto(Projeto projeto, boolean exigeIdUnico, Integer idIgnoradoNaAlteracao) {
        if (projeto == null) {
            throw new IllegalArgumentException("Projeto não pode ser nulo.");
        }

        // ID: inteiro positivo e não repetido
        if (projeto.getId() <= 0) {
            throw new IllegalArgumentException("ID deve ser um número inteiro positivo.");
        }

        if (exigeIdUnico) {
            Projeto existente = buscaPorId(projeto.getId());
            if (existente != null && (idIgnoradoNaAlteracao == null || existente.getId() != idIgnoradoNaAlteracao)) {
                throw new IllegalArgumentException("Já existe um projeto cadastrado com o ID " + projeto.getId() + ".");
            }
        }

        // Nome: obrigatório, trim, 3 a 100 caracteres
        String nomeTrim = projeto.getNome() != null ? projeto.getNome().trim() : "";
        if (nomeTrim.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (nomeTrim.length() < 3 || nomeTrim.length() > 100) {
            throw new IllegalArgumentException("Nome deve ter entre 3 e 100 caracteres.");
        }
        projeto.setNome(nomeTrim);

        // Descrição: trim, 10 a 500 caracteres
        String descTrim = projeto.getDescricao() != null ? projeto.getDescricao().trim() : "";
        if (descTrim.isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }
        if (descTrim.length() < 10 || descTrim.length() > 500) {
            throw new IllegalArgumentException("Descrição deve ter entre 10 e 500 caracteres.");
        }
        projeto.setDescricao(descTrim);

        // Categoria: catálogo fechado (Software, Mobile, Web, Desktop)
        String catTrim = projeto.getCategoria() != null ? projeto.getCategoria().trim() : "";
        String categoriaOficial = CATEGORIAS_VALIDAS.stream()
                .filter(c -> c.equalsIgnoreCase(catTrim))
                .findFirst()
                .orElse(null);
        if (categoriaOficial == null) {
            throw new IllegalArgumentException("Categoria inválida. Use: Software, Mobile, Web ou Desktop.");
        }
        projeto.setCategoria(categoriaOficial);

        // Status: lista fechada (Planejado, Em Desenvolvimento, Concluído, Cancelado)
        String statusTrim = projeto.getStatus() != null ? projeto.getStatus().trim() : "";
        String statusOficial = STATUS_VALIDOS.stream()
                .filter(s -> s.equalsIgnoreCase(statusTrim))
                .findFirst()
                .orElse(null);
        if (statusOficial == null) {
            throw new IllegalArgumentException("Status inválido. Use: Planejado, Em Desenvolvimento, Concluído ou Cancelado.");
        }
        projeto.setStatus(statusOficial);

        // Datas: dataCriacao e prazo
        LocalDate criacao = projeto.getDataCriacao() != null ? projeto.getDataCriacao() : LocalDate.now();
        projeto.setDataCriacao(criacao);

        if (projeto.getPrazo() != null && projeto.getPrazo().isBefore(criacao)) {
            throw new IllegalArgumentException("Prazo não pode ser menor que a data de criação.");
        }
    }

    public boolean adicionar(Projeto projeto) {
        validarProjeto(projeto, true, null);
        projetos.add(projeto);
        return true;
    }

    public List<Projeto> listar() {
        return projetos.stream().toList();
    }

    public Projeto buscaPorId(int id) {
        for (Projeto projeto : projetos) {
            if (projeto.getId() == id) {
                return projeto;
            }
        }
        return null;
    }

    public List<Projeto> buscarPorCategoria(String categoria) {
        if (categoria == null) {
            return List.of();
        }
        return projetos.stream()
                .filter(projeto -> projeto.getCategoria() != null
                        && projeto.getCategoria().equalsIgnoreCase(categoria.trim()))
                .toList();
    }

    public List<Projeto> buscarPorStatus(String status) {
        if (status == null) {
            return List.of();
        }
        return projetos.stream()
                .filter(projeto -> projeto.getStatus() != null
                        && projeto.getStatus().equalsIgnoreCase(status.trim()))
                .toList();
    }

    public boolean removerPorId(int id) {
        return projetos.removeIf(projeto -> projeto.getId() == id);
    }

    public boolean alterar(Projeto projetoAtualizado) {
        Projeto projeto = buscaPorId(projetoAtualizado.getId());
        if (projeto == null) {
            return false;
        }

        if (projetoAtualizado.getDataCriacao() == null) {
            projetoAtualizado.setDataCriacao(projeto.getDataCriacao());
        }

        if (projetoAtualizado.getUsuarioLogin() == null || projetoAtualizado.getUsuarioLogin().isBlank()) {
            projetoAtualizado.setUsuarioLogin(projeto.getUsuarioLogin());
        }

        validarProjeto(projetoAtualizado, true, projeto.getId());

        projeto.setNome(projetoAtualizado.getNome());
        projeto.setCategoria(projetoAtualizado.getCategoria());
        projeto.setDescricao(projetoAtualizado.getDescricao());
        projeto.setStatus(projetoAtualizado.getStatus());
        projeto.setDataCriacao(projetoAtualizado.getDataCriacao());
        projeto.setPrazo(projetoAtualizado.getPrazo());
        projeto.setUsuarioLogin(projetoAtualizado.getUsuarioLogin());

        return true;
    }

    public int gerarId() {
        int greatest = 0;
        for (Projeto projeto : projetos) {
            if (projeto.getId() >= greatest) {
                greatest = projeto.getId();
            }
        }
        return greatest + 1;
    }
}
