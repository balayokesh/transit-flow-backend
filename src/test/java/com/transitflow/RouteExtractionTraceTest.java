package com.transitflow;

import com.transitflow.dto.RouteExtractionResponse;
import com.transitflow.service.GeminiRouteExtractionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
public class RouteExtractionTraceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeminiRouteExtractionService extractionService;

    @Test
    void testRouteExtractionMismatchFlowTracedEndToEnd(CapturedOutput output) throws Exception {
        when(extractionService.extractRoute(any()))
                .thenReturn(new RouteExtractionResponse("96", "36H", "HIGH"));

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "bus-sighting.jpg",
                "image/jpeg",
                "sample-image-data".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/gemini/extract-route").file(file))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.actualRoute").value("36H"))
                .andExpect(jsonPath("$.desiredRoute").value("96"))
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();

        // Verify that logs contain the exact trace/request ID across controller, spotting service, and alert service
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("Starting Gemini OCR for image: filename='bus-sighting.jpg'");
        assertThat(output.getOut()).contains("Route mismatch detected: actual='36H', desired='96'");
        assertThat(output.getOut()).contains("Spotting mismatch detected for spotting ID");
        assertThat(output.getOut()).contains("Persisted alert:");
    }

    @Test
    void testRouteExtractionMatchingRoutesFlow(CapturedOutput output) throws Exception {
        when(extractionService.extractRoute(any()))
                .thenReturn(new RouteExtractionResponse("36H", "36H", "HIGH"));

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "bus-match.png",
                "image/png",
                "sample-png-data".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/gemini/extract-route").file(file))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.actualRoute").value("36H"))
                .andExpect(jsonPath("$.desiredRoute").value("36H"))
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("actualRoute ('36H') matches desiredRoute — no action needed.");
    }
}
