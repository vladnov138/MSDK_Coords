package com.example.msdk_coords.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.msdk_coords.R
import com.example.msdk_coords.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _viewBinding: ActivityMainBinding? = null
    private val binding get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val bottomNavView = binding.bottomNavigationView
        val navController = navHostFragment.navController
        bottomNavView.setupWithNavController(navController)

//        DJISDKManager.getInstance().registerApp(application, object : DJISDKManager.SDKManagerCallback {
//            override fun onRegister(result: DJIError?) {
//                if (result == DJISDKError.REGISTRATION_SUCCESS) {
//                    Log.d("DJI", "SDK успешно зарегистрирован")
//                    Toast.makeText(baseContext, "Registrated", Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e("DJI", "Ошибка: ${result?.description}")
//                    Toast.makeText(baseContext, "${result?.description}", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onProductDisconnect() {
//                Log.i("DJI", "Product disconnected")
//                runOnUiThread {
////                    binding.statusTv.text = "Disconnected";
//                }
//                Toast.makeText(baseContext, "Disconnected", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onProductConnect(p0: BaseProduct?) {
//                Log.i("DJI", "Product connected")
//                runOnUiThread {
////                    binding.statusTv.text = "Connected"
//                }
//                val aircraft = DJISDKManager.getInstance().product as? Aircraft
//                aircraft?.flightController?.setStateCallback { state ->
//                    val location = state.aircraftLocation
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//                    val altitude = location.altitude
//                    val gpsSignalLvl = state.gpsSignalLevel
////                    val windSpeed = state.wind
//                    runOnUiThread {
////                        binding.gpsSignalTv.text = "${gpsSignalLvl}"
////                        binding.latTv.text = "${latitude}"
////                        binding.lonTv.text = "${longitude}"
////                        binding.altTv.text = "${altitude}"
//                    }
//                    Log.d("DJI", "Latitude: $latitude, Longitude: $longitude, Altitude: $altitude")
//                }
//            }
//
//            override fun onProductChanged(p0: BaseProduct?) {
//                Log.i("DJI", "Product changed")
//            }
//
//            override fun onComponentChange(
//                p0: BaseProduct.ComponentKey?,
//                p1: BaseComponent?,
//                p2: BaseComponent?
//            ) {
//                Log.i("DJI", "Component changed")
//            }
//
//            override fun onInitProcess(p0: DJISDKInitEvent?, p1: Int) {
//                Log.i("DJI", "init process")
//            }
//
//            override fun onDatabaseDownloadProgress(p0: Long, p1: Long) {
//                Log.i("DJI", "Database download progress")
//            }
//        })
    }
}