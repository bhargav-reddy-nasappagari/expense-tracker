package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.dto.ReportData;
import com.expensetracker.dto.CategorySummary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PdfReportGenerator {

    // Layout Constants
    private static final int MARGIN = 50;
    private static final int Y_TOP_FIRST_PAGE = 750;
    private static final int Y_TOP_SUBSEQUENT_PAGES = 800; // More space on new pages (no dashboard)
    
    // Brand Colors
    private static final Color COL_PRIMARY = new Color(84, 98, 54);    // Olive Leaf
    private static final Color COL_ACCENT = new Color(234, 237, 230);  // Light Olive BG
    private static final Color COL_TEXT = new Color(35, 37, 40);       // Dark Grey
    private static final Color COL_GRAY = Color.GRAY;

    // Helper to get Standard Fonts in PDFBox 3.0
    private PDType1Font getFont(Standard14Fonts.FontName name) {
        return new PDType1Font(name);
    }

    public void generate(HttpServletResponse response, List<Expense> expenses, 
                         Map<Integer, String> categoryMap, ReportData analytics,
                         LocalDate from, LocalDate to) throws IOException {
        
        try (PDDocument document = new PDDocument()) {
            
            // 1. Setup First Page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // We cannot use try-with-resources for contentStream here because 
            // we need to swap it out for a new one during pagination.
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            int y = Y_TOP_FIRST_PAGE;

            // 2. Draw Dashboard (Only on First Page)
            drawHeader(contentStream, y, from, to);
            y -= 60;

            if (analytics != null) {
                y = drawKPICards(contentStream, y, analytics);
                y -= 20; 
                y = drawCategoryBars(contentStream, y, analytics.getCategoryBreakdown());
                y -= 40; 
            }

            // 3. Draw Table Header (Initial)
            drawTableHeader(contentStream, y);
            y -= 20;

            // 4. Draw Rows with Pagination Logic
            contentStream.setFont(getFont(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.setNonStrokingColor(COL_TEXT);

            for (Expense e : expenses) {
                // --- PAGINATION LOGIC START ---
                // If we are too close to the bottom (Margin + Footer space)
                if (y < MARGIN + 20) {
                    // A. Close the current stream (finish writing to current page)
                    contentStream.close();

                    // B. Create a new blank page
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    // C. Start a new stream for the new page
                    contentStream = new PDPageContentStream(document, page);

                    // D. Reset Y coordinate to top of new page
                    y = Y_TOP_SUBSEQUENT_PAGES;

                    // E. Re-draw Table Header on the new page
                    drawTableHeader(contentStream, y);
                    y -= 20;

                    // F. Re-apply Fonts (Context is lost on new stream)
                    contentStream.setFont(getFont(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.setNonStrokingColor(COL_TEXT);
                }
                // --- PAGINATION LOGIC END ---

                String date = e.getExpenseDate().format(DateTimeFormatter.ofPattern("MMM dd"));
                String cat = categoryMap.getOrDefault(e.getCategoryId(), "-");
                // Truncate Description to fit column
                String desc = e.getDescription();
                if (desc != null && desc.length() > 28) {
                    desc = desc.substring(0, 25) + "...";
                }
                String amt = String.format("%.2f", e.getAmount());

                drawRow(contentStream, y, date, cat, desc, amt);
                y -= 20;
            }

            // Always close the final stream
            contentStream.close();

            // 5. Send to Browser
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"Analysis_Report.pdf\"");
            document.save(response.getOutputStream());
        }
    }

    // --- DRAWING HELPERS ---

    private void drawHeader(PDPageContentStream stream, int y, LocalDate from, LocalDate to) throws IOException {
        stream.beginText();
        stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
        stream.setNonStrokingColor(COL_PRIMARY);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText("Financial Statement");
        stream.endText();

        stream.beginText();
        stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA), 12);
        stream.setNonStrokingColor(COL_GRAY);
        stream.newLineAtOffset(MARGIN, y - 20);
        stream.showText("Period: " + from.toString() + " to " + to.toString());
        stream.endText();
    }

    private int drawKPICards(PDPageContentStream stream, int y, ReportData data) throws IOException {
        int cardHeight = 50;
        
        // Card 1: Total
        drawCard(stream, MARGIN, y, "TOTAL SPENT", "Rs. " + data.getTotalSpending());
        
        // Card 2: Daily Avg
        drawCard(stream, MARGIN + 170, y, "DAILY AVG", "Rs. " + data.getAveragePerDay());
        
        // Card 3: Trend
        String trend = "N/A";
        if (data.getComparison() != null && data.getComparison().getPercentageChange() != null) {
            double chg = data.getComparison().getPercentageChange();
            String arrow = (chg > 0) ? "+" : ""; // up or down arrow
            trend = arrow + String.format("%.1f", chg) + "% vs last";
        }
        drawCard(stream, MARGIN + 340, y, "TREND", trend);

        return y - cardHeight;
    }

    private void drawCard(PDPageContentStream stream, int x, int y, String label, String value) throws IOException {
        // Draw Box
        stream.setNonStrokingColor(COL_ACCENT);
        stream.addRect(x, y - 40, 150, 40);
        stream.fill();

        // Label
        stream.beginText();
        stream.setNonStrokingColor(COL_PRIMARY);
        stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
        stream.newLineAtOffset(x + 10, y - 12);
        stream.showText(label);
        stream.endText();

        // Value
        stream.beginText();
        stream.setNonStrokingColor(COL_TEXT);
        stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
        stream.newLineAtOffset(x + 10, y - 30);
        stream.showText(value);
        stream.endText();
    }

    private int drawCategoryBars(PDPageContentStream stream, int y, List<CategorySummary> cats) throws IOException {
        if (cats == null || cats.isEmpty()) return y;

        stream.beginText();
        stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
        stream.setNonStrokingColor(COL_TEXT);
        stream.newLineAtOffset(MARGIN, y);
        stream.showText("Top Spending Categories");
        stream.endText();
        
        y -= 20;

        int count = 0;
        for (CategorySummary cat : cats) {
            if (count++ > 2) break; // Only show top 3
            
            // Text Label
            stream.beginText();
            stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA), 10);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(cat.getCategoryName());
            stream.endText();

            // Bar Graphic
            double percent = cat.getPercentage(); 
            float barWidth = (float) (percent * 3); // Scale factor
            
            stream.setNonStrokingColor(COL_PRIMARY);
            stream.addRect(MARGIN + 100, y, barWidth, 8);
            stream.fill();

            // Percentage Text
            stream.beginText();
            stream.setNonStrokingColor(COL_GRAY);
            stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA), 9);
            stream.newLineAtOffset(MARGIN + 100 + barWidth + 5, y);
            stream.showText(String.format("%.1f%%", percent));
            stream.endText();

            y -= 15;
        }
        return y;
    }

    private void drawTableHeader(PDPageContentStream stream, int y) throws IOException {
        stream.setNonStrokingColor(COL_TEXT);
        stream.setFont(getFont(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        
        // Draw Text
        stream.beginText();
        stream.newLineAtOffset(MARGIN, y);
        stream.showText("Date");
        stream.newLineAtOffset(80, 0);
        stream.showText("Category");
        stream.newLineAtOffset(100, 0);
        stream.showText("Description");
        stream.newLineAtOffset(220, 0);
        stream.showText("Amount");
        stream.endText();
        
        // Draw Line
        stream.setStrokingColor(Color.LIGHT_GRAY);
        stream.setLineWidth(1f);
        stream.moveTo(MARGIN, y - 5);
        stream.lineTo(MARGIN + 500, y - 5);
        stream.stroke();
    }
    
    private void drawRow(PDPageContentStream stream, int y, String d, String c, String desc, String a) throws IOException {
        stream.beginText();
        stream.newLineAtOffset(MARGIN, y);
        stream.showText(d);
        stream.newLineAtOffset(80, 0);
        stream.showText(c);
        stream.newLineAtOffset(100, 0);
        stream.showText(desc);
        stream.newLineAtOffset(220, 0);
        stream.showText("Rs. " + a);
        stream.endText();
    }
}