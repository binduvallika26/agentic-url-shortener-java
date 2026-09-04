package com.example.agentic.workflow;

import java.time.Instant;
import java.util.*;

public final class WorkflowModels {
    private WorkflowModels() {}
    public enum RunStatus { PENDING,RUNNING,AWAITING_APPROVAL,SUCCEEDED,SAFE_STOPPED,ROLLED_BACK }
    public enum StepStatus { PENDING,RUNNING,AWAITING_APPROVAL,SUCCEEDED,FAILED,ROLLED_BACK }
    public record StepDefinition(String id,String agent,List<String> dependsOn,boolean requiresApproval,int maxAttempts,boolean highImpact){}
    public record Decision(Instant at,String kind,String subject,String reason,String actor){}
    public record AgentResult(String summary,List<String> artifacts,List<String> evidence,List<String> risks,List<String> retrievedContext,String mode){}
    public static final class StepState {
        private String id; private StepStatus status=StepStatus.PENDING; private int attempts; private boolean fallbackUsed; private AgentResult result; private String error;
        public StepState(){} public StepState(String id){this.id=id;}
        public String getId(){return id;} public void setId(String id){this.id=id;} public StepStatus getStatus(){return status;} public void setStatus(StepStatus status){this.status=status;} public int getAttempts(){return attempts;} public void setAttempts(int attempts){this.attempts=attempts;} public boolean isFallbackUsed(){return fallbackUsed;} public void setFallbackUsed(boolean value){fallbackUsed=value;} public AgentResult getResult(){return result;} public void setResult(AgentResult result){this.result=result;} public String getError(){return error;} public void setError(String error){this.error=error;}
    }
    public static final class WorkflowRun {
        private UUID id=UUID.randomUUID(); private String scenario; private String requirement; private int revision=1; private RunStatus status=RunStatus.PENDING; private Map<String,StepDefinition> graph=new LinkedHashMap<>(); private Map<String,StepState> steps=new LinkedHashMap<>(); private List<Decision> decisions=new ArrayList<>(); private Set<String> approvals=new HashSet<>(); private Instant startedAt=Instant.now(); private Instant completedAt; private Instant lastFailureAt; private Instant recoveredAt; private String finalSummary;
        public WorkflowRun(){}
        public UUID getId(){return id;} public void setId(UUID id){this.id=id;} public String getScenario(){return scenario;} public void setScenario(String s){scenario=s;} public String getRequirement(){return requirement;} public void setRequirement(String r){requirement=r;} public int getRevision(){return revision;} public void setRevision(int r){revision=r;} public RunStatus getStatus(){return status;} public void setStatus(RunStatus s){status=s;} public Map<String,StepDefinition> getGraph(){return graph;} public void setGraph(Map<String,StepDefinition> g){graph=g;} public Map<String,StepState> getSteps(){return steps;} public void setSteps(Map<String,StepState> s){steps=s;} public List<Decision> getDecisions(){return decisions;} public void setDecisions(List<Decision> d){decisions=d;} public Set<String> getApprovals(){return approvals;} public void setApprovals(Set<String> a){approvals=a;} public Instant getStartedAt(){return startedAt;} public void setStartedAt(Instant v){startedAt=v;} public Instant getCompletedAt(){return completedAt;} public void setCompletedAt(Instant v){completedAt=v;} public Instant getLastFailureAt(){return lastFailureAt;} public void setLastFailureAt(Instant v){lastFailureAt=v;} public Instant getRecoveredAt(){return recoveredAt;} public void setRecoveredAt(Instant v){recoveredAt=v;}
        public String getFinalSummary(){return finalSummary;} public void setFinalSummary(String value){finalSummary=value;}
    }
}
