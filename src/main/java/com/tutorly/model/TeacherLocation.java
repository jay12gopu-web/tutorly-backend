package com.tutorly.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name="teacher_locations",indexes=@Index(name="idx_location_enabled_lat",columnList="enabled,latitude,teacher_id"))
public class TeacherLocation {
    @Id @Column(name="teacher_id") private Long teacherId;
    @MapsId @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="teacher_id",nullable=false) private Teacher teacher;
    @Column(nullable=false,precision=9,scale=6) private BigDecimal latitude;
    @Column(nullable=false,precision=9,scale=6) private BigDecimal longitude;
    @Column(nullable=false) private boolean enabled;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected TeacherLocation(){}
    public TeacherLocation(Teacher teacher){this.teacher=teacher;}
    public void update(BigDecimal latitude,BigDecimal longitude,boolean enabled,Instant now){this.latitude=latitude;this.longitude=longitude;this.enabled=enabled;this.updatedAt=now;}
    public Long getTeacherId(){return teacherId;} public BigDecimal getLatitude(){return latitude;}
    public BigDecimal getLongitude(){return longitude;} public boolean isEnabled(){return enabled;} public Instant getUpdatedAt(){return updatedAt;}
}
