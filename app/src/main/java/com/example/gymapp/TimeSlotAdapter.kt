package com.example.gymapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlot>,
    private val onTimeSlotClick: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startTime: TextView = view.findViewById(R.id.tvStartTime)
        val endTime: TextView = view.findViewById(R.id.tvEndTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_slot_item, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.startTime.text = timeSlot.startTime
        holder.endTime.text = timeSlot.endTime

        holder.itemView.setOnClickListener {
            onTimeSlotClick(timeSlot)
        }

        holder.itemView.setBackgroundResource(
            if (timeSlot.isSelected) android.R.color.darker_gray
            else android.R.color.white
        )
    }

    override fun getItemCount() = timeSlots.size
}