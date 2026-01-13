package sf.mephi.hotel.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sf.mephi.hotel.config.BaseControllerTest;
import sf.mephi.hotel.dto.request.CreateHotelRequest;
import sf.mephi.hotel.dto.response.HotelDTO;
import sf.mephi.hotel.service.HotelService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class HotelControllerTest extends BaseControllerTest {

    @MockitoBean
    private HotelService hotelService;

    @Test
    @WithMockUser(roles = "USER")
    void getAllHotels_ShouldReturnHotels() throws Exception {
        HotelDTO hotel = HotelDTO.builder().id(1L).name("Grand Hotel").build();
        when(hotelService.getAllHotels()).thenReturn(List.of(hotel));

        mockMvc.perform(get("/api/v1/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Grand Hotel"));

        verify(hotelService).getAllHotels();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHotelById_ShouldReturnHotel() throws Exception {
        HotelDTO hotel = HotelDTO.builder().id(1L).name("Grand Hotel").city("Moscow").build();
        when(hotelService.getHotelById(1L)).thenReturn(hotel);

        mockMvc.perform(get("/api/v1/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Grand Hotel"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHotelsByCity_ShouldReturnFilteredHotels() throws Exception {
        HotelDTO hotel = HotelDTO.builder().id(1L).name("Moscow Hotel").city("Moscow").build();
        when(hotelService.getHotelsByCity("Moscow")).thenReturn(List.of(hotel));

        mockMvc.perform(get("/api/v1/hotels")
                        .param("city", "Moscow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Moscow"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createHotel_ShouldReturnCreated_WhenAdmin() throws Exception {
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("New Hotel")
                .address("123 St")
                .city("Moscow")
                .build();

        HotelDTO hotel = HotelDTO.builder().id(1L).name("New Hotel").build();
        when(hotelService.createHotel(any(CreateHotelRequest.class))).thenReturn(hotel);

        mockMvc.perform(post("/api/v1/hotels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Hotel"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createHotel_ShouldReturn403_WhenNotAdmin() throws Exception {
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("New Hotel")
                .address("123 St")
                .city("Moscow")
                .build();

        mockMvc.perform(post("/api/v1/hotels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(hotelService, never()).createHotel(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateHotel_ShouldReturnUpdated() throws Exception {
        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("Updated Hotel")
                .address("456 St")
                .city("Kazan")
                .build();

        HotelDTO hotel = HotelDTO.builder().id(1L).name("Updated Hotel").build();
        when(hotelService.updateHotel(eq(1L), any(CreateHotelRequest.class))).thenReturn(hotel);

        mockMvc.perform(put("/api/v1/hotels/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hotel"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteHotel_ShouldReturnNoContent() throws Exception {
        doNothing().when(hotelService).deleteHotel(1L);

        mockMvc.perform(delete("/api/v1/hotels/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(hotelService).deleteHotel(1L);
    }

    @Test
    void getAllHotels_ShouldReturn401_WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/hotels"))
                .andExpect(status().isUnauthorized());

        verify(hotelService, never()).getAllHotels();
    }
}
