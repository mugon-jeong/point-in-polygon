package com.example.backend

import org.locationtech.jts.geom.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TerritoryRepository : JpaRepository<Territory, Long> {
    // ST_Within 함수로 좌표가 폴리곤 안에 있는지 확인 (SRID 4326으로 설정)
    @Query("SELECT CASE WHEN ST_Within(ST_SetSRID(:point, 4326), t.boundary) = true THEN true ELSE false END FROM Territory t WHERE t.id = :id")
    fun isPointWithinTerritory(@Param("id") id: Long, @Param("point") point: Point): Boolean
}