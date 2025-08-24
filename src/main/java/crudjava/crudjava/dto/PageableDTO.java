package crudjava.crudjava.dto;

import java.util.List;

public record PageableDTO<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious
) {
    public PageableDTO(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this(
            content,
            pageNumber,
            pageSize,
            totalElements,
            (int) Math.ceil((double) totalElements / pageSize),
            pageNumber == 0,
            pageNumber >= (int) Math.ceil((double) totalElements / pageSize) - 1,
            pageNumber < (int) Math.ceil((double) totalElements / pageSize) - 1,
            pageNumber > 0
        );
    }
}
