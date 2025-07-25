package com.example.gestordetareas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestordetareas.data.Task
import com.example.gestordetareas.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Cargando)
    val uiState: StateFlow<TaskUiState> = _uiState

    private val formatoFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd") // Formato para ISO

    init {
        obtenerTareas()
    }

    fun obtenerTareas() { // se valida la fecha tambien y si ya paso su fecha limite se cambia a tareas completadas
        viewModelScope.launch {
            repository.obtenerTodas()
                .catch { e -> _uiState.value = TaskUiState.Error("Error al cargar: ${e.message}") }
                .collectLatest { tareas ->
                    val hoy = LocalDate.now()
                    val tareasActualizadas = tareas.map { tarea ->
                        try {
                            val fechaTarea = LocalDate.parse(tarea.fechaLimite, formatoFecha)
                            if (!tarea.completada && fechaTarea.isBefore(hoy)) {
                                val tareaVencida = tarea.copy(completada = true)
                                repository.actualizar(tareaVencida)
                                tareaVencida
                            } else {
                                tarea
                            }
                        } catch (e: Exception) {
                            tarea // si hay error en parseo, se ignora y se mantiene
                        }
                    }
                    _uiState.value = TaskUiState.Exito(tareasActualizadas)
                }
        }
    }

    fun cargarTareas() {
        obtenerTareas()
    }

    fun agregarTarea(task: Task, onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.insertar(task)
            obtenerTareas()
            onFinished()
        }
    }

    fun eliminarTarea(task: Task, onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.eliminar(task)
            obtenerTareas()
            onFinished()
        }
    }

    fun cambiarEstadoTarea(tarea: Task, completada: Boolean) {
        viewModelScope.launch {
            repository.actualizar(tarea.copy(completada = completada))
            obtenerTareas()
        }
    }

    fun obtenerTareaPorId(id: Int, callback: (Task?) -> Unit) {
        viewModelScope.launch {
            val tarea = repository.obtenerPorId(id)
            callback(tarea)
        }
    }

    fun actualizarTarea(task: Task, onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.actualizar(task)
            obtenerTareas()
            onFinished()
        }
    }
}

