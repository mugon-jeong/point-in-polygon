package com.example.backend

import jakarta.persistence.*
import org.locationtech.jts.geom.Polygon

@Entity
class Territory(
    boundary: Polygon
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    var boundary: Polygon = boundary
        private set
}