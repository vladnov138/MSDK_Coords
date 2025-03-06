package com.example.msdk_coords.utils

import android.util.Log
import dji.common.flightcontroller.LocationCoordinate3D
import dji.common.model.LocationCoordinate2D
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

    fun buildGraph(homePosition: LocationCoordinate2D, waypoints: List<LocationCoordinate3D>): Array<DoubleArray> {
        val graph = Array(waypoints.size + 1) { DoubleArray(waypoints.size + 1) } // + homePosition
        val homePosition3d = LocationCoordinate3D(homePosition.latitude, homePosition.longitude, waypoints[0].altitude)
        // Заполняем расстояния от home к всем точкам и обратно
        for (j in 1 until graph.size) {
            val distance = calculate3dDistance(homePosition3d, waypoints[j - 1])
            graph[0][j] = distance
            graph[j][0] = distance
        }

        for (i in 1 until graph.size) {
            for (j in i + 1 until graph.size) {
                val distance = calculate3dDistance(waypoints[i - 1], waypoints[j - 1])
                graph[i][j] = distance
                graph[j][i] = distance
            }
        }
        return graph
    }
}