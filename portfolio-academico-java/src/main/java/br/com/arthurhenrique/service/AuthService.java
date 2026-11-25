package br.com.arthurhenrique.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import br.com.arthurhenrique.dao.UsuarioCSV;
import br.com.arthurhenrique.log.LogManager;
import br.com.arthurhenrique.model.Usuario;

public class AuthService {

    private final UsuarioCSV usuarioCSV;

    public AuthService() {
        this(new UsuarioCSV());
    }

    public AuthService(UsuarioCSV usuarioCSV) {
        this.usuarioCSV = usuarioCSV;
    }

    public void inicializar() throws Exception {
        usuarioCSV.garantirArquivoPadrao();
    }

    public Optional autenticar(String login, String senha) throws Exception {
        if (login == null || login.isBlank() || senha == null || senha.isBlank()) {
            LogManager.access(login, "LOGIN_FALHA_CAMPOS_VAZIOS");
            return Optional.empty();
        }

        Optional<Usuario> encontrado = usuarioCSV.buscarPorUsuario(login);
        if (encontrado.isEmpty()) {
            LogManager.access(login, "LOGIN_FALHA_USUARIO_INEXISTENTE");
            return Optional.empty();
        }

        Usuario usuario = encontrado.get();
        String hashInformado = SenhaUtil.gerarHashSha256(senha);

        if (!hashInformado.equalsIgnoreCase(usuario.getSenhaHash())) {
            LogManager.access(usuario.getUsuario(), "LOGIN_FALHA_SENHA_INVALIDA");
            return Optional.empty();
        }

        LogManager.access(usuario.getUsuario(), "LOGIN_SUCESSO");

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("autenticado", true);
        resposta.put("id", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("usuario", usuario.getUsuario());
        resposta.put("perfil", usuario.getPerfil());
        return Optional.of(resposta);
    }

    public Map<String, Object> cadastrar(String nome, String login, String senha) throws Exception {
        String nomeLimpo = nome != null ? nome.trim() : "";
        String loginLimpo = login != null ? login.trim() : "";
        String senhaLimpa = senha != null ? senha : "";

        if (nomeLimpo.isEmpty() || loginLimpo.isEmpty() || senhaLimpa.isEmpty()) {
            throw new IllegalArgumentException("Preencha nome, usuário e senha.");
        }

        if (loginLimpo.contains(" ")) {
            throw new IllegalArgumentException("O nome de usuário não pode conter espaços.");
        }

        if (senhaLimpa.length() < 4) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 4 caracteres.");
        }

        if (usuarioCSV.existeUsuario(loginLimpo)) {
            throw new IllegalArgumentException("Já existe um usuário com esse login.");
        }

        Usuario novo = new Usuario();
        novo.setId(usuarioCSV.gerarProximoId());
        novo.setNome(nomeLimpo);
        novo.setUsuario(loginLimpo);
        novo.setSenhaHash(SenhaUtil.gerarHashSha256(senhaLimpa));
        novo.setPerfil("USER");

        usuarioCSV.salvar(novo);
        LogManager.access(novo.getUsuario(), "CADASTRO_SUCESSO");

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", novo.getId());
        resposta.put("nome", novo.getNome());
        resposta.put("usuario", novo.getUsuario());
        resposta.put("perfil", novo.getPerfil());
        return resposta;
    }
}
