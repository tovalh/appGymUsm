package com.example.gymapp


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appgymusm.model.BloqueHorario

class MyAdapter(
    private val bloquesList: List<BloqueHorario>,
    private val onBloqueClickCallback: (BloqueHorario) -> Unit
) : RecyclerView.Adapter<MyAdapter.BloqueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return BloqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: BloqueViewHolder, position: Int) {
        val bloque = bloquesList[position]

        // Formatear el texto para mostrar la hora y los cupos disponibles
        holder.horaInicio.text = "${bloque.hora_inicio} (${bloque.dia})"
        holder.horaFinal.text = "Hasta: ${bloque.hora_final} - ${bloque.cupos_disponibles} cupos"

        holder.itemView.setOnClickListener {
            onBloqueClickCallback(bloque)
        }
    }

    override fun getItemCount(): Int = bloquesList.size

    class BloqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val horaInicio: TextView = itemView.findViewById(R.id.hora_inicio)
        val horaFinal: TextView = itemView.findViewById(R.id.hora_final)
    }
}