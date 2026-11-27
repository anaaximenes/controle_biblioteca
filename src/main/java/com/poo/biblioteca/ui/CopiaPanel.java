package com.poo.biblioteca.ui;

import com.poo.biblioteca.Copia;
import com.poo.biblioteca.Obra;
import com.poo.biblioteca.StatusCopia;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Painel de gerenciamento de Cópias físicas de livros.
 * Suporta estados do enum StatusCopia com feedback visual colorido.
 */
public class CopiaPanel extends JPanel {

    private final BibliotecaService service;
    private JTable tabela;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtCodigoBarras;
    private JComboBox<StatusCopia> cmbStatus;
    private JComboBox<ObraItem> cmbObra;

    // Item auxiliar para o JComboBox de Obras
    private static class ObraItem {
        final int id;
        final String titulo;

        ObraItem(int id, String titulo) {
            this.id = id;
            this.titulo = titulo;
        }

        @Override
        public String toString() {
            return "[" + id + "] " + titulo;
        }
    }

    public CopiaPanel(BibliotecaService service) {
        this.service = service;
        initComponents();
        carregarComboObras();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitulo = UIHelper.createTitleLabel("Cópias");
        JLabel lblSub = new JLabel("Controle de exemplares, código de barras e disponibilidade");
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
        String[] colunas = {"ID", "Código de Barras", "Obra Associada", "Status Atual", "Disponível?"};
        tableModel = new DefaultTableModel(colunas, 0);
        tabela = UIHelper.createStyledTable(tableModel);

        // Renderizador colorido para a coluna de Status
        tabela.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(FONT_BODY_BOLD);
                if (value != null) {
                    String st = value.toString();
                    if (st.equals(StatusCopia.DISPONIVEL.name())) {
                        c.setForeground(STATUS_DISPONIVEL);
                        c.setText("● DISPONÍVEL");
                    } else if (st.equals(StatusCopia.EMPRESTADA.name())) {
                        c.setForeground(STATUS_EMPRESTADA);
                        c.setText("EMPRESTADA");
                    } else if (st.equals(StatusCopia.RESERVADA.name())) {
                        c.setForeground(STATUS_RESERVADA);
                        c.setText("RESERVADA");
                    } else {
                        c.setForeground(STATUS_MANUTENCAO);
                        c.setText("MANUTENÇÃO");
                    }
                }
                return c;
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                preencherFormularioDaTabela();
            }
        });

        JPanel pnlTabela = new JPanel(new BorderLayout(0, 8));
        pnlTabela.setOpaque(false);
        pnlTabela.add(UIHelper.wrapInScrollPane(tabela), BorderLayout.CENTER);

        JButton btnRecarregar = UIHelper.createButton("Atualizar Lista", BTN_PRIMARY);
        btnRecarregar.addActionListener(e -> {
            carregarComboObras();
            carregarTabela();
        });
        JPanel pnlAcoesTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlAcoesTab.setOpaque(false);
        pnlAcoesTab.add(btnRecarregar);
        pnlTabela.add(pnlAcoesTab, BorderLayout.SOUTH);

        splitPane.setLeftComponent(pnlTabela);

        // Formulário (Card)
        JPanel pnlCard = UIHelper.createCardPanel();
        pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));

        JLabel lblFormTitulo = new JLabel("Dados do Exemplar");
        lblFormTitulo.setFont(FONT_SUBTITLE);
        lblFormTitulo.setForeground(TEXT_PRIMARY);

        txtId = UIHelper.createTextField("");
        txtId.setEditable(false);
        txtId.setBackground(BG_MAIN);

        txtCodigoBarras = UIHelper.createTextField("Ex: BAR-2026-001");
        cmbStatus = UIHelper.createComboBox(StatusCopia.values());
        cmbObra = new JComboBox<>();
        cmbObra.setFont(FONT_INPUT);
        cmbObra.setBackground(BG_INPUT);
        cmbObra.setForeground(TEXT_PRIMARY);

        pnlCard.add(lblFormTitulo);
        pnlCard.add(Box.createVerticalStrut(14));
        pnlCard.add(UIHelper.createFormRow("Código (ID):", txtId));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Código de Barras:", txtCodigoBarras));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Obra do Acervo:", cmbObra));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Situação/Status:", cmbStatus));
        pnlCard.add(Box.createVerticalStrut(20));

        // Botões
        JPanel pnlBotoes = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlBotoes.setOpaque(false);
        pnlBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));

        JButton btnNovo = UIHelper.createButton("Novo", BTN_PRIMARY);
        btnNovo.addActionListener(e -> limparFormulario());

        JButton btnSalvar = UIHelper.createSuccessButton("Salvar");
        btnSalvar.addActionListener(e -> salvarCopia());

        JButton btnExcluir = UIHelper.createDangerButton("Excluir");
        btnExcluir.addActionListener(e -> excluirCopia());

        pnlBotoes.add(btnNovo);
        pnlBotoes.add(btnSalvar);
        pnlBotoes.add(btnExcluir);

        pnlCard.add(pnlBotoes);
        pnlCard.add(Box.createVerticalGlue());

        splitPane.setRightComponent(pnlCard);
        add(splitPane, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        carregarComboObras();
        carregarTabela();
    }

    private void carregarComboObras() {
        cmbObra.removeAllItems();
        try {
            List<Obra> obras = service.listarObras();
            for (Obra o : obras) {
                cmbObra.addItem(new ObraItem(o.getId(), o.getTitulo()));
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar lista de obras para o seletor: " + ex.getMessage());
        }
    }

    private void carregarTabela() {
        tableModel.setRowCount(0);
        try {
            List<Copia> copias = service.listarCopias();
            for (Copia c : copias) {
                String obraTitulo = (c.getObra() != null && c.getObra().getTitulo() != null)
                        ? "[" + c.getObra().getId() + "] " + c.getObra().getTitulo()
                        : (c.getObra() != null ? "Obra #" + c.getObra().getId() : "-");

                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getCodigoBarras(),
                        obraTitulo,
                        c.getStatus() != null ? c.getStatus().name() : "N/D",
                        c.isDisponivel() ? "Sim" : "Não"
                });
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar cópias: " + ex.getMessage());
        }
    }

    private void preencherFormularioDaTabela() {
        int row = tabela.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Copia c = service.getCopiaDAO().carregar(id);
            if (c != null && c.getId() > 0) {
                txtId.setText(String.valueOf(c.getId()));
                txtCodigoBarras.setText(c.getCodigoBarras());
                if (c.getStatus() != null) {
                    cmbStatus.setSelectedItem(c.getStatus());
                }
                if (c.getObra() != null) {
                    for (int i = 0; i < cmbObra.getItemCount(); i++) {
                        if (cmbObra.getItemAt(i).id == c.getObra().getId()) {
                            cmbObra.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void limparFormulario() {
        tabela.clearSelection();
        txtId.setText("");
        txtCodigoBarras.setText("");
        cmbStatus.setSelectedItem(StatusCopia.DISPONIVEL);
        if (cmbObra.getItemCount() > 0) cmbObra.setSelectedIndex(0);
    }

    private void salvarCopia() {
        String codigoBarras = txtCodigoBarras.getText().trim();
        StatusCopia status = (StatusCopia) cmbStatus.getSelectedItem();
        ObraItem obraSelecionada = (ObraItem) cmbObra.getSelectedItem();

        if (codigoBarras.isEmpty()) {
            UIHelper.showWarning(this, "O código de barras é obrigatório.");
            return;
        }

        if (obraSelecionada == null) {
            UIHelper.showWarning(this, "Selecione uma obra cadastrada para vincular este exemplar.");
            return;
        }

        try {
            Obra obraRef = new Obra();
            obraRef.setId(obraSelecionada.id);

            Copia copia = new Copia();
            copia.setCodigoBarras(codigoBarras);
            copia.setStatus(status);
            copia.setObra(obraRef);

            boolean isEdicao = !txtId.getText().isBlank();
            if (isEdicao) {
                copia.setId(Integer.parseInt(txtId.getText()));
                int linhas = service.getCopiaDAO().alterar(copia);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Cópia \"" + copia.getCodigoBarras() + "\" atualizada com sucesso!");
                } else {
                    UIHelper.showWarning(this, "Aviso: a atualização não afetou nenhum registro.");
                }
            } else {
                int linhas = service.getCopiaDAO().inserir(copia);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Novo exemplar cadastrado com sucesso!\n" +
                            "Código de barras: " + copia.getCodigoBarras());
                } else {
                    UIHelper.showError(this, "Não foi possível inserir a cópia no banco de dados.");
                }
            }

            carregarTabela();
            limparFormulario();

        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao salvar cópia: " + ex.getMessage());
        }
    }

    private void excluirCopia() {
        if (txtId.getText().isBlank()) {
            UIHelper.showWarning(this, "Selecione um exemplar na tabela para excluir.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        String cod = txtCodigoBarras.getText();

        boolean confirmou = UIHelper.showConfirm(this, "Tem certeza que deseja remover o exemplar:\n"
                + "Código de Barras: " + cod + " (ID: " + id + ")?");
        if (!confirmou) return;

        try {
            Copia copia = new Copia();
            copia.setId(id);
            int linhas = service.getCopiaDAO().remover(copia);
            if (linhas > 0) {
                UIHelper.showSuccess(this, "Exemplar removido com sucesso!");
                carregarTabela();
                limparFormulario();
            } else {
                UIHelper.showError(this, "Não foi possível remover o exemplar do banco.");
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao excluir cópia: " + ex.getMessage());
        }
    }
}
