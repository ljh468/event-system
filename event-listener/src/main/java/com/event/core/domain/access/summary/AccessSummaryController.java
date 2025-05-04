package com.event.core.domain.access.summary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/api/v1/event/access")
public class AccessSummaryController {

  private final AccessSummaryService accessSummaryService;

  @Autowired
  public AccessSummaryController(AccessSummaryService accessSummaryService) {
    this.accessSummaryService = accessSummaryService;
  }

  @GetMapping("/summary/{date:[0-9]{4}-[0-9]{2}-[0-9]{2}}")
  public ResponseEntity<AccessSummary> getAccessSummary(@PathVariable("date") LocalDate date) {
    return ResponseEntity.ok(accessSummaryService.getAccessSummary(date));
  }
}
