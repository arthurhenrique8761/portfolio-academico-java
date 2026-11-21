package br.com.arthurhenrique.view;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import br.com.arthurhenrique.api.Api;
import br.com.arthurhenrique.client.ApiClient;
import br.com.arthurhenrique.log.LogManager;
import br.com.arthurhenrique.model.Projeto;
import br.com.arthurhenrique.service.ProjetoService;

public class TelaPrincipal extends JFrame {

    private ProjetoService service;
    private ApiClient apiClient;

    private JTextField campoNome;
    private JTextField campoDescricao;
    private JTextField campoPrazo;

    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    private DefaultTableModel modelo;
    private JTable tabela;

    private JButton botaoCadastrar;
    private JButton botaoLimpar;
    private JButton botaoExcluir;
    private JButton botaoAlterar;

    public TelaPrincipal() throws Exception {
        setTitle("Sistema de Projetos");
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        service = new ProjetoService();
        apiClient = new ApiClient();

        criarComponentes();
        criarEventos();

        service.carregar();
        carregarTabela();
    }

    public void criarComponentes() {
        campoNome = new JTextField(22);
        campoDescricao = new JTextField(26);
        campoPrazo = new JTextField(10);
        campoPrazo.setToolTipText("AAAA-MM-DD ou DD/MM/AAAA");

        comboCategoria = new JComboBox<>();
        comboCategoria.addItem("Software");
        comboCategoria.addItem("Mobile");
        comboCategoria.addItem("Web");
        comboCategoria.addItem("Desktop");

        comboStatus = new JComboBox<>();
        comboStatus.addItem("Planejado");
        comboStatus.addItem("Em Desenvolvimento");
        comboStatus.addItem("Concluído");
        comboStatus.addItem("Cancelado");

        botaoCadastrar = new JButton("Cadastrar");
        botaoLimpar = new JButton("Limpar");
        botaoExcluir = new JButton("Excluir");
        botaoAlterar = new JButton("Alterar");

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");
        modelo.addColumn("Prazo");
        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel();

        painelFormulario.add(new JLabel("Nome: "));
        painelFormulario.add(campoNome);

        painelFormulario.add(new JLabel("Descrição: "));
        painelFormulario.add(campoDescricao);

        painelFormulario.add(new JLabel("Categoria: "));
        painelFormulario.add(comboCategoria);

        painelFormulario.add(new JLabel("Status: "));
        painelFormulario.add(comboStatus);

        painelFormulario.add(new JLabel("Prazo: "));
        painelFormulario.add(campoPrazo);

        painelFormulario.add(botaoCadastrar);
        painelFormulario.add(botaoLimpar);
        painelFormulario.add(botaoExcluir);
        painelFormulario.add(botaoAlterar);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        add(painelFormulario);
        add(new JScrollPane(tabela));
    }

    public void criarEventos() {
        botaoCadastrar.addActionListener(e -> cadastrar());
        botaoLimpar.addActionListener(e -> limparFormulario());
        botaoExcluir.addActionListener(e -> excluirProjeto());
        botaoAlterar.addActionListener(e -> alterarProjeto());
    }

    public void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        campoPrazo.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocus();
    }

    private Projeto montarEValidarProjeto(int id) {
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();
        LocalDate dataCriacao = LocalDate.now();
        LocalDate prazo = ProjetoService.parseDataFlexivel(campoPrazo.getText());

        Projeto projeto = new Projeto(id, nome, descricao, categoria, status, dataCriacao, prazo);
        service.validarProjeto(projeto, false, null);
        return projeto;
    }

    public void cadastrar() {
        try {
            service.carregar();
            int id = service.gerarId();
            Projeto projeto = montarEValidarProjeto(id);

            apiClient.cadastrarProjeto(projeto);
            JOptionPane.showMessageDialog(this, "Projeto Cadastrado: " + projeto.getNome());

            limparFormulario();
            carregarTabela();
        } catch (Exception e) {
            LogManager.error("DESKTOP_CADASTRO", e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void excluirProjeto() {
        try {
            int linhaSelecionada = tabela.getSelectedRow();
            if (linhaSelecionada == -1) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int id = Integer.parseInt(modelo.getValueAt(linhaSelecionada, 0).toString());

            int option = JOptionPane.showConfirmDialog(this,
                    "Um projeto será deletado.\nEstá certo disso?",
                    "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                apiClient.excluirProjeto(id);
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso", "Sucesso",
                        JOptionPane.PLAIN_MESSAGE);
                carregarTabela();
            }
        } catch (Exception e) {
            LogManager.error("DESKTOP_EXCLUSAO", e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void alterarProjeto() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(modelo.getValueAt(linhaSelecionada, 0).toString());
            Projeto projetoAtualizado = montarEValidarProjeto(id);

            int option = JOptionPane.showConfirmDialog(this,
                    "Um projeto será alterado.\nEstá certo disso?",
                    "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION) {
                apiClient.alterarProjeto(id, projetoAtualizado);
                carregarTabela();
                JOptionPane.showMessageDialog(this, "Projeto alterado com sucesso", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LogManager.error("DESKTOP_ALTERACAO", e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void carregarTabela() {
        new SwingWorker<List<Projeto>, Void>() {
            @Override
            protected List<Projeto> doInBackground() throws Exception {
                return apiClient.listarProjetos();
            }

            @Override
            protected void done() {
                try {
                    atualizarTabela(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaPrincipal.this, "Erro ao carregar: " + e.getMessage(), "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public void atualizarTabela(List<Projeto> projetos) {
        modelo.setRowCount(0);

        for (Projeto projeto : projetos) {
            modelo.addRow(new Object[]{
                projeto.getId(),
                projeto.getNome(),
                projeto.getCategoria(),
                projeto.getStatus(),
                projeto.getPrazo() != null ? projeto.getPrazo().toString() : "-"
            });
        }
    }

    public static void main(String[] args) throws Exception {
        Api.iniciar(System.getProperty("user.name"));
        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                tela.carregarTabela();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(task, 5, 5, TimeUnit.SECONDS);
    }
}
