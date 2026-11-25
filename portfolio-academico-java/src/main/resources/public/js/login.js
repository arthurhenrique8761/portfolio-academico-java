const API_LOGIN_URL = "http://localhost:7070/api/auth/login";
const CHAVE_SESSAO = "sistema_projetos_sessao";

const formLogin = document.getElementById("formLogin");
const campoUsuario = document.getElementById("usuario");
const campoSenha = document.getElementById("senha");
const mensagemLogin = document.getElementById("mensagemLogin");
const btnEntrar = document.getElementById("btnEntrar");

// Se já estiver autenticado na sessão atual, vai direto para o dashboard
try {
    const sessaoSalva = sessionStorage.getItem(CHAVE_SESSAO);
    if (sessaoSalva) {
        const parsed = JSON.parse(sessaoSalva);
        if (parsed && parsed.autenticado) {
            window.location.replace("/dashboard.html");
        }
    }
} catch (e) {
    sessionStorage.removeItem(CHAVE_SESSAO);
}

function definirMensagem(texto, tipo = "erro") {
    if (!mensagemLogin) return;
    mensagemLogin.textContent = texto;
    mensagemLogin.classList.remove("mensagem-erro", "mensagem-sucesso");
    if (texto) {
        mensagemLogin.classList.add(tipo === "sucesso" ? "mensagem-sucesso" : "mensagem-erro");
    }
}

formLogin.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    definirMensagem("");

    const usuario = campoUsuario.value.trim();
    const senha = campoSenha.value;

    if (!usuario || !senha) {
        definirMensagem("Preencha o usuário e a senha.");
        return;
    }

    btnEntrar.disabled = true;
    btnEntrar.textContent = "Entrando...";

    try {
        const resposta = await fetch(API_LOGIN_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ usuario, senha })
        });

        const dados = await resposta.json().catch(() => ({}));

        if (!resposta.ok || !dados.autenticado) {
            definirMensagem(dados.erro || "Usuário ou senha inválidos.");
            return;
        }

        sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(dados));
        definirMensagem("Login realizado! Redirecionando...", "sucesso");
        window.location.replace("/dashboard.html");
    } catch (erro) {
        console.error(erro);
        definirMensagem("Não foi possível conectar ao servidor de autenticação.");
    } finally {
        btnEntrar.disabled = false;
        btnEntrar.textContent = "Entrar";
    }
});