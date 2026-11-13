package com.ulpgc.proyectopamn.domain.ports.input

/** Caso de uso: borrar la cuenta del usuario actual (no se puede deshacer). */
interface DeleteAccountInputPort {
    suspend fun execute(): Result<Unit>
}
