package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.model.User
import com.ulpgc.proyectopamn.domain.ports.input.GetCurrentUserInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway

class GetCurrentUserUseCase(
    private val authGateway: AuthGateway
) : GetCurrentUserInputPort {

    override fun execute(): User? = authGateway.currentUser()
}
