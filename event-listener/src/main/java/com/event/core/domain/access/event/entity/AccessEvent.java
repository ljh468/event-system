package com.event.core.domain.access.event.entity;

import com.event.core.domain.access.category.entity.AccessCategory;
import com.event.infra.util.DateTimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import static java.util.Objects.isNull;

@Entity
@Table(name = "access_event")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private AccessCategory accessCategory;

  @Setter
  @Column(name = "category_code", nullable = false, unique = true, updatable = false)
  private String categoryCode;

  @Column(name = "user_id", nullable = false, length = 255)
  private String userId;

  @Column(name = "endpoint", nullable = false, length = 255)
  private String endpoint;

  @Column(name = "http_method", nullable = false, length = 10)
  private String httpMethod;

  @Column(name = "response_status", nullable = false)
  private Integer responseStatus;

  @Column(name = "response_time", nullable = false)
  private Double responseTime;

  @Column(name = "ip_address", nullable = false, length = 50)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "inputs")
  private String inputs;

  @Column(name = "outputs")
  private String outputs;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (isNull(createdAt)) {
      createdAt = OffsetDateTime.now();
    }
  }

  public static AccessEvent newEvent(AccessCategory accessCategory,
                                     String userId,
                                     String endpoint,
                                     String httpMethod,
                                     Integer responseStatus,
                                     Double responseTime,
                                     String ipAddress,
                                     String userAgent,
                                     String inputs,
                                     String outputs,
                                     String createdAt
  ) {
    return AccessEvent.builder()
                      .accessCategory(accessCategory)
                      .categoryCode(accessCategory.getCategoryCode())
                      .userId(userId)
                      .endpoint(endpoint)
                      .httpMethod(httpMethod)
                      .responseStatus(responseStatus)
                      .responseTime(responseTime)
                      .ipAddress(ipAddress)
                      .userAgent(userAgent)
                      .inputs(inputs)
                      .outputs(outputs)
                      .createdAt(DateTimeUtils.parseToOffsetDateTime(createdAt))
                      .build();
  }

}