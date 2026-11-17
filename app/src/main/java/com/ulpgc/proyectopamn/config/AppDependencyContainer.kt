package com.ulpgc.proyectopamn.config

import android.content.Context
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthViewModel
import com.ulpgc.proyectopamn.adapters.secondary.network.firebase.FirebaseAuthAdapter
import com.ulpgc.proyectopamn.adapters.secondary.persistence.mock.InMemoryAuthGateway
import com.ulpgc.proyectopamn.application.usecase.AuthUseCase
import com.ulpgc.proyectopamn.application.usecase.LoginUseCase
import com.ulpgc.proyectopamn.application.usecase.RegisterUseCase
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

    /**
     * Obtiene el puerto de entrada para la autenticación y gestión de usuarios.
     * Utiliza [FirebaseAuthAdapter] (con respaldo en memoria interno si no hay google-services.json).
     */
    fun getAuthInputPort(context: Context, useMock: Boolean = false): AuthInputPort {
        val currentInstance = authInputPortInstance
        if (currentInstance != null) return currentInstance

        val outputPort = authOutputPortInstance ?: if (useMock) {
            InMemoryAuthGateway()
        } else {
            FirebaseAuthAdapter(context.applicationContext)
        }.also {
            authOutputPortInstance = it
        }

        val newUseCase = AuthUseCase(outputPort)
        authInputPortInstance = newUseCase
        return newUseCase
    }

    /**
     * Fábrica para AuthViewModel manteniendo compatibilidad.
     */
    fun getAuthViewModelFactory(context: Context) = viewModelFactory {
        initializer {
            val authPort = getAuthInputPort(context)
            AuthViewModel(
                login = LoginUseCase(authPort),
                register = RegisterUseCase(authPort)
            )
        }
    }
}
