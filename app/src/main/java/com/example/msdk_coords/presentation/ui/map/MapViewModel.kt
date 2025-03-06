package com.example.msdk_coords.presentation.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.msdk_coords.graph_algorithms.AStarAlgo
import com.example.msdk_coords.utils.GraphConverter
import com.yandex.mapkit.map.PlacemarkMapObject
import dji.common.error.DJIError
import dji.common.flightcontroller.FlightControllerState
import dji.common.flightcontroller.LocationCoordinate3D
import dji.common.mission.waypoint.Waypoint
import dji.common.mission.waypoint.WaypointMission
import dji.common.mission.waypoint.WaypointMissionDownloadEvent
import dji.common.mission.waypoint.WaypointMissionExecutionEvent
import dji.common.mission.waypoint.WaypointMissionFinishedAction
import dji.common.mission.waypoint.WaypointMissionFlightPathMode
import dji.common.mission.waypoint.WaypointMissionHeadingMode
import dji.common.mission.waypoint.WaypointMissionUploadEvent
import dji.common.model.LocationCoordinate2D
import dji.sdk.mission.MissionControl
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener
import dji.sdk.products.Aircraft
import dji.sdk.sdkmanager.DJISDKManager

class MapViewModel : ViewModel() {
    private val _isRouteCreationMode = MutableLiveData(false)
    val isRouteCreationMode: LiveData<Boolean> = _isRouteCreationMode

    private val _mapPlacemarks = MutableLiveData<List<PlacemarkMapObject>>(emptyList())
    val mapPlacemarks: LiveData<List<PlacemarkMapObject>> = _mapPlacemarks

    private val _selectedPlacemark = MutableLiveData<PlacemarkMapObject?>(null)
    val selectedPlacemark: LiveData<PlacemarkMapObject?> = _selectedPlacemark

    private val _droneWaypoints = MutableLiveData<List<LocationCoordinate3D>>(emptyList())
    val droneWaypoints: LiveData<List<LocationCoordinate3D>> = _droneWaypoints

    private val _location = MutableLiveData<LocationCoordinate3D>(null)
    val location: LiveData<LocationCoordinate3D> = _location

    private val _heading = MutableLiveData<Double>(null)
    val heading: LiveData<Double> = _heading

    private val _homePosition = MutableLiveData<LocationCoordinate2D>(null)
    val homePosition: LiveData<LocationCoordinate2D> = _homePosition

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
        val aircraft = DJISDKManager.getInstance().product as? Aircraft
        val flightController = aircraft?.flightController
        val lat = location.value?.latitude
        val lon = location.value?.longitude
        _homePosition.value = LocationCoordinate2D(lat!!, lon!!)
        flightController?.setHomeLocation(homePosition.value!!) { error ->
            if (error == null) {
                Log.d("DJI", "Home Position установлена вручную")
            } else {
                Log.e("DJI", "Ошибка: ${error.description}")
            }
        }

        val waypoints = _droneWaypoints.value ?: return
        val graph = GraphConverter().buildGraph(homePosition.value!!, droneWaypoints.value!!)
        val (shortestWay, _) = AStarAlgo(graph).findPath()
        val waypointList = mutableListOf<Waypoint>()
        Log.d("Path", "${waypoints.size}, ${shortestWay}")
        for (i in shortestWay) {
            if (i != 0) {
                val point = waypoints[i - 1]
                waypointList.add(Waypoint(point.latitude, point.longitude, point.altitude))
            }
        }
        val mission = WaypointMission.Builder().apply {
            waypointList(waypointList)
            waypointCount(waypointList.size)
            autoFlightSpeed(10.0f)
            maxFlightSpeed(15.0f)
            headingMode(WaypointMissionHeadingMode.AUTO)
            flightPathMode(WaypointMissionFlightPathMode.NORMAL)
            finishedAction(WaypointMissionFinishedAction.GO_HOME)
        }.build()
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
        val err = operator.loadMission(mission)
        if (err != null) {
            Log.e("DJI", "$err, ${err.description}")
        }
        operator.uploadMission { error ->
            if (error == null) {
                Log.d("DJI", "Mission uploaded")
                operator.startMission { startError ->
                    Log.d("DJI", "Start result: $startError")
                }
            } else {
                Log.e("DJI", "Upload error: ${error.description}")
            }
        }
        operator.startMission { p0 -> Log.d("DJI", "Result: $p0") }
    }

    fun setupDroneLocationListener() {
        val aircraft = DJISDKManager.getInstance().product as? Aircraft
        if (aircraft != null && aircraft.isConnected) {
            val flightController = aircraft.flightController
            flightController?.setStateCallback { state: FlightControllerState ->
                val location = state.aircraftLocation
                if (!location.latitude.isNaN() && !location.longitude.isNaN()) {
                    _location.postValue(location)
                    _heading.postValue(state.attitude.yaw)
                }
            }
        }
    }

    fun startRTH() {

    }
}