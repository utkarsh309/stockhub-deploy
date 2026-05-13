package com.stockhub.report.scheduler;

import com.stockhub.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ReportService reportService;

    // Run every day at midnight
    // cron = second minute hour day month weekday
    @Scheduled(cron = "0 0 0 * * *")
    public void takeDailySnapshot() {
        log.info("Taking daily inventory snapshot...");
        try {
            // Take snapshot of all stock levels
            reportService.takeSnapshot();
            log.info("Daily snapshot completed!");
        } catch (Exception e) {
            // Log error but dont crash scheduler
            log.error("Snapshot failed: {}",
                    e.getMessage());
        }
    }
}