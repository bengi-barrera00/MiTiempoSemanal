package com.senati.mitiemposemanal.bbarrera

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senati.mitiemposemanal.bbarrera.adapter.FilaAdapter
import com.senati.mitiemposemanal.bbarrera.data.CategoriaDao
import com.senati.mitiemposemanal.bbarrera.data.EventoDao
import com.senati.mitiemposemanal.bbarrera.data.TareaDao
import com.senati.mitiemposemanal.bbarrera.databinding.ActivityEventosBinding
import com.senati.mitiemposemanal.bbarrera.model.Evento
import com.senati.mitiemposemanal.bbarrera.model.Fila
import com.senati.mitiemposemanal.bbarrera.model.Tarea
import com.senati.mitiemposemanal.bbarrera.util.Tiempo
import com.senati.mitiemposemanal.bbarrera.util.Ui
import java.time.Duration

class EventosActivity : AppCompatActivity() {

    private lateinit var b: ActivityEventosBinding
    private val eventoDao by lazy { EventoDao(this) }
    private val tareaDao by lazy { TareaDao(this) }
    private val categoriaDao by lazy { CategoriaDao(this) }
    private lateinit var adapterEventos: FilaAdapter
    private lateinit var adapterSugerencias: FilaAdapter

    private var sugerencias = emptyList<Tarea>()
    private var hayEventos = false
    private var enCurso: Evento? = null
    private var elegida: Tarea? = null
    private var poniendoTexto = false

    private val handler = Handler(Looper.getMainLooper())
    private val reloj = object : Runnable {
        override fun run() {
            val e = enCurso ?: return
            val seg = Duration.between(Tiempo.leer(e.inicio), Tiempo.ahora()).seconds
            b.tvCronometro.text = Tiempo.reloj(seg.coerceAtLeast(0))
            handler.postDelayed(this, 1000)
        }
    }

    private fun iniciarReloj() {
        handler.removeCallbacks(reloj)
        handler.post(reloj)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.edgeToEdge(this)
        b = ActivityEventosBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.ajustar(b.root)

        b.btnAtras.setOnClickListener { finish() }

        adapterEventos = FilaAdapter(emptyList()) { }
        b.rvEventos.adapter = adapterEventos
        adapterSugerencias = FilaAdapter(emptyList()) { fila ->
            sugerencias.find { it.id == fila.id }?.let { elegir(it) }
        }
        b.rvSugerencias.adapter = adapterSugerencias

        b.etTarea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, antes: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!poniendoTexto) buscar(s.toString().trim())
            }
        })

        b.btnIniciar.setOnClickListener { iniciar() }
        b.btnFinalizar.setOnClickListener { finalizar() }

        refrescar()
    }

    override fun onResume() {
        super.onResume()
        if (enCurso != null) iniciarReloj()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(reloj)
    }


    private fun buscar(texto: String) {
        elegida = null
        b.tvCategoria.visibility = View.GONE
        b.tilTarea.error = null
        sugerencias = if (texto.length >= 2) tareaDao.buscar(texto) else emptyList()
        mostrarLista()
    }

    private fun elegir(t: Tarea) {
        elegida = t
        poniendoTexto = true
        b.etTarea.setText(t.nombre)
        b.etTarea.setSelection(t.nombre.length)
        poniendoTexto = false
        sugerencias = emptyList()
        b.tvCategoria.text = getString(R.string.categoria_de, t.categoria)
        b.tvCategoria.visibility = View.VISIBLE
        mostrarLista()
    }


    private fun iniciar() {
        if (enCurso != null) return
        val nombre = b.etTarea.text.toString().trim()
        if (nombre.isEmpty()) {
            b.tilTarea.error = getString(R.string.err_tarea)
            return
        }

        val existente = elegida ?: tareaDao.buscarExacta(nombre)
        if (existente != null) {
            empezar(existente.id)
            return
        }

        val categorias = categoriaDao.listar()
        if (categorias.isEmpty()) {
            Toast.makeText(this, R.string.primero_categoria, Toast.LENGTH_SHORT).show()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.elige_categoria, nombre))
            .setItems(categorias.map { it.nombre }.toTypedArray()) { _, pos ->
                if (tareaDao.insertar(nombre, categorias[pos].id)) {
                    tareaDao.buscarExacta(nombre)?.let { empezar(it.id) }
                }
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun empezar(idTarea: Int) {
        eventoDao.iniciar(idTarea)
        poniendoTexto = true
        b.etTarea.text?.clear()
        poniendoTexto = false
        elegida = null
        sugerencias = emptyList()
        b.tvCategoria.visibility = View.GONE
        refrescar()
    }

    private fun finalizar() {
        val e = enCurso ?: return
        eventoDao.finalizar(e)
        refrescar()
    }


    private fun refrescar() {
        val eventos = eventoDao.listar()
        enCurso = eventoDao.enCurso()
        hayEventos = eventos.isNotEmpty()
        adapterEventos.actualizar(eventos.map { Fila(it.id, "${it.tarea} · ${it.categoria}", detalle(it)) })

        val e = enCurso
        b.cardEnCurso.visibility = if (e != null) View.VISIBLE else View.GONE
        if (e != null) {
            b.tvEnCurso.text = getString(
                R.string.en_curso, e.tarea, e.categoria, Tiempo.mostrar(Tiempo.leer(e.inicio))
            )
        }
        b.tilTarea.isEnabled = e == null
        habilitar(b.btnIniciar, e == null)
        habilitar(b.btnFinalizar, e != null)

        handler.removeCallbacks(reloj)
        if (e != null) iniciarReloj()

        mostrarLista()
    }

    private fun mostrarLista() {
        val buscando = sugerencias.isNotEmpty()
        adapterSugerencias.actualizar(sugerencias.map { Fila(it.id, "${it.nombre} · ${it.categoria}") })
        b.rvSugerencias.visibility = if (buscando) View.VISIBLE else View.GONE
        b.rvEventos.visibility = if (buscando) View.GONE else View.VISIBLE
        b.tvLista.setText(if (buscando) R.string.coincidencias else R.string.eventos_registrados)
        b.tvVacio.visibility = if (!buscando && !hayEventos) View.VISIBLE else View.GONE
    }

    private fun detalle(e: Evento): String {
        val inicio = Tiempo.mostrar(Tiempo.leer(e.inicio))
        val fin = e.fin ?: return getString(R.string.en_curso_desde, inicio)
        return "$inicio → ${Tiempo.mostrar(Tiempo.leer(fin))} · ${Tiempo.duracion(e.segundos ?: 0)}"
    }

    private fun habilitar(boton: MaterialButton, activo: Boolean) {
        boton.isEnabled = activo
        boton.alpha = if (activo) 1f else 0.4f
    }
}