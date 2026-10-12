package com.senati.mitiemposemanal.bbarrera.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.senati.mitiemposemanal.bbarrera.databinding.ItemFilaBinding
import com.senati.mitiemposemanal.bbarrera.model.Fila

class FilaAdapter(
    private var items: List<Fila>,
    private val onClick: (Fila) -> Unit
) : RecyclerView.Adapter<FilaAdapter.VH>() {

    class VH(val b: ItemFilaBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemFilaBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, i: Int) {
        val f = items[i]
        h.b.tvTitulo.text = f.titulo
        h.b.tvDetalle.text = f.detalle
        h.b.tvDetalle.visibility = if (f.detalle == null) View.GONE else View.VISIBLE
        h.b.root.setOnClickListener { onClick(f) }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevos: List<Fila>) {
        items = nuevos
        notifyDataSetChanged()
    }
}