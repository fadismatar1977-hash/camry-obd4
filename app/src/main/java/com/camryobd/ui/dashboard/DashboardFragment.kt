package com.camryobd.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.camryobd.databinding.FragmentDashboardBinding
import com.camryobd.model.ConnectionState
import com.camryobd.ui.MainViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.liveData.collect { data ->
                binding.apply {
                    tvRpm.text = "${data.rpm}"
                    tvSpeed.text = "${data.speed}"
                    tvCoolantTemp.text = "${data.coolantTemp}°C"
                    tvThrottle.text = "${"%.1f".format(data.throttlePosition)}%"
                    tvEngineLoad.text = "${"%.1f".format(data.engineLoad)}%"
                    tvFuelLevel.text = "${"%.1f".format(data.fuelLevel)}%"
                    tvIntakeTemp.text = "${data.intakeAirTemp}°C"

                    progressRpm.progress = (data.rpm / 80).coerceIn(0, 100)
                    progressThrottle.progress = data.throttlePosition.toInt().coerceIn(0, 100)
                    progressEngineLoad.progress = data.engineLoad.toInt().coerceIn(0, 100)
                    progressFuel.progress = data.fuelLevel.toInt().coerceIn(0, 100)

                    // Warning colors
                    val coolantColor = when {
                        data.coolantTemp > 100 -> requireContext().getColor(com.camryobd.R.color.warning_red)
                        data.coolantTemp > 90 -> requireContext().getColor(com.camryobd.R.color.warning_yellow)
                        else -> requireContext().getColor(com.camryobd.R.color.value_normal)
                    }
                    tvCoolantTemp.setTextColor(coolantColor)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                val isConnected = state == ConnectionState.CONNECTED
                binding.layoutConnected.visibility = if (isConnected) View.VISIBLE else View.GONE
                binding.layoutDisconnected.visibility = if (!isConnected) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
