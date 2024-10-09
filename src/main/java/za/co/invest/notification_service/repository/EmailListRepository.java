package za.co.invest.notification_service.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.invest.notification_service.entity.EmailList;

import java.time.LocalDateTime;

@Repository
public interface EmailListRepository extends JpaRepository<EmailList, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE EmailList e SET e.status = :status, e.processDate = :processDate WHERE e.id = :id")
    int updateEmailListState(@Param("status") String status, @Param("processDate") LocalDateTime processDate, @Param("id") Long id);
}
