package com.example.agentic;
import com.example.agentic.ai.KnowledgeService;
import com.example.agentic.links.SecureUrlPolicy;
import com.example.agentic.workflow.DefaultWorkflowGraphProvider;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ArchitectureUnitTest {
    @Test void graphContainsParallelAndSynchronizationPaths(){var graph=new DefaultWorkflowGraphProvider().graphFor("greenfield");assertThat(graph.get("development").dependsOn()).containsExactly("design");assertThat(graph.get("documentation").dependsOn()).containsExactly("design");assertThat(graph.get("release").dependsOn()).containsExactlyInAnyOrder("qa","security","documentation");assertThat(graph.get("release").requiresApproval()).isTrue();}
    @Test void localRetrievalGroundsSecurityQueries(){var results=new KnowledgeService().retrieve("secure URL private addresses",2);assertThat(results).isNotEmpty();assertThat(results.getFirst().id()).isEqualTo("SEC-001");}
    @Test void urlPolicyRejectsLoopback(){assertThatThrownBy(()->new SecureUrlPolicy().validate("http://localhost/admin")).hasMessageContaining("public absolute");}
}
