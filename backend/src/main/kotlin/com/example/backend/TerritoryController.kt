package com.example.backend

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/territories")
class TerritoryController(
    private val territoryService: TerritoryService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTerritory(@RequestBody polygonRequest: PolygonRequest): Territory {
        return territoryService.createTerritory(polygonRequest)
    }

    @GetMapping("/{id}/within")
    fun isPointInsideTerritory(
        @PathVariable id: Long,
        @RequestParam lat: Double,
        @RequestParam long: Double
    ): Boolean {
        // Territory boundary 안에 좌표가 있는지 확인
        return territoryService.isPointInsideTerritory(id, lat, long)
    }

    // ST_Within을 사용하여 좌표가 다각형 안에 있는지 확인
    @GetMapping("/{id}/st-within")
    fun isPointWithinTerritory(
        @PathVariable id: Long,
        @RequestParam lat: Double,
        @RequestParam long: Double
    ): Boolean {
        return territoryService.isPointWithinTerritoryByDb(id, lat, long)
    }

    @GetMapping("/{id}")
    fun getTerritoryPolygon(@PathVariable id: Long): Map<String, Any> {
        // Territory 조회
        val territory = territoryService.getTerritory(id)

        // Polygon을 GeoJSON 형식으로 변환하여 응답
        return polygonToGeoJson(territory.boundary)
    }
}