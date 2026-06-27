package com.scholario.reserve.repository;

import com.scholario.reserve.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByBookIdOrderByReservedAtAsc(Long bookId);

    long countByBookId(Long bookId);

    long countByUserId(Long userId);

    long countByStatusType(String statusType);
}
