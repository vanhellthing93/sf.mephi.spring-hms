package sf.mephi.common.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageDTOTest {
    @Test
    void fromPage_ShouldMapPageCorrectly() {
        List<String> content = List.of("item1", "item2");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 10);

        PageDTO<String> dto = PageDTO.fromPage(page);

        assertEquals(2, dto.getContent().size());
        assertEquals(1, dto.getPageNumber());
        assertEquals(2, dto.getPageSize());
        assertEquals(10, dto.getTotalElements());
        assertTrue(dto.isHasNext());
        assertFalse(dto.isEmpty());
    }
}
