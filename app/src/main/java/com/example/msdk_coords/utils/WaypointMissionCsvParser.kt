package dji.midware

import dji.common.mission.waypoint.*
import java.io.BufferedReader
import java.io.File
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.io.readLines
import kotlin.text.split
import kotlin.text.toDouble
import kotlin.text.trim

object WaypointMissionCsvParser {

    fun parseCsvToMission(reader: BufferedReader): WaypointMission? {
        val waypoints = mutableListOf<Waypoint>()

        try {
            reader.forEachLine { line ->
                val parts = line.split(",").map { it.trim().toDouble() }
                if (parts.size >= 4) {
                    val latitude = parts[0]
                    val longitude = parts[1]
                    val altitude = parts[2].toFloat()
                    val heading = parts[3].toInt()

                    val wp = Waypoint(latitude, longitude, altitude)
                    wp.heading = heading
                    wp.gimbalPitch = -90f
                    wp.turnMode = WaypointTurnMode.CLOCKWISE
                    waypoints.add(wp)
                }
            }

            return WaypointMission.Builder()
                .waypointList(waypoints)
                .waypointCount(waypoints.size)
                .autoFlightSpeed(5f)
                .maxFlightSpeed(10f)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL)
                .finishedAction(WaypointMissionFinishedAction.NO_ACTION)
                .headingMode(WaypointMissionHeadingMode.USING_WAYPOINT_HEADING)
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}