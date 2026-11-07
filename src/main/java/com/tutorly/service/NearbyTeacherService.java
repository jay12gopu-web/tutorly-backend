package com.tutorly.service;
import com.tutorly.dto.NearbyTeacherDtos.*;
import com.tutorly.dto.TeacherDtos;
import com.tutorly.model.*;
import com.tutorly.repository.*;
import jakarta.persistence.*;
import java.math.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class NearbyTeacherService {
    private final TeacherLocationRepository locations;private final TeacherRepository teachers;
    private final NearbyTeacherQuery query;private final EntityManager em;
    public NearbyTeacherService(TeacherLocationRepository locations,TeacherRepository teachers,NearbyTeacherQuery query,EntityManager em){
        this.locations=locations;this.teachers=teachers;this.query=query;this.em=em;
    }
    public OwnLocation get(Long teacherId){var teacher=teachers.findById(teacherId).orElseThrow(NearbyTeacherService::missing);
        return locations.findById(teacherId).map(l->view(teacher,l)).orElse(new OwnLocation(teacherId,false,null,null,false,false,null));}
    @Transactional
    public OwnLocation put(Long teacherId,LocationInput input){
        coordinates(input.latitude(),input.longitude());
        if(input.enabled()==null)throw bad("enabled is required");
        var teacher=lockTeacher(teacherId);
        if(input.enabled()&&teacher.getTeachingMode()==Teacher.TeachingMode.ONLINE)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Choose IN_PERSON or BOTH in your profile before enabling nearby discovery");
        var location=locations.findById(teacherId).orElseGet(()->new TeacherLocation(teacher));
        location.update(BigDecimal.valueOf(input.latitude()).setScale(6,RoundingMode.HALF_UP),
            BigDecimal.valueOf(input.longitude()).setScale(6,RoundingMode.HALF_UP),input.enabled(),Instant.now().truncatedTo(ChronoUnit.MICROS));
        return view(teacher,locations.saveAndFlush(location));
    }
    @Transactional public void remove(Long teacherId){lockTeacher(teacherId);locations.findById(teacherId).ifPresent(l->{locations.delete(l);locations.flush();});}
    public Results search(Double latitude,Double longitude,Double radiusKm,String subject,int page,int size){
        coordinates(latitude,longitude);
        if(radiusKm==null||!Double.isFinite(radiusKm)||radiusKm<1||radiusKm>100)throw bad("radiusKm must be between 1 and 100");
        if(page<0||page>10000||size<1||size>50)throw bad("Use page 0..10000 and size 1..50");
        String filter=subject==null?"":subject.strip().toLowerCase(Locale.ROOT);
        if(filter.length()>60)throw bad("Subject must be at most 60 characters");
        var hits=query.search(latitude,longitude,radiusKm,filter,page,size);
        var matches=hits.getContent().stream().map(hit->{var teacher=teachers.findById(hit.teacherId()).orElseThrow(NearbyTeacherService::missing);
            return new Match(TeacherDtos.PublicProfile.from(teacher),BigDecimal.valueOf(hit.distanceKm()).setScale(1,RoundingMode.HALF_UP));}).toList();
        return new Results(matches,page,size,hits.getTotalElements(),hits.getTotalPages(),hits.hasNext(),radiusKm,"STRAIGHT_LINE");
    }
    private Teacher lockTeacher(Long id){var t=em.find(Teacher.class,id,LockModeType.PESSIMISTIC_WRITE);if(t==null)throw missing();return t;}
    private static OwnLocation view(Teacher t,TeacherLocation l){return new OwnLocation(t.getId(),true,l.getLatitude(),l.getLongitude(),l.isEnabled(),
        l.isEnabled()&&t.isListed()&&t.getTeachingMode()!=Teacher.TeachingMode.ONLINE,l.getUpdatedAt());}
    private static void coordinates(Double latitude,Double longitude){
        if(latitude==null||!Double.isFinite(latitude)||latitude < -90||latitude > 90)throw bad("Latitude must be between -90 and 90");
        if(longitude==null||!Double.isFinite(longitude)||longitude < -180||longitude > 180)throw bad("Longitude must be between -180 and 180");
    }
    private static ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}
    private static ResponseStatusException missing(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher not available");}
}
