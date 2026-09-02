package com.example.agentic.links;
import java.net.URI;
public interface UrlPolicy { URI validate(String value); void validateCode(String code); }
