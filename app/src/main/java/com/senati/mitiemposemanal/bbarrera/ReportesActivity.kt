package com.senati.mitiemposemanal.bbarrera

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.senati.mitiemposemanal.bbarrera.adapter.FilaAdapter
import com.senati.mitiemposemanal.bbarrera.data.EventoDao
import com.senati.mitiemposemanal.bbarrera.data.ReporteDao
import com.senati.mitiemposemanal.bbarrera.databinding.ActivityReportesBinding
import com.senati.mitiemposemanal.bbarrera.databinding.ItemPanelBinding
import com.senati.mitiemposemanal.bbarrera.model.Evento
import com.senati.mitiemposemanal.bbarrera.model.Fila
import com.senati.mitiemposemanal.bbarrera.util.Tiempo
import com.senati.mitiemposemanal.bbarrera.util.Ui
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

class ReportesActivity : AppCompatActivity() {

    private lateinit var b: ActivityReportesBinding
    private val reporteDao by lazy { ReporteDao(this) }
    private val eventoDao by lazy { EventoDao(this) }
    private lateinit var adapter: FilaAdapter
    private var semanas = emptyList<LocalDate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.edgeToEdge(this)
        b = ActivityReportesBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.ajustar(b.root)

        b.btnAtras.setOnClickListener { finish() }
        adapter = FilaAdapter(emptyList()) { }
        b.rvEventos.adapter = adapter

        reporteDao.cerrarSemanas()
        semanas = reporteDao.semanas()

        if (semanas.isEmpty()) {
            b.llContenido.visibility = View.GONE
            b.tvVacio.visibility = View.VISIBLE
            return
        }

        val rangos = semanas.map { Tiempo.rangoDe(it) }
        val a = ArrayAdapter(this, android.R.layout.simple_spinner_item, rangos)
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spSemanas.adapter = a
        b.spSemanas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                mostrar(semanas[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun mostrar(lunes: LocalDate) {
        b.llPanel.removeAllViews()
        var registrados = 0L
        reporteDao.detalle(lunes).forEach {
            registrados += it.segundos
            agregarFila(it.nombre, it.segundos, it.porcentaje, R.color.morado)
        }
        val libres = (Tiempo.SEMANA_SEG - registrados).coerceAtLeast(0)
        agregarFila(
            getString(R.string.sin_registrar), libres,
            libres * 100.0 / Tiempo.SEMANA_SEG, R.color.texto_suave
        )

        val eventos = eventoDao.listarSemana(lunes.atStartOfDay())
        adapter.actualizar(eventos.map { Fila(it.id, "${it.tarea} · ${it.categoria}", detalle(it)) })
    }

    private fun agregarFila(nombre: String, segundos: Long, porcentaje: Double, color: Int) {
        val f = ItemPanelBinding.inflate(layoutInflater, b.llPanel, false)
        f.tvNombre.text = nombre
        f.tvValor.text = "${Tiempo.duracion(segundos)} · ${String.format(Locale.US, "%.1f", porcentaje)} %"
        f.pb.max = 1000
        f.pb.progress = (porcentaje * 10).roundToInt()
        f.pb.setIndicatorColor(getColor(color))
        b.llPanel.addView(f.root)
    }

    private fun detalle(e: Evento): String {
        val inicio = Tiempo.mostrar(Tiempo.leer(e.inicio))
        val fin = e.fin ?: return getString(R.string.en_curso_desde, inicio)
        return "$inicio → ${Tiempo.mostrar(Tiempo.leer(fin))} · ${Tiempo.duracion(e.segundos ?: 0)}"
    }
}