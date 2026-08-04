package com.datasheild.dpbi.service;

import com.datasheild.dpbi.repository.BreachNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final BreachNotificationRepository repository;

    @Scheduled(fixedDelay = 3600000)
    public void sendDeadlineReminders() {
        repository.findByStatusInAndNotificationDueDateBetween(List.of("DRAFT", "REVIEW"), LocalDate.now(), LocalDate.now().plusDays(2))
                .forEach(notification -> log.info("Reminder queued for breach notification {} due on {}", notification.getId(), notification.getNotificationDueDate()));
    }
}
