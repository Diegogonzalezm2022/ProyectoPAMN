package com.ulpgc.proyectopamn.config

import com.ulpgc.proyectopamn.adapters.primary.ui.SampleUiAdapter
import com.ulpgc.proyectopamn.adapters.secondary.persistence.SamplePersistenceAdapter
import com.ulpgc.proyectopamn.application.usecase.SampleUseCase

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
}
