package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import model.Projeto;
import service.ProjetoService;

public class TelaPrincipal extends JFrame {

    private ProjetoService service;

    // campos de texto
    private JTextField campoNome;
    private JTextField campoDescricao;

    // tabela de opções
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    // tabela
    private DefaultTableModel modelo;
    private JTable tabela;

    // botões
    private JButton botaoCadastrar;
    private JButton botaoLimpar;

    public TelaPrincipal() throws Exception {
        // configurações da janela
        setTitle("Sistema de Projetos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // chamada dos métodos criam os elementos da janela (modularização de tarefas)

        service = new ProjetoService();

        criarComponentes();
        criarEventos();

        service.carregar();
        carregarTabela();
    }

    public void criarComponentes() {

        // definição e instanciação dos elementos da janela
        campoNome = new JTextField(30);

        campoDescricao = new JTextField(30);

        comboCategoria = new JComboBox<>();
        comboCategoria.addItem("Web");
        comboCategoria.addItem("Software");
        comboCategoria.addItem("Mobile");
        comboCategoria.addItem("Outro");

        comboStatus = new JComboBox<>();
        comboStatus.addItem("Planejado");
        comboStatus.addItem("Em desenvolvimento");
        comboStatus.addItem("Concluído");

        botaoCadastrar = new JButton("Cadastrar");
        botaoLimpar = new JButton("Limpar");

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");
        tabela = new JTable(modelo);

        // instanciação da janela e inserção dos componentes (em ordem)
        JPanel painelFormulario = new JPanel();

        painelFormulario.add(new JLabel("Nome: "));
        painelFormulario.add(campoNome);

        painelFormulario.add(new JLabel("Descrição: "));
        painelFormulario.add(campoDescricao);

        painelFormulario.add(new JLabel("Categoria: "));
        painelFormulario.add(comboCategoria);

        painelFormulario.add(new JLabel("Status: "));
        painelFormulario.add(comboStatus);

        painelFormulario.add(botaoCadastrar);
        painelFormulario.add(botaoLimpar);

        setLayout(new BoxLayout(
                getContentPane(),
                BoxLayout.Y_AXIS));

        add(painelFormulario);
        add(new JScrollPane(tabela));
    }

    public void criarEventos() {
        botaoCadastrar.addActionListener(e -> {
            try {
                cadastrar();
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Erro: " + e1.getMessage());
            }
        });

        botaoLimpar.addActionListener(e -> limparFormulário());
    }

    public void limparFormulário() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocus();
    }

    public void cadastrar() throws Exception {
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do projeto");
            return;
        }

        Projeto projeto = new Projeto(nome, descricao, categoria, status);

        service.adicionar(projeto);

        service.salvar();

        JOptionPane.showMessageDialog(this, "Projeto Cadastrado: " + nome);

        carregarTabela();
        limparFormulário();
    }

    public void carregarTabela() {
        modelo.setRowCount(0);

        for (Projeto projeto : service.listar()) {
            modelo.addRow(new Object[] {
                    projeto.getId(),
                    projeto.getNome(),
                    projeto.getCategoria(),
                    projeto.getStatus()
            });
        }

    }

    public static void main(String[] args) throws Exception {
        // TO DO
        // Tabela não carregando dados atuais ao iniciar
        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);
    }
}
