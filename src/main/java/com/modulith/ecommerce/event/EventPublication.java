package com.modulith.ecommerce.event;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/***
 * This entity forces the Spring Modulith uses 'TEXT' to field serializedEvent
 * to avoid issues with event serialization size exceeding typical VARCHAR limits.
 */
@Entity
@Table(name = "event_publication")
public class EventPublication {

    @Id
    private UUID id;

    private OffsetDateTime completionDate;

    private String eventType;

    private String listenerId;

    private OffsetDateTime publicationDate;

    @Column(columnDefinition = "TEXT")
    private String serializedEvent;

}