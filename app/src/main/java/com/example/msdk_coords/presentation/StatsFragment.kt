package com.example.msdk_coords.presentation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.msdk_coords.databinding.FragmentStatsBinding
import dji.common.battery.BatteryState
import dji.common.battery.BatteryState.Callback
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.products.Aircraft
import dji.sdk.sdkmanager.DJISDKInitEvent
import dji.sdk.sdkmanager.DJISDKManager
class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater)
        DJISDKManager.getInstance().registerApp(requireContext().applicationContext, object : DJISDKManager.SDKManagerCallback {
            override fun onRegister(result: DJIError?) {
                if (result == DJISDKError.REGISTRATION_SUCCESS) {
                    Log.d("DJI", "SDK успешно зарегистрирован")
                    Toast.makeText(context, "Registrated", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("DJI", "Ошибка: ${result?.description}")
                    Toast.makeText(context, "${result?.description}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onProductDisconnect() {
                Log.i("DJI", "Product disconnected")
                binding.root.post {
                    binding.statusTv.text = "Disconnected";
                }
                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
            }

            override fun onProductConnect(p0: BaseProduct?) {
                Log.i("DJI", "Product connected")
                binding.root.post {
                    binding.statusTv.text = "Connected"
                }
                val aircraft = DJISDKManager.getInstance().product as? Aircraft
                aircraft?.flightController?.setStateCallback { state ->
                    val location = state.aircraftLocation
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val altitude = location.altitude
                    val gpsSignalLvl = state.gpsSignalLevel
//                    val windSpeed = state.wind
                    binding.root.post {
                        binding.gpsSignalTv.text = "${gpsSignalLvl}"
                        binding.latTv.text = "${latitude}"
                        binding.lonTv.text = "${longitude}"
                        binding.altTv.text = "${altitude}"
                    }
                    Log.d("DJI", "Latitude: $latitude, Longitude: $longitude, Altitude: $altitude")
                }
                aircraft?.getBattery()?.setStateCallback(object : Callback {
                    override fun onUpdate(state: BatteryState?) {
                        if (state != null) {
                            val percent = state.chargeRemainingInPercent
                            val volt = state.voltage / 1000f
                            binding.root.post {
                                binding.batteryTv.text = "$percent%"
                                binding.batteryVoltTv.text = "$volt V"
                            }
                        }
                    }
                })
            }

            override fun onProductChanged(p0: BaseProduct?) {
                Log.i("DJI", "Product changed")
            }

            override fun onComponentChange(
                p0: BaseProduct.ComponentKey?,
                p1: BaseComponent?,
                p2: BaseComponent?
            ) {
                Log.i("DJI", "Component changed")
            }

            override fun onInitProcess(p0: DJISDKInitEvent?, p1: Int) {
                Log.i("DJI", "init process")
            }

            override fun onDatabaseDownloadProgress(p0: Long, p1: Long) {
                Log.i("DJI", "Database download progress")
            }
        })

        return binding.root
    }
}