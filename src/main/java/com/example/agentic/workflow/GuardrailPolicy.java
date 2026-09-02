package com.example.agentic.workflow;
public interface GuardrailPolicy { void validateEntry(WorkflowModels.WorkflowRun run,WorkflowModels.StepDefinition step,String actor); void validateExit(WorkflowModels.StepDefinition step,WorkflowModels.AgentResult result); }
