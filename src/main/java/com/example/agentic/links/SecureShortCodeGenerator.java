package com.example.agentic.links;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
@Component public class SecureShortCodeGenerator implements ShortCodeGenerator {
    private static final char[] ALPHABET="23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private final SecureRandom random=new SecureRandom();
    public String generate(String target){var value=new char[8];for(int i=0;i<value.length;i++)value[i]=ALPHABET[random.nextInt(ALPHABET.length)];return new String(value);}
}
