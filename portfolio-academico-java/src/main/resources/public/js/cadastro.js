const API_CADASTRO_URL = "http://localhost:7070/api/auth/register";

const formCadastro = document.getElementById("formCadastro");
const campoNome = document.getElementById("nome");
const campoUsuario = document.getElementById("usuario");
const campoSenha = document.getElementById("senha");
const mensagemCadastro = document.getElementById("mensagemCadastro");
const btnCadastrar = document.getElementById("btnCadastrar");

function definirMensagemCadastro(texto, tipo = "erro") {
    if (!mensagemCadastro) return;
    mensagemCadastro.textContent = texto;
    mensagemCadastro.classList.remove("mensagem-erro", "mensagem-sucesso");
    if (texto) {
        mensagemCadastro.classList.add(tipo === "sucesso" ? "mensagem-sucesso" : "mensagem-erro");
    }
}

formCadastro.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    definirMensagemCadastro("");

    const nome = campoNome.value.trim();
    const usuario = campoUsuario.value.trim();
    const senha = campoSenha.value;

    if (!nome || !usuario || !senha) {
        definirMensagemCadastro("Preencha nome, usuário e senha.");
        return;
    }

    btnCadastrar.disabled = true;
    btnCadastrar.textContent = "Cadastrando...";

    try {
        const resposta = await fetch(API_CADASTRO_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ nome, usuario, senha })
        });

        const dados = await resposta.json().catch(() => ({}));

        if (!resposta.ok) {
            definirMensagemCadastro(dados.erro || "Não foi possível concluir o cadastro.");
            return;
        }

        definirMensagemCadastro("Usuário cadastrado com sucesso! Redirecionando para o login...", "sucesso");
        formCadastro.reset();

        setTimeout(() => {
            window.location.replace("/login.html");
        }, 900);
    } catch (erro) {
        console.error(erro);
        definirMensagemCadastro("Não foi possível conectar ao servidor.");
    } finally {
        btnCadastrar.disabled = false;
        btnCadastrar.textContent = "Cadastrar";
    }
});