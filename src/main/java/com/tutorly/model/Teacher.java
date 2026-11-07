package com.tutorly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers", indexes = @Index(name = "idx_teacher_city_listed", columnList = "city,listed"))
public class Teacher {
    public enum TeachingMode { ONLINE, IN_PERSON, BOTH }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=200)
    private String email;
    @Column(name="password_hash", nullable=false, length=100)
    private String passwordHash;
    @Column(nullable=false, length=80)
    private String name;
    @Column(nullable=false, length=80)
    private String city;
    @Column(nullable=false, length=80)
    private String area;
    @Column(nullable=false, length=1000)
    private String bio;
    @Column(nullable=false, length=200)
    private String qualification;
    @Column(name="experience_years", nullable=false)
    private int experienceYears;
    @Column(name="hourly_rate", nullable=false, precision=8, scale=2)
    private BigDecimal hourlyRate;
    @Enumerated(EnumType.STRING)
    @Column(name="teaching_mode", nullable=false, length=20)
    private TeachingMode teachingMode;
    @Column(nullable=false)
    private boolean listed;
    @ElementCollection(fetch=FetchType.LAZY)
    @CollectionTable(name="teacher_subjects", joinColumns=@JoinColumn(name="teacher_id"))
    @OrderColumn(name="subject_order")
    @Column(name="subject", nullable=false, length=60)
    private List<String> subjects = new ArrayList<>();

    protected Teacher() {}
    public Teacher(String email, String passwordHash) { this.email=email; this.passwordHash=passwordHash; }
    public void updateProfile(String name, String city, String area, String bio,
                              String qualification, int experienceYears, BigDecimal hourlyRate,
                              TeachingMode teachingMode, List<String> subjects) {
        this.name=name; this.city=city; this.area=area; this.bio=bio;
        this.qualification=qualification; this.experienceYears=experienceYears;
        this.hourlyRate=hourlyRate; this.teachingMode=teachingMode;
        this.subjects.clear(); this.subjects.addAll(subjects);
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    @JsonIgnore public String getPasswordHash() { return passwordHash; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getArea() { return area; }
    public String getBio() { return bio; }
    public String getQualification() { return qualification; }
    public int getExperienceYears() { return experienceYears; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public TeachingMode getTeachingMode() { return teachingMode; }
    public boolean isListed() { return listed; }
    public void setListed(boolean listed) { this.listed=listed; }
    public List<String> getSubjects() { return subjects; }
}
