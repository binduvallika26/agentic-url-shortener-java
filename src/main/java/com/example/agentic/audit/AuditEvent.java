package com.example.agentic.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class AuditEvent {
    @Id private UUID id;
    private Instant occurredAt;
    private String actor;
    private String action;
    private String resource;
    private String outcome;
    private String correlationId;
    @Column(length=4000) private String details;
    protected AuditEvent() {}
    public AuditEvent(String actor,String action,String resource,String outcome,String correlationId,String details){this.id=UUID.randomUUID();this.occurredAt=Instant.now();this.actor=actor;this.action=action;this.resource=resource;this.outcome=outcome;this.correlationId=correlationId;this.details=details;}
    public UUID getId(){return id;} public Instant getOccurredAt(){return occurredAt;} public String getActor(){return actor;} public String getAction(){return action;} public String getResource(){return resource;} public String getOutcome(){return outcome;} public String getCorrelationId(){return correlationId;} public String getDetails(){return details;}
}
