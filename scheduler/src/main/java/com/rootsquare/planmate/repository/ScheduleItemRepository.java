package com.rootsquare.planmate.repository;

import com.rootsquare.planmate.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
}
