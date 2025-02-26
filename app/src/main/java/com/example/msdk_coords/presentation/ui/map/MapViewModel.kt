package com.example.msdk_coords.presentation.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.map.PlacemarkMapObject
import dji.common.error.DJIError
import dji.common.flightcontroller.LocationCoordinate3D
import dji.common.mission.waypoint.Waypoint
import dji.common.mission.waypoint.WaypointMission
import dji.common.mission.waypoint.WaypointMissionDownloadEvent
import dji.common.mission.waypoint.WaypointMissionExecutionEvent
import dji.common.mission.waypoint.WaypointMissionFlightPathMode
import dji.common.mission.waypoint.WaypointMissionHeadingMode
import dji.common.mission.waypoint.WaypointMissionUploadEvent
import dji.sdk.mission.MissionControl
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener

class MapViewModel : ViewModel() {
    private val _isRouteCreationMode = MutableLiveData(false)
    val isRouteCreationMode: LiveData<Boolean> = _isRouteCreationMode

    private val _mapPlacemarks = MutableLiveData<List<PlacemarkMapObject>>(emptyList())
    val mapPlacemarks: LiveData<List<PlacemarkMapObject>> = _mapPlacemarks

    private val _selectedPlacemark = MutableLiveData<PlacemarkMapObject?>(null)
    val selectedPlacemark: LiveData<PlacemarkMapObject?> = _selectedPlacemark

    private val _droneWaypoints = MutableLiveData<List<LocationCoordinate3D>>(emptyList())
    val droneWaypoints: LiveData<List<LocationCoordinate3D>> = _droneWaypoints

    fun toggleRouteCreationMode() {
        _isRouteCreationMode.value = !_isRouteCreationMode.value!!
    }

    fun addPlacemark(placemark: PlacemarkMapObject, altitude: Float) {
        _mapPlacemarks.value = _mapPlacemarks.value!! + placemark
        val lat = placemark.geometry.latitude
        val lon = placemark.geometry.longitude
        val waypoint = LocationCoordinate3D(lat, lon, altitude)
        _droneWaypoints.value = _droneWaypoints.value!! + waypoint
    }

    fun removePlacemark(placemark: PlacemarkMapObject) {
        _mapPlacemarks.value = _mapPlacemarks.value!!.filter { it != placemark }
        val lat = placemark.geometry.latitude
        val lon = placemark.geometry.longitude
        _droneWaypoints.value =
            _droneWaypoints.value!!.filter { it.latitude != lat && it.longitude != lon }
        if (_selectedPlacemark.value == placemark) {
            _selectedPlacemark.value = null
        }
    }

    fun selectPlacemark(placemark: PlacemarkMapObject?) {
        _selectedPlacemark.value = placemark
    }

    fun startRoute() {
        val waypoints = _droneWaypoints.value ?: return
        val waypointList = waypoints.map {
            Waypoint(it.latitude, it.longitude, it.altitude)
        }
        val mission = WaypointMission(WaypointMission.Builder().apply {
            waypointList(waypointList)
            headingMode(WaypointMissionHeadingMode.USING_WAYPOINT_HEADING)
            flightPathMode(WaypointMissionFlightPathMode.NORMAL)
        })
        val operator = MissionControl.getInstance().waypointMissionOperator
        operator.addListener(object : WaypointMissionOperatorListener {
            override fun onDownloadUpdate(p0: WaypointMissionDownloadEvent) {
                Log.d("DJI", "Download update")
            }

            override fun onUploadUpdate(p0: WaypointMissionUploadEvent) {
                Log.d("DJI", "Upload update")
            }

            override fun onExecutionUpdate(p0: WaypointMissionExecutionEvent) {
                Log.d("DJI", "Execution update")
            }

            override fun onExecutionStart() {
                Log.d("DJI", "Execution start")
            }

            override fun onExecutionFinish(p0: DJIError?) {
                Log.d("DJI", "Mission started")
            }
        })
//        operator.startMission()
    }
}