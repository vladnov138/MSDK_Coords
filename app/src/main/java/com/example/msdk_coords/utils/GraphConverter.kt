package com.example.msdk_coords.utils

import dji.common.flightcontroller.LocationCoordinate3D
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GraphConverter {
    private fun calculateHaversineDistance(p1: LocationCoordinate3D, p2: LocationCoordinate3D): Double {
        val R = 6378137.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) *
                cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun calculate3dDistance(p1: LocationCoordinate3D, p2: LocationCoordinate3D): Double {
        // Горизонтальное расстояние по формуле гаверсинусов
        val horizontalDist = calculateHaversineDistance(p1, p2)
        val verticalDist = abs(p1.altitude - p2.altitude)
        return sqrt(horizontalDist.pow(2) + verticalDist.pow(2))
    }

    fun buildGraph(waypoints: List<LocationCoordinate3D>): Array<DoubleArray> {
        val graph = Array(waypoints.size) { DoubleArray(waypoints.size) }
        for (i in waypoints.indices) {
            for (j in i + 1 until waypoints.indices.last) {
                val distance = calculate3dDistance(waypoints[i], waypoints[j])
                graph[i][j] = distance
                graph[j][i] = distance
            }
        }
        return graph
    }
}