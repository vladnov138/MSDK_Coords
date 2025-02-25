package com.example.msdk_coords.presentation.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.map.PlacemarkMapObject
import dji.common.mission.waypoint.Waypoint

class MapViewModel: ViewModel() {
    private val _isRouteCreationMode = MutableLiveData(false)
    val isRouteCreationMode: LiveData<Boolean> = _isRouteCreationMode

    private val _mapPlacemarks = MutableLiveData<List<PlacemarkMapObject>>(emptyList())
    val mapPlacemarks: LiveData<List<PlacemarkMapObject>> = _mapPlacemarks

    private val _selectedPlacemark = MutableLiveData<PlacemarkMapObject?>(null)
    val selectedPlacemark: LiveData<PlacemarkMapObject?> = _selectedPlacemark

    private val _droneWaypoints = MutableLiveData<List<Waypoint>>(emptyList())
    val droneWaypoints: LiveData<List<Waypoint>> = _droneWaypoints

    fun toggleRouteCreationMode() {
        _isRouteCreationMode.value = !_isRouteCreationMode.value!!
    }

    fun addPlacemark(placemark: PlacemarkMapObject) {
        _mapPlacemarks.value = _mapPlacemarks.value!! + placemark
    }

    fun removePlacemark(placemark: PlacemarkMapObject) {
        _mapPlacemarks.value = _mapPlacemarks.value!!.filter { it != placemark }
        if (_selectedPlacemark.value == placemark) {
            _selectedPlacemark.value = null
        }
    }

    fun selectPlacemark(placemark: PlacemarkMapObject?) {
        _selectedPlacemark.value = placemark
    }

//    fun addDroneWaypoint(point: Point) {
//        val waypoint = Waypoint(point.latitude.toFloat(), point.longitude.toFloat(), 30f)
//        _droneWaypoints.value = _droneWaypoints.value!! + waypoint
//    }
}