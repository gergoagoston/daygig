package edu.bbte.daygig.backend.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageOutDto<T>(List<T> content,
                            int page,
                            int size,
                            long totalElements,
                            int totalPages,
                            boolean first,
                            boolean last) {

    public static <T> PageOutDto<T> from(Page<T> page) {
        return new PageOutDto<>(page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast());
    }
}
