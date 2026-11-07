package com.tutorly.service;

import com.tutorly.dto.TeacherDtos.*;
import com.tutorly.model.Teacher;
import com.tutorly.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class TeacherService {
    private final TeacherRepository teachers;
    public TeacherService(TeacherRepository teachers) { this.teachers=teachers; }

    public OwnProfile me(Long id) { return OwnProfile.from(requireTeacher(id)); }

    @Transactional
    public OwnProfile update(Long id, Update request) {
        Teacher t=requireTeacher(id);
        applyProfile(t, request.profile());
        t.setListed(request.listed());
        return OwnProfile.from(teachers.saveAndFlush(t));
    }

    public PublicProfile getListed(Long id) {
        return teachers.findByIdAndListedTrue(id).map(PublicProfile::from).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Teacher not found"));
    }

    public SearchResult search(String city, String area, String subject, int page, int size) {
        if(page<0 || page>10000 || size<1 || size>50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use page 0–10000 and size 1–50");
        var result=teachers.search(filter(city,80),filter(area,80),filter(subject,60),
                PageRequest.of(page,size,Sort.by("id").ascending()));
        return new SearchResult(result.getContent().stream().map(PublicProfile::from).toList(),
                result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages(),result.hasNext());
    }

    public static void applyProfile(Teacher teacher, ProfileInput p) {
        var unique=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        p.subjects().forEach(s -> unique.add(s.strip()));
        teacher.updateProfile(p.name().strip(),p.city().strip(),p.area().strip(),p.bio().strip(),
                p.qualification().strip(),p.experienceYears(),p.hourlyRate(),p.teachingMode(),new ArrayList<>(unique));
    }

    private String filter(String value, int max) {
        String result=value==null?"":value.strip().toLowerCase(Locale.ROOT);
        if(result.length()>max)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Search filter is too long");
        return result;
    }
    private Teacher requireTeacher(Long id) {
        return teachers.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Teacher account no longer exists"));
    }
}
