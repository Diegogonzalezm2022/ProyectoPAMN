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

/**
 * Punto de Ensamblado / Contenedor de Dependencias (Composition Root)
 *
 * Instancia y enlaza los adaptadores concretos con los casos de uso y puertos.
 */
object AppDependencyContainer {

    val samplePersistenceAdapter by lazy {
        SamplePersistenceAdapter()
    }

    val sampleUseCase by lazy {
        SampleUseCase(samplePersistenceAdapter)
    }

    val sampleUiAdapter by lazy {
        SampleUiAdapter(sampleUseCase)
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
