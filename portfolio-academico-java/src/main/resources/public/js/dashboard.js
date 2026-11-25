const API_URL = "http://localhost:7070/api/projetos";

const mensagemDashboard = document.getElementById("mensagemDashboard");
const kpiTotal = document.getElementById("kpiTotal");
const kpiConcluidos = document.getElementById("kpiConcluidos");
const kpiAndamento = document.getElementById("kpiAndamento");
const kpiCancelados = document.getElementById("kpiCancelados");
const legendaPrazos = document.getElementById("legendaPrazos");
const listaRecentes = document.getElementById("listaRecentes");

let graficoStatus = null;
let graficoCategorias = null;
let graficoEvolucao = null;
let graficoPrazos = null;

function formatarDataBR(dataIso, fallback = "Não definido") {
    if (!dataIso) {
        return fallback;
    }

    const parteData = String(dataIso).split("T")[0];
    const partes = parteData.split("-");
    if (partes.length !== 3) {
        return dataIso;
    }

    const [ano, mes, dia] = partes;
    return `${dia}/${mes}/${ano}`;
}

function normalizarStatus(status) {
    return String(status || "").trim().toLowerCase();
}

function atualizarKpis(projetos) {
    const total = projetos.length;
    const concluidos = projetos.filter(p => normalizarStatus(p.status) === "concluído").length;
    const ativos = projetos.filter(p => {
        const s = normalizarStatus(p.status);
        return s === "em desenvolvimento" || s === "em andamento" || s === "planejado";
    }).length;
    const cancelados = projetos.filter(p => normalizarStatus(p.status) === "cancelado").length;

    if (kpiTotal) kpiTotal.textContent = total;
    if (kpiConcluidos) kpiConcluidos.textContent = concluidos;
    if (kpiAndamento) kpiAndamento.textContent = ativos;
    if (kpiCancelados) kpiCancelados.textContent = cancelados;
}

function destruirGrafico(instancia) {
    if (instancia && typeof instancia.destroy === "function") {
        instancia.destroy();
    }
}

function renderizarGraficoStatus(projetos) {
    const canvas = document.getElementById("graficoStatusPizza");
    if (!canvas || typeof Chart === "undefined") return;

    destruirGrafico(graficoStatus);

    const rotulos = ["Concluído", "Em Desenvolvimento", "Planejado", "Cancelado"];
    const contagens = [
        projetos.filter(p => normalizarStatus(p.status) === "concluído").length,
        projetos.filter(p => normalizarStatus(p.status) === "em desenvolvimento").length,
        projetos.filter(p => normalizarStatus(p.status) === "planejado").length,
        projetos.filter(p => normalizarStatus(p.status) === "cancelado").length
    ];

    graficoStatus = new Chart(canvas, {
        type: "pie",
        data: {
            labels: rotulos,
            datasets: [{
                data: contagens,
                backgroundColor: ["#2e7d32", "#ed6c02", "#1976d2", "#c62828"]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "bottom"
                }
            }
        }
    });
}

function renderizarGraficoCategorias(projetos) {
    const canvas = document.getElementById("graficoCategoriasColunas");
    if (!canvas || typeof Chart === "undefined") return;

    destruirGrafico(graficoCategorias);

    const categoriasPadrao = ["Web", "Software", "Mobile", "Desktop"];
    const mapa = new Map();
    categoriasPadrao.forEach(cat => mapa.set(cat, 0));

    projetos.forEach(p => {
        const cat = String(p.categoria || "Outros").trim();
        mapa.set(cat, (mapa.get(cat) || 0) + 1);
    });

    const labels = Array.from(mapa.keys());
    const valores = Array.from(mapa.values());

    graficoCategorias = new Chart(canvas, {
        type: "bar",
        data: {
            labels,
            datasets: [{
                label: "Projetos",
                data: valores,
                backgroundColor: ["#1976d2", "#263238", "#00796b", "#7b1fa2"]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        precision: 0
                    }
                }
            }
        }
    });
}

function renderizarGraficoEvolucao(projetos) {
    const canvas = document.getElementById("graficoEvolucaoLinha");
    if (!canvas || typeof Chart === "undefined") return;

    destruirGrafico(graficoEvolucao);

    const meses = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"];
    const contagemMeses = new Array(12).fill(0);

    projetos.forEach(p => {
        const baseData = p.dataCriacao || p.prazo;
        if (!baseData) return;

        const parteData = String(baseData).split("T")[0];
        const partes = parteData.split("-");
        if (partes.length >= 2) {
            const indiceMes = Number(partes[1]) - 1;
            if (indiceMes >= 0 && indiceMes < 12) {
                contagemMeses[indiceMes] += 1;
            }
        }
    });

    graficoEvolucao = new Chart(canvas, {
        type: "line",
        data: {
            labels: meses,
            datasets: [{
                label: "Projetos Criados",
                data: contagemMeses,
                borderColor: "#1976d2",
                backgroundColor: "rgba(25, 118, 210, 0.14)",
                fill: true,
                tension: 0.35
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "bottom"
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        precision: 0
                    }
                }
            }
        }
    });
}

function renderizarGraficoPrazos(projetos) {
    const canvas = document.getElementById("graficoPrazosDonut");
    if (!canvas || typeof Chart === "undefined") return;

    destruirGrafico(graficoPrazos);

    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);

    let noPrazo = 0;
    let atrasados = 0;

    projetos.forEach(p => {
        if (!p.prazo) {
            noPrazo += 1;
            return;
        }

        const parteData = String(p.prazo).split("T")[0];
        const partes = parteData.split("-");
        if (partes.length !== 3) {
            noPrazo += 1;
            return;
        }

        const dataPrazo = new Date(Number(partes[0]), Number(partes[1]) - 1, Number(partes[2]));
        const concluido = normalizarStatus(p.status) === "concluído";

        if (!concluido && dataPrazo < hoje) {
            atrasados += 1;
        } else {
            noPrazo += 1;
        }
    });

    const totalAvaliado = noPrazo + atrasados;
    const percNoPrazo = totalAvaliado > 0 ? Math.round((noPrazo / totalAvaliado) * 100) : 0;
    const percAtrasados = totalAvaliado > 0 ? Math.round((atrasados / totalAvaliado) * 100) : 0;

    graficoPrazos = new Chart(canvas, {
        type: "doughnut",
        data: {
            labels: ["No prazo", "Atrasados"],
            datasets: [{
                data: [noPrazo, atrasados],
                backgroundColor: ["#2e7d32", "#c62828"]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "bottom"
                }
            }
        }
    });

    if (legendaPrazos) {
        legendaPrazos.textContent = `No prazo: ${noPrazo} (${percNoPrazo}%) | Atrasados: ${atrasados} (${percAtrasados}%)`;
    }
}

function renderizarRecentes(projetos) {
    if (!listaRecentes) return;
    listaRecentes.replaceChildren();

    if (!Array.isArray(projetos) || projetos.length === 0) {
        const vazio = document.createElement("p");
        vazio.textContent = "Nenhum projeto cadastrado.";
        listaRecentes.appendChild(vazio);
        return;
    }

    const ordenados = [...projetos].sort((a, b) => Number(b.id || 0) - Number(a.id || 0));

    ordenados.forEach(projeto => {
        const item = document.createElement("article");
        item.classList.add("projeto");

        const titulo = document.createElement("h3");
        titulo.textContent = `#${projeto.id} - ${projeto.nome}`;

        const meta = document.createElement("p");
        meta.textContent = `Categoria: ${projeto.categoria} | Status: ${projeto.status} | Prazo: ${formatarDataBR(projeto.prazo, "Não definido")}`;

        item.appendChild(titulo);
        item.appendChild(meta);
        listaRecentes.appendChild(item);
    });
}

async function carregarDashboard() {
    try {
        if (mensagemDashboard) {
            mensagemDashboard.textContent = "";
        }

        const resposta = await fetch(API_URL);
        if (!resposta.ok) {
            throw new Error("Erro ao carregar dados do dashboard.");
        }

        const projetos = await resposta.json();
        const lista = Array.isArray(projetos) ? projetos : [];

        atualizarKpis(lista);
        renderizarGraficoStatus(lista);
        renderizarGraficoCategorias(lista);
        renderizarGraficoEvolucao(lista);
        renderizarGraficoPrazos(lista);
        renderizarRecentes(lista);
    } catch (erro) {
        if (mensagemDashboard) {
            mensagemDashboard.textContent = "Não foi possível carregar os indicadores do dashboard.";
        }
        console.error(erro);
    }
}

carregarDashboard();