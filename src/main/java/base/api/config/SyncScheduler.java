package base.api.config;

import base.api.service.ISyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "sync.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SyncScheduler {

    private final ISyncService syncService;

    // Chạy sync category mỗi 30 giây
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void syncCategories() {
        log.info("Starting scheduled category sync...");
        try {
            syncService.syncCategoryBatch();
        } catch (Exception e) {
            log.error("Error in scheduled category sync: {}", e.getMessage());
        }
    }

    // Chạy sync product mỗi 30 giây
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void syncProducts() {
        log.info("Starting scheduled product sync...");
        try {
            syncService.syncProductBatch();
        } catch (Exception e) {
            log.error("Error in scheduled product sync: {}", e.getMessage());
        }
    }
}