package com.ulpgc.proyectopamn.config

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ulpgc.proyectopamn.adapters.primary.ui.SampleUiAdapter
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthViewModel
import com.ulpgc.proyectopamn.adapters.secondary.persistence.SamplePersistenceAdapter
import com.ulpgc.proyectopamn.adapters.secondary.persistence.mock.InMemoryAuthGateway
import com.ulpgc.proyectopamn.application.usecase.LoginUseCase
import com.ulpgc.proyectopamn.application.usecase.RegisterUseCase
import com.ulpgc.proyectopamn.application.usecase.SampleUseCase
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway
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

    // --- Autenticación ---
    // Para pasar a Firebase solo habrá que cambiar esta línea por FirebaseAuthGateway().
    private val authGateway: AuthGateway by lazy {
        InMemoryAuthGateway()
    }

    private val loginUseCase by lazy {
        LoginUseCase(authGateway)
    }

    private val registerUseCase by lazy {
        RegisterUseCase(authGateway)
    }

    val authViewModelFactory = viewModelFactory {
        initializer { AuthViewModel(loginUseCase, registerUseCase) }
    }
}
