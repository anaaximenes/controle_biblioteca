package com.poo.biblioteca.ui;

import com.poo.biblioteca.Obra;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Painel de gerenciamento de Obras (Títulos / Livros).
 */
public class ObraPanel extends JPanel {

    private final BibliotecaService service;
    private JTable tabela;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtAno;

    public ObraPanel(BibliotecaService service) {
        this.service = service;
        initComponents();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitulo = UIHelper.createTitleLabel("Catálogo de Obras");
        JLabel lblSub = new JLabel("Cadastro bibliográfico de livros, autores e publicações");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Divisão Tabela e Formulário
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Tabela
        String[] colunas = {"ID", "Título da Obra", "Autor", "Ano de Publicação"};
        tableModel = new DefaultTableModel(colunas, 0);
        tabela = UIHelper.createStyledTable(tableModel);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioDaTabela();
            }
        });

        JPanel pnlTabela = new JPanel(new BorderLayout(0, 8));
        pnlTabela.setOpaque(false);
        pnlTabela.add(UIHelper.wrapInScrollPane(tabela), BorderLayout.CENTER);

        JButton btnRecarregar = UIHelper.createButton("Atualizar Lista", BTN_PRIMARY);
        btnRecarregar.addActionListener(e -> carregarTabela());
        JPanel pnlAcoesTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlAcoesTab.setOpaque(false);
        pnlAcoesTab.add(btnRecarregar);
        pnlTabela.add(pnlAcoesTab, BorderLayout.SOUTH);

        splitPane.setLeftComponent(pnlTabela);

        // Formulário (Card)
        JPanel pnlCard = UIHelper.createCardPanel();
        pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));

        JLabel lblFormTitulo = new JLabel("Dados da Obra");
        lblFormTitulo.setFont(FONT_SUBTITLE);
        lblFormTitulo.setForeground(TEXT_PRIMARY);

        txtId = UIHelper.createTextField("");
        txtId.setEditable(false);
        txtId.setBackground(BG_MAIN);

        txtTitulo = UIHelper.createTextField("Ex: Clean Code");
        txtAutor = UIHelper.createTextField("Ex: Robert C. Martin");
        txtAno = UIHelper.createTextField("Ex: 2008");

        pnlCard.add(lblFormTitulo);
        pnlCard.add(Box.createVerticalStrut(14));
        pnlCard.add(UIHelper.createFormRow("Código (ID):", txtId));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Título:", txtTitulo));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Autor:", txtAutor));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Ano de Publicação:", txtAno));
        pnlCard.add(Box.createVerticalStrut(20));

        // Botões
        JPanel pnlBotoes = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlBotoes.setOpaque(false);
        pnlBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));

        JButton btnNovo = UIHelper.createButton("Novo", BTN_PRIMARY);
        btnNovo.addActionListener(e -> limparFormulario());

        JButton btnSalvar = UIHelper.createSuccessButton("Salvar");
        btnSalvar.addActionListener(e -> salvarObra());

        JButton btnExcluir = UIHelper.createDangerButton("Excluir");
        btnExcluir.addActionListener(e -> excluirObra());

        pnlBotoes.add(btnNovo);
        pnlBotoes.add(btnSalvar);
        pnlBotoes.add(btnExcluir);

        pnlCard.add(pnlBotoes);
        pnlCard.add(Box.createVerticalGlue());

        splitPane.setRightComponent(pnlCard);
        add(splitPane, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        carregarTabela();
    }

    private void carregarTabela() {
        tableModel.setRowCount(0);
        try {
            List<Obra> lista = service.listarObras();
            for (Obra o : lista) {
                tableModel.addRow(new Object[]{
                        o.getId(),
                        o.getTitulo(),
                        o.getAutor(),
                        o.getAnoPublicacao()
                });
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar catálogo de obras: " + ex.getMessage());
        }
    }

    private void preencherFormularioDaTabela() {
        int row = tabela.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Obra o = service.getObraDAO().carregar(id);
            if (o != null && o.getId() > 0) {
                txtId.setText(String.valueOf(o.getId()));
                txtTitulo.setText(o.getTitulo());
                txtAutor.setText(o.getAutor());
                txtAno.setText(String.valueOf(o.getAnoPublicacao()));
            }
        }
    }

    private void limparFormulario() {
        tabela.clearSelection();
        txtId.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtAno.setText("");
    }

    private void salvarObra() {
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        String anoStr = txtAno.getText().trim();

        if (titulo.isEmpty() || autor.isEmpty()) {
            UIHelper.showWarning(this, "Título e Autor são campos obrigatórios.");
            return;
        }

        int ano = 0;
        try {
            ano = Integer.parseInt(anoStr);
            if (ano < 0 || ano > 2100) {
                UIHelper.showWarning(this, "Por favor, informe um ano de publicação válido.");
                return;
            }
        } catch (NumberFormatException e) {
            UIHelper.showWarning(this, "O ano de publicação deve ser um número inteiro.");
            return;
        }

        try {
            Obra obra = new Obra();
            obra.setTitulo(titulo);
            obra.setAutor(autor);
            obra.setAnoPublicacao(ano);

            boolean isEdicao = !txtId.getText().isBlank();
            if (isEdicao) {
                obra.setId(Integer.parseInt(txtId.getText()));
                int linhas = service.getObraDAO().alterar(obra);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Obra \"" + obra.getTitulo() + "\" atualizada com sucesso!");
                } else {
                    UIHelper.showWarning(this, "Aviso: a atualização não afetou nenhum registro.");
                }
            } else {
                int linhas = service.getObraDAO().inserir(obra);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Nova obra \"" + obra.getTitulo() + "\" cadastrada com sucesso!");
                } else {
                    UIHelper.showError(this, "Não foi possível inserir a obra no banco de dados.");
                }
            }

            carregarTabela();
            limparFormulario();

        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao salvar obra: " + ex.getMessage());
        }
    }

    private void excluirObra() {
        if (txtId.getText().isBlank()) {
            UIHelper.showWarning(this, "Selecione uma obra na tabela para excluir.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        String titulo = txtTitulo.getText();

        boolean confirmou = UIHelper.showConfirm(this, "Tem certeza que deseja excluir a obra:\n\""
                + titulo + "\" (ID: " + id + ")?\n\nAtenção: verifique se existem cópias vinculadas antes de excluir.");
        if (!confirmou) return;

        try {
            Obra obra = new Obra();
            obra.setId(id);
            int linhas = service.getObraDAO().remover(obra);
            if (linhas > 0) {
                UIHelper.showSuccess(this, "Obra excluída com sucesso!");
                carregarTabela();
                limparFormulario();
            } else {
                UIHelper.showError(this, "Não foi possível remover a obra do banco.");
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao excluir obra: " + ex.getMessage());
        }
    }
}
