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

public class CategoryServlet extends BaseServlet {

    private static final Logger log = LoggerFactory.getLogger(CategoryServlet.class);
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void handleAuthenticatedGet(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        log.debug("User {} requested category management", userId);

        // Fetch all categories for the logged-in user
        List<Category> categories = categoryService.listCategories(userId);
        
        // Set categories as request attribute
        req.setAttribute("categories", categories);
        
        // Forward to the manage categories JSP page
        req.getRequestDispatcher("/WEB-INF/views/manage-categories.jsp")
                .forward(req, resp);
    }

    @Override
    protected void handleAuthenticatedPost(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        log.debug("Processing category action '{}' for user {}", action, userId);

        if (action == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action is required");
            return;
        }

        try {
            switch (action) {
                case "add" -> {
                    String name = req.getParameter("categoryName");
                    if (name == null || name.trim().isEmpty()) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category name is required");
                        return;
                    }
                    categoryService.addCategory(userId, name.trim());
                    log.info("Category '{}' added for user {}", name, userId);
                }
                case "rename" -> {
                    String idStr = req.getParameter("categoryId");
                    String newName = req.getParameter("newCategoryName");
                    
                    if (idStr == null || idStr.trim().isEmpty()) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category ID is required");
                        return;
                    }
                    if (newName == null || newName.trim().isEmpty()) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "New category name is required");
                        return;
                    }
                    Integer categoryId = Integer.valueOf(idStr);
                    categoryService.renameCategory(userId, categoryId, newName.trim());
                    log.info("Category {} renamed to '{}' for user {}", categoryId, newName, userId);
                }
                case "delete" -> {
                    String idStr = req.getParameter("categoryId");
                    if (idStr == null || idStr.trim().isEmpty()) {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Category ID is required");
                        return;
                    }
                    Integer categoryId = Integer.valueOf(idStr);
                    categoryService.deleteCategory(userId, categoryId);
                    log.info("Category {} deleted for user {}", categoryId, userId);
                }
                default -> {
                    log.warn("Invalid category action '{}' for user {}", action, userId);
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action: " + action);
                    return;
                }
            }

            resp.sendRedirect("manage-categories");

        } catch (NumberFormatException e) {
            log.warn("Invalid ID format in category action for user {}: {}", userId, e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
        } catch (Exception e) {
            log.error("Error processing category action '{}' for user {}: {}", action, userId, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}