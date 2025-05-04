package com.event.core.domain.access.event;

import com.event.core.domain.access.event.entity.AccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AccessEventRepository extends JpaRepository<AccessEvent, Long> {

  List<AccessEvent> findByCreatedAtBetween(OffsetDateTime startDateTime,
                                           OffsetDateTime endDateTime);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM AccessEvent al WHERE al.createdAt < :threshold")
  int deleteByCreatedAtBefore(OffsetDateTime threshold);

  @Transactional
  @Modifying(clearAutomatically = false)
  @Query("UPDATE AccessEvent a SET a.categoryCode = :newCategoryCode WHERE a.accessCategory.id = :categoryId")
  int bulkUpdateCategoryCode(String newCategoryCode, Long categoryId);
}
