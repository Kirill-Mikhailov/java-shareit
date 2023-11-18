package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findBookingsByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findBookingsByBookerIdAndStartBeforeAndEndAfter(Long bookerId,
                                                                  LocalDateTime startTime,
                                                                  LocalDateTime endTime, Pageable pageable);

    Page<Booking> findBookingsByBookerIdAndEndBefore(Long bookerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findBookingsByBookerIdAndStartAfter(Long bookerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findBookingsByBookerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerId(Long bookerId, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime startTime,
                                                                     LocalDateTime endTime, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerIdAndEndBefore(Long bookerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerIdAndStartAfter(Long bookerId, LocalDateTime time, Pageable pageable);

    Page<Booking> findBookingsByItemOwnerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    Booking findTopBookingByItemIdAndStatusNotAndStartBeforeOrderByEndDesc(Long itemId, Status status, LocalDateTime now);

    Booking findTopBookingByItemIdAndStatusNotAndStartAfterOrderByStartAsc(Long itemId, Status status, LocalDateTime now);

    Boolean existsBookingByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);
}
