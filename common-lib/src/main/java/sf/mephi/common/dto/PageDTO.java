package sf.mephi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic DTO для пагинированных ответов
 *
 * @param <T> тип элементов в списке
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    /**
     * Список элементов на текущей странице
     */
    private List<T> content;

    /**
     * Номер текущей страницы (начиная с 0)
     */
    private int pageNumber;

    /**
     * Размер страницы (количество элементов)
     */
    private int pageSize;

    /**
     * Общее количество элементов
     */
    private long totalElements;

    /**
     * Общее количество страниц
     */
    private int totalPages;

    /**
     * Является ли текущая страница первой
     */
    private boolean first;

    /**
     * Является ли текущая страница последней
     */
    private boolean last;

    /**
     * Есть ли следующая страница
     */
    private boolean hasNext;

    /**
     * Есть ли предыдущая страница
     */
    private boolean hasPrevious;

    /**
     * Пустая ли страница
     */
    private boolean empty;

    /**
     * Создать PageDTO из Spring Data Page
     *
     * @param page Spring Data Page object
     * @param <T> тип элементов
     * @return PageDTO с данными из Page
     */
    public static <T> PageDTO<T> fromPage(org.springframework.data.domain.Page<T> page) {
        return PageDTO.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Создать PageDTO с маппингом содержимого
     * Полезно для конвертации Entity -> DTO
     *
     * @param page Spring Data Page object
     * @param mapper функция для маппинга элементов
     * @param <S> исходный тип элементов
     * @param <T> целевой тип элементов
     * @return PageDTO с замапленными элементами
     */
    public static <S, T> PageDTO<T> fromPage(
            org.springframework.data.domain.Page<S> page,
            java.util.function.Function<S, T> mapper) {

        List<T> mappedContent = page.getContent().stream()
                .map(mapper)
                .toList();

        return PageDTO.<T>builder()
                .content(mappedContent)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .empty(page.isEmpty())
                .build();
    }
}
