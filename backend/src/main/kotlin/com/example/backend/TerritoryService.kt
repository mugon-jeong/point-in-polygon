package com.example.backend

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.operation.valid.IsValidOp
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TerritoryService(
    private val territoryRepository: TerritoryRepository
) {

    private val geometryFactory = GeometryFactory()

    @Transactional
    fun createTerritory(polygonRequest: PolygonRequest): Territory {
        // 좌표 검증
        validatePolygon(polygonRequest.coordinates)

        // 좌표를 Coordinate 타입으로 변환 (경도, 위도를 사용)
        val coordinates =
            polygonRequest.coordinates.map { (lng, lat) -> Coordinate(lng, lat) }.toTypedArray()

        // LinearRing 생성
        val linearRing = geometryFactory.createLinearRing(coordinates)
        if (!linearRing.isValid) {
            val isValidOp = IsValidOp(linearRing)
            val validationError = isValidOp.getValidationError()
            throw RuntimeException("validationError: ${validationError.message}")
        }

        // 다각형 생성 (LinearRing을 사용)
        val polygon = geometryFactory.createPolygon(linearRing)

        if (!polygon.isValid) {
            val isValidOp = IsValidOp(polygon)
            val validationError = isValidOp.getValidationError()
            throw RuntimeException("validationError: ${validationError.message}")
        }

        // Territory 엔티티 생성 및 저장
        val territory = Territory(boundary = polygon)
        return territoryRepository.save(territory)
    }

    @Transactional(readOnly = true)
    fun isPointInsideTerritory(territoryId: Long, lat: Double, lon: Double): Boolean {
        // Territory 엔티티 조회
        val territory = territoryRepository.findById(territoryId).orElseThrow {
            IllegalArgumentException("Territory not found with id: $territoryId")
        }
        validateCoordinate(listOf(lon, lat))
        // 좌표를 Coordinate 타입의 Point로 변환 (경도, 위도를 사용)
        val point = geometryFactory.createPoint(Coordinate(lon, lat))

        // 다각형(boundary) 안에 point가 있는지 확인
        return territory.boundary.contains(point)
    }

    @Transactional(readOnly = true)
    fun isPointWithinTerritoryByDb(territoryId: Long, lat: Double, lon: Double): Boolean {
        validateCoordinate(listOf(lon, lat))
        // 좌표를 Point로 변환
        val point = geometryFactory.createPoint(Coordinate(lon, lat))
        // ST_Within과 유사하게 폴리곤 내에 좌표가 있는지 확인
        return territoryRepository.isPointWithinTerritory(territoryId, point)
    }

    @Transactional(readOnly = true)
    fun getTerritory(id: Long): Territory {
        return territoryRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Territory not found with id: $id") }
    }

    /**
     * 좌표가 유효한지 확인하는 메서드
     */
    fun validatePolygon(coordinates: List<List<Double>>) {
        // 1. 최소 3개의 좌표가 있어야 함
        if (coordinates.size < 4) {
            throw RuntimeException("Invalid polygon: A polygon must have at least 3 points.")
        }

        // 2. 각 좌표가 유효한지 확인 (경도와 위도의 범위를 체크)
        for (coord in coordinates) {
            validateCoordinate(coord)
        }

        // 3. 첫 번째 좌표와 마지막 좌표가 동일해야 함 (다각형을 닫기 위해)
        if (coordinates.first() != coordinates.last()) {
            throw RuntimeException("Invalid polygon: Polygon must be closed (first and last points must be the same).")
        }
    }

    private fun validateCoordinate(coord: List<Double>) {
        if (coord.size != 2 || coord[0] !in -180.0..180.0 || coord[1] !in -90.0..90.0) {
            throw RuntimeException("Invalid polygon: Coordinates out of bounds.")
        }
    }
}