package com.fitness.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class UsdaClient {

    private final RestTemplate restTemplate;
    private final String apiKey;

    private static final String SEARCH_URL = "https://api.nal.usda.gov/fdc/v1/foods/search";
    private static final String FOOD_URL = "https://api.nal.usda.gov/fdc/v1/food/{fdcId}";

    public UsdaClient(@Value("${usda.api-key:}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !"DEMO_KEY".equals(apiKey);
    }

    public List<UsdaFoodSummary> searchFoods(String query, int pageSize) {
        if (!isConfigured()) {
            log.debug("USDA API key not configured, skipping USDA search");
            return Collections.emptyList();
        }
        try {
            String url = SEARCH_URL + "?api_key=" + apiKey + "&query=" + query
                    + "&pageSize=" + pageSize + "&dataType=Foundation,SR%20Legacy";
            ResponseEntity<JsonNode> resp = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode body = resp.getBody();
            if (body == null || !body.has("foods")) return Collections.emptyList();

            List<UsdaFoodSummary> results = new ArrayList<>();
            for (JsonNode food : body.get("foods")) {
                UsdaFoodSummary item = new UsdaFoodSummary();
                item.setFdcId(food.get("fdcId").asInt());
                item.setDescription(food.get("description").asText());

                if (food.has("foodNutrients")) {
                    for (JsonNode n : food.get("foodNutrients")) {
                        int nutrientId = n.get("nutrientId").asInt();
                        BigDecimal value = new BigDecimal(n.get("value").asText());
                        switch (nutrientId) {
                            case 1008 -> item.setCalories(value);  // Energy (kcal)
                            case 1003 -> item.setProtein(value);   // Protein
                            case 1004 -> item.setFat(value);       // Total Fat
                            case 1005 -> item.setCarbs(value);     // Carbs
                        }
                    }
                }
                results.add(item);
            }
            log.debug("USDA search '{}' returned {} results", query, results.size());
            return results;
        } catch (Exception e) {
            log.warn("USDA API search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public UsdaFoodDetail getFoodDetail(int fdcId) {
        if (!isConfigured()) return null;
        try {
            String url = FOOD_URL + "?api_key=" + apiKey + "&nutrients=1008,1003,1004,1005";
            ResponseEntity<JsonNode> resp = restTemplate.getForEntity(url, JsonNode.class, fdcId);
            JsonNode body = resp.getBody();
            if (body == null) return null;

            UsdaFoodDetail detail = new UsdaFoodDetail();
            detail.setFdcId(body.get("fdcId").asInt());
            detail.setDescription(body.get("description").asText());

            if (body.has("foodNutrients")) {
                for (JsonNode n : body.get("foodNutrients")) {
                    int nutrientId = n.get("nutrientId").asInt();
                    BigDecimal value = new BigDecimal(n.get("value").asText());
                    switch (nutrientId) {
                        case 1008 -> detail.setCalories(value);
                        case 1003 -> detail.setProtein(value);
                        case 1004 -> detail.setFat(value);
                        case 1005 -> detail.setCarbs(value);
                    }
                }
            }
            return detail;
        } catch (Exception e) {
            log.warn("USDA food detail failed for fdcId={}: {}", fdcId, e.getMessage());
            return null;
        }
    }

    @Data
    public static class UsdaFoodSummary {
        private int fdcId;
        private String description;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
    }

    @Data
    public static class UsdaFoodDetail {
        private int fdcId;
        private String description;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
    }
}
