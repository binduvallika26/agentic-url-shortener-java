package com.example.agentic.workflow;
import com.example.agentic.common.CorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/workflows") public class WorkflowController {
    private final WorkflowOrchestrator orchestrator;
    public WorkflowController(WorkflowOrchestrator orchestrator){this.orchestrator=orchestrator;}
    @PostMapping public WorkflowModels.WorkflowRun start(@RequestBody StartRequest body,HttpServletRequest req){return orchestrator.start(body.scenario().toLowerCase(),body.requirement(),CorrelationFilter.actor(req),CorrelationFilter.id(req));}
    @GetMapping public List<WorkflowModels.WorkflowRun> all(){return orchestrator.all();}
    @GetMapping("/{id}") public WorkflowModels.WorkflowRun get(@PathVariable UUID id){return orchestrator.get(id);}
    @PostMapping("/{id}/advance") public WorkflowModels.WorkflowRun advance(@PathVariable UUID id,HttpServletRequest req){return orchestrator.advance(id,CorrelationFilter.actor(req),CorrelationFilter.id(req));}
    @PostMapping("/{id}/steps/{step}/approve") public WorkflowModels.WorkflowRun approve(@PathVariable UUID id,@PathVariable String step,@RequestBody ReasonRequest body,HttpServletRequest req){return orchestrator.approve(id,step,CorrelationFilter.actor(req),body.reason(),CorrelationFilter.id(req));}
    @PostMapping("/{id}/replan") public WorkflowModels.WorkflowRun replan(@PathVariable UUID id,@RequestBody ReplanRequest body,HttpServletRequest req){return orchestrator.replan(id,body.requirement(),CorrelationFilter.actor(req),CorrelationFilter.id(req));}
    @PostMapping("/{id}/rollback") public WorkflowModels.WorkflowRun rollback(@PathVariable UUID id,@RequestBody ReasonRequest body,HttpServletRequest req){return orchestrator.rollback(id,CorrelationFilter.actor(req),body.reason(),CorrelationFilter.id(req));}
    public record StartRequest(String scenario,String requirement){} public record ReasonRequest(String reason){} public record ReplanRequest(String requirement){}
}
