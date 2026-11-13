package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.ports.input.LogoutInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway

class LogoutUseCase(
    private val authGateway: AuthGateway
) : LogoutInputPort {

    override suspend fun execute() {
        authGateway.logout()
    }
}
