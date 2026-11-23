package com.poo.biblioteca.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import static com.poo.biblioteca.ui.UIConstants.*;

/**
 * Fábrica de componentes Swing estilizados com a identidade visual da Biblioteca.
 */
public final class UIHelper {

    public static final DateTimeFormatter DATA_BR_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_BR_BARRA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private UIHelper() {}

    // ── Formatação e Conversão de Datas (Padrão BR: dd-MM-yyyy) ──

    public static String formatarDataBR(LocalDate data) {
        if (data == null) {
            return "-";
        }
        return data.format(DATA_BR_FORMATTER);
    }

    public static LocalDate parseDataBR(String texto) throws DateTimeParseException {
        if (texto == null || texto.isBlank()) {
            throw new DateTimeParseException("Data vazia", "", 0);
        }
        String limpo = texto.trim();
        if (limpo.contains("/")) {
            return LocalDate.parse(limpo, DATA_BR_BARRA_FORMATTER);
        }
        return LocalDate.parse(limpo, DATA_BR_FORMATTER);
    }

    // ── Formatação de CPF e Telefone ──

    public static String formatarCpf(String valor) {
        if (valor == null) return "";
        String digits = valor.replaceAll("\\D", "");
        if (digits.length() > 11) {
            digits = digits.substring(0, 11);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i == 3 || i == 6) sb.append('.');
            else if (i == 9) sb.append('-');
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    public static String formatarTelefone(String valor) {
        if (valor == null) return "";
        String digits = valor.replaceAll("\\D", "");
        if (digits.length() > 11) {
            digits = digits.substring(0, 11);
        }
        int len = digits.length();
        if (len == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < len; i++) {
            if (i == 2) {
                sb.append(") ");
            } else if ((len <= 10 && i == 6) || (len == 11 && i == 7)) {
                sb.append('-');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    public static String formatarTextoDataBR(String valor) {
        if (valor == null) return "";
        String digits = valor.replaceAll("\\D", "");
        if (digits.length() > 8) {
            digits = digits.substring(0, 8);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i == 2 || i == 4) sb.append('-');
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static void aplicarMascaraCpf(JTextField field) {
        aplicarMascaraNumerica(field, 11, UIHelper::formatarCpf);
    }

    public static void aplicarMascaraTelefone(JTextField field) {
        aplicarMascaraNumerica(field, 11, UIHelper::formatarTelefone);
    }

    public static void aplicarMascaraDataBR(JTextField field) {
        aplicarMascaraNumerica(field, 8, UIHelper::formatarTextoDataBR);
    }

    private interface MaskFormatterFn {
        String format(String digits);
    }

    private static void aplicarMascaraNumerica(JTextField field, int maxDigitos, MaskFormatterFn formatter) {
        Document doc = field.getDocument();
        if (!(doc instanceof AbstractDocument)) return;
        ((AbstractDocument) doc).setDocumentFilter(new DocumentFilter() {
            private int contarDigitosAte(String texto, int limite) {
                int count = 0;
                int end = Math.min(limite, texto.length());
                for (int i = 0; i < end; i++) {
                    if (Character.isDigit(texto.charAt(i))) count++;
                }
                return count;
            }

            private int encontrarPosicaoPorDigitos(String formatado, int qtdDigitos) {
                if (qtdDigitos <= 0) return 0;
                int count = 0;
                for (int i = 0; i < formatado.length(); i++) {
                    if (Character.isDigit(formatado.charAt(i))) {
                        count++;
                        if (count == qtdDigitos) {
                            return i + 1;
                        }
                    }
                }
                return formatado.length();
            }

            private void aplicar(FilterBypass fb, int offset, int length, String textoInserido) throws BadLocationException {
                String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String inseridoDigitos = apenasDigitos(textoInserido);

                // Caso o usuário aperte Backspace exatamente sobre um caractere de máscara (., -, (, ), espaço),
                // remove o dígito imediatamente anterior para que apagar funcione de forma natural.
                int realOffset = offset;
                int realLength = length;
                if (textoInserido.isEmpty() && length == 1 && offset < atual.length()
                        && !Character.isDigit(atual.charAt(offset))) {
                    int prevDigitIdx = offset - 1;
                    while (prevDigitIdx >= 0 && !Character.isDigit(atual.charAt(prevDigitIdx))) {
                        prevDigitIdx--;
                    }
                    if (prevDigitIdx >= 0) {
                        realOffset = prevDigitIdx;
                        realLength = (offset + length) - prevDigitIdx;
                    }
                }

                String antes = atual.substring(0, realOffset);
                String depois = atual.substring(Math.min(realOffset + realLength, atual.length()));

                String digitosAntes = apenasDigitos(antes);
                String digitosDepois = apenasDigitos(depois);

                int espacoDisponivel = Math.max(0, maxDigitos - (digitosAntes.length() + digitosDepois.length()));
                if (inseridoDigitos.length() > espacoDisponivel) {
                    inseridoDigitos = inseridoDigitos.substring(0, espacoDisponivel);
                }

                String todosDigitos = digitosAntes + inseridoDigitos + digitosDepois;
                if (todosDigitos.length() > maxDigitos) {
                    todosDigitos = todosDigitos.substring(0, maxDigitos);
                }

                String formatado = formatter.format(todosDigitos);
                int digitosAteCursor = Math.min(digitosAntes.length() + inseridoDigitos.length(), todosDigitos.length());

                fb.replace(0, fb.getDocument().getLength(), formatado, null);

                int novaPosicao = encontrarPosicaoPorDigitos(formatado, digitosAteCursor);
                SwingUtilities.invokeLater(() -> {
                    int pos = Math.min(novaPosicao, field.getDocument().getLength());
                    field.setCaretPosition(pos);
                });
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                aplicar(fb, offset, 0, string);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                aplicar(fb, offset, length, text == null ? "" : text);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                aplicar(fb, offset, length, "");
            }
        });
    }

    // ── Popups ──

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    // ── Campos de Texto ──

    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (placeholder != null && !placeholder.isEmpty() && getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(TEXT_PLACEHOLDER);
                    g2.setFont(getFont());
                    Insets insets = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    int y = insets.top + ((getHeight() - insets.top - insets.bottom - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(placeholder, insets.left, y);
                    g2.dispose();
                }
            }
        };
        field.setFont(FONT_INPUT);
        field.setPreferredSize(new Dimension(250, INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_INPUT, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        return field;
    }

    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_INPUT);
        field.setPreferredSize(new Dimension(250, INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_INPUT, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        return field;
    }

    public static String getFieldText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        if (text.equals(placeholder)) return "";
        return text;
    }

    // ── Labels ──

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    // ── Botões ──

    public static JButton createButton(String text, Color bg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(140, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            final Color normalBg = bg;
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(normalBg.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalBg);
            }
        });
        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, BTN_PRIMARY);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, BTN_SUCCESS);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, BTN_DANGER);
    }

    // ── Tabela ──

    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(TABLE_SELECTION);
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ROW_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
                return c;
            }
        };
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(TABLE_GRID);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setFont(FONT_TABLE_HEADER);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 2, 0, ACCENT));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        return table;
    }

    public static JScrollPane wrapInScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(TABLE_GRID, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ── Painel de Formulário ──

    public static JPanel createFormRow(String labelText, JComponent field) {
        return createFormRow(labelText, field, false);
    }

    public static JPanel createFormRow(String labelText, JComponent field, boolean obrigatorio) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT + 4));
        String textoFinal = labelText;
        if (obrigatorio) {
            if (labelText.endsWith(":")) {
                String base = labelText.substring(0, labelText.length() - 1);
                textoFinal = "<html>" + base + " <font color='#C62828'>*</font>:</html>";
            } else {
                textoFinal = "<html>" + labelText + " <font color='#C62828'>*</font></html>";
            }
        }
        JLabel label = createLabel(textoFinal);
        label.setPreferredSize(new Dimension(145, INPUT_HEIGHT));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(TABLE_GRID, 1, true),
                BorderFactory.createEmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
        ));
        return panel;
    }

    // ── ComboBox ──

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_INPUT);
        combo.setPreferredSize(new Dimension(250, INPUT_HEIGHT));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT));
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        return combo;
    }
}
