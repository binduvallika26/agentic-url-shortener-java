package com.example.agentic.audit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
@Service public class AuditService {
    private final AuditRepository repository;
    public AuditService(AuditRepository repository){this.repository=repository;}
    public void record(String actor,String action,String resource,String outcome,String correlation,String details){repository.save(new AuditEvent(actor,action,resource,outcome,correlation,details));}
    public List<AuditEvent> recent(){return repository.findAll(Sort.by(Sort.Direction.DESC,"occurredAt"));}
}
