package com.example.agentic.workflow;
import java.util.Map;
public interface WorkflowGraphProvider { Map<String,WorkflowModels.StepDefinition> graphFor(String scenario); }
