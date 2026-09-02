package com.example.agentic.ai;
import org.springframework.stereotype.Service;
import java.util.*;
@Service public class KnowledgeService {
    public record KnowledgeChunk(String id,String title,String text){}
    private final List<KnowledgeChunk> chunks=List.of(
        new KnowledgeChunk("SEC-001","Secure URL handling","Accept only HTTP and HTTPS. Resolve DNS and reject loopback, private, link-local, and metadata-service addresses. Revalidate redirects and apply abuse controls."),
        new KnowledgeChunk("GOV-001","Controlled autonomy","High-impact actions require identified human approval. Enforce least privilege, tool allowlists, bounded retries, safe stop, and append-only decision lineage."),
        new KnowledgeChunk("QA-001","Engineering quality gates","Require compilation, unit tests, integration tests, security validation, API compatibility review, and traceable evidence before release."),
        new KnowledgeChunk("ARCH-001","Extensible architecture","Keep domain, transport, orchestration, AI providers, persistence, and policy evaluation behind explicit interfaces. Prefer typed artifacts over free-form text."),
        new KnowledgeChunk("OPS-001","Reliability","Track success rate, retries, fallbacks, rollback frequency, MTTR, and end-to-end latency. Use idempotency, optimistic concurrency, and durable state."));
    public List<KnowledgeChunk> retrieve(String query,int limit){var terms=Arrays.stream(query.toLowerCase().split("\\W+")).filter(t->t.length()>2).collect(java.util.stream.Collectors.toSet());return chunks.stream().sorted(Comparator.comparingInt((KnowledgeChunk c)->score(c,terms)).reversed()).limit(limit).toList();}
    private int score(KnowledgeChunk chunk,Set<String> terms){var text=(chunk.title()+" "+chunk.text()).toLowerCase();return (int)terms.stream().filter(text::contains).count();}
    public int indexedCount(){return chunks.size();}
}
