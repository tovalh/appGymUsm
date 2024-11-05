package com.example.gymapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.Usuario

class AdaptadorUsuario(
    private val usuarioLista: List<Usuario>,
    private val onEditClick: (Usuario) -> Unit,
    private val onDeleteClick: (Usuario) -> Unit
) : RecyclerView.Adapter<AdaptadorUsuario.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.admi_layaut, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return usuarioLista.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val usuario = usuarioLista[position]
        holder.nombre.text = usuario.nombre

        holder.btnAsistir.setOnClickListener {
            onEditClick(usuario)
        }

        holder.btnCancelar.setOnClickListener {
            onDeleteClick(usuario)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.nombre)
        val btnAsistir: ImageButton = itemView.findViewById(R.id.btnAsistir)
        val btnCancelar: ImageButton = itemView.findViewById(R.id.btnCancelar)
    }
}