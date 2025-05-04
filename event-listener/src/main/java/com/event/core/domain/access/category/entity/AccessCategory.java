package com.event.core.domain.access.category.entity;

import com.event.core.domain.access.event.entity.AccessEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "access_category")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "category_code", nullable = false, updatable = false)
  private String categoryCode;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;

  @Builder.Default
  @OneToMany(mappedBy = "accessCategory", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccessEvent> accessEvents = new ArrayList<>();

  // 편의 메서드: AccessEvent 추가 시 양방향 관계 설정
  public void addAccessEvent(AccessEvent accessEvent) {
    this.accessEvents.add(accessEvent);
    accessEvent.setAccessCategory(this);
  }

  public void removeAccessEvent(AccessEvent accessEvent) {
    this.accessEvents.remove(accessEvent);
    accessEvent.setAccessCategory(null);
  }

  @PrePersist
  public void prePersist() {
    createdAt = OffsetDateTime.now();
    isDeleted = false;
  }

  public void change(String name, String description, Boolean isDeleted) {
    if (nonNull(name)) {
      this.name = name;
    }
    if (nonNull(description)) {
      this.description = description;
    }
    if (nonNull(isDeleted)) {
      this.isDeleted = isDeleted;
    }
    this.updatedAt = OffsetDateTime.now();
  }

  public void softDelete() {
    this.updatedAt = OffsetDateTime.now();
    this.isDeleted = true;
  }

  @Override
  public String toString() {
    return "AccessCategory{" +
        "id=" + id +
        ", categoryCode='" + categoryCode + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", isDeleted=" + isDeleted +
        ", accessEvents=" + accessEvents +
        '}';
  }
}