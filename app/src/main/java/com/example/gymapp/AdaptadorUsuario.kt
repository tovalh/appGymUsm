package com.example.gymapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymapp.model.Usuario

class AdaptadorUsuario(
    private var usuarios: List<Usuario>,
    private val onAsistenciaClick: (Usuario) -> Unit,
    private val onCancelarClick: (Usuario) -> Unit
) : RecyclerView.Adapter<AdaptadorUsuario.AsistenciaViewHolder>() {

    class AsistenciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.nombre)
        val btnAsistir: ImageButton = itemView.findViewById(R.id.btnAsistir)
        val btnCancelar: ImageButton = itemView.findViewById(R.id.btnCancelar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_layout, parent, false)
        return AsistenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AsistenciaViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.nombreTextView.text = usuario.nombre

        holder.btnAsistir.setOnClickListener { onAsistenciaClick(usuario) }
        holder.btnCancelar.setOnClickListener { onCancelarClick(usuario) }
    }

    override fun getItemCount() = usuarios.size

    fun updateUsuarios(newUsuarios: List<Usuario>) {
        usuarios = newUsuarios
        notifyDataSetChanged()
    }
}