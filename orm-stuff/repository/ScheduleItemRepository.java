package com.rootsquare.planyourday.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rootsquare.planyourday.model.ScheduleItem;

public interface ScheduleItemRepository extends JpaRepository<SheduleItem, Integer> {
}