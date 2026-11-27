package com.poo.biblioteca.ui;

import com.poo.biblioteca.*;
import com.poo.biblioteca.exception.DataDevolucaoInvalidaException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Painel completo de Circulação: Empréstimos, Devoluções e Cobrança de Multas.
 * Integra regras de negócio de cálculo de atraso, alteração automática de status da cópia
 * e validações de data com popups.
 */
public class EmprestimoPanel extends JPanel {

    private final BibliotecaService service;
    private final Funcionario funcionarioLogado;

    private JTable tabela;
    private DefaultTableModel tableModel;

    private JComboBox<LeitorItem> cmbLeitor;
    private JComboBox<CopiaItem> cmbCopia;
    private JTextField txtDataEmprestimo;
    private JLabel lblPrazoInfo;

    private JTextField txtDataDevolucao;
    private JLabel lblEmprestimoSelecionado;
    private Emprestimo emprestimoSelecionado = null;

    private static class LeitorItem {
        final int id;
        final String nome;
        final int prazoDias;
        final boolean funcionario;

        LeitorItem(int id, String nome, int prazoDias, boolean funcionario) {
            this.id = id;
            this.nome = nome;
            this.prazoDias = prazoDias;
            this.funcionario = funcionario;
        }

        String getTag() {
            return funcionario ? "[FUNCIONÁRIO] " : "[LEITOR] ";
        }

        String getNomeComTag() {
            return getTag() + nome;
        }

        @Override
        public String toString() {
            return getNomeComTag() + " (" + prazoDias + " dias)";
        }
    }

    private static class CopiaItem {
        final int id;
        final String codigoBarras;
        final String obraTitulo;

        CopiaItem(int id, String codigoBarras, String obraTitulo) {
            this.id = id;
            this.codigoBarras = codigoBarras;
            this.obraTitulo = obraTitulo;
        }

        @Override
        public String toString() {
            return "[" + id + "] " + codigoBarras + " - " + obraTitulo;
        }
    }

    public EmprestimoPanel(BibliotecaService service, Funcionario funcionarioLogado) {
        this.service = service;
        this.funcionarioLogado = funcionarioLogado;
        initComponents();
        carregarCombos();
        carregarTabela();
    }

    private void initComponents() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Cabeçalho
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitulo = UIHelper.createTitleLabel("Empréstimos e Multas");
        String nomeOperador = (funcionarioLogado != null) ? funcionarioLogado.getNome() : "Administrador";
        JLabel lblSub = new JLabel("Operador responsável: " + nomeOperador + " | Registro de retiradas, entregas e multas");
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);
        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        add(pnlHeader, BorderLayout.NORTH);

        // Painel Central: Tabela Superior e Cards Inferiores
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 16));
        pnlCentro.setOpaque(false);

        // Tabela
        String[] colunas = {"ID", "Leitor", "Exemplar / Cópia", "Empréstimo", "Previsão", "Devolução Real", "Multa (R$)", "Situação"};
        tableModel = new DefaultTableModel(colunas, 0);
        tabela = UIHelper.createStyledTable(tableModel);

        // Renderizador de Situação
        tabela.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                c.setFont(FONT_BODY_BOLD);
                if (value != null) {
                    String st = value.toString();
                    if (st.contains("ABERTO")) {
                        c.setForeground(STATUS_EMPRESTADA);
                    } else {
                        c.setForeground(STATUS_DISPONIVEL);
                    }
                }
                return c;
            }
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selecionarEmprestimoDaTabela();
            }
        });

        pnlCentro.add(UIHelper.wrapInScrollPane(tabela), BorderLayout.CENTER);

        // Painel Inferior: 2 Cards (Novo Empréstimo e Devolução com Multa)
        JPanel pnlCards = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 240));

        // CARD 1: Novo Empréstimo
        JPanel cardNovo = UIHelper.createCardPanel();
        cardNovo.setLayout(new BoxLayout(cardNovo, BoxLayout.Y_AXIS));

        JLabel lblNovoTit = new JLabel("Registrar Novo Empréstimo");
        lblNovoTit.setFont(FONT_SUBTITLE);
        lblNovoTit.setForeground(TEXT_PRIMARY);

        cmbLeitor = new JComboBox<>();
        cmbLeitor.setFont(FONT_INPUT);
        cmbLeitor.setBackground(BG_INPUT);
        cmbLeitor.setForeground(TEXT_PRIMARY);
        cmbLeitor.addActionListener(e -> atualizarInfoPrazo());

        cmbCopia = new JComboBox<>();
        cmbCopia.setFont(FONT_INPUT);
        cmbCopia.setBackground(BG_INPUT);
        cmbCopia.setForeground(TEXT_PRIMARY);

        txtDataEmprestimo = UIHelper.createTextField("dd-mm-aaaa");
        UIHelper.aplicarMascaraDataBR(txtDataEmprestimo);
        txtDataEmprestimo.setText(UIHelper.formatarDataBR(LocalDate.now()));
        lblPrazoInfo = new JLabel("Prazo calculado automaticamente conforme perfil do leitor.");
        lblPrazoInfo.setFont(FONT_SMALL);
        lblPrazoInfo.setForeground(ACCENT);

        JButton btnEmprestar = UIHelper.createSuccessButton("Confirmar Empréstimo");
        btnEmprestar.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        btnEmprestar.addActionListener(e -> realizarEmprestimo());

        cardNovo.add(lblNovoTit);
        cardNovo.add(Box.createVerticalStrut(8));
        cardNovo.add(UIHelper.createFormRow("Leitor / Func.:", cmbLeitor));
        cardNovo.add(Box.createVerticalStrut(6));
        cardNovo.add(UIHelper.createFormRow("Cópia Disponível:", cmbCopia));
        cardNovo.add(Box.createVerticalStrut(6));
        cardNovo.add(UIHelper.createFormRow("Data Retirada:", txtDataEmprestimo));
        cardNovo.add(Box.createVerticalStrut(4));
        cardNovo.add(lblPrazoInfo);
        cardNovo.add(Box.createVerticalStrut(10));
        cardNovo.add(btnEmprestar);
        cardNovo.add(Box.createVerticalGlue());

        // CARD 2: Finalização de Devolução
        JPanel cardDevolucao = UIHelper.createCardPanel();
        cardDevolucao.setLayout(new BoxLayout(cardDevolucao, BoxLayout.Y_AXIS));

        JLabel lblDevTit = new JLabel("Efetuar Devolução & Apurar Multa");
        lblDevTit.setFont(FONT_SUBTITLE);
        lblDevTit.setForeground(TEXT_PRIMARY);

        lblEmprestimoSelecionado = new JLabel("Selecione um empréstimo em aberto na tabela.");
        lblEmprestimoSelecionado.setFont(FONT_BODY);
        lblEmprestimoSelecionado.setForeground(TEXT_SECONDARY);

        txtDataDevolucao = UIHelper.createTextField("dd-mm-aaaa");
        UIHelper.aplicarMascaraDataBR(txtDataDevolucao);
        txtDataDevolucao.setText(UIHelper.formatarDataBR(LocalDate.now()));

        JButton btnDevolver = UIHelper.createPrimaryButton("Receber Devolução");
        btnDevolver.addActionListener(e -> finalizarDevolucao());

        JButton btnExcluir = UIHelper.createDangerButton("Excluir Registro");
        btnExcluir.addActionListener(e -> excluirEmprestimo());

        JPanel pnlBtnDev = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlBtnDev.setOpaque(false);
        pnlBtnDev.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        pnlBtnDev.add(btnDevolver);
        pnlBtnDev.add(btnExcluir);

        cardDevolucao.add(lblDevTit);
        cardDevolucao.add(Box.createVerticalStrut(8));
        cardDevolucao.add(lblEmprestimoSelecionado);
        cardDevolucao.add(Box.createVerticalStrut(10));
        cardDevolucao.add(UIHelper.createFormRow("Data Entrega:", txtDataDevolucao));
        cardDevolucao.add(Box.createVerticalStrut(14));
        cardDevolucao.add(pnlBtnDev);
        cardDevolucao.add(Box.createVerticalGlue());

        pnlCards.add(cardNovo);
        pnlCards.add(cardDevolucao);

        pnlCentro.add(pnlCards, BorderLayout.SOUTH);
        add(pnlCentro, BorderLayout.CENTER);
    }

    public void recarregarDados() {
        carregarCombos();
        carregarTabela();
        if (txtDataEmprestimo.getText().isBlank()) {
            txtDataEmprestimo.setText(UIHelper.formatarDataBR(LocalDate.now()));
        }
        if (txtDataDevolucao.getText().isBlank()) {
            txtDataDevolucao.setText(UIHelper.formatarDataBR(LocalDate.now()));
        }
    }

    private void atualizarInfoPrazo() {
        LeitorItem selecionado = (LeitorItem) cmbLeitor.getSelectedItem();
        if (selecionado != null) {
            String tipo = selecionado.funcionario ? "Funcionário" : "Leitor";
            lblPrazoInfo.setText("Prazo padrão: " + selecionado.prazoDias + " dias (" + tipo + ")");
        } else {
            lblPrazoInfo.setText("Prazo calculado automaticamente conforme perfil do leitor.");
        }
    }

    private void carregarCombos() {
        cmbLeitor.removeAllItems();
        cmbCopia.removeAllItems();
        try {
            List<Leitor> leitores = service.listarLeitores();
            for (Leitor l : leitores) {
                if (l.isAtivo()) {
                    cmbLeitor.addItem(new LeitorItem(l.getId(), l.getNome(), l.getPrazoEmprestimoDias(), false));
                }
            }
            List<Funcionario> funcionarios = service.listarFuncionarios();
            for (Funcionario f : funcionarios) {
                cmbLeitor.addItem(new LeitorItem(f.getId(), f.getNome(), f.getPrazoEmprestimoDias(), true));
            }
            // Exibe no ComboBox somente cópias com status DISPONIVEL
            List<Copia> copiasDisponiveis = service.listarCopiasDisponiveis();
            for (Copia c : copiasDisponiveis) {
                if (c != null && c.isDisponivel()) {
                    String titulo = (c.getObra() != null && c.getObra().getTitulo() != null)
                            ? c.getObra().getTitulo() : "Sem Título";
                    cmbCopia.addItem(new CopiaItem(c.getId(), c.getCodigoBarras(), titulo));
                }
            }
            atualizarInfoPrazo();
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao carregar seletores de empréstimo: " + ex.getMessage());
        }
    }

    private void carregarTabela() {
        tableModel.setRowCount(0);
        emprestimoSelecionado = null;
        lblEmprestimoSelecionado.setText("Selecione um empréstimo em aberto na tabela.");

        try {
            List<Emprestimo> lista = service.listarEmprestimos();
            for (Emprestimo emp : lista) {
                String nomeLeitor;
                Integer funcionarioLeitorId = service.buscarFuncionarioLeitorDoEmprestimo(emp.getId());
                if (funcionarioLeitorId != null) {
                    nomeLeitor = "[FUNCIONÁRIO] Funcionário #" + funcionarioLeitorId;
                    try {
                        Funcionario f = service.getFuncionarioDAO().carregar(funcionarioLeitorId);
                        if (f != null && f.getNome() != null) {
                            nomeLeitor = "[FUNCIONÁRIO] " + f.getNome();
                        }
                    } catch (Exception ignored) {}
                } else {
                    nomeLeitor = "[LEITOR] Leitor #" + emp.getLeitor().getId();
                    try {
                        Leitor l = service.getLeitorDAO().carregar(emp.getLeitor().getId());
                        if (l != null && l.getNome() != null) {
                            nomeLeitor = "[LEITOR] " + l.getNome();
                        }
                    } catch (Exception ignored) {}
                }

                String infoCopia = "Cópia #" + emp.getCopia().getId();
                try {
                    Copia c = service.getCopiaDAO().carregar(emp.getCopia().getId());
                    if (c != null && c.getCodigoBarras() != null) {
                        infoCopia = c.getCodigoBarras();
                    }
                } catch (Exception ignored) {}

                boolean isAberto = (emp.getDataDevolucaoReal() == null);
                String situacao = isAberto ? "EM ABERTO" : "DEVOLVIDO";
                String dataDevReal = isAberto ? "Pendente" : UIHelper.formatarDataBR(emp.getDataDevolucaoReal());
                String multaFormatada = String.format("R$ %.2f", emp.getValorMulta());

                tableModel.addRow(new Object[]{
                        emp.getId(),
                        nomeLeitor,
                        infoCopia,
                        UIHelper.formatarDataBR(emp.getDataEmprestimo()),
                        UIHelper.formatarDataBR(emp.getDataPrevistaDevolucao()),
                        dataDevReal,
                        multaFormatada,
                        situacao
                });
            }
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao listar empréstimos: " + ex.getMessage());
        }
    }

    private void selecionarEmprestimoDaTabela() {
        int row = tabela.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            emprestimoSelecionado = service.buscarEmprestimo(id);
            if (emprestimoSelecionado != null) {
                if (emprestimoSelecionado.getDataDevolucaoReal() == null) {
                    lblEmprestimoSelecionado.setText("<html>Empréstimo <b>#" + emprestimoSelecionado.getId()
                            + "</b> em aberto.<br>Devolução Prevista: <b>"
                            + UIHelper.formatarDataBR(emprestimoSelecionado.getDataPrevistaDevolucao())
                            + "</b></html>");
                    txtDataDevolucao.setText(UIHelper.formatarDataBR(LocalDate.now()));
                } else {
                    lblEmprestimoSelecionado.setText("<html>Empréstimo <b>#" + emprestimoSelecionado.getId()
                            + "</b> já finalizado em "
                            + UIHelper.formatarDataBR(emprestimoSelecionado.getDataDevolucaoReal())
                            + " (Multa: R$ " + String.format("%.2f", emprestimoSelecionado.getValorMulta()) + ")</html>");
                }
            }
        } else {
            emprestimoSelecionado = null;
            lblEmprestimoSelecionado.setText("Selecione um empréstimo em aberto na tabela.");
        }
    }

    private void realizarEmprestimo() {
        LeitorItem leitorItem = (LeitorItem) cmbLeitor.getSelectedItem();
        CopiaItem copiaItem = (CopiaItem) cmbCopia.getSelectedItem();

        if (leitorItem == null || copiaItem == null) {
            UIHelper.showWarning(this, "Selecione o leitor/funcionário e uma cópia disponível.");
            return;
        }

        LocalDate dataEmprestimo;
        try {
            dataEmprestimo = UIHelper.parseDataBR(txtDataEmprestimo.getText().trim());
        } catch (DateTimeParseException ex) {
            UIHelper.showWarning(this, "A data de empréstimo deve estar no formato DD-MM-AAAA (ex: "
                    + UIHelper.formatarDataBR(LocalDate.now()) + ").");
            return;
        }

        try {
            Leitor leitor = new Leitor();
            leitor.setId(leitorItem.id);
            leitor.setNome(leitorItem.nome);

            Copia copia = service.getCopiaDAO().carregar(copiaItem.id);
            if (copia == null || copia.getId() == 0) {
                UIHelper.showError(this, "Exemplar selecionado não pôde ser carregado do acervo.");
                carregarCombos();
                return;
            }

            if (!copia.isDisponivel()) {
                UIHelper.showWarning(this, "A cópia selecionada não está disponível para empréstimo (Status: " + copia.getStatus() + ").");
                carregarCombos();
                return;
            }

            LocalDate dataPrevista = dataEmprestimo.plusDays(leitorItem.prazoDias);

            Emprestimo emp = new Emprestimo();
            emp.setLeitor(leitor);
            emp.setFuncionario(funcionarioLogado != null ? funcionarioLogado : new Funcionario());
            emp.setCopia(copia);
            emp.setDataEmprestimo(dataEmprestimo);
            emp.setDataPrevistaDevolucao(dataPrevista);
            emp.setValorMulta(0.0);

            // 1. Atualiza status da cópia para EMPRESTADA
            copia.setStatus(StatusCopia.EMPRESTADA);
            service.getCopiaDAO().alterar(copia);

            // 2. Salva o empréstimo
            service.getEmprestimoDAO().inserir(emp);

            // 3. Caso o tomador seja um funcionário, registra o vínculo auxiliar na camada de UI
            if (leitorItem.funcionario && emp.getId() > 0) {
                service.registrarEmprestimoDeFuncionario(emp.getId(), leitorItem.id);
            }

            UIHelper.showSuccess(this, "Empréstimo registrado com sucesso!\n\n"
                    + "Código do Empréstimo: #" + emp.getId() + "\n"
                    + "Tomador: " + leitorItem.getNomeComTag() + "\n"
                    + "Exemplar: " + copiaItem.codigoBarras + " (" + copiaItem.obraTitulo + ")\n"
                    + "Data de Retirada: " + UIHelper.formatarDataBR(dataEmprestimo) + "\n"
                    + "Prazo Concedido: " + leitorItem.prazoDias + " dias\n"
                    + "Data Limite de Devolução: " + UIHelper.formatarDataBR(dataPrevista));

            carregarCombos();
            carregarTabela();

        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao registrar empréstimo: " + ex.getMessage());
        }
    }

    private void finalizarDevolucao() {
        if (emprestimoSelecionado == null) {
            UIHelper.showWarning(this, "Selecione um empréstimo na tabela para efetuar a devolução.");
            return;
        }

        if (emprestimoSelecionado.getDataDevolucaoReal() != null) {
            UIHelper.showWarning(this, "Este empréstimo já foi finalizado em "
                    + UIHelper.formatarDataBR(emprestimoSelecionado.getDataDevolucaoReal()) + ".");
            return;
        }

        LocalDate dataDevolucao;
        try {
            dataDevolucao = UIHelper.parseDataBR(txtDataDevolucao.getText().trim());
        } catch (DateTimeParseException ex) {
            UIHelper.showWarning(this, "A data de devolução deve estar no formato DD-MM-AAAA (ex: "
                    + UIHelper.formatarDataBR(LocalDate.now()) + ").");
            return;
        }

        try {
            // Carrega cópia associada para restaurar disponibilidade
            Copia copia = service.getCopiaDAO().carregar(emprestimoSelecionado.getCopia().getId());
            emprestimoSelecionado.setCopia(copia);

            // Invoca regra de negócio de finalização no modelo (calcula multa e seta copia para DISPONIVEL)
            // Lança DataDevolucaoInvalidaException se data nula, anterior ao empréstimo ou já finalizado
            emprestimoSelecionado.finalizarDevolucao(dataDevolucao);

            // Persiste atualização do empréstimo no banco
            service.getEmprestimoDAO().atualizar(emprestimoSelecionado);

            // Persiste status da cópia liberada no banco
            if (copia != null && copia.getId() > 0) {
                copia.setStatus(StatusCopia.DISPONIVEL);
                service.getCopiaDAO().alterar(copia);
            }

            double multa = emprestimoSelecionado.getValorMulta();
            String msgMulta = (multa > 0.0)
                    ? String.format("Atraso identificado! Multa a recolher: R$ %.2f", multa)
                    : "Devolução no prazo! Sem multa aplicada.";

            UIHelper.showSuccess(this, "Devolução concluída com sucesso!\n\n"
                    + "Empréstimo: #" + emprestimoSelecionado.getId() + "\n"
                    + "Data de Devolução Real: " + UIHelper.formatarDataBR(dataDevolucao) + "\n"
                    + "Exemplar liberado e voltou ao status DISPONÍVEL.\n\n"
                    + "Situação Financeira: " + msgMulta);

            carregarCombos();
            carregarTabela();

        } catch (DataDevolucaoInvalidaException ex) {
            UIHelper.showError(this, "Falha na validação de devolução:\n" + ex.getMessage());
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao finalizar devolução: " + ex.getMessage());
        }
    }

    private void excluirEmprestimo() {
        if (emprestimoSelecionado == null) {
            UIHelper.showWarning(this, "Selecione um empréstimo na tabela para excluir.");
            return;
        }

        boolean confirmou = UIHelper.showConfirm(this,
                "Tem certeza que deseja apagar o registro do empréstimo #"
                        + emprestimoSelecionado.getId() + "?");
        if (!confirmou) return;

        try {
            if (emprestimoSelecionado.getDataDevolucaoReal() == null && emprestimoSelecionado.getCopia() != null) {
                Copia copia = service.getCopiaDAO().carregar(emprestimoSelecionado.getCopia().getId());
                if (copia != null && copia.getId() > 0 && copia.getStatus() == StatusCopia.EMPRESTADA) {
                    copia.setStatus(StatusCopia.DISPONIVEL);
                    service.getCopiaDAO().alterar(copia);
                }
            }
            service.removerVinculoFuncionarioEmprestimo(emprestimoSelecionado.getId());
            service.getEmprestimoDAO().deletar(emprestimoSelecionado.getId());
            UIHelper.showSuccess(this, "Registro do empréstimo excluído com sucesso.");
            carregarCombos();
            carregarTabela();
        } catch (Exception ex) {
            UIHelper.showError(this, "Erro ao excluir empréstimo: " + ex.getMessage());
        }
    }
}
