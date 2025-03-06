package com.example.msdk_coords.presentation.ui.map

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
import androidx.lifecycle.LifecycleObserver
import com.example.msdk_coords.R
import com.example.msdk_coords.databinding.FragmentMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("fbc5d75a-a477-4d06-983d-9bb1eea91cc0")
        MapKitFactory.initialize(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _vectorDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.pin, null) as VectorDrawable
        _vectorDroneDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.navigation, null) as VectorDrawable
        _vectorHomeDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.home_position, null) as VectorDrawable

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
}