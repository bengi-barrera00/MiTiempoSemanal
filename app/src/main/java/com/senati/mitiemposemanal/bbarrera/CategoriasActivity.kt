package com.senati.mitiemposemanal.bbarrera

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senati.mitiemposemanal.bbarrera.adapter.FilaAdapter
import com.senati.mitiemposemanal.bbarrera.data.CategoriaDao
import com.senati.mitiemposemanal.bbarrera.databinding.ActivityCategoriasBinding
import com.senati.mitiemposemanal.bbarrera.databinding.DialogNombreBinding
import com.senati.mitiemposemanal.bbarrera.model.Categoria
import com.senati.mitiemposemanal.bbarrera.model.Fila
import com.senati.mitiemposemanal.bbarrera.util.Ui

class CategoriasActivity : AppCompatActivity() {

    private lateinit var b: ActivityCategoriasBinding
    private val dao by lazy { CategoriaDao(this) }
    private lateinit var adapter: FilaAdapter
    private var categorias = emptyList<Categoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.edgeToEdge(this)
        b = ActivityCategoriasBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.ajustar(b.root)

        b.btnAtras.setOnClickListener { finish() }
        adapter = FilaAdapter(emptyList()) { fila ->
            categorias.find { it.id == fila.id }?.let { mostrarDialogo(it) }
        }
        b.rvCategorias.adapter = adapter
        b.fabNueva.setOnClickListener { mostrarDialogo(null) }
        cargar()
    }

    private fun cargar() {
        categorias = dao.listar()
        adapter.actualizar(categorias.map {
            Fila(it.id, it.nombre, getString(R.string.tareas_cantidad, it.tareas))
        })
        b.tvVacio.visibility = if (categorias.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogo(cat: Categoria?) {
        val d = DialogNombreBinding.inflate(layoutInflater)
        d.etNombre.setText(cat?.nombre)

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(if (cat == null) R.string.nueva_categoria else R.string.editar_categoria)
            .setView(d.root)
            .setPositiveButton(R.string.guardar, null)
            .setNegativeButton(R.string.cancelar, null)
        if (cat != null) builder.setNeutralButton(R.string.eliminar, null)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = d.etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                d.tilNombre.error = getString(R.string.err_nombre)
                return@setOnClickListener
            }
            val ok = if (cat == null) dao.insertar(nombre) else dao.actualizar(cat.id, nombre)
            if (!ok) {
                d.tilNombre.error = getString(R.string.err_cat_existe)
                return@setOnClickListener
            }
            dialog.dismiss()
            cargar()
        }

        if (cat != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                intentarEliminar(cat, dialog)
            }
        }
    }

    private fun intentarEliminar(cat: Categoria, dialogEdicion: AlertDialog) {
        val n = dao.contarTareas(cat.id)
        if (n > 0) {
            Toast.makeText(
                this, getString(R.string.cat_con_tareas, cat.nombre, n), Toast.LENGTH_LONG
            ).show()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.eliminar)
            .setMessage(getString(R.string.confirmar_eliminar_cat, cat.nombre))
            .setPositiveButton(R.string.si) { _, _ ->
                dao.eliminar(cat.id)
                dialogEdicion.dismiss()
                cargar()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}