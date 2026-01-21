package base.api.service;

import base.api.dto.response.RecommendationResponse;

public interface IRecommendationService {
    RecommendationResponse getPersonalizedRecommendations(Long userId, Integer limit);
}
