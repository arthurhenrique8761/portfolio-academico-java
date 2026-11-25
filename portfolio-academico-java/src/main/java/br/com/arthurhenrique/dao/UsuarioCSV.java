package br.com.arthurhenrique.dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.arthurhenrique.model.Usuario;
import br.com.arthurhenrique.service.SenhaUtil;

public class UsuarioCSV {

    private static final String CABECALHO = "id;nome;usuario;senhaHash;perfil";
    private final Path caminho;

    public UsuarioCSV() {
        this(Path.of("portfolio-academico-java/data/usuarios.csv"));
    }

    public UsuarioCSV(Path caminho) {
        this.caminho = caminho;
    }

    public void garantirArquivoPadrao() throws Exception {
        if (caminho.getParent() != null) {
            Files.createDirectories(caminho.getParent());
        }

        if (!Files.exists(caminho)) {
            String hashAdmin = SenhaUtil.gerarHashSha256("123456");
            List<String> linhas = new ArrayList<>();
            linhas.add(CABECALHO);
            linhas.add("1;Administrador;admin;" + hashAdmin + ";ADMIN");
            Files.write(caminho, linhas, StandardCharsets.UTF_8);
        }
    }

    public synchronized List<Usuario> listar() throws Exception {
        garantirArquivoPadrao();
        List<Usuario> usuarios = new ArrayList<>();

        List<String> linhas = Files.readAllLines(caminho, StandardCharsets.UTF_8);
        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha == null || linha.isBlank()) {
                continue;
            }

            String[] dados = linha.split(";", -1);
            if (dados.length < 4) {
                continue;
            }

            Usuario usuario = new Usuario();
            usuario.setId(parseIntSeguro(dados[0]));
            usuario.setNome(dados[1].trim());
            usuario.setUsuario(dados[2].trim());
            usuario.setSenhaHash(dados[3].trim());
            usuario.setPerfil(dados.length >= 5 && !dados[4].isBlank() ? dados[4].trim() : "USER");
            usuarios.add(usuario);
        }

        return usuarios;
    }

    public Optional<Usuario> buscarPorUsuario(String login) throws Exception {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }

        String alvo = login.trim().toLowerCase();
        return listar().stream()
                .filter(u -> u.getUsuario() != null && u.getUsuario().trim().toLowerCase().equals(alvo))
                .findFirst();
    }

    public boolean existeUsuario(String login) throws Exception {
        return buscarPorUsuario(login).isPresent();
    }

    public int gerarProximoId() throws Exception {
        int maiorId = 0;
        for (Usuario u : listar()) {
            if (u.getId() > maiorId) {
                maiorId = u.getId();
            }
        }
        return maiorId + 1;
    }

    public synchronized void salvar(Usuario novoUsuario) throws Exception {
        garantirArquivoPadrao();

        String linha = novoUsuario.getId() + ";"
                + sanitizarCampo(novoUsuario.getNome()) + ";"
                + sanitizarCampo(novoUsuario.getUsuario()) + ";"
                + sanitizarCampo(novoUsuario.getSenhaHash()) + ";"
                + sanitizarCampo(novoUsuario.getPerfil());

        Files.writeString(
                caminho,
                linha + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    private String sanitizarCampo(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(";", ",").replace("\r", " ").replace("\n", " ").trim();
    }

    private int parseIntSeguro(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
