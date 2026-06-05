package com.rootsquare.planmate.service;

import com.rootsquare.planmate.dto.ScheduleItemRequest;
import com.rootsquare.planmate.dto.ScheduleItemResponse;

import java.util.List;

public interface ScheduleItemService {

    List<ScheduleItemResponse> getScheduleItems();

    ScheduleItemResponse getScheduleItem(Long id);

    ScheduleItemResponse createScheduleItem(ScheduleItemRequest request);

    ScheduleItemResponse updateScheduleItem(Long id, ScheduleItemRequest request);

    void deleteScheduleItem(Long id);
}
