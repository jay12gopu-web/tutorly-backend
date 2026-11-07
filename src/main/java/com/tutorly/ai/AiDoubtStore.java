package com.tutorly.ai;
import com.tutorly.dto.AiDoubtDtos.*;
import com.tutorly.model.*;
import com.tutorly.repository.AiDoubtRepository;
import jakarta.persistence.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class AiDoubtStore {
    private static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    private final AiDoubtRepository repo;private final EntityManager em;private final AiSettings settings;private final Clock clock;
    public record Reservation(Detail doubt,String grade,boolean created){}
    @Autowired public AiDoubtStore(AiDoubtRepository repo,EntityManager em,AiSettings settings){this(repo,em,settings,Clock.systemUTC());}
    public AiDoubtStore(AiDoubtRepository repo,EntityManager em,AiSettings settings,Clock clock){this.repo=repo;this.em=em;this.settings=settings;this.clock=clock;}
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Reservation reserve(Long studentId,Ask request){
        Student student=lockStudent(studentId);Instant now=now();String key=request.requestId().toString();
        String subject=request.subject().strip(),question=request.question().strip();
        var previous=repo.findByStudent_IdAndRequestId(studentId,key);
        if(previous.isPresent()){
            var d=previous.get();if(!d.getSubject().equals(subject)||!d.getQuestion().equals(question))throw error(HttpStatus.CONFLICT,"REQUEST_ID_REUSED");
            expire(d,now);return new Reservation(Detail.from(d),d.getGrade(),false);
        }
        if(!settings.enabled()||!settings.configured())throw error(HttpStatus.SERVICE_UNAVAILABLE,"AI_NOT_CONFIGURED");
        var pending=repo.findByStudent_IdAndStatus(studentId,AiDoubt.Status.PROCESSING);
        for(var d:pending){if(d.getCreatedAt().isAfter(now.minusSeconds(120)))throw error(HttpStatus.CONFLICT,"REQUEST_IN_PROGRESS");expire(d,now);}
        LocalDate day=now.atZone(ZONE).toLocalDate();
        if(repo.countByStudent_IdAndRequestDay(studentId,day)>=settings.dailyLimit())throw error(HttpStatus.TOO_MANY_REQUESTS,"DAILY_LIMIT_REACHED");
        var d=repo.saveAndFlush(new AiDoubt(student,key,day,subject,question,settings.model(),now));
        return new Reservation(Detail.from(d),student.getGrade(),true);
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Detail complete(Long studentId,Long id,AiProvider.Answer answer,String failure){
        lockStudent(studentId);var d=owned(studentId,id);
        if(failure!=null)d.fail(failure,now());else d.answer(answer.text(),answer.truncated(),now());
        return Detail.from(repo.saveAndFlush(d));
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Detail get(Long studentId,Long id){lockStudent(studentId);var d=owned(studentId,id);expire(d,now());return Detail.from(d);}
    public Results<Detail> history(Long studentId,int page,int size){
        if(page<0||page>10000||size<1||size>50)throw error(HttpStatus.BAD_REQUEST,"INVALID_PAGE");
        return Results.from(repo.findByStudent_Id(studentId,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt","id"))).map(Detail::from));
    }
    public Usage usage(Long studentId){Instant now=now();LocalDate day=now.atZone(ZONE).toLocalDate();long used=repo.countByStudent_IdAndRequestDay(studentId,day);
        return new Usage(settings.enabled(),settings.configured(),"Groq",settings.model(),settings.dailyLimit(),used,Math.max(0,settings.dailyLimit()-used),day.plusDays(1).atStartOfDay(ZONE).toInstant());}
    private void expire(AiDoubt d,Instant now){if(d.getStatus()==AiDoubt.Status.PROCESSING&&!d.getCreatedAt().isAfter(now.minusSeconds(120)))d.fail("AI_INTERRUPTED",now);}
    private AiDoubt owned(Long studentId,Long id){if(id==null||id<=0)throw error(HttpStatus.BAD_REQUEST,"INVALID_ID");return repo.findByIdAndStudent_Id(id,studentId).orElseThrow(()->error(HttpStatus.NOT_FOUND,"DOUBT_NOT_FOUND"));}
    private Student lockStudent(Long id){var s=em.find(Student.class,id,LockModeType.PESSIMISTIC_WRITE);if(s==null)throw error(HttpStatus.UNAUTHORIZED,"ACCOUNT_UNAVAILABLE");return s;}
    private Instant now(){return clock.instant().truncatedTo(ChronoUnit.MICROS);}
    private static ResponseStatusException error(HttpStatus s,String code){return new ResponseStatusException(s,code);}
}
