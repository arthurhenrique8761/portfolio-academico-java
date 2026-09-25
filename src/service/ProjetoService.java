package service;

import java.util.List;
import java.util.ArrayList;

import model.Projeto;
import dao.*;

// regras de negócio
public class ProjetoService {
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

    public boolean adicionar(Projeto projeto) {
        Projeto existente = buscaPorId(projeto.getId());
        if (projeto.getNome().isBlank()) {
            return false;
        }
        if (existente != null) {
            System.out.println("Erro: Projeto já existente");
            return false;
        } else {
            projetos.add(projeto);
            return true;
        }
    }

    public List<Projeto> listar() { // TO DO: retornar uma lista
        List<Projeto> resultado = projetos.stream().toList();
        return resultado;
    }

    // buscas
    public Projeto buscaPorId(int id) {
        for (Projeto projeto : projetos) {
            if (projeto.getId() == id)
                return projeto;
        }
        return null;
    }

    public List<Projeto> buscarPorCategoria(String categoria) {
        List<Projeto> resultado = projetos.stream()
                .filter(projeto -> projeto.getCategoria().equalsIgnoreCase(categoria))
                .toList();
        return resultado;
    }

    public List<Projeto> buscarPorStatus(String status) {
        List<Projeto> resultado = projetos.stream()
                .filter(projeto -> projeto.getStatus().equalsIgnoreCase(status))
                .toList();
        return resultado;
    }

    // remoções
    public boolean removerPorId(int id) { // não da pra fazer isso um void?
        for (Projeto projeto : projetos) {
            if (projeto.getId() == id) {
                projetos.remove(id - 1);
                return true;
            }
        }
        return false;
    }

    // alterações
    public boolean alterar(Projeto projetoAtualizado) {
        Projeto projeto = buscaPorId(projetoAtualizado.getId());

        // se o projeto não existe
        if (projeto == null)
            return false;

        projeto.setNome(projetoAtualizado.getNome());
        projeto.setCategoria(projetoAtualizado.getCategoria());
        projeto.setDescricao(projetoAtualizado.getDescricao());
        projeto.setStatus(projetoAtualizado.getStatus());

        return true;
    }
}
