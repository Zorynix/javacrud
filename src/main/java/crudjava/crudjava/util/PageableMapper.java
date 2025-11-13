package crudjava.crudjava.util;

import crudjava.crudjava.dto.PageableDTO;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

@UtilityClass
public class PageableMapper {

    public static <T> PageableDTO<T> toPageableDTO(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean first = pageNumber == 0;
        boolean last = pageNumber >= totalPages - 1;
        boolean hasNext = pageNumber < totalPages - 1;
        boolean hasPrevious = pageNumber > 0;

        return new PageableDTO<>(
            content,
            pageNumber,
            pageSize,
            totalElements,
            totalPages,
            first,
            last,
            hasNext,
            hasPrevious
        );
    }

    public static <T> PageableDTO<T> toPageableDTO(Page<T> page) {
        return toPageableDTO(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
    }
}
