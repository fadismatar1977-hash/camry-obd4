package com.camryobd.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.camryobd.databinding.FragmentSettingsBinding
import com.camryobd.model.ConnectionState
import com.camryobd.ui.MainViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadPairedDevices()

        lifecycleScope.launch {
            viewModel.pairedDevices.collect { devices ->
                val names = devices.map { "${it.name}\n${it.address}" }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, names)
                binding.listDevices.adapter = adapter
                binding.tvNoDevices.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE

                binding.listDevices.setOnItemClickListener { _, _, position, _ ->
                    viewModel.connectToDevice(devices[position].address)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                val isConnected = state == ConnectionState.CONNECTED
                binding.btnDisconnect.isEnabled = isConnected
                binding.btnConnect.isEnabled = !isConnected && state != ConnectionState.CONNECTING

                binding.tvConnectionState.text = when (state) {
                    ConnectionState.CONNECTED -> "✅ متصل"
                    ConnectionState.CONNECTING -> "⏳ جارٍ الاتصال..."
                    ConnectionState.DISCONNECTED -> "❌ غير متصل"
                    ConnectionState.ERROR -> "⚠️ خطأ في الاتصال"
                }
            }
        }

        binding.btnDisconnect.setOnClickListener { viewModel.disconnect() }
        binding.btnConnect.setOnClickListener {
            // Connect to selected device
            val pos = binding.listDevices.checkedItemPosition
            val devices = viewModel.pairedDevices.value
            if (pos >= 0 && pos < devices.size) {
                viewModel.connectToDevice(devices[pos].address)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
