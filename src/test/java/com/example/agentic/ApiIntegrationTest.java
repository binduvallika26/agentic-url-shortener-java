package com.example.agentic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.agentic.links.ShortLink;
import com.example.agentic.links.ShortLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired ShortLinkRepository links;
    @Test void createsAndRetrievesPersistentLink() throws Exception {
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).header("X-Actor","qa-agent").content(json.writeValueAsString(Map.of("url","https://example.com/docs","customCode","testlink")))).andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("testlink"));
        mvc.perform(get("/api/links/testlink")).andExpect(status().isOk()).andExpect(jsonPath("$.targetUrl").value("https://example.com/docs"));
        mvc.perform(get("/testlink")).andExpect(status().isFound()).andExpect(header().string("Location","https://example.com/docs"));
        mvc.perform(get("/api/links/testlink")).andExpect(status().isOk()).andExpect(jsonPath("$.visits").value(1));
    }
    @Test void rejectsUnsafeUrl() throws Exception { mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"file:///secret\",\"customCode\":\"unsafe1\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("invalid_url")); }
    @Test void greenfieldSynchronizesAtHumanReleaseGate() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","candidate").content("{\"scenario\":\"greenfield\",\"requirement\":\"Build a secure URL shortener\"}")).andExpect(status().isOk()).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        var advanced=mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AWAITING_APPROVAL")).andExpect(jsonPath("$.steps.qa.status").value("SUCCEEDED")).andExpect(jsonPath("$.steps.documentation.status").value("SUCCEEDED")).andReturn();
        assertThat(advanced.getResponse().getContentAsString()).contains("grounded-chunks=3").contains("upstream-context=1");
    }
    @Test void ambiguousRequirementStopsBeforeAgentExecution() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","candidate").content("{\"scenario\":\"ambiguous\",\"requirement\":\"Make links smart\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","candidate")).andExpect(jsonPath("$.status").value("AWAITING_APPROVAL")).andExpect(jsonPath("$.steps.requirements.status").value("AWAITING_APPROVAL"));
    }
    @Test void primaryFailureUsesFallback() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","candidate").content("{\"scenario\":\"greenfield\",\"requirement\":\"Modernize the URL service safely [simulate-development-failure]\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","identified-agent")).andExpect(status().isOk()).andExpect(jsonPath("$.steps.requirements.fallbackUsed").value(false)).andExpect(jsonPath("$.steps.development.fallbackUsed").value(true)).andExpect(jsonPath("$.steps.development.attempts").value(2));
        mvc.perform(get("/api/metrics")).andExpect(status().isOk()).andExpect(jsonPath("$.meanTimeToRecoveryMs").isNumber());
    }
    @Test void exposesHonestCapabilities() throws Exception { mvc.perform(get("/api/capabilities")).andExpect(status().isOk()).andExpect(jsonPath("$.llm.mode").value("DEMO")).andExpect(jsonPath("$.rag.enabled").value(true)).andExpect(jsonPath("$.fallback").value(true)); }
    @Test void rejectsOutOfScopeAndScenarioMismatchRequirements() throws Exception {
        mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"brownfield\",\"requirement\":\"Build a snake game and open it in a new page\"}"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title").value("out_of_scope"));
        mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"brownfield\",\"requirement\":\"Build a new URL shortener from scratch\"}"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title").value("scenario_mismatch"));
    }
    @Test void exposesOnlyCataloguedRepositoryArtifacts() throws Exception {
        mvc.perform(get("/api/artifacts/architecture")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML)).andExpect(content().string(org.hamcrest.Matchers.containsString("Architecture and control flow")));
        mvc.perform(get("/api/artifacts/not-approved")).andExpect(status().isNotFound()).andExpect(jsonPath("$.title").value("artifact_not_found"));
        mvc.perform(get("/api/artifacts/coverage-report")).andExpect(status().isNotFound()).andExpect(jsonPath("$.title").value("artifact_unavailable"));
    }
    @Test void replanIsCountedWithoutCreatingAnotherRun() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"ambiguous\",\"requirement\":\"Make links smart\"}")).andReturn();
        var createdJson=json.readTree(created.getResponse().getContentAsString());
        var id=createdJson.get("id").asText();
        var initialRevision=createdJson.get("revision").asInt();
        mvc.perform(post("/api/workflows/"+id+"/replan").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"requirement\":\"Add user-selected expiration to new links\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.revision").value(initialRevision+1));
        var metrics=mvc.perform(get("/api/metrics")).andExpect(status().isOk()).andExpect(jsonPath("$.replans").isNumber()).andExpect(jsonPath("$.meanTimeToRecoveryMs").isNumber()).andReturn();
        assertThat(json.readTree(metrics.getResponse().getContentAsString()).get("replans").asInt()).isGreaterThanOrEqualTo(1);
    }
    @Test void completesReleaseAfterAccountableApprovalAndBuildsFinalSummary() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"greenfield\",\"requirement\":\"Build a URL shortener with aliases, expiration, analytics, and audit\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"));
        mvc.perform(post("/api/workflows/"+id+"/steps/release/approve").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"reason\":\"Evidence reviewed\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCEEDED")).andExpect(jsonPath("$.finalSummary").value(org.hamcrest.Matchers.containsString("Artifacts:")));
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }
    @Test void rejectsUnaccountableApprovalAndSupportsRollback() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"greenfield\",\"requirement\":\"Build a secure URL shortener\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator"));
        mvc.perform(post("/api/workflows/"+id+"/steps/release/approve").contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"\"}")).andExpect(status().isForbidden()).andExpect(jsonPath("$.title").value("human_decision_required"));
        mvc.perform(post("/api/workflows/"+id+"/rollback").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"reason\":\"Risk rejected\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ROLLED_BACK"));
    }
    @Test void safeStopsWhenPrimaryAndFallbackBothFail() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"greenfield\",\"requirement\":\"Build a URL service [simulate-development-failure] [simulate-fallback-failure]\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SAFE_STOPPED")).andExpect(jsonPath("$.steps.development.status").value("FAILED"));
    }
    @Test void rejectsPrivateAndMetadataDestinationsAndPastExpiry() throws Exception {
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"http://10.0.0.1/admin\",\"customCode\":\"private1\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("invalid_url"));
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"http://169.254.169.254/latest/meta-data\",\"customCode\":\"metadata1\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("invalid_url"));
        var expired=json.writeValueAsString(Map.of("url","https://example.com","customCode","expired1","expiresAt",Instant.now().minusSeconds(60).toString()));
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content(expired)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("invalid_expiry"));
    }
    @Test void coversGeneratedCodesConflictsMissingAndExpiredLinks() throws Exception {
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).header("X-Actor","qa-agent").content("{\"url\":\"https://example.com/generated\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").isNotEmpty());
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com/first\",\"customCode\":\"conflict1\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com/second\",\"customCode\":\"conflict1\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("code_conflict"));
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com\",\"customCode\":\"bad!\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("invalid_code"));
        mvc.perform(get("/api/links/doesnotexist")).andExpect(status().isNotFound()).andExpect(jsonPath("$.title").value("not_found"));
        links.save(new ShortLink("expired2","https://example.com/old",Instant.now().minusSeconds(120),Instant.now().minusSeconds(60)));
        mvc.perform(get("/expired2")).andExpect(status().isGone()).andExpect(jsonPath("$.title").value("expired"));
    }
    @Test void coversCatalogKnowledgeAuditAndWorkflowReadEndpoints() throws Exception {
        mvc.perform(get("/api/artifacts")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").isNotEmpty());
        mvc.perform(get("/api/knowledge").param("query","secure URL")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("SEC-001"));
        mvc.perform(get("/api/audit")).andExpect(status().isOk());
        mvc.perform(get("/api/capabilities")).andExpect(status().isOk()).andExpect(jsonPath("$.humanApproval").value(true));
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","reader").content("{\"scenario\":\"greenfield\",\"requirement\":\"Build a URL shortener\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(get("/api/workflows")).andExpect(status().isOk());
        mvc.perform(get("/api/workflows/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    }
    @Test void rejectsBlankInvalidAndMismatchedRequirements() throws Exception {
        mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).content("{\"scenario\":\"invalid\",\"requirement\":\"Build a URL shortener\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("invalid_scenario"));
        mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).content("{\"scenario\":\"greenfield\",\"requirement\":\"\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("requirement_required"));
        mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).content("{\"scenario\":\"greenfield\",\"requirement\":\"Enhance the existing URL service without breaking redirects\"}")).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.title").value("scenario_mismatch"));
        mvc.perform(get("/api/workflows/00000000-0000-0000-0000-000000000001")).andExpect(status().isNotFound()).andExpect(jsonPath("$.title").value("run_not_found"));
    }
    @Test void brownfieldOutputIncludesCompatibilityReasoningAndInvalidApprovalIsRejected() throws Exception {
        var created=mvc.perform(post("/api/workflows").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"scenario\":\"brownfield\",\"requirement\":\"Enhance the existing URL service with expiration\"}")).andReturn();
        var id=json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/workflows/"+id+"/advance").header("X-Actor","agent-orchestrator")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("preserve existing redirect and API behavior")));
        mvc.perform(post("/api/workflows/"+id+"/steps/not-a-step/approve").contentType(MediaType.APPLICATION_JSON).header("X-Actor","Bindu Vallika").content("{\"reason\":\"reviewed\"}")).andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("invalid_approval"));
    }
}
