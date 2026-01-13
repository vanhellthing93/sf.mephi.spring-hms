package sf.mephi.common.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class PageRequestDTOTest {
    @Test
    void toPageable_ShouldCreateCorrectPageRequest() {
        PageRequestDTO dto = new PageRequestDTO(1, 10, "name", "asc");
        PageRequest pageable = (PageRequest) dto.toPageable();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "name"), pageable.getSort());
    }
}
