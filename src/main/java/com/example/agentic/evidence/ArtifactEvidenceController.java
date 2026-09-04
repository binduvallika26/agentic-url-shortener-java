package com.example.agentic.evidence;

import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/artifacts")
public class ArtifactEvidenceController {
    private static final Map<String,Artifact> ARTIFACTS=artifacts();

    @GetMapping public List<ArtifactInfo> all(){return ARTIFACTS.values().stream().map(a->new ArtifactInfo(a.id(),a.label(),a.path(),Files.isRegularFile(resolve(a.path())))).toList();}

    @GetMapping(value="/{id}",produces=MediaType.TEXT_HTML_VALUE)
    public String view(@PathVariable String id){
        var artifact=ARTIFACTS.get(id);
        if(artifact==null)throw new DomainException("artifact_not_found","Artifact is not in the approved evidence catalog",HttpStatus.NOT_FOUND);
        var path=resolve(artifact.path());
        if(!Files.isRegularFile(path))throw new DomainException("artifact_unavailable","Run the documented build/test step to generate this artifact: "+artifact.path(),HttpStatus.NOT_FOUND);
        try{
            var content=Files.readString(path,StandardCharsets.UTF_8);
            if(content.length()>120_000)content=content.substring(0,120_000)+"\n\n[Preview truncated]";
            return "<!doctype html><html><head><meta charset=\"utf-8\"><title>"+escape(artifact.label())+"</title><style>body{margin:0;background:#07100e;color:#eafff5;font:14px ui-monospace,monospace}main{max-width:1100px;margin:auto;padding:40px}a{color:#8dffd1}pre{white-space:pre-wrap;word-break:break-word;background:#0d1c18;border:1px solid #28483e;border-radius:12px;padding:24px;line-height:1.5}</style></head><body><main><a href=\"/\">&larr; Dashboard</a><h1>"+escape(artifact.label())+"</h1><p>Repository evidence: "+escape(artifact.path())+"</p><pre>"+escape(content)+"</pre></main></body></html>";
        }catch(Exception ex){throw new DomainException("artifact_unavailable","Could not read the approved artifact",HttpStatus.INTERNAL_SERVER_ERROR);}
    }

    private static Path resolve(String relative){
        var root=Path.of("").toAbsolutePath().normalize();
        var resolved=root.resolve(relative).normalize();
        if(!resolved.startsWith(root))throw new IllegalStateException("Artifact escaped repository root");
        return resolved;
    }
    private static String escape(String value){return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
    private static Map<String,Artifact> artifacts(){
        var values=new LinkedHashMap<String,Artifact>();
        add(values,"product-readme","Product scope and setup","README.md");
        add(values,"architecture","Architecture and control flow","docs/ARCHITECTURE.md");
        add(values,"link-service","URL-shortener implementation","src/main/java/com/example/agentic/links/LinkService.java");
        add(values,"url-policy","Security URL policy","src/main/java/com/example/agentic/links/SecureUrlPolicy.java");
        add(values,"api-tests","Integration tests","src/test/java/com/example/agentic/ApiIntegrationTest.java");
        add(values,"test-report","Maven/Surefire test report","target/surefire-reports/com.example.agentic.ApiIntegrationTest.txt");
        add(values,"coverage-report","JaCoCo coverage summary","target/site/jacoco/index.html");
        add(values,"demo-guide","Interview demo guide","docs/INTERVIEW-DEMO-GUIDE.md");
        add(values,"build-contract","Maven build contract","pom.xml");
        return Map.copyOf(values);
    }
    private static void add(Map<String,Artifact> values,String id,String label,String path){values.put(id,new Artifact(id,label,path));}
    private record Artifact(String id,String label,String path){}
    public record ArtifactInfo(String id,String label,String path,boolean available){}
}
