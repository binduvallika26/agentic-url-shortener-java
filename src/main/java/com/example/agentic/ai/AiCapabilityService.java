package com.example.agentic.ai;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service public class AiCapabilityService {
    private final boolean enabled; private final String modelName; private final String apiKey;
    public AiCapabilityService(@Value("${app.ai.enabled:false}") boolean enabled,@Value("${app.ai.model:gpt-4o-mini}") String modelName,@Value("${app.ai.api-key:}") String apiKey){this.enabled=enabled&&!apiKey.isBlank();this.modelName=modelName;this.apiKey=apiKey;}
    public Optional<String> generate(String prompt){if(!enabled)return Optional.empty();var model=OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).temperature(0.1).build();return Optional.of(model.chat(prompt));}
    public String mode(){return enabled?"CONNECTED":"DEMO";} public String model(){return enabled?modelName:"deterministic-local";} public boolean connected(){return enabled;}
}
