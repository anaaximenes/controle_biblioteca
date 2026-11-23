package com.poo.biblioteca.ui;

import java.awt.*;

/**
 * Paleta de cores e constantes visuais inspiradas em biblioteca clássica.
 * Tons de madeira (carvalho, nogueira, mogno), bege, creme e off-white.
 */
public final class UIConstants {

    private UIConstants() {}

    // ── Cores Primárias ──
    public static final Color SIDEBAR_BG        = new Color(0x3E, 0x27, 0x23);  // nogueira escura
    public static final Color SIDEBAR_HOVER     = new Color(0x5D, 0x40, 0x37);  // mogno médio
    public static final Color SIDEBAR_SELECTED  = new Color(0x6D, 0x4C, 0x41);  // mocha
    public static final Color SIDEBAR_TEXT       = new Color(0xEF, 0xEB, 0xE9);  // off-white quente

    public static final Color BG_MAIN           = new Color(0xF5, 0xF0, 0xEB);  // pergaminho
    public static final Color BG_CARD           = Color.WHITE;
    public static final Color BG_INPUT          = Color.WHITE;
    public static final Color BORDER_INPUT      = new Color(0xBC, 0xAA, 0xA4);  // bege rosado

    public static final Color TEXT_PRIMARY      = new Color(0x3E, 0x27, 0x23);  // marrom escuro
    public static final Color TEXT_SECONDARY    = new Color(0x79, 0x55, 0x48);  // marrom médio
    public static final Color TEXT_PLACEHOLDER  = new Color(0xA1, 0x88, 0x7F);  // bege escuro

    // ── Cores de Ação ──
    public static final Color BTN_PRIMARY       = new Color(0x5D, 0x40, 0x37);  // mogno
    public static final Color BTN_PRIMARY_HOVER = new Color(0x4E, 0x34, 0x2E);
    public static final Color BTN_SUCCESS       = new Color(0x55, 0x8B, 0x2F);  // verde musgo
    public static final Color BTN_DANGER        = new Color(0xC6, 0x28, 0x28);  // vermelho sóbrio
    public static final Color BTN_WARNING       = new Color(0xF9, 0xA8, 0x25);  // âmbar

    public static final Color ACCENT            = new Color(0x8D, 0x6E, 0x63);  // marrom quente

    // ── Cores de Status ──
    public static final Color STATUS_DISPONIVEL = new Color(0x2E, 0x7D, 0x32);
    public static final Color STATUS_EMPRESTADA = new Color(0xE6, 0x51, 0x00);
    public static final Color STATUS_RESERVADA  = new Color(0x15, 0x65, 0xC0);
    public static final Color STATUS_MANUTENCAO = new Color(0x9E, 0x9E, 0x9E);

    // ── Tabela ──
    public static final Color TABLE_HEADER_BG   = new Color(0xEF, 0xEB, 0xE9);
    public static final Color TABLE_ROW_ALT     = new Color(0xFA, 0xF8, 0xF6);
    public static final Color TABLE_GRID        = new Color(0xE0, 0xD6, 0xD0);
    public static final Color TABLE_SELECTION   = new Color(0xD7, 0xCC, 0xC8);

    // ── Fontes ──
    public static final Font FONT_TITLE         = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE      = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY          = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD     = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL         = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_INPUT         = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SIDEBAR       = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SIDEBAR_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TABLE_HEADER  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_CELL    = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Dimensões ──
    public static final int SIDEBAR_WIDTH       = 220;
    public static final int INPUT_HEIGHT        = 36;
    public static final int BUTTON_HEIGHT       = 36;
    public static final int TABLE_ROW_HEIGHT    = 32;
    public static final int CARD_PADDING        = 20;
    public static final int CARD_RADIUS         = 12;
}
