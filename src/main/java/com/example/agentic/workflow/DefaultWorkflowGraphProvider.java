package com.example.agentic.workflow;
import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.util.*;
@Component public class DefaultWorkflowGraphProvider implements WorkflowGraphProvider {
    public Map<String,WorkflowModels.StepDefinition> graphFor(String scenario){if(!Set.of("greenfield","brownfield","ambiguous").contains(scenario))throw new DomainException("invalid_scenario","Scenario must be greenfield, brownfield, or ambiguous",HttpStatus.BAD_REQUEST);var graph=new LinkedHashMap<String,WorkflowModels.StepDefinition>();graph.put("requirements",step("requirements","requirements",List.of(),scenario.equals("ambiguous"),false));graph.put("design",step("design","architecture",List.of("requirements"),false,false));graph.put("development",step("development","development",List.of("design"),false,scenario.equals("brownfield")));graph.put("security",step("security","security-review",List.of("design"),false,true));graph.put("qa",step("qa","qa",List.of("development"),false,false));graph.put("documentation",step("documentation","documentation",List.of("design"),false,false));graph.put("release",step("release","release-readiness",List.of("qa","security","documentation"),true,true));return graph;}
    private WorkflowModels.StepDefinition step(String id,String agent,List<String> deps,boolean approval,boolean highImpact){return new WorkflowModels.StepDefinition(id,agent,deps,approval,2,highImpact);}
}
