package com.csehub.app.core.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Generic API response wrapper matching the backend format:
 * { success: boolean, message: string, data: T, meta: PaginationMeta }
 */
public class ApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("meta")
    private PaginationMeta meta;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public PaginationMeta getMeta() { return meta; }

    /**
     * Pagination metadata
     */
    public static class PaginationMeta {
        @SerializedName("page")
        private int page;

        @SerializedName("limit")
        private int limit;

        @SerializedName("total")
        private int total;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("hasNext")
        private boolean hasNext;

        @SerializedName("hasPrev")
        private boolean hasPrev;

        public int getPage() { return page; }
        public int getLimit() { return limit; }
        public int getTotal() { return total; }
        public int getTotalPages() { return totalPages; }
        public boolean hasNext() { return hasNext; }
        public boolean hasPrev() { return hasPrev; }
    }
}
