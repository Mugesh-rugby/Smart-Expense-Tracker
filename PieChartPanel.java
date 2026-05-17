package view;

import utils.AppConstants;
import utils.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * PieChartPanel - Donut-style pie chart for category-wise expense breakdown.
 */
public class PieChartPanel extends JPanel {

    private static final Color[] PALETTE = {
        new Color(255, 107, 107), new Color(78, 205, 196),
        new Color(69, 183, 209),  new Color(150, 206, 180),
        new Color(255, 234, 167), new Color(67, 97, 238),
        new Color(247, 37, 133),  new Color(76, 201, 144)
    };

    private List<String>     labels = new ArrayList<>();
    private List<BigDecimal> values = new ArrayList<>();

    public PieChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(300, 160));
    }

    public void setData(Map<String, BigDecimal> data) {
        labels.clear();
        values.clear();
        labels.addAll(data.keySet());
        values.addAll(data.values());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values.isEmpty()) {
            drawNoData(g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int diameter = Math.min(w / 2 - 20, h - 20);
        int x0 = 10, y0 = (h - diameter) / 2;

        double total = values.stream().mapToDouble(BigDecimal::doubleValue).sum();
        if (total == 0) { drawNoData(g); return; }

        // Draw donut slices
        double startAngle = 0;
        for (int i = 0; i < values.size(); i++) {
            double pct  = values.get(i).doubleValue() / total;
            double sweep = pct * 360;

            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fillArc(x0, y0, diameter, diameter, (int)startAngle, (int)sweep);
            startAngle += sweep;
        }

        // Donut hole
        int holeDiam = (int)(diameter * 0.55);
        int holeX    = x0 + (diameter - holeDiam) / 2;
        int holeY    = y0 + (diameter - holeDiam) / 2;
        g2.setColor(getParent() != null ? getParent().getBackground() : Color.WHITE);
        g2.fillOval(holeX, holeY, holeDiam, holeDiam);

        // Center text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(AppConstants.TEXT_PRIMARY);
        String total2 = CurrencyUtil.compact(BigDecimal.valueOf(total));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(total2, holeX + (holeDiam - fm.stringWidth(total2)) / 2,
                      holeY + holeDiam / 2 + 4);

        // Legend
        int legendX = x0 + diameter + 14;
        int legendY = y0;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i < Math.min(labels.size(), 6); i++) {
            g2.setColor(PALETTE[i % PALETTE.length]);
            g2.fillRoundRect(legendX, legendY + i * 22, 12, 12, 4, 4);
            g2.setColor(AppConstants.TEXT_PRIMARY);
            double pct = values.get(i).doubleValue() / total * 100;
            String lbl = labels.get(i) + " (" + String.format("%.0f%%", pct) + ")";
            g2.drawString(lbl, legendX + 16, legendY + i * 22 + 10);
        }
    }

    private void drawNoData(Graphics g) {
        g.setColor(AppConstants.TEXT_SECONDARY);
        g.setFont(AppConstants.FONT_BODY);
        String msg = "No expense data";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}
