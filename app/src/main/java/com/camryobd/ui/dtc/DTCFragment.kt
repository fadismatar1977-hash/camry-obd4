package com.camryobd.ui.dtc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.camryobd.R
import com.camryobd.databinding.FragmentDtcBinding
import com.camryobd.model.ConnectionState
import com.camryobd.ui.MainViewModel
import kotlinx.coroutines.launch

class DTCFragment : Fragment() {

    private var _binding: FragmentDtcBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: DTCAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDtcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DTCAdapter()
        binding.recyclerDtc.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDtc.adapter = adapter

        binding.btnReadDtc.setOnClickListener {
            viewModel.readDTCs()
        }

        binding.btnClearDtc.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("مسح أكواد الأعطال")
                .setMessage("هل تريد مسح جميع أكواد الأعطال؟ تأكد من إصلاح المشكلة أولاً.")
                .setPositiveButton("مسح") { _, _ -> viewModel.clearDTCs() }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        lifecycleScope.launch {
            viewModel.dtcCodes.collect { codes ->
                adapter.submitList(codes)
                binding.tvNoDtc.visibility = if (codes.isEmpty()) View.VISIBLE else View.GONE
                binding.tvDtcCount.text = "عدد الأعطال: ${codes.size}"
            }
        }

        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                val connected = state == ConnectionState.CONNECTED
                binding.btnReadDtc.isEnabled = connected
                binding.btnClearDtc.isEnabled = connected
            }
        }

        lifecycleScope.launch {
            viewModel.statusMessage.collect { msg ->
                if (msg.isNotEmpty()) binding.tvStatus.text = msg
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
