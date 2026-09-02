package com.example.agentic.workflow;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity public class WorkflowEntity {
    @Id private UUID id; private String scenario; private String status; private Instant updatedAt; @Lob @Column(columnDefinition="CLOB") private String payload;
    protected WorkflowEntity(){} public WorkflowEntity(UUID id,String scenario,String status,String payload){this.id=id;this.scenario=scenario;this.status=status;this.payload=payload;this.updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getScenario(){return scenario;} public String getStatus(){return status;} public Instant getUpdatedAt(){return updatedAt;} public String getPayload(){return payload;}
}
