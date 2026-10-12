package com.senati.mitiemposemanal.bbarrera

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senati.mitiemposemanal.bbarrera.adapter.FilaAdapter
import com.senati.mitiemposemanal.bbarrera.data.CategoriaDao
import com.senati.mitiemposemanal.bbarrera.data.TareaDao
import com.senati.mitiemposemanal.bbarrera.databinding.ActivityTareasBinding
import com.senati.mitiemposemanal.bbarrera.databinding.DialogTareaBinding
import com.senati.mitiemposemanal.bbarrera.model.Fila
import com.senati.mitiemposemanal.bbarrera.model.Tarea
import com.senati.mitiemposemanal.bbarrera.util.Ui

class TareasActivity : AppCompatActivity() {

    private lateinit var b: ActivityTareasBinding
    private val dao by lazy { TareaDao(this) }
    private val categoriaDao by lazy { CategoriaDao(this) }
    private lateinit var adapter: FilaAdapter
    private var tareas = emptyList<Tarea>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.edgeToEdge(this)
        b = ActivityTareasBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.ajustar(b.root)

        b.btnAtras.setOnClickListener { finish() }
        adapter = FilaAdapter(emptyList()) { fila ->
            tareas.find { it.id == fila.id }?.let { mostrarDialogo(it) }
        }
        b.rvTareas.adapter = adapter
        b.fabNueva.setOnClickListener { mostrarDialogo(null) }
        cargar()
    }

    private fun cargar() {
        tareas = dao.listar()
        // Se muestra como "Leer Kotlin · Estudio"
        adapter.actualizar(tareas.map { Fila(it.id, "${it.nombre} · ${it.categoria}") })
        b.tvVacio.visibility = if (tareas.isEmpty()) View.VISIBLE else View.GONE
    }

    /** t == null: crear. t != null: editar o eliminar. */
    private fun mostrarDialogo(t: Tarea?) {
        val categorias = categoriaDao.listar()
        if (categorias.isEmpty()) {
            Toast.makeText(this, R.string.primero_categoria, Toast.LENGTH_SHORT).show()
            return
        }

        val d = DialogTareaBinding.inflate(layoutInflater)
        var idCategoria: Int? = t?.idCategoria

        // Lista desplegable con las categorías
        d.actvCategoria.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, categorias.map { it.nombre })
        )
        d.actvCategoria.setOnItemClickListener { _, _, pos, _ ->
            idCategoria = categorias[pos].id
            d.tilCategoria.error = null
        }
        if (t != null) {
            d.etNombre.setText(t.nombre)
            d.actvCategoria.setText(t.categoria, false)   // false: no filtra la lista
        }

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(if (t == null) R.string.nueva_tarea else R.string.editar_tarea)
            .setView(d.root)
            .setPositiveButton(R.string.guardar, null)
            .setNegativeButton(R.string.cancelar, null)
        if (t != null) builder.setNeutralButton(R.string.eliminar, null)

        val dialog = builder.create()
        dialog.show()

        // Se asigna después de show() para que el cuadro NO se cierre si hay un error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = d.etNombre.text.toString().trim()
            val idCat = idCategoria

            d.tilNombre.error = if (nombre.isEmpty()) getString(R.string.err_nombre) else null
            d.tilCategoria.error = if (idCat == null) getString(R.string.err_categoria) else null
            if (nombre.isEmpty() || idCat == null) return@setOnClickListener

            val ok = if (t == null) dao.insertar(nombre, idCat) else dao.actualizar(t.id, nombre, idCat)
            if (!ok) {
                d.tilNombre.error = getString(R.string.err_tarea_existe)
                return@setOnClickListener
            }
            dialog.dismiss()
            cargar()
        }

        if (t != null) {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.eliminar)
                    .setMessage(getString(R.string.confirmar_eliminar_tarea, t.nombre))
                    .setPositiveButton(R.string.si) { _, _ ->
                        dao.eliminar(t.id)
                        dialog.dismiss()
                        cargar()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        }
    }
}