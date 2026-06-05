package com.rootsquare.planyourday.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.rootsquare.planyourday.model.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
}