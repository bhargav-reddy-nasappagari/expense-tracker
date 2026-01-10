package com.expensetracker.util;

import java.util.List;

/**
 * Generic pagination result wrapper
 * Contains both the data and pagination metadata
 */
public class PagedResult<T> {
    
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
    
    public PagedResult(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }
    
    // ==================== Getters ====================
    
    public List<T> getItems() {
        return items;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public long getTotalItems() {
        return totalItems;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    // ==================== Helper Methods ====================
    
    public boolean hasNext() {
        return currentPage < totalPages;
    }
    
    public boolean hasPrevious() {
        return currentPage > 1;
    }
    
    public int getNextPage() {
        return hasNext() ? currentPage + 1 : currentPage;
    }
    
    public int getPreviousPage() {
        return hasPrevious() ? currentPage - 1 : currentPage;
    }
    
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    public int getStartIndex() {
        return (currentPage - 1) * pageSize + 1;
    }
    
    public int getEndIndex() {
        return Math.min(currentPage * pageSize, (int) totalItems);
    }
    
    @Override
    public String toString() {
        return "PagedResult{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalItems=" + totalItems +
                ", totalPages=" + totalPages +
                ", items=" + items.size() +
                '}';
    }
}