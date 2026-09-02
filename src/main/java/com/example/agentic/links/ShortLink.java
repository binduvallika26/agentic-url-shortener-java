package com.example.agentic.links;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ShortLink {
    @Id @Column(length=32) private String code;
    @Column(nullable=false,length=2048) private String targetUrl;
    @Column(nullable=false) private Instant createdAt;
    private Instant expiresAt;
    @Version private long version;
    private long visits;
    protected ShortLink() {}
    public ShortLink(String code,String targetUrl,Instant createdAt,Instant expiresAt){this.code=code;this.targetUrl=targetUrl;this.createdAt=createdAt;this.expiresAt=expiresAt;}
    public void visit(){visits++;}
    public String getCode(){return code;} public String getTargetUrl(){return targetUrl;} public Instant getCreatedAt(){return createdAt;} public Instant getExpiresAt(){return expiresAt;} public long getVisits(){return visits;}
}
