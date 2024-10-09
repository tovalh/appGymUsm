package com.example.gymapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlot>,
    private val onItemClick: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val spotsTextView: TextView = view.findViewById(R.id.spotsTextView)
        val cardView: MaterialCardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeSlot = timeSlots[position]
        holder.timeTextView.text = timeSlot.startTime
        holder.spotsTextView.text = "Cupos disponibles: ${timeSlot.availableSpots}"

        holder.cardView.isChecked = timeSlot.isSelected
        holder.cardView.setOnClickListener {
            if (timeSlot.availableSpots > 0) {
                onItemClick(timeSlot)
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "No hay cupos disponibles",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Deshabilitar la tarjeta si no hay cupos
        holder.cardView.isEnabled = timeSlot.availableSpots > 0
    }

    override fun getItemCount() = timeSlots.size
}