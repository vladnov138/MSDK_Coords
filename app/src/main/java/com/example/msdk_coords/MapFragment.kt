package com.example.msdk_coords

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.example.msdk_coords.databinding.FragmentMapBinding
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapView: MapView
    private var isRouteCreationMode = false
    private val dronePoints: MutableList<PlacemarkMapObject> = mutableListOf()
    private var inputListener: InputListener? = null

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
        binding.floatingActionButton.bringToFront()


        mapView = binding.mapview
        mapView.mapWindow.map.move(
            CameraPosition(
                Point(52.2978, 104.296),
                /* zoom = */ 17.0f,
                /* azimuth = */ 150.0f,
                /* tilt = */ 30.0f
            )
        )


        binding.floatingActionButton.setOnClickListener {
            isRouteCreationMode = !isRouteCreationMode
            updateFabState()

            if (isRouteCreationMode) {
//                startRouteCreation()
            } else {
//                finishRouteCreation()
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        inputListener = object : InputListener {
            override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                Log.d("Map", "${isRouteCreationMode}")
                if (!isRouteCreationMode) return

//                updateRouteDisplay()

                // Добавляем нумерованную метку
                val placemark = map.mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(ImageProvider.fromResource(context, com.yandex.maps.mobile.R.drawable.notification_action_background))
                    setText("${dronePoints.size} Special place")
                }
                dronePoints.add(placemark)
                Log.d("Map", "${dronePoints.size} ${dronePoints}")
            }

            override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                Log.d("Map", "Long tap")
            }
        }
        mapView.mapWindow.map.addInputListener(inputListener!!)
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun updateFabState() {
        // Анимация изменения цвета
        val animator = ViewCompat.animate(binding.floatingActionButton)
            .setDuration(300L)
            .scaleX(0.9f)
            .scaleY(0.9f)

        animator.withEndAction {
            binding.floatingActionButton.isActivated = isRouteCreationMode
            ViewCompat.animate(binding.floatingActionButton)
                .scaleX(1f)
                .scaleY(1f)
                .start()
        }
        animator.start()
    }
}