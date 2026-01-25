package base.api.service.impl;

import base.api.dto.response.RecommendationResponse;
import base.api.service.IRecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService implements IRecommendationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RecommendationResponse getPersonalizedRecommendations(Long userId, Integer limit) {
        try {
            String url = String.format("https://good-fun.org/recommendations/personalized/%d?limit=%d", userId, limit);

            log.info("Calling recommendation API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Recommendation API response: {}", response.getBody());

                JsonNode rootNode = objectMapper.readTree(response.getBody());

                RecommendationResponse recommendationResponse = new RecommendationResponse();
                recommendationResponse.setSuccess(rootNode.has("success") ? rootNode.get("success").asBoolean() : true);
                recommendationResponse.setCount(rootNode.has("count") ? rootNode.get("count").asInt() : 0);
                recommendationResponse
                        .setUserId(rootNode.has("user_id") ? rootNode.get("user_id").asInt() : userId.intValue());

                List<RecommendationResponse.RecommendationItem> items = new ArrayList<>();
                if (rootNode.has("recommendations") && rootNode.get("recommendations").isArray()) {
                    JsonNode recommendationsArray = rootNode.get("recommendations");

                    for (JsonNode item : recommendationsArray) {
                        RecommendationResponse.RecommendationItem recommendationItem = new RecommendationResponse.RecommendationItem();
                        recommendationItem
                                .setFavorite_count(item.has("favorite_count") ? item.get("favorite_count").asInt() : 0);
                        recommendationItem.setImages(item.has("images") ? item.get("images").asText() : "[]");
                        recommendationItem.setName(item.has("name") ? item.get("name").asText() : "");
                        recommendationItem.setPrice(item.has("price") ? item.get("price").asInt() : 0);
                        recommendationItem.setProduct_id(item.has("product_id") ? item.get("product_id").asInt() : 0);
                        recommendationItem.setPurchase_count(
                                item.has("purchase_count") ? item.get("purchase_count").asText() : "0");
                        recommendationItem.setReason(item.has("reason") ? item.get("reason").asText() : "");
                        recommendationItem.setScore(item.has("score") ? item.get("score").asDouble() : 0.0);
                        recommendationItem.setStock(item.has("stock") ? item.get("stock").asInt() : 0);

                        items.add(recommendationItem);
                    }
                }

                recommendationResponse.setRecommendations(items);
                return recommendationResponse;
            } else {
                log.warn("Recommendation API returned non-2xx status: {}", response.getStatusCode());
                return createEmptyResponse(userId);
            }
        } catch (RestClientException e) {
            log.error("Error calling recommendation API: {}", e.getMessage(), e);
            return createEmptyResponse(userId);
        } catch (Exception e) {
            log.error("Unexpected error calling recommendation API: {}", e.getMessage(), e);
            return createEmptyResponse(userId);
        }
    }

    private RecommendationResponse createEmptyResponse(Long userId) {
        RecommendationResponse response = new RecommendationResponse();
        response.setSuccess(false);
        response.setCount(0);
        response.setUserId(userId != null ? userId.intValue() : 0);
        response.setRecommendations(new ArrayList<>());
        return response;
    }
}
