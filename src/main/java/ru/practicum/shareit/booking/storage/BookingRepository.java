package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBookingsByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findBookingsByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                  LocalDateTime startTime,
                                                                                  LocalDateTime endTime);

    List<Booking> findBookingsByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findBookingsByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findBookingsByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findBookingsByItemOwnerIdOrderByStartDesc(Long bookerId);

    List<Booking> findBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                     LocalDateTime startTime,
                                                                                     LocalDateTime endTime);

    List<Booking> findBookingsByItemOwnerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findBookingsByItemOwnerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime time);

    List<Booking> findBookingsByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    Booking findTopBookingByItemIdAndStatusNotAndStartBeforeOrderByEndDesc(Long itemId, Status status, LocalDateTime now);

    Booking findTopBookingByItemIdAndStatusNotAndStartAfterOrderByStartAsc(Long itemId, Status status, LocalDateTime now);

    Boolean existsBookingByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);
}
