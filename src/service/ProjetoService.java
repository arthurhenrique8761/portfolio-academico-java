package service;

import java.util.List;
import java.util.ArrayList;

import model.Projeto;

public class ProjetoService {
    private List<Projeto> projetos;

    public ProjetoService() {
        projetos = new ArrayList<>();
    }

    public void adicionar(Projeto projeto) {
        projetos.add(projeto);
    }

    public void listar() { // TO DO: voltar lista de projeto
        projetos.stream()
        .forEach(n -> System.out.println(n));
    }

    public void totalDeProjetos() {
        System.out.println("Total de projetos: " + projetos.size());
    }

    public Projeto buscarPorId(int id) {
        for (Projeto projeto : projetos) {
            if (projeto.getId() == id)
                return projeto;
        }
        return null;
    }

    public List<Projeto> buscarPorCategoria(String categoria) {
        List<Projeto> resultado = new ArrayList<>();
        for (Projeto projeto : projetos) {
            if (projeto.getCategoria().equalsIgnoreCase(categoria)) {
                resultado.add(projeto);
            }
        }
        return resultado;
    }

}
