package com.example.agentic.links;
import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;
@Component public class SecureUrlPolicy implements UrlPolicy {
    private static final Pattern CODE=Pattern.compile("^[A-Za-z0-9_-]{4,32}$");
    private static final Set<String> LOCAL=Set.of("localhost","127.0.0.1","::1","0.0.0.0","metadata.google.internal","metadata.aws.internal");
    public URI validate(String value){try{var uri=URI.create(value);var host=uri.getHost();if(!Set.of("http","https").contains(uri.getScheme())||host==null||blockedHost(host))throw invalid();return uri;}catch(IllegalArgumentException ex){throw invalid();}}
    private boolean blockedHost(String value){
        var host=value.toLowerCase().replace("[","").replace("]","");
        if(LOCAL.contains(host)||host.endsWith(".localhost")||host.endsWith(".local")||host.equals("169.254.169.254"))return true;
        if(host.matches("\\d{1,3}(\\.\\d{1,3}){3}")||host.contains(":"))try{var address=InetAddress.getByName(host);return address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isSiteLocalAddress()||address.isLinkLocalAddress()||address.isMulticastAddress();}catch(Exception ex){return true;}
        return false;
    }
    public void validateCode(String code){if(!CODE.matcher(code).matches())throw new DomainException("invalid_code","Custom code must be 4-32 URL-safe characters",HttpStatus.BAD_REQUEST);}
    private DomainException invalid(){return new DomainException("invalid_url","Only public absolute HTTP(S) URLs are accepted",HttpStatus.BAD_REQUEST);}
}
