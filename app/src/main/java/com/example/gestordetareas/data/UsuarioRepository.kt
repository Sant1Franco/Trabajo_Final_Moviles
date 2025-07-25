package com.example.gestordetareas.data

class UsuarioRepository(private val dao: UsuarioDao) {
    suspend fun insertar(usuario: Usuario) = dao.insertar(usuario)
    suspend fun autenticar(usuario: String, contrasena: String): Usuario? = dao.autenticar(usuario, contrasena)
}
