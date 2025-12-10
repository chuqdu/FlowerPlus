package base.api.controller;

import base.api.base.BaseAPIController;
import base.api.service.ISyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController extends BaseAPIController {

    private final ISyncService syncService;

    @PostMapping("/categories")
    public ResponseEntity<?> syncCategories() {
        try {
            syncService.syncCategoryBatch();
            return success("Category sync started successfully");
        } catch (Exception e) {
            return badRequest("Failed to start category sync: " + e.getMessage());
        }
    }

    @PostMapping("/products")
    public ResponseEntity<?> syncProducts() {
        try {
            syncService.syncProductBatch();
            return success("Product sync started successfully");
        } catch (Exception e) {
            return badRequest("Failed to start product sync: " + e.getMessage());
        }
    }

    @PostMapping("/all")
    public ResponseEntity<?> syncAll() {
        try {
            syncService.syncCategoryBatch();
            syncService.syncProductBatch();
            return success("Full sync started successfully");
        } catch (Exception e) {
            return badRequest("Failed to start full sync: " + e.getMessage());
        }
    }

    @PostMapping("/products/{id}/generate-string")
    public ResponseEntity<?> generateProductString(@PathVariable Long id) {
        try {
            String productString = syncService.generateProductString(id);
            return success(productString);
        } catch (Exception e) {
            return badRequest("Failed to generate product string: " + e.getMessage());
        }
    }
}