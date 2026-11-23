package com.ulpgc.proyectopamn.domain.model

/**
 * Motivos por los que puede fallar una operación con productos.
 *
 * Son errores del dominio: dicen *qué* ha fallado, y la UI decide *qué texto* enseñar.
 */
sealed class ProductError : Exception() {
    data object EmptyTitle : ProductError()
    data object InvalidPrice : ProductError()
    data object NotLoggedIn : ProductError()
    data object NotFound : ProductError()
    /** Se intenta editar un producto de otro usuario. */
    data object NotOwner : ProductError()
}
