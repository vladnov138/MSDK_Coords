package com.example.msdk_ardupilot.presentation.ui.map

import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.msdk_coords.R
import com.example.msdk_coords.databinding.FragmentMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.LinearRing
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polygon
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectDragListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolygonMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.offline_cache.OfflineCacheManager
import com.yandex.mapkit.offline_cache.RegionListUpdatesListener
import com.yandex.mapkit.offline_cache.RegionState
import com.yandex.runtime.image.ImageProvider
import dji.common.flightcontroller.LocationCoordinate3D
import dji.common.model.LocationCoordinate2D

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()

    private var _mapView: MapView? = null
    private val mapView get() = _mapView!!

    private var inputListener: InputListener? = null

    private var _vectorDrawable: VectorDrawable? = null
    private val vectorDrawable get() = _vectorDrawable!!

    private var _vectorDroneDrawable: VectorDrawable? = null
    private val vectorDroneDrawable get() = _vectorDroneDrawable!!
    private var _drone: PlacemarkMapObject? = null
    private val drone get() = _drone!!

    private var _vectorHomeDrawable: VectorDrawable? = null
    private val vectorHomeDrawable get() = _vectorHomeDrawable!!
    private var _home: PlacemarkMapObject? = null
    private val home get() = _home!!

    private var _vectorCenterZoneDrawable: VectorDrawable? = null
    private val vectorCenterZoneDrawable get() = _vectorCenterZoneDrawable!!
    private var _vectorCornerZoneDrawable: VectorDrawable? = null
    private val vectorCornerZoneDrawable get() = _vectorCornerZoneDrawable!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val offlineCacheManager = MapKitFactory.getInstance().offlineCacheManager

        val regionId = 63
        val regionState = offlineCacheManager.getState(regionId)

        if (regionState != RegionState.COMPLETED && regionState != RegionState.DOWNLOADING) {
            offlineCacheManager.startDownload(regionId)
        }
        _vectorDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.pin, null) as VectorDrawable
        _vectorDroneDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.navigation, null) as VectorDrawable
        _vectorHomeDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.home_position, null) as VectorDrawable
        _vectorCenterZoneDrawable =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.center_zone_icon,
                null
            ) as VectorDrawable
        _vectorCornerZoneDrawable =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.corner_circle,
                null
            ) as VectorDrawable

        _mapView = binding.mapview
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(52.2978, 104.296),
                /* zoom = */ 17.0f,
                /* azimuth = */ 150.0f,
                /* tilt = */ 30.0f
            )
        )

        _home = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            vectorHomeDrawable.setTint(Color.MAGENTA)
            setIcon(ImageProvider.fromBitmap(vectorHomeDrawable.toBitmap()))
        }

        _drone = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            vectorDroneDrawable.setTint(Color.BLUE)
            setIcon(ImageProvider.fromBitmap(vectorDroneDrawable.toBitmap()))
            setIconStyle(
                IconStyle(
                    /* anchor = */ PointF(0.5f, 0.5f),
                    /* rotationType = */ RotationType.ROTATE,
                    /* zIndex = */ null,
                    /* flat = */ null,
                    /* visible = */ null,
                    /* scale = */ null,
                    /* tappableArea = */ null
                )
            )
        }

        binding.floatingActionButton.setOnClickListener { viewModel.toggleRouteCreationMode() }
        binding.floatingDeleteButton.setOnClickListener { removeSelectedPlacemark() }
        binding.floatingSendButton.setOnClickListener { viewModel.startRoute() }
        binding.floatingRTHButton.setOnClickListener { viewModel.startRTH() }
        binding.floatingCreateZoneButton.setOnClickListener { createRestrictZone() }
        binding.floatingCompassButton.setOnClickListener { compassHandler() }
        binding.floatingPositionUAVButton.setOnClickListener { holdCameraOnUAV() }

        viewModel.setupDroneLocationListener()
        observeViewModel()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        inputListener = object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                viewModel.selectPlacemark(null)
                viewModel.isRouteCreationMode.value?.let {
                    if (it) {
                        addPlacemark(point)
                    }
                }
            }

            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                Log.d("Map", "Long tap")
            }
        }
        mapView.mapWindow.map.addInputListener(inputListener!!)


    }

    private fun observeViewModel() {
        viewModel.isRouteCreationMode.observe(viewLifecycleOwner) {
            updateFabState(it)
            binding.floatingDeleteButton.visibility = View.INVISIBLE
            viewModel.selectPlacemark(null)
        }

        viewModel.location.observe(viewLifecycleOwner) {
            if (it != null) {
                updateDroneLocation(it)
            }
        }

        viewModel.heading.observe(viewLifecycleOwner) {
            if (it != null) {
                updateDroneHeading(it)
            }
        }

        viewModel.homePosition.observe(viewLifecycleOwner) {
            if (it != null) {
                updateHomeLocation(it)
            }
        }

//        viewModel.isMissionStarting.observe(viewLifecycleOwner) {
//            if (viewModel.isMissionStarting.value!!) {
//                binding.floatingSendButton.visibility = View.INVISIBLE
//                binding.floatingRTHButton.visibility = View.VISIBLE
//            } else {
//                binding.floatingSendButton.visibility = View.VISIBLE
//                binding.floatingRTHButton.visibility = View.INVISIBLE
//            }
//        }

        viewModel.selectedPlacemark.observe(viewLifecycleOwner) { placemark ->
            binding.floatingDeleteButton.visibility =
                if (placemark == null) View.INVISIBLE else View.VISIBLE
            placemark?.let { highlightPlacemark(it) }
        }
    }

    private fun addPlacemark(point: Point) {
        val placemark = mapView.mapWindow.map.mapObjects.addPlacemark().apply {
            geometry = point
            isDraggable = true
            vectorDrawable.setTint(Color.BLUE)
            setIcon(ImageProvider.fromBitmap(vectorDrawable.toBitmap()))
            setText("${viewModel.mapPlacemarks.value!!.size}", TextStyle().apply {
                size = 10f
                placement = TextStyle.Placement.BOTTOM
                offset = 5f
            })
        }
        viewModel.addPlacemark(placemark, binding.altPicker.altitude)
        placemark.addTapListener { _, _ ->
            viewModel.selectPlacemark(placemark)
            true
        }
    }

    private fun highlightPlacemark(placemark: PlacemarkMapObject) {
        vectorDrawable.setTint(Color.BLUE)
        viewModel.mapPlacemarks.value?.forEach { it.setIcon(ImageProvider.fromBitmap(vectorDrawable.toBitmap())) }

        vectorDrawable.setTint(Color.RED)
        placemark.setIcon(ImageProvider.fromBitmap(vectorDrawable.toBitmap()))
    }

    private fun removeSelectedPlacemark() {
        viewModel.selectedPlacemark.value?.let {
            mapView.mapWindow.map.mapObjects.remove(it)
            viewModel.removePlacemark(it)
            viewModel.mapPlacemarks.value?.forEachIndexed { index, placemarkMapObject ->
                placemarkMapObject.setText("$index")
            }
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun updateFabState(isActive: Boolean) {
        val animator = ViewCompat.animate(binding.floatingActionButton)
            .setDuration(300L)
            .scaleX(0.9f)
            .scaleY(0.9f)

        animator.withEndAction {
            binding.floatingActionButton.isActivated = isActive
            ViewCompat.animate(binding.floatingActionButton)
                .scaleX(1f)
                .scaleY(1f)
                .start()
        }
        animator.start()
    }

    private fun updateDroneLocation(location: LocationCoordinate3D) {
//        Log.d("Map", "Drone location updated: ${location}")
        drone.geometry = Point(location.latitude, location.longitude)
    }

    private fun updateDroneHeading(heading: Double) {
        Log.d("Map", "Drone heading updated: ${heading}")
        drone.direction = heading.toFloat()
    }

    private fun updateHomeLocation(location: LocationCoordinate2D) {
        home.geometry = Point(location.latitude, location.longitude)
    }

    private var rectZone: PolygonMapObject? = null
    private var centerPlacemark: PlacemarkMapObject? = null
    private val cornerPlacemarks = mutableListOf<PlacemarkMapObject>()

    fun createRestrictZone() {
        val centerPoint = mapView.mapWindow.map.cameraPosition.target
        val latOffset = 0.002
        val lonOffset = 0.003
        val corners = listOf(
            Point(centerPoint.latitude + latOffset, centerPoint.longitude - lonOffset),
            Point(centerPoint.latitude + latOffset, centerPoint.longitude + lonOffset),
            Point(centerPoint.latitude - latOffset, centerPoint.longitude + lonOffset),
            Point(centerPoint.latitude - latOffset, centerPoint.longitude - lonOffset)
        )

        currentCorners = corners.toMutableList()

        val rectGeometry = Polygon(LinearRing(corners + corners.first()), emptyList())
        val mapObjects = mapView.mapWindow.map.mapObjects

        rectZone = mapObjects.addPolygon(rectGeometry).apply {
            strokeWidth = 2.0f
            strokeColor = Color.RED
            fillColor = 0x55FF0000
        }

        val center = calculateCenter(corners)
        centerPlacemark = mapObjects.addPlacemark(center).apply {
            isDraggable = true
            val handleBitmap = vectorCenterZoneDrawable.apply { setTint(Color.RED) }.toBitmap()
            setIcon(ImageProvider.fromBitmap(handleBitmap))
            setDragListener(centerDragListener)
        }

        cornerPlacemarks.clear()
        corners.forEachIndexed { index, corner ->
            val placemark = mapObjects.addPlacemark(corner).apply {
                isDraggable = true
                val handleBitmap = vectorCornerZoneDrawable.apply { setTint(Color.RED) }.toBitmap()
                setIcon(ImageProvider.fromBitmap(handleBitmap))
                setDragListener(object : MapObjectDragListener {
                    override fun onMapObjectDragStart(mapObject: MapObject) {}

                    override fun onMapObjectDrag(mapObject: MapObject, point: Point) {
                        onCornerDragged(index, point)
                    }

                    override fun onMapObjectDragEnd(mapObject: MapObject) {}
                })
            }
            cornerPlacemarks.add(placemark)
        }
    }

    private val centerDragListener = object : MapObjectDragListener {
        private var previousDragPoint: Point? = null

        override fun onMapObjectDragStart(mapObject: MapObject) {
            previousDragPoint = (mapObject as PlacemarkMapObject).geometry
        }

        override fun onMapObjectDrag(mapObject: MapObject, point: Point) {
            val prevPoint = previousDragPoint ?: return
            val deltaLat = point.latitude - prevPoint.latitude
            val deltaLon = point.longitude - prevPoint.longitude

            val newCorners = currentCorners.map { p ->
                Point(p.latitude + deltaLat, p.longitude + deltaLon)
            }
            updatePolygon(newCorners, updateCenter = false)

            previousDragPoint = point
        }

        override fun onMapObjectDragEnd(mapObject: MapObject) {
            previousDragPoint = null
        }
    }

    private fun onCornerDragged(index: Int, newPoint: Point) {
        val newCorners = currentCorners.toMutableList()
        newCorners[index] = newPoint
        updatePolygon(newCorners)
    }

    private var currentCorners: MutableList<Point> = mutableListOf()

    private fun updatePolygon(corners: List<Point>, updateCenter: Boolean = true) {
        currentCorners = corners.toMutableList()
        cornerPlacemarks.forEachIndexed { i, placemark ->
            placemark.geometry = corners[i]
        }
        rectZone?.geometry = Polygon(LinearRing(corners + corners.first()), emptyList())
        if (updateCenter) {
            val newCenter = calculateCenter(corners)
            centerPlacemark?.geometry = newCenter
        }
    }

    private fun calculateCenter(corners: List<Point>): Point {
        val lat = corners.sumOf { it.latitude } / corners.size
        val lon = corners.sumOf { it.longitude } / corners.size
        return Point(lat, lon)
    }

    private fun compassHandler() {
        mapView.mapWindow.map.move(
            CameraPosition(
                mapView.mapWindow.map.cameraPosition.target,
                mapView.mapWindow.map.cameraPosition.zoom,
                0f,
                mapView.mapWindow.map.cameraPosition.tilt
            )
        )
    }

    private fun holdCameraOnUAV() {
        viewModel.location.value?.let { location ->
            mapView.mapWindow.map.move(
                CameraPosition(
                    Point(location.latitude, location.longitude),
                    mapView.mapWindow.map.cameraPosition.zoom,
                    mapView.mapWindow.map.cameraPosition.azimuth,
                    mapView.mapWindow.map.cameraPosition.tilt
                )
            )
        }
    }
}