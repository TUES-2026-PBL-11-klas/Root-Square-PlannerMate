package com.rootsquare.planmate.service;

import com.rootsquare.planmate.dto.ScheduleItemRequest;
import com.rootsquare.planmate.dto.ScheduleItemResponse;
import com.rootsquare.planmate.exception.NotFoundException;
import com.rootsquare.planmate.model.ScheduleItem;
import com.rootsquare.planmate.repository.ScheduleItemRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class DatabaseScheduleItemService implements ScheduleItemService {

    private final ScheduleItemRepository scheduleItemRepository;

    public DatabaseScheduleItemService(ScheduleItemRepository scheduleItemRepository) {
        this.scheduleItemRepository = scheduleItemRepository;
    }

    @PostConstruct
    public void loadSampleData() {
        if (scheduleItemRepository.count() > 0) {
            return;
        }

        createScheduleItem(sampleRequest(
                "Math revision",
                "Practice equations and review weak topics.",
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                "Library",
                true,
                true
        ));
        createScheduleItem(sampleRequest(
                "Project planning",
                "Break the schedule module into backend tasks.",
                LocalDate.now().plusDays(2),
                LocalTime.of(16, 30),
                LocalTime.of(18, 0),
                "School lab",
                false,
                true
        ));
        createScheduleItem(sampleRequest(
                "Weekly review",
                "Review completed plans and prepare next week.",
                LocalDate.now().plusDays(5),
                LocalTime.of(17, 0),
                LocalTime.of(17, 45),
                "Home",
                true,
                false
        ));
    }

    @Override
    public List<ScheduleItemResponse> getScheduleItems() {
        return scheduleItemRepository.findAll().stream()
                .sorted(Comparator.comparing(ScheduleItem::getDate).thenComparing(ScheduleItem::getStartTime))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ScheduleItemResponse getScheduleItem(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public ScheduleItemResponse createScheduleItem(ScheduleItemRequest request) {
        ScheduleItem item = toModel(null, request);
        return toResponse(scheduleItemRepository.save(item));
    }

    @Override
    public ScheduleItemResponse updateScheduleItem(Long id, ScheduleItemRequest request) {
        findById(id);
        ScheduleItem updatedItem = toModel(id, request);
        return toResponse(scheduleItemRepository.save(updatedItem));
    }

    @Override
    public void deleteScheduleItem(Long id) {
        findById(id);
        scheduleItemRepository.deleteById(id);
    }

    private ScheduleItem findById(Long id) {
        return scheduleItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule item with id " + id + " was not found"));
    }

    private ScheduleItem toModel(Long id, ScheduleItemRequest request) {
        return new ScheduleItem(
                id,
                request.getTitle().trim(),
                normalize(request.getDescription()),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                normalize(request.getLocation()),
                request.isRepeating(),
                request.isActive()
        );
    }

    private ScheduleItemResponse toResponse(ScheduleItem item) {
        return new ScheduleItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getDate(),
                item.getStartTime(),
                item.getEndTime(),
                item.getLocation(),
                item.isRepeating(),
                item.isActive()
        );
    }

    private ScheduleItemRequest sampleRequest(
            String title,
            String description,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            String location,
            boolean repeating,
            boolean active
    ) {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setDate(date);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setLocation(location);
        request.setRepeating(repeating);
        request.setActive(active);
        return request;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
