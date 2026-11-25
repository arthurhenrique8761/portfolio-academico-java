package br.com.arthurhenrique.model;

public class Usuario {

    private int id;
    private String nome;
    private String usuario;
    private String senhaHash;
    private String perfil;

    public Usuario() {
    }

    public Usuario(int id, String nome, String usuario, String senhaHash, String perfil) {
        this.id = id;
        this.nome = nome;
        this.usuario = usuario;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}
