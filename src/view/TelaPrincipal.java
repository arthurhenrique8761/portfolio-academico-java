package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TelaPrincipal extends JFrame {

    private JLabel labelNome;
    private JTextField campoNome;

    private JLabel labelDescricao;
    private JTextField campoDescricao;

    private JLabel labelCategoria;
    private JTextField campoCategoria;

    private JTable tabela;
    private DefaultTableModel modelo;

    // botões
    private JButton botaoCadastrar;
    private JButton botaoMostrar;

    public TelaPrincipal() {
        // configurações da janela
        setTitle("Sistema de Projetos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // componentes da janela
        labelNome = new JLabel("Nome: ");
        campoNome = new JTextField(30);

        labelDescricao = new JLabel("Descrição: ");
        campoDescricao = new JTextField(30);

        labelCategoria = new JLabel("Categoria: ");
        campoCategoria = new JTextField(30);

        botaoMostrar = new JButton("Mostrar");
        botaoMostrar.addActionListener(e -> {
            String nome = campoNome.getText();
            String descricao = campoDescricao.getText();
            String categoria = campoCategoria.getText();

            System.out.printf("Projeto cadastrado\nNome: %s\nDescrição: %s\nCategoria: %s\n\n", nome, descricao,
                    categoria);
        });

        // tabela
        modelo = new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Descrição: ");
        modelo.addColumn("Status: ");
        tabela = new JTable(modelo);

        modelo.addRow(new Object[] {
                1,
                "Sistema academico",
                "Software",
                "Em desenvolvimento"
        });

        // botaoCadastrar = new JButton("Cadastrar");
        // botaoCadastrar.addActionListener(e -> {
        // String nome = campoNome.getText();
        // System.out.println("Projeto: " + nome);
        // });

        // criaçao do painel da janela e inserção de elementos nele (em ordem de
        // exibição)
        JPanel painel = new JPanel();

        painel.add(labelNome);
        painel.add(campoNome);

        painel.add(labelDescricao);
        painel.add(campoDescricao);

        painel.add(labelCategoria);
        painel.add(campoCategoria);

        painel.add(botaoMostrar);
        // painel.add(botaoCadastrar);

        // adição do painel à janela/frame
        add(painel);
    }

    public static void main(String[] args) {
        TelaPrincipal tela = new TelaPrincipal();
        tela.setVisible(true);
    }
}