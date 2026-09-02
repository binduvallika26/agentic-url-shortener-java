package com.example.agentic.links;
import com.example.agentic.common.CorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
@RestController public class LinkController {
    private final LinkService service;
    public LinkController(LinkService service){this.service=service;}
    @PostMapping("/api/links") ResponseEntity<ShortLink> create(@RequestBody LinkService.CreateLinkRequest body,HttpServletRequest request){var link=service.create(body,CorrelationFilter.actor(request),CorrelationFilter.id(request));return ResponseEntity.created(URI.create("/api/links/"+link.getCode())).body(link);}
    @GetMapping("/api/links/{code}") ShortLink get(@PathVariable String code){return service.get(code);}
    @GetMapping("/{code:[A-Za-z0-9_-]{4,32}}") ResponseEntity<Void> resolve(@PathVariable String code,HttpServletRequest request){var link=service.resolve(code,CorrelationFilter.actor(request),CorrelationFilter.id(request));return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(link.getTargetUrl())).build();}
}
