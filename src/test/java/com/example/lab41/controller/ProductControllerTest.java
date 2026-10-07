package com.example.lab41.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.lab41.dto.PageResponse;
import com.example.lab41.dto.ProductResponse;
import com.example.lab41.dto.ReviewResponse;
import com.example.lab41.exception.ResourceNotFoundException;
import com.example.lab41.service.ProductService;

/** Controller contract test: khong can MongoDB that. */
@WebMvcTest(controllers = ProductController.class, properties = "app.seed.enabled=false")
class ProductControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ProductService service;

    private static final String VALID_BODY = """
            {"name":"Keyboard","price":120,"stock":10,"categoryId":"c1"}
            """;

    private ProductResponse response() {
        return new ProductResponse("p1", "Keyboard", new BigDecimal("120"), 10, "c1", List.of());
    }

    @Test
    void create_shouldReturn201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(response());

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/p1"))
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    // #66 Reject invalid DTO
    @Test
    void create_blankName_shouldReturn400() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":120,\"stock\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // Break-It #39: price am
    @Test
    void create_negativePrice_shouldReturn400() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"K\",\"price\":-1,\"stock\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // Break-It #40: sai kieu du lieu
    @Test
    void create_wrongType_shouldReturn400() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"K\",\"price\":\"abc\",\"stock\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void create_invalidCategory_shouldReturn404() throws Exception {
        when(service.create(any())).thenThrow(new ResourceNotFoundException("Category not found: x"));

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(service.findById("p1")).thenReturn(response());

        mvc.perform(get("/api/products/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"));
    }

    @Test
    void getById_missing_shouldReturn404() throws Exception {
        when(service.findById("nope")).thenThrow(new ResourceNotFoundException("Product not found: nope"));

        mvc.perform(get("/api/products/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product not found: nope"));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(service.update(eq("p1"), any())).thenReturn(response());

        mvc.perform(put("/api/products/p1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/products/p1"))
                .andExpect(status().isNoContent());
        verify(service).delete("p1");
    }

    @Test
    void delete_missing_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found: nope")).when(service).delete("nope");

        mvc.perform(delete("/api/products/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_shouldReturnPageContract() throws Exception {
        when(service.search(anyString(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageResponse<>(List.of(response()), 0, 5, 1, 1));

        mvc.perform(get("/api/products").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("p1"))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void search_unsupportedSort_shouldReturn400() throws Exception {
        when(service.search(anyString(), any(), anyInt(), anyInt(), eq("password"), anyString()))
                .thenThrow(new IllegalArgumentException("Unsupported sort field: password"));

        mvc.perform(get("/api/products").param("sortBy", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void addReview_shouldReturn200() throws Exception {
        when(service.addReview(eq("p1"), any())).thenReturn(new ProductResponse(
                "p1", "Keyboard", new BigDecimal("120"), 10, "c1",
                List.of(new ReviewResponse("u1", 5, "Great", null))));

        mvc.perform(post("/api/products/p1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"rating\":5,\"comment\":\"Great\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].userId").value("u1"));
    }

    // #75 Reject review rating ngoai 1-5
    @Test
    void addReview_ratingOutOfRange_shouldReturn400() throws Exception {
        mvc.perform(post("/api/products/p1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"rating\":6,\"comment\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
