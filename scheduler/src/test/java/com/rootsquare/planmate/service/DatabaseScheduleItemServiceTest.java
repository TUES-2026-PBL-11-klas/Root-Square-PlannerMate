package com.rootsquare.planmate.service;

import com.rootsquare.planmate.dto.ScheduleItemRequest;
import com.rootsquare.planmate.dto.ScheduleItemResponse;
import com.rootsquare.planmate.exception.NotFoundException;
import com.rootsquare.planmate.model.ScheduleItem;
import com.rootsquare.planmate.repository.ScheduleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseScheduleItemServiceTest {

    @Mock
    private ScheduleItemRepository repository;

    @InjectMocks
    private DatabaseScheduleItemService service;

    private ScheduleItem sampleItem;
    private ScheduleItemRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleItem = new ScheduleItem(
                1L,
                "Math revision",
                "Practice equations",
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                "Library",
                false,
                true
        );

        sampleRequest = new ScheduleItemRequest();
        sampleRequest.setTitle("Math revision");
        sampleRequest.setDescription("Practice equations");
        sampleRequest.setDate(LocalDate.of(2026, 6, 10));
        sampleRequest.setStartTime(LocalTime.of(9, 0));
        sampleRequest.setEndTime(LocalTime.of(10, 30));
        sampleRequest.setLocation("Library");
        sampleRequest.setRepeating(false);
        sampleRequest.setActive(true);
    }

    // ── getScheduleItems ──────────────────────────────────────────────────────

    @Test
    void getScheduleItems_returnsAllItemsSortedByDateAndTime() {
        ScheduleItem later = new ScheduleItem(
                2L, "Physics", "desc",
                LocalDate.of(2026, 6, 10), LocalTime.of(11, 0), LocalTime.of(12, 0),
                null, false, true
        );
        ScheduleItem earlier = new ScheduleItem(
                3L, "Chemistry", "desc",
                LocalDate.of(2026, 6, 9), LocalTime.of(8, 0), LocalTime.of(9, 0),
                null, false, true
        );
        when(repository.findAll()).thenReturn(List.of(later, earlier));

        List<ScheduleItemResponse> result = service.getScheduleItems();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Chemistry");  // earlier date first
        assertThat(result.get(1).getTitle()).isEqualTo("Physics");
    }

    @Test
    void getScheduleItems_returnsEmptyListWhenNoItems() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.getScheduleItems()).isEmpty();
    }

    // ── getScheduleItem ───────────────────────────────────────────────────────

    @Test
    void getScheduleItem_returnsItemWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));

        ScheduleItemResponse response = service.getScheduleItem(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Math revision");
        assertThat(response.getDescription()).isEqualTo("Practice equations");
        assertThat(response.getDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(response.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.getEndTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(response.getLocation()).isEqualTo("Library");
        assertThat(response.isActive()).isTrue();
        assertThat(response.isRepeating()).isFalse();
    }

    @Test
    void getScheduleItem_throwsNotFoundExceptionWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getScheduleItem(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createScheduleItem ────────────────────────────────────────────────────

    @Test
    void createScheduleItem_savesAndReturnsResponse() {
        when(repository.save(any(ScheduleItem.class))).thenReturn(sampleItem);

        ScheduleItemResponse response = service.createScheduleItem(sampleRequest);

        assertThat(response.getTitle()).isEqualTo("Math revision");
        verify(repository, times(1)).save(any(ScheduleItem.class));
    }

    @Test
    void createScheduleItem_trimsTitleWhitespace() {
        sampleRequest.setTitle("  Math revision  ");
        when(repository.save(any(ScheduleItem.class))).thenAnswer(inv -> {
            ScheduleItem saved = inv.getArgument(0);
            assertThat(saved.getTitle()).isEqualTo("Math revision");
            return sampleItem;
        });

        service.createScheduleItem(sampleRequest);
    }

    @Test
    void createScheduleItem_normalizesBlankDescriptionToEmptyString() {
        sampleRequest.setDescription("   ");
        when(repository.save(any(ScheduleItem.class))).thenAnswer(inv -> {
            ScheduleItem saved = inv.getArgument(0);
            assertThat(saved.getDescription()).isEqualTo("");
            return sampleItem;
        });

        service.createScheduleItem(sampleRequest);
    }

    @Test
    void createScheduleItem_normalizesNullLocationToEmptyString() {
        sampleRequest.setLocation(null);
        when(repository.save(any(ScheduleItem.class))).thenAnswer(inv -> {
            ScheduleItem saved = inv.getArgument(0);
            assertThat(saved.getLocation()).isEqualTo("");
            return sampleItem;
        });

        service.createScheduleItem(sampleRequest);
    }

    // ── updateScheduleItem ────────────────────────────────────────────────────

    @Test
    void updateScheduleItem_updatesAndReturnsResponse() {
        ScheduleItem updated = new ScheduleItem(
                1L, "Updated title", "Updated desc",
                LocalDate.of(2026, 6, 11), LocalTime.of(10, 0), LocalTime.of(11, 0),
                "Room 2", true, false
        );
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(repository.save(any(ScheduleItem.class))).thenReturn(updated);

        sampleRequest.setTitle("Updated title");
        ScheduleItemResponse response = service.updateScheduleItem(1L, sampleRequest);

        assertThat(response.getTitle()).isEqualTo("Updated title");
        verify(repository).findById(1L);
        verify(repository).save(any(ScheduleItem.class));
    }

    @Test
    void updateScheduleItem_throwsNotFoundForMissingId() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateScheduleItem(42L, sampleRequest))
                .isInstanceOf(NotFoundException.class);
    }

    // ── deleteScheduleItem ────────────────────────────────────────────────────

    @Test
    void deleteScheduleItem_deletesWhenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));

        service.deleteScheduleItem(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteScheduleItem_throwsNotFoundForMissingId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteScheduleItem(99L))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    // ── loadSampleData ────────────────────────────────────────────────────────

    @Test
    void loadSampleData_doesNothingWhenDataExists() {
        when(repository.count()).thenReturn(3L);

        service.loadSampleData();

        verify(repository, never()).save(any());
    }

    @Test
    void loadSampleData_seedsThreeItemsWhenRepositoryIsEmpty() {
        when(repository.count()).thenReturn(0L);
        when(repository.save(any(ScheduleItem.class))).thenAnswer(inv -> {
            ScheduleItem arg = inv.getArgument(0);
            return new ScheduleItem(1L, arg.getTitle(), arg.getDescription(),
                    arg.getDate(), arg.getStartTime(), arg.getEndTime(),
                    arg.getLocation(), arg.isRepeating(), arg.isActive());
        });

        service.loadSampleData();

        verify(repository, times(3)).save(any(ScheduleItem.class));
    }
}