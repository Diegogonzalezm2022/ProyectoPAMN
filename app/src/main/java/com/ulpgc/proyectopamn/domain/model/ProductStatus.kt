package com.ulpgc.proyectopamn.domain.model

/**
 * Estado de un producto.
 *
 * Distinguir "en venta" de "vendido" permite que, al borrar una cuenta,
 * desaparezca lo que ofrece pero se conserve el historial de lo que ya vendió.
 */
enum class ProductStatus {
    AVAILABLE,
    SOLD,
}
