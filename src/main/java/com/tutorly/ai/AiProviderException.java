package com.tutorly.ai;
public class AiProviderException extends RuntimeException {
    private final String code;
    public AiProviderException(String code){super(code);this.code=code;}
    public String code(){return code;}
}
