package com.example.agentic.workflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.*;
@Component public class JpaWorkflowStore implements WorkflowStore {
    private final WorkflowJpaRepository repository; private final ObjectMapper json;
    public JpaWorkflowStore(WorkflowJpaRepository repository,ObjectMapper json){this.repository=repository;this.json=json;}
    public void save(WorkflowModels.WorkflowRun run){try{repository.save(new WorkflowEntity(run.getId(),run.getScenario(),run.getStatus().name(),json.writeValueAsString(run)));}catch(Exception ex){throw new IllegalStateException("Could not persist workflow",ex);}}
    public Optional<WorkflowModels.WorkflowRun> find(UUID id){return repository.findById(id).map(this::decode);}
    public List<WorkflowModels.WorkflowRun> all(){return repository.findAll().stream().map(this::decode).toList();}
    private WorkflowModels.WorkflowRun decode(WorkflowEntity entity){try{return json.readValue(entity.getPayload(),WorkflowModels.WorkflowRun.class);}catch(Exception ex){throw new IllegalStateException("Could not read workflow",ex);}}
}
