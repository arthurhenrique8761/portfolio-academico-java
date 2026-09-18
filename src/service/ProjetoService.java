package service;

import java.util.List;
import java.util.ArrayList;

import model.Projeto;

// regras de negócio
public class ProjetoService {
    private List<Projeto> projetos;

    public ProjetoService() {
        projetos = new ArrayList<>();
    }

    public boolean adicionar(Projeto projeto) {
        Projeto existente = buscaPorId(projeto.getId());
        if (projeto.getNome().isBlank()) {
            return false;
        }
        if (existente != null) {
            System.out.println("Erro: Projeto já existente");
            return false;
        }
        else {
            projetos.add(projeto);
            System.out.println("Projeto " + projeto.getId() + " adicionado com sucesso");
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
                projetos.remove(id);
                System.out.println("Projeto " + projeto.getId() + " removido com sucesso");
                return true;
            }
        }
        System.out.println("Projeto não encontrado");
        return false;
    }
}
