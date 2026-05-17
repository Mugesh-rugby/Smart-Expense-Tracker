package utils;

import java.awt.Color;
import java.awt.Font;

/**
 * AppConstants - Application-wide constants for colors, fonts, and dimensions.
 * Central place to manage the visual identity of the app.
 */
public final class AppConstants {

    private AppConstants() {}

    // ── Application meta ───────────────────────────────────────────────────
    public static final String APP_NAME    = "Smart Expense Tracker";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_AUTHOR  = "Smart Finance";

    // ── Light theme colors ──────────────────────────────────────────────────
    public static final Color PRIMARY          = new Color(67, 97, 238);   // #4361EE vibrant blue
    public static final Color PRIMARY_DARK     = new Color(58, 12, 163);   // #3A0CA3
    public static final Color PRIMARY_LIGHT    = new Color(114, 9, 183);   // #7209B7
    public static final Color ACCENT           = new Color(247, 37, 133);  // #F72585 pink accent
    public static final Color SUCCESS          = new Color(76, 201, 144);  // #4CC990 income green
    public static final Color DANGER           = new Color(247, 37, 133);  // #F72585
    public static final Color WARNING          = new Color(255, 190, 11);  // #FFBE0B
    public static final Color INFO             = new Color(0, 180, 216);   // #00B4D8

    // Backgrounds
    public static final Color BG_MAIN          = new Color(248, 249, 255); // nearly white-blue
    public static final Color BG_SIDEBAR       = new Color(22, 33, 62);    // dark navy
    public static final Color BG_CARD          = Color.WHITE;
    public static final Color BG_HEADER        = new Color(67, 97, 238);

    // Text
    public static final Color TEXT_PRIMARY     = new Color(22, 33, 62);
    public static final Color TEXT_SECONDARY   = new Color(108, 117, 149);
    public static final Color TEXT_LIGHT       = new Color(255, 255, 255);
    public static final Color TEXT_MUTED       = new Color(173, 181, 211);

    // Borders
    public static final Color BORDER_COLOR     = new Color(226, 228, 248);

    // ── Dark theme colors ───────────────────────────────────────────────────
    public static final Color DARK_BG_MAIN     = new Color(18, 18, 30);
    public static final Color DARK_BG_SIDEBAR  = new Color(10, 10, 20);
    public static final Color DARK_BG_CARD     = new Color(30, 30, 48);
    public static final Color DARK_TEXT        = new Color(220, 220, 240);
    public static final Color DARK_BORDER      = new Color(50, 50, 80);

    // ── Fonts ───────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI",   Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI",   Font.PLAIN, 14);
    public static final Font FONT_HEADING  = new Font("Segoe UI",   Font.BOLD,  16);
    public static final Font FONT_BODY     = new Font("Segoe UI",   Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI",   Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas",   Font.PLAIN, 12);
    public static final Font FONT_BOLD     = new Font("Segoe UI",   Font.BOLD,  13);
    public static final Font FONT_AMOUNT   = new Font("Segoe UI",   Font.BOLD,  20);

    // ── Dimensions ──────────────────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH      = 220;
    public static final int HEADER_HEIGHT      = 65;
    public static final int CARD_ARC           = 16;
    public static final int BUTTON_HEIGHT      = 38;
    public static final int INPUT_HEIGHT       = 38;
    public static final int TABLE_ROW_HEIGHT   = 42;

    // ── Category colors (matching DB seeds) ────────────────────────────────
    public static final Color CAT_FOOD          = new Color(255, 107, 107);
    public static final Color CAT_TRAVEL        = new Color(78, 205, 196);
    public static final Color CAT_SHOPPING      = new Color(69, 183, 209);
    public static final Color CAT_BILLS         = new Color(150, 206, 180);
    public static final Color CAT_ENTERTAINMENT = new Color(255, 234, 167);
    public static final Color CAT_OTHERS        = new Color(176, 176, 176);
    public static final Color CAT_INCOME        = new Color(46, 204, 113);
}
