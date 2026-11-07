package com.tutorly.repository;

import com.tutorly.model.Teacher;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherRepository extends JpaRepository<Teacher,Long> {
    Optional<Teacher> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Teacher> findByIdAndListedTrue(Long id);

    @Query(value="""
            select distinct t from Teacher t left join t.subjects s
            where t.listed = true
              and (:city = '' or lower(t.city) = :city)
              and (:area = '' or lower(t.area) = :area)
              and (:subject = '' or lower(s) = :subject)
            """, countQuery="""
            select count(distinct t.id) from Teacher t left join t.subjects s
            where t.listed = true
              and (:city = '' or lower(t.city) = :city)
              and (:area = '' or lower(t.area) = :area)
              and (:subject = '' or lower(s) = :subject)
            """)
    Page<Teacher> search(@Param("city") String city, @Param("area") String area,
                         @Param("subject") String subject, Pageable pageable);
}
