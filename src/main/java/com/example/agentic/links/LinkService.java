package com.example.agentic.links;
import com.example.agentic.audit.AuditService;
import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
@Service public class LinkService {
    private final ShortLinkRepository repository; private final UrlPolicy policy; private final ShortCodeGenerator codes; private final AuditService audit;
    public LinkService(ShortLinkRepository repository,UrlPolicy policy,ShortCodeGenerator codes,AuditService audit){this.repository=repository;this.policy=policy;this.codes=codes;this.audit=audit;}
    @Transactional public ShortLink create(CreateLinkRequest request,String actor,String correlation){var uri=policy.validate(request.url());if(request.expiresAt()!=null&&!request.expiresAt().isAfter(Instant.now()))throw new DomainException("invalid_expiry","Expiry must be in the future",HttpStatus.BAD_REQUEST);var code=request.customCode()==null||request.customCode().isBlank()?allocate(uri.toString()):request.customCode().trim();policy.validateCode(code);if(repository.existsById(code))throw new DomainException("code_conflict","Custom code already exists",HttpStatus.CONFLICT);var link=repository.save(new ShortLink(code,uri.toString(),Instant.now(),request.expiresAt()));audit.record(actor,"link.create",code,"success",correlation,"target="+uri);return link;}
    @Transactional public ShortLink resolve(String code,String actor,String correlation){var link=get(code);if(link.getExpiresAt()!=null&&!link.getExpiresAt().isAfter(Instant.now()))throw new DomainException("expired","Short link has expired",HttpStatus.GONE);link.visit();audit.record(actor,"link.resolve",code,"success",correlation,"visits="+link.getVisits());return link;}
    @Transactional(readOnly=true) public ShortLink get(String code){return repository.findById(code).orElseThrow(()->new DomainException("not_found","Short code was not found",HttpStatus.NOT_FOUND));}
    private String allocate(String target){for(int i=0;i<8;i++){var code=codes.generate(target);if(!repository.existsById(code))return code;}throw new DomainException("capacity_error","Could not allocate a unique code",HttpStatus.SERVICE_UNAVAILABLE);}
    public record CreateLinkRequest(String url,String customCode,Instant expiresAt){}
}
