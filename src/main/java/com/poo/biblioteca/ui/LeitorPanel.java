package com.poo.biblioteca.ui;

import com.poo.biblioteca.Leitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Painel de gerenciamento de Leitores.
 * Permite cadastrar, visualizar, atualizar e remover leitores com popups de confirmação.
 */
public class LeitorPanel extends JPanel {

    private final BibliotecaService service;
    private JTable tabela;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtNome;
    private JTextField txtCpf;
    private JTextField txtEmail;
    private JTextField txtTelefone;
    private JTextField txtMatricula;
    private JCheckBox chkAtivo;
    private JLabel lblIdentificador;

    public LeitorPanel(BibliotecaService service) {
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
        JLabel lblTitulo = UIHelper.createTitleLabel("👤 Gestão de Leitores");
        JLabel lblSub = new JLabel("Cadastro de associados, matrículas e prazos de circulação");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Divisão Tabela e Formulário
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.62);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        // Lado Esquerdo: Tabela
        String[] colunas = {"ID", "Identificador", "Nome", "CPF", "Matrícula", "Email", "Telefone", "Ativo"};
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

        JButton btnRecarregar = UIHelper.createButton("🔄 Atualizar Lista", BTN_PRIMARY);
        btnRecarregar.addActionListener(e -> carregarTabela());
        JPanel pnlAcoesTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlAcoesTab.setOpaque(false);
        pnlAcoesTab.add(btnRecarregar);
        pnlTabela.add(pnlAcoesTab, BorderLayout.SOUTH);

        splitPane.setLeftComponent(pnlTabela);

        // Lado Direito: Formulário (Card)
        JPanel pnlCard = UIHelper.createCardPanel();
        pnlCard.setLayout(new BoxLayout(pnlCard, BoxLayout.Y_AXIS));

        JLabel lblFormTitulo = new JLabel("Dados do Leitor");
        lblFormTitulo.setFont(FONT_SUBTITLE);
        lblFormTitulo.setForeground(TEXT_PRIMARY);

        JLabel lblObrigatorio = new JLabel("<html><font color='#C62828'>*</font> Campos obrigatórios</html>");
        lblObrigatorio.setFont(FONT_SMALL);
        lblObrigatorio.setForeground(TEXT_SECONDARY);

        txtId = UIHelper.createTextField("");
        txtId.setEditable(false);
        txtId.setBackground(BG_MAIN);

        txtNome = UIHelper.createTextField("Ex: Ana Beatriz Silva");
        txtCpf = UIHelper.createTextField("000.000.000-00");
        UIHelper.aplicarMascaraCpf(txtCpf);
        txtEmail = UIHelper.createTextField("exemplo@email.com");
        txtTelefone = UIHelper.createTextField("(00) 00000-0000");
        UIHelper.aplicarMascaraTelefone(txtTelefone);
        txtMatricula = UIHelper.createTextField("2026-A01");

        chkAtivo = new JCheckBox("Cadastro Ativo na Biblioteca");
        chkAtivo.setFont(FONT_BODY);
        chkAtivo.setForeground(TEXT_PRIMARY);
        chkAtivo.setOpaque(false);
        chkAtivo.setSelected(true);

        lblIdentificador = new JLabel("Prazo padrão: 15 dias | Limite: 3 livros");
        lblIdentificador.setFont(FONT_SMALL);
        lblIdentificador.setForeground(ACCENT);

        pnlCard.add(lblFormTitulo);
        pnlCard.add(Box.createVerticalStrut(4));
        pnlCard.add(lblObrigatorio);
        pnlCard.add(Box.createVerticalStrut(12));
        pnlCard.add(UIHelper.createFormRow("Código (ID):", txtId));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Nome Completo:", txtNome, true));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("CPF:", txtCpf, true));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Matrícula:", txtMatricula));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("E-mail:", txtEmail));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Telefone:", txtTelefone));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(UIHelper.createFormRow("Situação:", chkAtivo));
        pnlCard.add(Box.createVerticalStrut(8));
        pnlCard.add(lblIdentificador);
        pnlCard.add(Box.createVerticalStrut(16));

        // Botões de ação
        JPanel pnlBotoes = new JPanel(new GridLayout(1, 3, 8, 0));
        pnlBotoes.setOpaque(false);
        pnlBotoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));

        JButton btnNovo = UIHelper.createButton("Novo", BTN_PRIMARY);
        btnNovo.addActionListener(e -> limparFormulario());

        JButton btnSalvar = UIHelper.createSuccessButton("Salvar");
        btnSalvar.addActionListener(e -> salvarLeitor());

        JButton btnExcluir = UIHelper.createDangerButton("Excluir");
        btnExcluir.addActionListener(e -> excluirLeitor());

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
            List<Leitor> leitores = service.listarLeitores();
            for (Leitor l : leitores) {
                tableModel.addRow(new Object[]{
                        l.getId(),
                        l.getIdentificadorFormatado(),
                        l.getNome(),
                        UIHelper.formatarCpf(l.getCpf()),
                        l.getMatricula(),
                        l.getEmail(),
                        UIHelper.formatarTelefone(l.getTelefone()),
                        l.isAtivo() ? "Sim" : "Não"
                });
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar lista de leitores: " + ex.getMessage());
        }
    }

    private void preencherFormularioDaTabela() {
        int row = tabela.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            Leitor l = service.getLeitorDAO().carregar(id);
            if (l != null && l.getId() > 0) {
                txtId.setText(String.valueOf(l.getId()));
                txtNome.setText(l.getNome() != null ? l.getNome() : "");
                txtCpf.setText(UIHelper.formatarCpf(l.getCpf()));
                txtEmail.setText(l.getEmail() != null ? l.getEmail() : "");
                txtTelefone.setText(UIHelper.formatarTelefone(l.getTelefone()));
                txtMatricula.setText(l.getMatricula() != null ? l.getMatricula() : "");
                chkAtivo.setSelected(l.isAtivo());
                lblIdentificador.setText("Identificador: " + l.getIdentificadorFormatado() +
                        " | Prazo: " + l.getPrazoEmprestimoDias() + " dias | Limite: " + l.getLimiteLivros() + " livros");
            }
        }
    }

    private void limparFormulario() {
        tabela.clearSelection();
        txtId.setText("");
        txtNome.setText("");
        txtCpf.setText("");
        txtEmail.setText("");
        txtTelefone.setText("");
        txtMatricula.setText("");
        chkAtivo.setSelected(true);
        lblIdentificador.setText("Prazo padrão: 15 dias | Limite: 3 livros");
    }

    private void salvarLeitor() {
        String nome = txtNome.getText().trim();
        String cpf = txtCpf.getText().trim();
        String matricula = txtMatricula.getText().trim();
        String email = txtEmail.getText().trim();
        String telefone = txtTelefone.getText().trim();
        boolean ativo = chkAtivo.isSelected();

        String cpfDigitos = UIHelper.apenasDigitos(cpf);
        String telDigitos = UIHelper.apenasDigitos(telefone);

        if (nome.isEmpty() || cpfDigitos.isEmpty()) {
            UIHelper.showWarning(this, "Os campos obrigatórios (*) 'Nome Completo' e 'CPF' devem ser preenchidos.");
            return;
        }

        if (cpfDigitos.length() != 11) {
            UIHelper.showWarning(this, "Informe um CPF válido contendo 11 dígitos.");
            return;
        }

        if (!telDigitos.isEmpty() && telDigitos.length() < 10) {
            UIHelper.showWarning(this, "Informe um telefone válido com DDD (10 ou 11 dígitos) ou deixe o campo vazio.");
            return;
        }

        cpf = UIHelper.formatarCpf(cpfDigitos);
        telefone = telDigitos.isEmpty() ? "" : UIHelper.formatarTelefone(telDigitos);

        try {
            Leitor leitor = new Leitor();
            leitor.setNome(nome);
            leitor.setCpf(cpf);
            leitor.setMatricula(matricula);
            leitor.setEmail(email);
            leitor.setTelefone(telefone);
            leitor.setAtivo(ativo);

            boolean isEdicao = !txtId.getText().isBlank();
            if (isEdicao) {
                leitor.setId(Integer.parseInt(txtId.getText()));
                int linhas = service.getLeitorDAO().alterar(leitor);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Leitor \"" + leitor.getNome() + "\" atualizado com sucesso!");
                } else {
                    UIHelper.showWarning(this, "Aviso: a atualização não afetou nenhum registro no banco.");
                }
            } else {
                int linhas = service.getLeitorDAO().inserir(leitor);
                if (linhas > 0) {
                    UIHelper.showSuccess(this, "Novo leitor cadastrado com sucesso!\n" +
                            "Identificador: " + leitor.getIdentificadorFormatado());
                } else {
                    UIHelper.showError(this, "Não foi possível inserir o leitor no banco de dados.");
                }
            }

            carregarTabela();
            limparFormulario();

        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao salvar leitor: " + ex.getMessage());
        }
    }

    private void excluirLeitor() {
        if (txtId.getText().isBlank()) {
            UIHelper.showWarning(this, "Selecione um leitor na tabela para excluir.");
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        String nome = txtNome.getText();

        boolean confirmou = UIHelper.showConfirm(this, "Tem certeza que deseja excluir o leitor:\n"
                + nome + " (ID: " + id + ")?");
        if (!confirmou) return;

        try {
            Leitor leitor = new Leitor();
            leitor.setId(id);
            int linhas = service.getLeitorDAO().remover(leitor);
            if (linhas > 0) {
                UIHelper.showSuccess(this, "Leitor excluído com sucesso!");
                carregarTabela();
                limparFormulario();
            } else {
                UIHelper.showError(this, "Não foi possível remover o leitor do banco de dados.");
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao excluir leitor: " + ex.getMessage());
        }
    }
}
