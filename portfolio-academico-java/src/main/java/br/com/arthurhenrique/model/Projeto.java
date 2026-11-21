package br.com.arthurhenrique.model;

import java.time.LocalDate;

public class Projeto {

    private int id;
    private String nome;
    private String descricao;
    private String categoria;
    private String status;
    private LocalDate dataCriacao;
    private LocalDate prazo;

    public Projeto() {
        this.dataCriacao = LocalDate.now();
    }

    public Projeto(int id, String nome, String descricao, String categoria, String status) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.status = status;
        this.dataCriacao = LocalDate.now();
    }

    public Projeto(int id, String nome, String descricao, String categoria, String status,
            LocalDate dataCriacao, LocalDate prazo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.status = status;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDate.now();
        this.prazo = prazo;
    }

    public void exibirDados() {
        System.out.println("ID: " + id);
        System.out.println("Nome: " + nome);
        System.out.println("Descrição: " + descricao);
        System.out.println("Categoria: " + categoria);
        System.out.println("Status: " + status);
        System.out.println("Data de Criação: " + dataCriacao);
        System.out.println("Prazo: " + prazo);
        System.out.println("-------------------");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %d%nNome: %s%nDescrição: %s%nCategoria: %s%nStatus: %s%nData de Criação: %s%nPrazo: %s",
                id, nome, descricao, categoria, status, dataCriacao, prazo
        );
    }
}
