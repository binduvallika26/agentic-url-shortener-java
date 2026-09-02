package com.example.agentic.links;
import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;
@Component public class SecureUrlPolicy implements UrlPolicy {
    private static final Pattern CODE=Pattern.compile("^[A-Za-z0-9_-]{4,32}$");
    private static final Set<String> LOCAL=Set.of("localhost","127.0.0.1","::1");
    public URI validate(String value){try{var uri=URI.create(value);if(!Set.of("http","https").contains(uri.getScheme())||uri.getHost()==null||LOCAL.contains(uri.getHost().toLowerCase()))throw invalid();return uri;}catch(IllegalArgumentException ex){throw invalid();}}
    public void validateCode(String code){if(!CODE.matcher(code).matches())throw new DomainException("invalid_code","Custom code must be 4-32 URL-safe characters",HttpStatus.BAD_REQUEST);}
    private DomainException invalid(){return new DomainException("invalid_url","Only public absolute HTTP(S) URLs are accepted",HttpStatus.BAD_REQUEST);}
}
