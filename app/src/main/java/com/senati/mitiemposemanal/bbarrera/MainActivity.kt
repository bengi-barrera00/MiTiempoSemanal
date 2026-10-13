package com.senati.mitiemposemanal.bbarrera

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.senati.mitiemposemanal.bbarrera.data.ReporteDao
import com.senati.mitiemposemanal.bbarrera.databinding.ActivityMainBinding
import com.senati.mitiemposemanal.bbarrera.util.Ui

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val reporteDao by lazy { ReporteDao(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.edgeToEdge(this)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        Ui.ajustar(b.root)

        // "Bengi Barrera · v1.0.0 (4)"
        b.tvAutorVersion.text = getString(
            R.string.autor_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
        )

        b.cardCategorias.setOnClickListener {
            startActivity(Intent(this, CategoriasActivity::class.java))
        }
        b.cardTareas.setOnClickListener {
            startActivity(Intent(this, TareasActivity::class.java))
        }
        b.cardEventos.setOnClickListener {
            startActivity(Intent(this, EventosActivity::class.java))
        }
        b.cardReportes.setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        reporteDao.cerrarSemanas()
    }
}