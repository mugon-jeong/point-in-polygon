package com.example.backend

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Polygon

fun polygonToGeoJson(polygon: Polygon): Map<String, Any> {
    val coordinates = polygon.coordinates.map { coordinateToList(it) }

    return mapOf(
        "type" to "Polygon",
        "coordinates" to listOf(coordinates)
    )
}

fun coordinateToList(coordinate: Coordinate): List<Double> {
    return listOf(coordinate.x, coordinate.y)
}