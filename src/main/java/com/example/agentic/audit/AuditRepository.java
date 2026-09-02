package com.example.agentic.audit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AuditRepository extends JpaRepository<AuditEvent, UUID> {}
