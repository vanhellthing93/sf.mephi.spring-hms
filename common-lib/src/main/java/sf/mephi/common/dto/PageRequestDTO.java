package sf.mephi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sf.mephi.common.constants.ApiConstants;

/**
 * DTO для параметров пагинации и сортировки
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {

    /**
     * Номер страницы (начиная с 0)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Размер страницы
     */
    @Builder.Default
    private int size = ApiConstants.DEFAULT_PAGE_SIZE;

    /**
     * Поле для сортировки
     */
    @Builder.Default
    private String sortBy = ApiConstants.DEFAULT_SORT_FIELD;

    /**
     * Направление сортировки (ASC/DESC)
     */
    @Builder.Default
    private String sortDirection = ApiConstants.DEFAULT_SORT_DIRECTION;

    /**
     * Конвертировать в Spring Pageable
     */
    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Sort sort = Sort.by(direction, sortBy);

        // Ограничиваем максимальный размер страницы
        int validSize = Math.min(size, ApiConstants.MAX_PAGE_SIZE);

        return PageRequest.of(page, validSize, sort);
    }
}
