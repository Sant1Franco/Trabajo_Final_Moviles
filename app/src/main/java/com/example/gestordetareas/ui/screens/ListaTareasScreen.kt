package com.example.gestordetareas.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gestordetareas.data.Task
import com.example.gestordetareas.ui.Screens
import com.example.gestordetareas.ui.TaskUiState
import com.example.gestordetareas.ui.TaskViewModel
import androidx.compose.runtime.mutableIntStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaTareasScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    LaunchedEffect(Unit) { viewModel.cargarTareas() }

    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas Registradas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screens.CrearTarea.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF1F8FF), Color.White)
                    )
                )
                .padding(16.dp)
        ) {
            when (uiState) {
                is TaskUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is TaskUiState.Exito -> {
                    val tareasPendientes = uiState.tareas.filter { !it.completada }
                    val tareasHechas = uiState.tareas.filter { it.completada }

                    if (uiState.tareas.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay tareas registradas aún",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        Text(
                            "Pendientes",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(tareasPendientes) { tarea ->
                                TareaItem(tarea, navController, viewModel)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(thickness = 1.dp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Completadas",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF388E3C)
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(tareasHechas) { tarea ->
                                TareaItem(tarea, navController, viewModel)
                            }
                        }
                    }
                }

                is TaskUiState.Error -> {
                    Text("Error: ${uiState.mensaje}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun TareaItem(tarea: Task, navController: NavController, viewModel: TaskViewModel) {
    var expandida by remember { mutableStateOf(false) }
    var mostrarVerMas by remember { mutableStateOf(false) }
    val alphaTexto = if (tarea.completada) 0.5f else 1f

    val backgroundColor = if (tarea.completada) Color(0xFFDFF6DD) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { completado ->
                    viewModel.cambiarEstadoTarea(tarea, completado)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(Screens.EditarTarea.editarTareaConId(tarea.id))
                    }
            ) {
                Text(
                    tarea.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null,
                    modifier = Modifier.alpha(alphaTexto)
                )

                if (!tarea.descripcion.isNullOrBlank()) {
                    var lineCount by remember { mutableIntStateOf(0) }

                    Text(
                        text = tarea.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        onTextLayout = { result ->
                            lineCount = result.lineCount
                            mostrarVerMas = lineCount > 2
                        },
                        modifier = Modifier.height(0.dp),
                        maxLines = Int.MAX_VALUE
                    )

                    Text(
                        text = tarea.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        maxLines = if (expandida) Int.MAX_VALUE else 2,
                        textDecoration = if (tarea.completada) TextDecoration.LineThrough else null,
                        modifier = Modifier.alpha(alphaTexto)
                    )

                    if (mostrarVerMas) {
                        Text(
                            text = if (expandida) "Ver menos" else "Ver más",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { expandida = !expandida }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Vence: ${convertirFechaAUsuario(tarea.fechaLimite)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.alpha(alphaTexto)
                )
            }
        }
    }
}
