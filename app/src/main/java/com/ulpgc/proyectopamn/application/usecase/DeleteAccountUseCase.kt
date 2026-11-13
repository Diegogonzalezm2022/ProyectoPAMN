package com.ulpgc.proyectopamn.application.usecase

import com.ulpgc.proyectopamn.domain.ports.input.DeleteAccountInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthGateway

class DeleteAccountUseCase(
    private val authGateway: AuthGateway
) : DeleteAccountInputPort {

    override suspend fun execute(): Result<Unit> = authGateway.deleteAccount()
}
