package com.tutorly.ai;
import com.tutorly.dto.AiDoubtDtos.*;
import com.tutorly.model.AiDoubt;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AiDoubtService {
    private final AiDoubtStore store;private final AiProvider provider;
    public AiDoubtService(AiDoubtStore store,AiProvider provider){this.store=store;this.provider=provider;}
    public record Outcome(HttpStatus status,Detail doubt){}
    // No transaction surrounds the external call: reservation commits before contacting Groq.
    public Outcome ask(Long studentId,Ask request){
        var r=store.reserve(studentId,request);var d=r.doubt();
        if(!r.created())return outcome(d,false);
        AiProvider.Answer answer=null;String failure=null;
        try{answer=provider.answer(d.model(),r.grade(),d.subject(),d.question());}
        catch(AiProviderException ex){failure=ex.code();}
        return outcome(store.complete(studentId,d.id(),answer,failure),true);
    }
    private Outcome outcome(Detail d,boolean created){
        if(d.status()==AiDoubt.Status.PROCESSING)return new Outcome(HttpStatus.ACCEPTED,d);
        if(d.status()==AiDoubt.Status.ANSWERED)return new Outcome(created?HttpStatus.CREATED:HttpStatus.OK,d);
        HttpStatus status=switch(d.errorCode()){
            case "AI_TIMEOUT"->HttpStatus.GATEWAY_TIMEOUT;
            case "AI_INVALID_RESPONSE"->HttpStatus.BAD_GATEWAY;
            default->HttpStatus.SERVICE_UNAVAILABLE;
        };
        return new Outcome(status,d);
    }
}
