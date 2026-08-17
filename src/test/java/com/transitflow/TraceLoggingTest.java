package com.transitflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
public class TraceLoggingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSpottingRequestGeneratesTraceIdAndLogsWithMDC(CapturedOutput output) throws Exception {
        String payload = """
                {
                    "routeNumber": "36H",
                    "location": "UKKADAM",
                    "mismatch": true
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/spottings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.spottingId").exists())
                .andExpect(jsonPath("$.alertId").exists())
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();

        // Verify the log output contains the traceId
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("Received spotting submission");
        assertThat(output.getOut()).contains("Processing spotting ID");
        assertThat(output.getOut()).contains("Completed spotting submission");
    }

    @Test
    void testRouteLookupTracesRequest(CapturedOutput output) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.routeNumber").value("1"))
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("Fetching route details for routeNumber='1'");
        assertThat(output.getOut()).contains("Found route: routeNumber='1'");
    }

    @Test
    void testRouteNotFoundTracesRequest(CapturedOutput output) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/routes/UNKNOWN_ROUTE_999"))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"))
                .andExpect(jsonPath("$.error").value("Route 'UNKNOWN_ROUTE_999' is not found."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("Route 'UNKNOWN_ROUTE_999' was not found");
    }

    @Test
    void testAlertsEndpointTracesRequest(CapturedOutput output) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("Fetching all active alerts");
    }

    @Test
    void testValidationFailureLoggedWithTrace(CapturedOutput output) throws Exception {
        String invalidPayload = """
                {
                    "routeNumber": "",
                    "location": ""
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/spottings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        String requestId = result.getResponse().getHeader("X-Request-ID");
        assertThat(requestId).isNotBlank();
        assertThat(output.getOut()).contains(requestId);
        assertThat(output.getOut()).contains("Request validation failed");
    }
}
