package com.camryobd.ui.dtc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.camryobd.R
import com.camryobd.databinding.ItemDtcBinding
import com.camryobd.model.DTCCode
import com.camryobd.model.DTCSeverity

class DTCAdapter : ListAdapter<DTCCode, DTCAdapter.DTCViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DTCViewHolder {
        val binding = ItemDtcBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DTCViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DTCViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DTCViewHolder(private val binding: ItemDtcBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dtc: DTCCode) {
            binding.tvDtcCode.text = dtc.code
            binding.tvDtcDesc.text = dtc.description
            binding.tvDtcSystem.text = dtc.system

            val (colorRes, icon) = when (dtc.severity) {
                DTCSeverity.CRITICAL -> Pair(R.color.warning_red, "🔴")
                DTCSeverity.WARNING -> Pair(R.color.warning_yellow, "🟡")
                DTCSeverity.INFO -> Pair(R.color.info_blue, "🔵")
            }
            val color = binding.root.context.getColor(colorRes)
            binding.tvDtcCode.setTextColor(color)
            binding.tvSeverityIcon.text = icon
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DTCCode>() {
        override fun areItemsTheSame(a: DTCCode, b: DTCCode) = a.code == b.code
        override fun areContentsTheSame(a: DTCCode, b: DTCCode) = a == b
    }
}
