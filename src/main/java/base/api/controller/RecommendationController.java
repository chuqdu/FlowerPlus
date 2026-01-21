package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.dto.response.RecommendationResponse;
import base.api.dto.response.TFUResponse;
import base.api.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController extends BaseAPIController {

    private final IRecommendationService recommendationService;

    @GetMapping("/personalized")
    public ResponseEntity<TFUResponse<RecommendationResponse>> getPersonalizedRecommendations(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                return unauthorized("User not authenticated");
            }
            
            RecommendationResponse response = recommendationService.getPersonalizedRecommendations(userId, limit);
            return success(response, "Recommendations retrieved successfully");
        } catch (Exception e) {
            return badRequest("Không thể tải gợi ý. Vui lòng thử lại sau.");
        }
    }
}
