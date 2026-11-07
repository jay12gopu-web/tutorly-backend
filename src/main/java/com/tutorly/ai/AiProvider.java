package com.tutorly.ai;
public interface AiProvider {
    record Answer(String text,boolean truncated){}
    Answer answer(String model,String grade,String subject,String question);
}
