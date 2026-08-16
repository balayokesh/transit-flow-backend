package com.transitflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transitflow.dto.RouteExtractionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class GeminiRouteExtractionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiRouteExtractionService.class);

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public GeminiRouteExtractionService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public RouteExtractionResponse extractRoute(MultipartFile image) throws IOException {
        log.info("Preparing Gemini vision prompt for image '{}'", image.getOriginalFilename());
        String prompt = "You are a helpful AI that extracts bus route information from images. " +
                "Examine the attached image and extract the route numbers. " +
                "Return exactly a JSON object using these specific keys: " +
                "'desiredRoute' (the route number explicitly mentioned in the red circular windshield sticker), " +
                "'actualRoute' (the route number explicitly mentioned in the digital LED display of the bus), and 'confidence'. "
                +
                "Format exactly like this: " +
                "{\"desiredRoute\": \"96\", \"actualRoute\": \"36H\", \"confidence\": \"HIGH\"}. " +
                "If the red circular sticker is missing or unreadable, set 'desiredRoute' to null. " +
                "If the LED display is missing, off, or unreadable, set 'actualRoute' to null. " +
                "If the image does not contain any identifiable bus routes, return: " +
                "{\"desiredRoute\": null, \"actualRoute\": null, \"confidence\": \"LOW\"}. " +
                "Do not include markdown, code blocks, or any other text in the response. Return only the raw JSON.";

        UserMessage userMessage = new UserMessage(prompt, List.of(new Media(
                MimeTypeUtils.parseMimeType(Objects.requireNonNullElse(image.getContentType(), "image/jpeg")),
                new ByteArrayResource(image.getBytes()))));

        log.debug("Sending chat prompt to Gemini API");
        String response = chatClient.prompt()
                .messages(userMessage)
                .call()
                .content();

        log.debug("Received raw response from Gemini API: {}", response);
        return parseResponse(response);
    }

    private RouteExtractionResponse parseResponse(String response) {
        if (response == null || response.isBlank()) {
            log.error("Empty response received from Gemini Vision API");
            throw new IllegalStateException("Received an empty response from Gemini Vision API.");
        }

        JsonNode jsonNode;
        try {
            // Clean up possible markdown code blocks if the AI still included them
            String cleanedResponse = response.replaceAll("```json", "").replaceAll("```", "").trim();
            jsonNode = objectMapper.readTree(cleanedResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response from Gemini Vision API: {}", response, e);
            throw new IllegalStateException("Failed to parse the response from Gemini Vision API.", e);
        }

        String desiredRoute = jsonNode.hasNonNull("desiredRoute") ? jsonNode.get("desiredRoute").asText() : null;
        String actualRoute = jsonNode.hasNonNull("actualRoute") ? jsonNode.get("actualRoute").asText() : null;
        String confidence = jsonNode.hasNonNull("confidence") ? jsonNode.get("confidence").asText() : "LOW";

        boolean isDesiredRouteEmpty = desiredRoute == null || desiredRoute.isEmpty() || desiredRoute.equals("null");
        boolean isActualRouteEmpty = actualRoute == null || actualRoute.isEmpty() || actualRoute.equals("null");

        if (isDesiredRouteEmpty && isActualRouteEmpty) {
            log.warn("No visible bus routes identified in Gemini Vision response");
            throw new IllegalStateException("Unable to identify any clearly visible bus routes in the image.");
        }

        return new RouteExtractionResponse(desiredRoute, actualRoute, confidence);
    }
}
