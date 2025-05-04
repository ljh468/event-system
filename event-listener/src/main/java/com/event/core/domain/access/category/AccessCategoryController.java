package com.event.core.domain.access.category;

import com.event.core.domain.access.category.inout.AccessCategoryResponse;
import com.event.core.domain.access.category.inout.UpdateAccessCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1/event/access")
public class AccessCategoryController {

  private final AccessCategoryService accessCategoryService;

  @Autowired
  public AccessCategoryController(AccessCategoryService accessCategoryService) {
    this.accessCategoryService = accessCategoryService;
  }

  @PutMapping("/category")
  public ResponseEntity<Void> updateBatchCategory(@RequestBody @Valid UpdateAccessCategoryRequest updateAccessCategoryRequest) {
    accessCategoryService.update(updateAccessCategoryRequest.id(),
                                 updateAccessCategoryRequest.name(),
                                 updateAccessCategoryRequest.description(),
                                 updateAccessCategoryRequest.isDeleted());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/categories")
  public ResponseEntity<List<AccessCategoryResponse>> getAllBatchCategories() {
    List<AccessCategoryResponse> responses = accessCategoryService.getAllCategoriesNotDeleted().stream()
                                                                  .map(AccessCategoryResponse::from)
                                                                  .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/category/{id:[0-9]+}")
  public ResponseEntity<AccessCategoryResponse> getAccessCategory(@PathVariable("id") Long id) {
    AccessCategoryResponse response = AccessCategoryResponse.from(accessCategoryService.getByIdNotDeleted(id));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/category/{categoryCode:[a-zA-Z][a-zA-Z0-9_]*}")
  public ResponseEntity<AccessCategoryResponse> getAccessCategory(@PathVariable("categoryCode") String categoryCode) {
    AccessCategoryResponse response = AccessCategoryResponse.from(accessCategoryService.getByCategoryCodeNotDeleted(categoryCode));
    return ResponseEntity.ok(response);
  }
}
