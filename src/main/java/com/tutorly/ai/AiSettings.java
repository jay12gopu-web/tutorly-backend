package com.tutorly.ai;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class AiSettings {
    private final boolean enabled;
    private final String apiKey,model;
    private final int dailyLimit;
    public AiSettings(@Value("${tutorly.ai.enabled:false}") boolean enabled,
                      @Value("${tutorly.ai.api-key:}") String apiKey,
                      @Value("${tutorly.ai.model:openai/gpt-oss-20b}") String model,
                      @Value("${tutorly.ai.daily-limit:20}") int dailyLimit){
        if(dailyLimit<1||dailyLimit>100)throw new IllegalArgumentException("tutorly.ai.daily-limit must be 1..100");
        if(model==null||model.isBlank()||model.length()>120)throw new IllegalArgumentException("Set a valid tutorly.ai.model");
        this.enabled=enabled;this.apiKey=apiKey==null?"":apiKey.strip();this.model=model.strip();this.dailyLimit=dailyLimit;
    }
    public boolean enabled(){return enabled;} public boolean configured(){return !apiKey.isBlank();}
    String apiKey(){return apiKey;} public String model(){return model;} public int dailyLimit(){return dailyLimit;}
}
