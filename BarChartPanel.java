package view;

import utils.AppConstants;
import utils.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BarChartPanel - Custom Swing bar chart for monthly expense trend.
 * Pure Java2D – no external charting library needed.
 */
public class BarChartPanel extends JPanel {

    private List<String>     labels = new ArrayList<>();
    private List<BigDecimal> values = new ArrayList<>();

    public BarChartPanel() {
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
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int padLeft = 50, padBottom = 30, padTop = 10, padRight = 10;
        int chartW = w - padLeft - padRight;
        int chartH = h - padBottom - padTop;

        // Max value
        double maxVal = values.stream()
                              .mapToDouble(BigDecimal::doubleValue)
                              .max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        // Grid lines
        g2.setColor(new Color(226, 228, 248));
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     1, new float[]{4, 4}, 0));
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = padTop + (int)(chartH * (1 - (double)i / gridLines));
            g2.drawLine(padLeft, y, w - padRight, y);
            // Y axis label
            g2.setFont(AppConstants.FONT_SMALL);
            g2.setColor(AppConstants.TEXT_SECONDARY);
            double amt = maxVal * i / gridLines;
            g2.drawString(CurrencyUtil.compact(BigDecimal.valueOf(amt)), 2, y + 4);
            g2.setColor(new Color(226, 228, 248));
        }
        g2.setStroke(new BasicStroke(1f));

        // Bars
        int n = labels.size();
        int barWidth = Math.min(40, (chartW / n) - 10);

        for (int i = 0; i < n; i++) {
            double val = values.get(i).doubleValue();
            int barH  = (int)(chartH * val / maxVal);
            int x     = padLeft + i * (chartW / n) + (chartW / n - barWidth) / 2;
            int y     = padTop + chartH - barH;

            // Gradient bar
            GradientPaint gp = new GradientPaint(x, y, AppConstants.PRIMARY,
                                                  x, y + barH, new Color(114, 9, 183));
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barWidth, barH, 6, 6);

            // Label
            g2.setColor(AppConstants.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String lbl = labels.get(i).split(" ")[0]; // first word (month abbrev)
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, x + (barWidth - fm.stringWidth(lbl)) / 2,
                          padTop + chartH + 16);
        }
    }

    private void drawNoData(Graphics g) {
        g.setColor(AppConstants.TEXT_SECONDARY);
        g.setFont(AppConstants.FONT_BODY);
        String msg = "No data yet";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}
