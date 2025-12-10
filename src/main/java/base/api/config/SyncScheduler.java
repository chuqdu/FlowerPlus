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

    // Chạy sync category mỗi 5 phút
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void syncCategories() {
        log.info("Starting scheduled category sync...");
        try {
            syncService.syncCategoryBatch();
        } catch (Exception e) {
            log.error("Error in scheduled category sync: {}", e.getMessage());
        }
    }

    // Chạy sync product mỗi 10 phút
    @Scheduled(fixedRate = 600000) // 10 minutes
    public void syncProducts() {
        log.info("Starting scheduled product sync...");
        try {
            syncService.syncProductBatch();
        } catch (Exception e) {
            log.error("Error in scheduled product sync: {}", e.getMessage());
        }
    }
}