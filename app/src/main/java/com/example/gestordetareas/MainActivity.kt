package com.example.gestordetareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.room.Room
import com.example.gestordetareas.data.AppDatabase
import com.example.gestordetareas.data.TaskRepository
import com.example.gestordetareas.data.UsuarioRepository
import com.example.gestordetareas.ui.TaskViewModel
import com.example.gestordetareas.ui.UsuarioViewModel
import com.example.gestordetareas.ui.screens.GestorDeTareasApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear base de datos Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tareas_db"
        )
            .fallbackToDestructiveMigration() // esto ver para eliminar antes de entregar
            .build()

        // Repositorios
        val taskRepository = TaskRepository(db.taskDao())
        val usuarioRepository = UsuarioRepository(db.usuarioDao())

        // ViewModels
        val taskViewModel = TaskViewModel(taskRepository)
        val usuarioViewModel = UsuarioViewModel(usuarioRepository)

        // UI Principal
        setContent {
            GestorDeTareasApp(
                taskViewModel = taskViewModel,
                usuarioViewModel = usuarioViewModel
            )

            // Registrar usuario admin por defecto si no existe
            LaunchedEffect(Unit) {
                try {
                    val existe = usuarioViewModel.autenticar("admin", "1234")
                    if (!existe) {
                        usuarioViewModel.registrar("admin", "1234")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}