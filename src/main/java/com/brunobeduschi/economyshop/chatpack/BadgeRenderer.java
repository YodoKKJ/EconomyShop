package com.brunobeduschi.economyshop.chatpack;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class BadgeRenderer {

    public static BufferedImage render(String label, Color background) {
        Font font = new Font("SansSerif", Font.BOLD, 32);
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = measure.createGraphics();
        mg.setFont(font);
        FontMetrics metrics = mg.getFontMetrics();
        int textWidth = metrics.stringWidth(label);
        int textHeight = metrics.getAscent() + metrics.getDescent();
        mg.dispose();

        int paddingX = 20;
        int paddingY = 10;
        int width = textWidth + paddingX * 2;
        int height = textHeight + paddingY * 2;
        int arc = height / 2;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(background);
        g.fillRoundRect(0, 0, width, height, arc, arc);
        g.setColor(background.darker());
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(2, 2, width - 4, height - 4, arc, arc);

        g.setFont(font);
        int textX = (width - textWidth) / 2;
        int textY = paddingY + metrics.getAscent();

        g.setColor(Color.BLACK);
        int[] dx = {-2, -2, -2, 0, 0, 2, 2, 2};
        int[] dy = {-2, 0, 2, -2, 2, -2, 0, 2};
        for (int i = 0; i < dx.length; i++) {
            g.drawString(label, textX + dx[i], textY + dy[i]);
        }
        g.setColor(Color.WHITE);
        g.drawString(label, textX, textY);

        g.dispose();
        return image;
    }

    private BadgeRenderer() {
    }
}
