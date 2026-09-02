package com.example.agentic.workflow;
import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
@Component public class EnterpriseGuardrailPolicy implements GuardrailPolicy {
    public void validateEntry(WorkflowModels.WorkflowRun run,WorkflowModels.StepDefinition step,String actor){if(step.highImpact()&&(actor==null||actor.isBlank()||actor.equals("anonymous")))throw new DomainException("actor_required","High-impact work requires an identified actor",HttpStatus.FORBIDDEN);}
    public void validateExit(WorkflowModels.StepDefinition step,WorkflowModels.AgentResult result){if(result==null||result.summary()==null||result.summary().isBlank()||result.evidence()==null||result.evidence().isEmpty())throw new IllegalStateException("Agent output failed structured evidence validation");}
}
