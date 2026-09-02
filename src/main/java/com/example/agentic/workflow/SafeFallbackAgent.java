package com.example.agentic.workflow;
import org.springframework.stereotype.Component;
import java.util.List;
@Component("fallbackAgentExecutor") public class SafeFallbackAgent implements AgentExecutor {
    public WorkflowModels.AgentResult execute(WorkflowModels.StepDefinition step,WorkflowModels.WorkflowRun run){return new WorkflowModels.AgentResult("Fallback produced a conservative review-only artifact for "+step.agent(),List.of(step.id()+"-fallback-artifact"),List.of("fallback-used","manual-review-required"),List.of("primary-provider-unavailable"),List.of(),"FALLBACK");}
}
