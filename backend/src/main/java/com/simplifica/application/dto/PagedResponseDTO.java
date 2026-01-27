package com.simplifica.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 *
 * Wraps paginated data with metadata about the page including
 * total elements, total pages, current page number, and page size.
 *
 * @param <T> the type of data being paginated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDTO<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean first;
    private boolean last;
    private boolean empty;

    /**
     * Creates a PagedResponseDTO from a Spring Data Page object.
     *
     * @param page the Spring Data Page
     * @param <T> the type of content in the page
     * @return a PagedResponseDTO instance
     */
    public static <T> PagedResponseDTO<T> fromPage(Page<T> page) {
        return PagedResponseDTO.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
