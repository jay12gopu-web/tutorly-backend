package com.tutorly.service;

import com.tutorly.auth.AuthStudentRepository;
import com.tutorly.dto.DoubtDtos.*;
import com.tutorly.model.*;
import com.tutorly.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

@Service
@Transactional(readOnly=true)
public class TeacherDoubtService {
    private final TeacherDoubtRepository doubts;
    private final DoubtReplyRepository replies;
    private final TeacherRepository teachers;
    private final AuthStudentRepository students;
    public TeacherDoubtService(TeacherDoubtRepository doubts,DoubtReplyRepository replies,
                               TeacherRepository teachers,AuthStudentRepository students){
        this.doubts=doubts;this.replies=replies;this.teachers=teachers;this.students=students;
    }
    @Transactional
    public Detail create(Long studentId,Create input){
        var teacher=teachers.findByIdAndListedTrue(input.teacherId()).orElseThrow(TeacherDoubtService::missing);
        var student=students.findById(studentId).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Account unavailable"));
        String subject=teacher.getSubjects().stream().filter(s->s.equalsIgnoreCase(input.subject().strip()))
                .findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a subject taught by this teacher"));
        var doubt=new TeacherDoubt(student,teacher,input.title().strip(),subject,input.question().strip(),Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        return Detail.from(doubts.saveAndFlush(doubt));
    }
    public Results<Summary> list(Long owner,boolean teacher,int page,int size){
        var paging=page(page,size,Sort.by(Sort.Direction.DESC,"updatedAt","id"));
        return Results.from((teacher?doubts.findByTeacher_Id(owner,paging):doubts.findByStudent_Id(owner,paging)).map(Summary::from));
    }
    public Detail get(Long owner,boolean teacher,Long id){return Detail.from(owned(owner,teacher,id,false));}
    public Results<Reply> replies(Long owner,boolean teacher,Long id,int page,int size){
        owned(owner,teacher,id,false);
        return Results.from(replies.findByDoubt_Id(id,page(page,size,Sort.by("id"))).map(Reply::from));
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Reply reply(Long owner,boolean teacher,Long id,ReplyInput input){
        var doubt=owned(owner,teacher,id,true);
        if(doubt.getStatus()!=TeacherDoubt.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Doubt is resolved; the student must reopen it before replies");
        Instant now=Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        var reply=replies.saveAndFlush(new DoubtReply(doubt,teacher?DoubtReply.AuthorRole.TEACHER:DoubtReply.AuthorRole.STUDENT,input.body().strip(),now));
        doubt.touch(now);
        return Reply.from(reply);
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Detail resolve(Long studentId,Long id,boolean resolved){
        var doubt=owned(studentId,false,id,true);
        doubt.setResolved(resolved,Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS));
        return Detail.from(doubt);
    }
    private TeacherDoubt owned(Long owner,boolean teacher,Long id,boolean lock){
        if(id==null||id<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Doubt ID must be positive");
        return (lock?(teacher?doubts.lockForTeacher(id,owner):doubts.lockForStudent(id,owner)):
            (teacher?doubts.findByIdAndTeacher_Id(id,owner):doubts.findByIdAndStudent_Id(id,owner))).orElseThrow(TeacherDoubtService::missing);
    }
    private static Pageable page(int page,int size,Sort sort){
        if(page<0||page>10000||size<1||size>50)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use page 0..10000 and size 1..50");
        return PageRequest.of(page,size,sort);
    }
    private static ResponseStatusException missing(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Doubt or teacher not available");}
}
