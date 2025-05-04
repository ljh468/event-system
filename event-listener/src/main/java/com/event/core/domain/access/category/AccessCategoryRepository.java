package com.event.core.domain.access.category;

import com.event.core.domain.access.category.entity.AccessCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessCategoryRepository extends JpaRepository<AccessCategory, Long> {

  boolean existsByCategoryCode(String categoryCode);

  Optional<AccessCategory> findByIdAndIsDeletedFalse(Long id);

  Optional<AccessCategory> findByCategoryCode(String categoryCode);

  Optional<AccessCategory> findByCategoryCodeAndIsDeletedFalse(String categoryCode);

  List<AccessCategory> findAllByIsDeletedFalse();
}
