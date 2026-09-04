package com.example.agentic.workflow;

import com.example.agentic.common.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Component
public class ScenarioRequirementPolicy {
    private static final Set<String> DOMAIN_TERMS=Set.of("url","link","shorten","redirect","alias","expiration","expiry","analytics","audit","service");
    private static final Set<String> NEW_SYSTEM_TERMS=Set.of("build ","create ","from scratch","new application","new system","snake game");
    private static final Set<String> EXISTING_SYSTEM_TERMS=Set.of("existing","enhance","modify","refactor","fix ","upgrade","without breaking","regression");

    public void validate(String scenario,String requirement){
        if(requirement==null||requirement.isBlank())throw new DomainException("requirement_required","Enter an engineering requirement",HttpStatus.BAD_REQUEST);
        var text=requirement.toLowerCase(Locale.ROOT);
        if(DOMAIN_TERMS.stream().noneMatch(text::contains))throw new DomainException("out_of_scope","This prototype governs the URL-shortener product. Describe a URL, link, redirect, alias, expiration, analytics, audit, or existing-service change.",HttpStatus.UNPROCESSABLE_ENTITY);
        if(scenario.equals("ambiguous"))return;
        var looksNew=NEW_SYSTEM_TERMS.stream().anyMatch(text::contains);
        var looksExisting=EXISTING_SYSTEM_TERMS.stream().anyMatch(text::contains);
        if(scenario.equals("brownfield")&&looksNew&&!looksExisting)throw new DomainException("scenario_mismatch","This reads like a new system or feature. Select Greenfield, or describe the existing URL-shortener behavior being changed.",HttpStatus.UNPROCESSABLE_ENTITY);
        if(scenario.equals("greenfield")&&looksExisting&&!looksNew)throw new DomainException("scenario_mismatch","This reads like a change to an existing system. Select Brownfield, or describe a new URL-shortener system or feature.",HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
