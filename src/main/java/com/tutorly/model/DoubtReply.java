package com.tutorly.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="doubt_replies",indexes=@Index(name="idx_reply_doubt_id",columnList="doubt_id,id"))
public class DoubtReply {
    public enum AuthorRole { STUDENT, TEACHER }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="doubt_id",nullable=false) private TeacherDoubt doubt;
    @Enumerated(EnumType.STRING) @Column(name="author_role",nullable=false,length=20) private AuthorRole authorRole;
    @Column(nullable=false,length=4000) private String body;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    protected DoubtReply() {}
    public DoubtReply(TeacherDoubt doubt,AuthorRole authorRole,String body,Instant now){
        this.doubt=doubt;this.authorRole=authorRole;this.body=body;this.createdAt=now;
    }
    public Long getId(){return id;}
    public AuthorRole getAuthorRole(){return authorRole;}
    public String getBody(){return body;}
    public Instant getCreatedAt(){return createdAt;}
}
