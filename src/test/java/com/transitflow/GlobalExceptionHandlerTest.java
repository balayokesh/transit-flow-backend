package com.transitflow;

import com.transitflow.exception.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.context.annotation.Import;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.TestExceptionController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestExceptionController {
        @GetMapping("/api/test/location-error")
        public void throwLocationNotFound() {
            throw new LocationNotFoundException("Location 'ABC' is not supported");
        }

        @GetMapping("/api/test/illegal-state")
        public void throwIllegalState() {
            throw new IllegalStateException("Service is temporarily in an illegal state");
        }

        @GetMapping("/api/test/type-mismatch/{id}")
        public void typeMismatch(@PathVariable Integer id) {
            // No-op
        }

        @GetMapping("/api/test/missing-param")
        public void missingParam(@RequestParam("requiredParam") String requiredParam) {
            // No-op
        }

        @GetMapping("/api/test/runtime-error")
        public void throwGenericException() {
            throw new RuntimeException("Unexpected catastrophic failure");
        }
    }

    @Test
    void testRouteNotFoundReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/routes/UNKNOWN_ROUTE_999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"))
                .andExpect(jsonPath("$.error").value("Route 'UNKNOWN_ROUTE_999' is not found."))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testValidationFailureReturnsConsistentErrorShape() throws Exception {
        String invalidPayload = """
                {
                    "routeNumber": "",
                    "location": ""
                }
                """;

        mockMvc.perform(post("/api/spottings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testLocationNotFoundReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/test/location-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOCATION_NOT_FOUND"))
                .andExpect(jsonPath("$.error").value("Location 'ABC' is not supported"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testIllegalArgumentReturnsConsistentErrorShape() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/gemini/extract-route").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error").value("Image file must not be empty"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testMalformedJsonReturnsConsistentErrorShape() throws Exception {
        String malformedJson = "{ invalidJson: true, ";

        mockMvc.perform(post("/api/spottings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.error").value("Malformed JSON request body or missing body"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testMethodNotAllowedReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(delete("/api/routes/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.error").value(containsString("Request method 'DELETE' is not supported")))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testUnsupportedMediaTypeReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(post("/api/spottings")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text payload"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.error").value(containsString("Content-Type 'text/plain")))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testNoResourceFoundReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/non-existent-endpoint-path"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error").value(containsString("No static resource api/non-existent-endpoint-path")))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testIllegalStateReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/test/illegal-state"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error").value("Service is temporarily in an illegal state"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testTypeMismatchReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/test/type-mismatch/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.error").value("Parameter 'id' should be of type 'Integer'"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testMissingParameterReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.error").value(containsString("Required request parameter 'requiredParam' for method parameter type String is not present")))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testGenericExceptionReturnsConsistentErrorShape() throws Exception {
        mockMvc.perform(get("/api/test/runtime-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").isString());
    }
}
