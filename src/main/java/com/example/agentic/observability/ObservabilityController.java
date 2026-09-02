package com.example.agentic.observability;
import com.example.agentic.ai.*;
import com.example.agentic.audit.*;
import com.example.agentic.workflow.*;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.*;
@RestController @RequestMapping("/api") public class ObservabilityController {
    private final AuditService audit; private final WorkflowOrchestrator workflows; private final AiCapabilityService ai; private final KnowledgeService knowledge;
    public ObservabilityController(AuditService audit,WorkflowOrchestrator workflows,AiCapabilityService ai,KnowledgeService knowledge){this.audit=audit;this.workflows=workflows;this.ai=ai;this.knowledge=knowledge;}
    @GetMapping("/audit") public List<AuditEvent> audit(){return audit.recent();}
    @GetMapping("/capabilities") public Map<String,Object> capabilities(){return Map.of("language","Java 21","framework","Spring Boot","llm",Map.of("connected",ai.connected(),"mode",ai.mode(),"model",ai.model(),"provider","LangChain4j"),"rag",Map.of("enabled",true,"strategy","local grounded retrieval","indexedChunks",knowledge.indexedCount()),"persistence","H2 / JPA","humanApproval",true,"fallback",true);}
    @GetMapping("/knowledge") public List<KnowledgeService.KnowledgeChunk> knowledge(@RequestParam(defaultValue="secure agentic software delivery") String query){return knowledge.retrieve(query,5);}
    @GetMapping("/metrics") public Map<String,Object> metrics(){var runs=workflows.all();var total=runs.size();var succeeded=runs.stream().filter(r->r.getStatus()==WorkflowModels.RunStatus.SUCCEEDED).count();var retries=runs.stream().flatMap(r->r.getSteps().values().stream()).mapToInt(s->Math.max(0,s.getAttempts()-1)).sum();var fallbacks=runs.stream().flatMap(r->r.getSteps().values().stream()).filter(WorkflowModels.StepState::isFallbackUsed).count();var rollbacks=runs.stream().filter(r->r.getStatus()==WorkflowModels.RunStatus.ROLLED_BACK).count();var latency=runs.stream().filter(r->r.getCompletedAt()!=null).mapToLong(r->Duration.between(r.getStartedAt(),r.getCompletedAt()).toMillis()).average().orElse(0);return Map.of("totalRuns",total,"successRate",total==0?0:(double)succeeded/total,"retries",retries,"fallbacks",fallbacks,"rollbackFrequency",total==0?0:(double)rollbacks/total,"meanEndToEndLatencyMs",latency);}
}
