package com.example.agentic.workflow;
import java.util.*;
public interface WorkflowStore { void save(WorkflowModels.WorkflowRun run); Optional<WorkflowModels.WorkflowRun> find(UUID id); List<WorkflowModels.WorkflowRun> all(); }
