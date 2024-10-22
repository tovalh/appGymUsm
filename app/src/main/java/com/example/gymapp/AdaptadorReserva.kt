package com.example.gymapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.Reserva


class AdaptadorReserva(
    private val reservaLista: List<Reserva>,
    private val addToCartCallback: (Reserva) -> Unit
) : RecyclerView.Adapter<AdaptadorReserva.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reserva_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reservaLista.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reserva = reservaLista[position]
        holder.horario.text = "${reserva.hora_inicio} - (${reserva.hora_final})"
        holder.dia.text = reserva.dia
        holder.fecha.text = reserva.fecha

        }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dia: TextView = itemView.findViewById(R.id.dia)
        val horario: TextView = itemView.findViewById(R.id.horario)
        val fecha: TextView = itemView.findViewById(R.id.fecha)
    }
    }





