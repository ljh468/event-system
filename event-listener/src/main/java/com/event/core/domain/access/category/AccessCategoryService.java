package com.event.core.domain.access.category;

import com.event.core.domain.access.category.entity.AccessCategory;
import com.event.core.domain.access.category.inout.AccessCategoryDto;
import com.event.core.exception.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccessCategoryService {

  private final AccessCategoryRepository accessCategoryRepository;

  @Autowired
  public AccessCategoryService(AccessCategoryRepository accessCategoryRepository) {
    this.accessCategoryRepository = accessCategoryRepository;
  }

  private boolean existByCategoryCode(String categoryCode) {
    return accessCategoryRepository.existsByCategoryCode(categoryCode);
  }

  private boolean nonExistByCategoryCode(String categoryCode) {
    return !existByCategoryCode(categoryCode);
  }

  public void update(Long id, String name, String description, Boolean isDeleted) {
    AccessCategory accessCategory =
        accessCategoryRepository.findByIdAndIsDeletedFalse(id)
                                .orElseThrow(() -> new DataNotFoundException("AccessCategory not found with id: " + id));

    accessCategory.change(name, description, isDeleted);
  }

  public List<AccessCategoryDto> getAllCategoriesNotDeleted() {
    return accessCategoryRepository.findAllByIsDeletedFalse()
                                   .stream()
                                   .map(AccessCategoryDto::from)
                                   .toList();
  }

  public AccessCategoryDto getByIdNotDeleted(Long id) {
    return accessCategoryRepository.findByIdAndIsDeletedFalse(id)
                                   .map(AccessCategoryDto::from)
                                   .orElseThrow(() -> new DataNotFoundException("AccessCategory not found with id: " + id));
  }

  public AccessCategoryDto getByCategoryCodeNotDeleted(String categoryCode) {
    return accessCategoryRepository.findByCategoryCodeAndIsDeletedFalse(categoryCode)
                                   .map(AccessCategoryDto::from)
                                   .orElseThrow(() -> new DataNotFoundException("AccessCategory not found with code: " + categoryCode));
  }
}
