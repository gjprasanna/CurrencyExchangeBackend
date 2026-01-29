package com.example.demo.Dto;

import java.util.List;

/**
 * DTO to track daily upload results
 */
public record UploadSummary(
        List<String> succeeded,
        List<String> failed
) {
    public int totalProcessed() {
        return succeeded.size() + failed.size();
    }

    public int successCount() {
        return succeeded.size();
    }

    public int failureCount() {
        return failed.size();
    }

    public boolean hasFailures() {
        return !failed.isEmpty();
    }

    public boolean isFullSuccess() {
        return failed.isEmpty() && !succeeded.isEmpty();
    }
}