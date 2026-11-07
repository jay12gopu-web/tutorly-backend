package com.tutorly.repository;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

@Repository
public class NearbyTeacherQuery {
    public static final double EARTH_RADIUS_KM=6371.0088;
    private final NamedParameterJdbcTemplate jdbc;
    public NearbyTeacherQuery(DataSource dataSource){jdbc=new NamedParameterJdbcTemplate(dataSource);}
    public record Hit(Long teacherId,double distanceKm){}
    // Haversine distance, clamped to [0,1] before ASIN to handle floating-point rounding.
    private static final String BASE="""
        SELECT l.teacher_id AS teacher_id,
          2.0 * 6371.0088 * ASIN(SQRT(LEAST(1.0,GREATEST(0.0,
            POWER(SIN(RADIANS(l.latitude - :lat) / 2.0),2)
            + COS(RADIANS(:lat)) * COS(RADIANS(l.latitude))
              * POWER(SIN(RADIANS(l.longitude - :lon) / 2.0),2)
          )))) AS distance_km
        FROM teacher_locations l JOIN teachers t ON t.id=l.teacher_id
        WHERE l.enabled=TRUE AND t.listed=TRUE AND t.teaching_mode IN ('IN_PERSON','BOTH')
          AND l.latitude BETWEEN :minLat AND :maxLat
          AND (:subject='' OR EXISTS (
            SELECT 1 FROM teacher_subjects s WHERE s.teacher_id=t.id AND LOWER(s.subject)=:subject
          ))
        """;
    public Page<Hit> search(double latitude,double longitude,double radiusKm,String subject,int page,int size){
        // A latitude band is valid across the date line and at both poles; no longitude bounding box.
        double delta=Math.toDegrees(radiusKm/EARTH_RADIUS_KM)+0.000001;
        var params=new MapSqlParameterSource().addValue("lat",latitude).addValue("lon",longitude)
            .addValue("minLat",Math.max(-90,latitude-delta)).addValue("maxLat",Math.min(90,latitude+delta))
            .addValue("radius",radiusKm).addValue("subject",subject).addValue("limit",size).addValue("offset",page*size);
        String filtered=" FROM ("+BASE+") nearby WHERE nearby.distance_km <= :radius";
        Long count=jdbc.queryForObject("SELECT COUNT(*)"+filtered,params,Long.class);
        var rows=jdbc.query("SELECT nearby.teacher_id,nearby.distance_km"+filtered+
            " ORDER BY nearby.distance_km ASC,nearby.teacher_id ASC LIMIT :limit OFFSET :offset",params,
            (rs,n)->new Hit(rs.getLong("teacher_id"),rs.getDouble("distance_km")));
        return new PageImpl<>(rows,PageRequest.of(page,size),count==null?0:count);
    }
}
