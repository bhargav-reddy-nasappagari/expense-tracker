package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.service.CategoryService;

// Logger Imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class ManageCategoriesServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(ManageCategoriesServlet.class);
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        log.debug("User {} requested manage categories page", userId);

        try {
            List<Category> categories = categoryService.listCategories(userId);
            req.setAttribute("categories", categories);

            req.getRequestDispatcher("/WEB-INF/views/manage-categories.jsp").forward(req, resp);

        } catch (Exception e) {
            log.error("Error loading categories for user {}: {}", userId, e.getMessage(), e);
            throw new ServletException("Error loading categories", e);
        }
    }
}