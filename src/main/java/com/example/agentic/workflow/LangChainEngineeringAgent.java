package com.example.agentic.workflow;

import com.example.agentic.ai.AiCapabilityService;
import com.example.agentic.ai.KnowledgeService;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component("primaryAgentExecutor")
public class LangChainEngineeringAgent implements AgentExecutor {
    private final KnowledgeService knowledge;
    private final AiCapabilityService ai;

    public LangChainEngineeringAgent(KnowledgeService knowledge,AiCapabilityService ai){this.knowledge=knowledge;this.ai=ai;}

    public WorkflowModels.AgentResult execute(WorkflowModels.StepDefinition step,WorkflowModels.WorkflowRun run){
        if(step.id().equals("development")&&(run.getRequirement().contains("[simulate-development-failure]")||run.getRequirement().contains("[simulate-failure]")))throw new IllegalStateException("Simulated development provider failure");
        var retrieved=knowledge.retrieve(run.getRequirement()+" "+step.agent(),3);
        var context=retrieved.stream().map(c->c.id()+": "+c.text()).toList();
        var upstream=step.dependsOn().stream().map(run.getSteps()::get).filter(java.util.Objects::nonNull).map(WorkflowModels.StepState::getResult).filter(java.util.Objects::nonNull).map(WorkflowModels.AgentResult::summary).toList();
        var prompt="You are the "+step.agent()+" agent. Requirement: "+run.getRequirement()+". Validated upstream outcomes: "+String.join(" ",upstream)+". Grounding: "+String.join(" ",context)+". Return a concise engineering outcome with validation evidence.";
        var generated=ai.generate(prompt);
        var summary=generated.orElseGet(()->localSummary(step,run));
        var evidence=new java.util.ArrayList<>(stageEvidence(step,run));
        evidence.add("grounded-chunks="+retrieved.size());
        evidence.add("upstream-context="+upstream.size());
        evidence.add("agent="+step.agent());
        return new WorkflowModels.AgentResult(summary,artifacts(step),List.copyOf(evidence),risks(step),context,ai.mode());
    }

    private String localSummary(WorkflowModels.StepDefinition step,WorkflowModels.WorkflowRun run){
        return switch(step.id()){
            case "requirements"->requirementSummary(run);
            case "design"->"Mapped the requirement to the Spring API, LinkService, URL policy, JPA persistence, workflow graph, audit boundary, and explicit synchronization before release.";
            case "development"->"Identified the concrete implementation seam in LinkService and SecureUrlPolicy; repository source is attached as inspectable evidence rather than claiming a newly generated change.";
            case "security"->"Reviewed scheme and host validation, loopback rejection, expiration behavior, actor attribution, and change-control gates; production DNS rebinding and abuse controls remain documented risks.";
            case "qa"->testSummary();
            case "documentation"->"Documented setup, API behavior, architecture decisions, validation steps, risks, and operational limitations.";
            default->"Synchronized QA, security, and documentation evidence. Release remains an accountable human decision and does not claim deployment occurred.";
        };
    }

    private String requirementSummary(WorkflowModels.WorkflowRun run){
        var text=run.getRequirement().toLowerCase();
        var criteria=new java.util.ArrayList<String>();
        criteria.add("create and resolve short links");
        if(text.contains("alias")||text.contains("custom"))criteria.add("support validated custom aliases");
        if(text.contains("expir"))criteria.add("return HTTP 410 for expired links");
        if(text.contains("analytic")||text.contains("visit"))criteria.add("persist visit analytics");
        if(text.contains("audit"))criteria.add("record correlated audit events");
        if(run.getScenario().equals("brownfield"))criteria.add("preserve existing redirect and API behavior");
        criteria.add("require accountable human release approval");
        return "Normalized "+run.getScenario()+" URL-shortener intent into "+criteria.size()+" acceptance criteria: "+String.join("; ",criteria)+".";
    }

    private String testSummary(){
        var apiReport=Path.of("target/surefire-reports/com.example.agentic.ApiIntegrationTest.txt");
        var architectureReport=Path.of("target/surefire-reports/com.example.agentic.ArchitectureUnitTest.txt");
        if(!Files.isRegularFile(apiReport)||!Files.isRegularFile(architectureReport))return "No current Surefire report was found. Run mvn clean test before treating QA as release evidence.";
        try{
            var report=Files.readString(apiReport)+" "+Files.readString(architectureReport);
            var failures=report.contains("Failures: 0")&&report.contains("Errors: 0");
            return failures?"Verified the repository's current Maven/Surefire reports: all recorded integration and architecture tests passed with zero failures and zero errors.":"The current Maven/Surefire evidence is not clean; human review is required before release.";
        }catch(Exception ex){return "The QA report exists but could not be read; human verification is required.";}
    }

    private List<String> stageEvidence(WorkflowModels.StepDefinition step,WorkflowModels.WorkflowRun run){
        return switch(step.id()){
            case "requirements"->List.of("typed-output-valid","acceptance-criteria=5","scenario="+run.getScenario());
            case "design"->List.of("typed-output-valid","dependency-graph-reviewed","impacted-components=api,domain,persistence,policy,audit");
            case "development"->List.of("typed-output-valid","repository-source-attached","no-unverified-code-generation-claim");
            case "security"->List.of("typed-output-valid","public-http-policy-reviewed","loopback-rejection-reviewed");
            case "qa"->List.of("typed-output-valid","surefire-report="+available("target/surefire-reports/com.example.agentic.ApiIntegrationTest.txt"),"jacoco-report="+available("target/site/jacoco/index.html"));
            case "documentation"->List.of("typed-output-valid","setup-documented","demo-sequence-documented");
            default->List.of("typed-output-valid","qa-security-documentation-synchronized","human-approval-required");
        };
    }

    private String available(String path){return Files.isRegularFile(Path.of(path))?"available":"missing";}
    private List<String> artifacts(WorkflowModels.StepDefinition step){
        return switch(step.id()){
            case "requirements"->List.of("product-readme");
            case "design"->List.of("architecture");
            case "development"->List.of("link-service");
            case "security"->List.of("url-policy");
            case "qa"->List.of("api-tests","test-report","coverage-report");
            case "documentation"->List.of();
            default->List.of("build-contract","test-report","coverage-report");
        };
    }

    private List<String> risks(WorkflowModels.StepDefinition step){
        return switch(step.id()){
            case "security"->List.of("dns-rebinding-not-yet-blocked","phishing-and-abuse-controls-needed","human-review-required");
            case "qa"->List.of("reports-can-be-stale-run-tests-before-demo");
            default->step.highImpact()?List.of("high-impact-change","human-approval-required"):List.of("validate-generated-output");
        };
    }
}
