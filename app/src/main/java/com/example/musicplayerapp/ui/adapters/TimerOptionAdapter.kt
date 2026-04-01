package com.example.musicplayerapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayerapp.databinding.ItemSongBinding
import com.example.musicplayerapp.databinding.ItemTimerOptionBinding
import com.example.musicplayerapp.models.TimerOption

class TimerOptionAdapter(
    private val items: List<TimerOption>,
    private val onItemClickListener: ((TimerOption) -> Unit)
) : RecyclerView.Adapter<TimerOptionAdapter.TimerOptionViewHolder>() {

    inner class TimerOptionViewHolder(val binding: ItemTimerOptionBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerOptionViewHolder {
        val binding = ItemTimerOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimerOptionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TimerOptionViewHolder, position: Int) {
        var item = items[position]
        holder.binding.apply {
            txtTime.text = item.label
            root.setOnClickListener {
                onItemClickListener?.let { it(item) }
            }
        }
    }
}