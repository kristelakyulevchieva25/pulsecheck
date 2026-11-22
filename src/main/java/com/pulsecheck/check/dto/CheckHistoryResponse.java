
package com.pulsecheck.check.dto;

import java.util.List;

public record CheckHistoryResponse(
        List<CheckResultResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
