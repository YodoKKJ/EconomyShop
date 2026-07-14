package com.brunobeduschi.economyshop.chatpack;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Desenha o badge em pixel-art no tamanho nativo em que o Minecraft o exibe
 * (10px de altura), sem antialiasing — assim o glyph não sofre downscale e
 * fica nítido e com a mesma estética da fonte do jogo.
 */
public class BadgeRenderer {

    private static final int HEIGHT = 10;
    private static final Font FONT = new Font("Dialog", Font.BOLD, 9);

    public static BufferedImage render(String label, Color background) {
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = measure.createGraphics();
        mg.setFont(FONT);
        FontMetrics metrics = mg.getFontMetrics();
        int textWidth = metrics.stringWidth(label);
        mg.dispose();

        int width = textWidth + 6;

        BufferedImage image = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        Color border = background.darker().darker();
        Color shade = background.darker();

        // corpo com cantos recortados em 1px (visual de selo pixel-art)
        g.setColor(background);
        g.fillRect(1, 0, width - 2, HEIGHT);
        g.fillRect(0, 1, width, HEIGHT - 2);

        // sombra inferior pra dar profundidade
        g.setColor(shade);
        g.fillRect(1, HEIGHT - 2, width - 2, 1);

        // borda
        g.setColor(border);
        g.drawLine(1, 0, width - 2, 0);
        g.drawLine(1, HEIGHT - 1, width - 2, HEIGHT - 1);
        g.drawLine(0, 1, 0, HEIGHT - 2);
        g.drawLine(width - 1, 1, width - 1, HEIGHT - 2);

        g.setFont(FONT);
        FontMetrics fm = g.getFontMetrics();
        int textX = (width - fm.stringWidth(label)) / 2;
        int textY = (HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(label, textX, textY);

        g.dispose();
        return image;
    }

    private BadgeRenderer() {
    }
}
