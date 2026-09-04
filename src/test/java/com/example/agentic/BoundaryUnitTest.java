package com.example.agentic;

import com.example.agentic.ai.AiCapabilityService;
import com.example.agentic.audit.AuditEvent;
import com.example.agentic.audit.AuditRepository;
import com.example.agentic.audit.AuditService;
import com.example.agentic.common.CorrelationFilter;
import com.example.agentic.links.SecureShortCodeGenerator;
import com.example.agentic.links.SecureUrlPolicy;
import com.example.agentic.workflow.DefaultWorkflowGraphProvider;
import com.example.agentic.workflow.EnterpriseGuardrailPolicy;
import com.example.agentic.workflow.ScenarioRequirementPolicy;
import com.example.agentic.workflow.WorkflowEntity;
import com.example.agentic.workflow.WorkflowModels;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BoundaryUnitTest {
    @Test void generatedCodesAreUrlSafeAndCorrectLength(){
        var code=new SecureShortCodeGenerator().generate("https://example.com");
        assertThat(code).hasSize(8).matches("[A-Za-z0-9]+" );
    }

    @Test void entityAndAuditAccessorsPreservePersistedEvidence(){
        var id=UUID.randomUUID();
        var entity=new WorkflowEntity(id,"greenfield","PENDING","{}");
        assertThat(entity.getId()).isEqualTo(id); assertThat(entity.getScenario()).isEqualTo("greenfield");
        assertThat(entity.getStatus()).isEqualTo("PENDING"); assertThat(entity.getPayload()).isEqualTo("{}"); assertThat(entity.getUpdatedAt()).isNotNull();
        var event=new AuditEvent("reviewer","approve","run/1","success","corr-1","reviewed");
        assertThat(event.getId()).isNotNull(); assertThat(event.getOccurredAt()).isNotNull(); assertThat(event.getActor()).isEqualTo("reviewer");
        assertThat(event.getAction()).isEqualTo("approve"); assertThat(event.getResource()).isEqualTo("run/1"); assertThat(event.getOutcome()).isEqualTo("success");
        assertThat(event.getCorrelationId()).isEqualTo("corr-1"); assertThat(event.getDetails()).isEqualTo("reviewed");
    }

    @Test void guardrailsRejectUnidentifiedActorsAndInvalidOutputs(){
        var policy=new EnterpriseGuardrailPolicy();
        var run=new WorkflowModels.WorkflowRun();
        var highImpact=new WorkflowModels.StepDefinition("security","security",List.of(),false,2,true);
        assertThatThrownBy(()->policy.validateEntry(run,highImpact,"anonymous")).hasMessageContaining("identified actor");
        policy.validateEntry(run,highImpact,"reviewer");
        assertThatThrownBy(()->policy.validateExit(highImpact,null)).isInstanceOf(IllegalStateException.class);
        var empty=new WorkflowModels.AgentResult("",List.of(),List.of(),List.of(),List.of(),"DEMO");
        assertThatThrownBy(()->policy.validateExit(highImpact,empty)).isInstanceOf(IllegalStateException.class);
        policy.validateExit(highImpact,new WorkflowModels.AgentResult("ok",List.of(),List.of("validated"),List.of(),List.of(),"DEMO"));
    }

    @Test void scenarioPolicyCoversAcceptedAndRejectedBoundaries(){
        var policy=new ScenarioRequirementPolicy();
        policy.validate("ambiguous","Make links smart");
        policy.validate("greenfield","Build a new URL shortener");
        policy.validate("brownfield","Enhance the existing URL service");
        assertThatThrownBy(()->policy.validate("brownfield","Create a URL shortener from scratch")).hasMessageContaining("Select Greenfield");
        assertThatThrownBy(()->policy.validate("greenfield","Fix existing URL redirects")).hasMessageContaining("Select Brownfield");
        assertThatThrownBy(()->policy.validate("greenfield","Build a snake game")).hasMessageContaining("URL-shortener product");
    }

    @Test void URLPolicyRejectsMalformedLocalAndMetadataHosts(){
        var policy=new SecureUrlPolicy();
        assertThatThrownBy(()->policy.validate("not a url")).hasMessageContaining("HTTP(S)");
        assertThatThrownBy(()->policy.validate("ftp://example.com/file")).hasMessageContaining("HTTP(S)");
        assertThatThrownBy(()->policy.validate("http://service.local/path")).hasMessageContaining("HTTP(S)");
        assertThatThrownBy(()->policy.validate("http://224.0.0.1/path")).hasMessageContaining("HTTP(S)");
        assertThatThrownBy(()->policy.validate("http://999.999.999.999/path")).hasMessageContaining("HTTP(S)");
        assertThat(policy.validate("https://example.com/path").getHost()).isEqualTo("example.com");
    }

    @Test void correlationFilterPreservesSuppliedIdAndGeneratesMissingId() throws Exception {
        var filter=new CorrelationFilter();
        var supplied=new MockHttpServletRequest(); supplied.addHeader("X-Correlation-ID","corr-fixed"); supplied.addHeader("X-Actor","reviewer");
        var suppliedResponse=new MockHttpServletResponse(); filter.doFilter(supplied,suppliedResponse,new MockFilterChain());
        assertThat(suppliedResponse.getHeader("X-Correlation-ID")).isEqualTo("corr-fixed"); assertThat(CorrelationFilter.actor(supplied)).isEqualTo("reviewer");
        var generated=new MockHttpServletRequest(); var generatedResponse=new MockHttpServletResponse(); filter.doFilter(generated,generatedResponse,new MockFilterChain());
        assertThat(generatedResponse.getHeader("X-Correlation-ID")).isNotBlank(); assertThat(CorrelationFilter.actor(generated)).isEqualTo("anonymous"); assertThat(CorrelationFilter.id(generated)).isNotBlank();
    }

    @Test void disabledAiAndAuditServiceExposeDeterministicBehavior(){
        var disabled=new AiCapabilityService(true,"test-model","");
        assertThat(disabled.connected()).isFalse(); assertThat(disabled.mode()).isEqualTo("DEMO"); assertThat(disabled.model()).isEqualTo("deterministic-local"); assertThat(disabled.generate("prompt")).isEmpty();
        var repository=mock(AuditRepository.class); when(repository.findAll(any(Sort.class))).thenReturn(List.of());
        var service=new AuditService(repository); service.record("a","b","c","success","d","e"); assertThat(service.recent()).isEmpty();
        verify(repository).save(any(AuditEvent.class)); verify(repository).findAll(any(Sort.class));
    }

    @Test void invalidGraphScenarioIsRejected(){
        assertThatThrownBy(()->new DefaultWorkflowGraphProvider().graphFor("unsupported")).hasMessageContaining("greenfield, brownfield, or ambiguous");
    }
}
