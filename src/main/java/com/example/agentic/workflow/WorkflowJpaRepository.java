package com.example.agentic.workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface WorkflowJpaRepository extends JpaRepository<WorkflowEntity,UUID>{}
