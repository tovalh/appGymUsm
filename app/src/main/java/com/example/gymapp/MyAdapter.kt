package com.example.gymapp


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.BloqueHorario
import com.google.android.material.card.MaterialCardView

class MyAdapter(
    private var bloquesList: List<BloqueHorario>,
    private val onBloqueClickCallback: (BloqueHorario) -> Unit
) : RecyclerView.Adapter<MyAdapter.BloqueViewHolder>() {

    // Posicion para guardar el seleccionado
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return BloqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: BloqueViewHolder, position: Int) {

        val bloque = bloquesList[position]

        // Formatear el texto para mostrar la hora y los cupos disponibles
        holder.titulo.text = "${bloque.dia}"
        holder.bloqueHorario.text = "${bloque.hora_inicio} - ${bloque.hora_final}"
        holder.cupos.text = "Cupos:${bloque.cupos_disponibles} "


        // Cambiar el color de fondo según si está seleccionado o no
        holder.cardView.setCardBackgroundColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (position == selectedPosition) R.color.selected_background else R.color.normal_background
            )
        )


        holder.cardView.setOnClickListener {
            // Guardar el estado anterior
            val previousSelected = selectedPosition
            // Actualizar la nueva posición seleccionada
            selectedPosition = holder.adapterPosition
            // Notificar los cambios
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            // Llamar al callback
            onBloqueClickCallback(bloque)
        }

    }

    fun updateBloques(newBloques: List<BloqueHorario>) {
        bloquesList = newBloques
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = bloquesList.size

    class BloqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        val titulo: TextView = itemView.findViewById(R.id.dia)
        val bloqueHorario: TextView = itemView.findViewById(R.id.horario)
        val cupos: TextView = itemView.findViewById(R.id.cupos)

    }
}