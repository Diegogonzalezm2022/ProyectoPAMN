package com.ulpgc.proyectopamn.config

import android.content.Context
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthViewModel
import com.ulpgc.proyectopamn.adapters.primary.ui.editor.ProductEditorViewModel
import com.ulpgc.proyectopamn.adapters.primary.ui.myproducts.MyProductsViewModel
import com.ulpgc.proyectopamn.adapters.secondary.network.firebase.FirebaseAuthAdapter
import com.ulpgc.proyectopamn.adapters.secondary.network.firebase.FirestoreProductAdapter
import com.ulpgc.proyectopamn.adapters.secondary.persistence.mock.InMemoryAuthGateway
import com.ulpgc.proyectopamn.adapters.secondary.persistence.mock.InMemoryProductRepository
import com.ulpgc.proyectopamn.application.usecase.AuthUseCase
import com.ulpgc.proyectopamn.application.usecase.CreateProductUseCase
import com.ulpgc.proyectopamn.application.usecase.GetMyProductsUseCase
import com.ulpgc.proyectopamn.application.usecase.UpdateProductUseCase
import com.ulpgc.proyectopamn.application.usecase.LoginUseCase
import com.ulpgc.proyectopamn.application.usecase.RegisterUseCase
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.ports.input.AuthInputPort
import com.ulpgc.proyectopamn.domain.ports.input.CreateProductInputPort
import com.ulpgc.proyectopamn.domain.ports.input.GetMyProductsInputPort
import com.ulpgc.proyectopamn.domain.ports.input.UpdateProductInputPort
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import com.ulpgc.proyectopamn.domain.ports.output.ProductOutputPort

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

        val newUseCase = AuthUseCase(getAuthOutputPort(context, useMock), getProductOutputPort(useMock))
        authInputPortInstance = newUseCase
        return newUseCase
    }

    /**
     * Puerto de salida de autenticación: Firebase o memoria para probar.
     * Una sola instancia para toda la app, compartida por todos los casos de uso
     * que necesitan saber quién tiene la sesión iniciada.
     */
    private fun getAuthOutputPort(context: Context, useMock: Boolean): AuthOutputPort =
        authOutputPortInstance ?: if (useMock) {
            InMemoryAuthGateway()
        } else {
            FirebaseAuthAdapter(context.applicationContext)
        }.also {
            authOutputPortInstance = it
        }

    /**
     * Caso de uso "ver mis productos". Es barato de crear (no guarda estado),
     * así que no hace falta guardarlo: lo importante es que los puertos de salida sí son únicos.
     */
    fun getMyProductsInputPort(context: Context, useMock: Boolean = false): GetMyProductsInputPort =
        GetMyProductsUseCase(getAuthOutputPort(context, useMock), getProductOutputPort(useMock))

    private var productOutputPortInstance: ProductOutputPort? = null

    /**
     * Puerto de salida de productos: Firestore (colección `products`) o memoria para probar.
     * Una sola instancia para toda la app.
     */
    private fun getProductOutputPort(useMock: Boolean): ProductOutputPort =
        productOutputPortInstance ?: if (useMock) {
            InMemoryProductRepository()
        } else {
            FirestoreProductAdapter()
        }.also {
            productOutputPortInstance = it
        }

    /** Caso de uso "publicar un producto". */
    fun getCreateProductInputPort(context: Context, useMock: Boolean = false): CreateProductInputPort =
        CreateProductUseCase(getAuthOutputPort(context, useMock), getProductOutputPort(useMock))

    /** Caso de uso "editar uno de mis productos". */
    fun getUpdateProductInputPort(context: Context, useMock: Boolean = false): UpdateProductInputPort =
        UpdateProductUseCase(getAuthOutputPort(context, useMock), getProductOutputPort(useMock))

    /**
     * Fábrica para el editor. Recibe el producto a editar (o `null` para crear uno nuevo),
     * así el ViewModel nace ya con los datos rellenos.
     */
    fun getProductEditorViewModelFactory(context: Context, product: Product?) = viewModelFactory {
        initializer {
            ProductEditorViewModel(
                createProduct = getCreateProductInputPort(context),
                updateProduct = getUpdateProductInputPort(context),
                productToEdit = product,
            )
        }
    }

    /**
     * Fábrica para MyProductsViewModel. Android crea los ViewModels por su cuenta
     * (para que sobrevivan al girar la pantalla), así que hay que decirle cómo
     * construirlo y qué caso de uso pasarle.
     */
    fun getMyProductsViewModelFactory(context: Context) = viewModelFactory {
        initializer {
            MyProductsViewModel(getMyProductsInputPort(context))
        }
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
