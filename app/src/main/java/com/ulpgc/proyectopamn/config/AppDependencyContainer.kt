package com.ulpgc.proyectopamn.config

import android.content.Context
import com.ulpgc.proyectopamn.adapters.secondary.network.firebase.FirebaseAuthAdapter
import com.ulpgc.proyectopamn.application.usecase.AuthUseCase
import com.ulpgc.proyectopamn.domain.ports.input.AuthInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort

/**
 * Punto de Ensamblado / Contenedor de Dependencias (Composition Root)
 *
 * Instancia y conecta los adaptadores concretos con los casos de uso y puertos.
 */
object AppDependencyContainer {

    private var authOutputPortInstance: AuthOutputPort? = null
    private var authInputPortInstance: AuthInputPort? = null

    fun getAuthInputPort(context: Context): AuthInputPort {
        val currentInstance = authInputPortInstance
        if (currentInstance != null) return currentInstance

        val outputPort = authOutputPortInstance ?: FirebaseAuthAdapter(context.applicationContext).also {
            authOutputPortInstance = it
        }
        val newUseCase = AuthUseCase(outputPort)
        authInputPortInstance = newUseCase
        return newUseCase
    }
}
