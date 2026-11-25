(function () {
    const CHAVE_SESSAO = "sistema_projetos_sessao";

    function obterSessao() {
        try {
            const bruta = sessionStorage.getItem(CHAVE_SESSAO);
            if (!bruta) return null;
            const dados = JSON.parse(bruta);
            if (!dados || !dados.autenticado) return null;
            return dados;
        } catch (e) {
            return null;
        }
    }

    function salvarSessao(dadosUsuario) {
        sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(dadosUsuario));
    }

    function limparSessao() {
        sessionStorage.removeItem(CHAVE_SESSAO);
    }

    function exigirAutenticacao() {
        const sessao = obterSessao();
        if (!sessao) {
            window.location.replace("/login.html");
            return null;
        }
        return sessao;
    }

    function instalarInterceptorFetch() {
        const fetchOriginal = window.fetch.bind(window);
        window.fetch = function (input, init) {
            const url = typeof input === "string" ? input : (input && input.url ? input.url : "");
            if (url.includes("/api/projetos")) {
                const sessao = obterSessao();
                const opcoes = Object.assign({}, init);
                const headers = new Headers(opcoes.headers || (input instanceof Request ? input.headers : undefined));
                if (sessao && sessao.usuario) {
                    headers.set("X-Usuario-Logado", sessao.usuario);
                }
                opcoes.headers = headers;
                return fetchOriginal(input, opcoes);
            }
            return fetchOriginal(input, init);
        };
    }

    function configurarBarraUsuario(sessao) {
        if (!sessao) return;

        const elUsuario = document.querySelector("[data-usuario-logado]");
        if (elUsuario) {
            const nomeExibicao = sessao.nome || sessao.usuario || "Usuário";
            const perfilExibicao = sessao.perfil ? ` (${sessao.perfil})` : "";
            elUsuario.textContent = `${nomeExibicao}${perfilExibicao}`;
        }

        const btnSair = document.querySelector("[data-btn-sair]");
        if (btnSair) {
            btnSair.addEventListener("click", () => {
                limparSessao();
                window.location.replace("/login.html");
            });
        }
    }

    window.AuthClient = {
        CHAVE_SESSAO,
        obterSessao,
        salvarSessao,
        limparSessao,
        exigirAutenticacao
    };

    const paginaAtual = window.location.pathname.toLowerCase();
    const ehPaginaPublica = paginaAtual.endsWith("/login.html")
        || paginaAtual.endsWith("/cadastro.html")
        || paginaAtual === "/";

    if (!ehPaginaPublica) {
        const sessaoAtiva = exigirAutenticacao();
        if (sessaoAtiva) {
            instalarInterceptorFetch();
            document.addEventListener("DOMContentLoaded", () => {
                configurarBarraUsuario(sessaoAtiva);
            });
        }
    }
})();