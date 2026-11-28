package com.ulpgc.proyectopamn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.chats.ChatsScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.editor.ProductEditorScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.home.HomeScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.myproducts.MyProductsScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.user.UserProfileScreen
import com.ulpgc.proyectopamn.config.AppDependencyContainer
import com.ulpgc.proyectopamn.domain.model.Product
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.ui.theme.ProyectoPAMNTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoPAMNTheme {
                MainAppHost()
            }
        }
    }
}

@Composable
fun MainAppHost() {
    val context = LocalContext.current
    val authInputPort = remember { AppDependencyContainer.getAuthInputPort(context) }

    var currentUser by remember { mutableStateOf<UserProfile?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }

    // Al abrir la app se pregunta (una vez) si había una sesión guardada.
    LaunchedEffect(Unit) {
        currentUser = authInputPort.getCurrentUser()
        isCheckingSession = false
    }

    if (isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = "Cargando" }
            )
        }
    } else if (currentUser == null) {
        AuthScreen(
            authInputPort = authInputPort,
            onAuthSuccess = { user ->
                currentUser = user
            }
        )
    } else {
        ProyectoPAMNApp(
            currentUser = currentUser!!,
            onLogout = { currentUser = null },
            onAccountDeleted = { currentUser = null },
            onProfileUpdated = { updatedUser -> currentUser = updatedUser }
        )
    }
}

@Composable
fun ProyectoPAMNApp(
    currentUser: UserProfile,
    onLogout: () -> Unit,
    onAccountDeleted: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val authInputPort = remember { AppDependencyContainer.getAuthInputPort(context) }
    var currentDestination by remember { mutableStateOf(AppDestinations.HOME) }
    // Si no es null, en la pestaña "Mis productos" se ve el editor en vez de la lista.
    var editorRequest by remember { mutableStateOf<EditorRequest?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = stringResource(it.label)
                        )
                    },
                    label = { Text(stringResource(it.label)) },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                        editorRequest = null
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen(username = currentUser.username)
                    AppDestinations.FAVORITES -> FavoritesScreen()
                    AppDestinations.CHATS -> ChatsScreen()
                    AppDestinations.MY_PRODUCTS -> {
                        val request = editorRequest
                        if (request == null) {
                            MyProductsScreen(
                                onAddClick = { editorRequest = EditorRequest(product = null) },
                                onProductClick = { product -> editorRequest = EditorRequest(product) },
                            )
                        } else {
                            ProductEditorScreen(
                                product = request.product,
                                viewModelKey = request.key,
                                onClose = { editorRequest = null },
                                onSaved = { messageRes ->
                                    editorRequest = null
                                    // Cierre del diálogo: confirmamos que se ha guardado.
                                    val message = context.getString(messageRes)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                },
                            )
                        }
                    }
                    AppDestinations.PROFILE -> UserProfileScreen(
                        userProfile = currentUser,
                        authInputPort = authInputPort,
                        onLogout = onLogout,
                        onAccountDeleted = onAccountDeleted,
                        onProfileUpdated = onProfileUpdated
                    )
                }
            }
        }
    }
}

/**
 * Petición de abrir el editor: con un producto (editar) o sin él (crear).
 * La `key` es distinta cada vez para que el editor estrene ViewModel en cada apertura.
 */
private data class EditorRequest(
    val product: Product?,
    val key: String = "editor_${System.nanoTime()}",
)

enum class AppDestinations(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    FAVORITES(R.string.nav_favorites, R.drawable.ic_favorite),
    CHATS(R.string.nav_chats, R.drawable.ic_chat),
    MY_PRODUCTS(R.string.nav_my_products, R.drawable.ic_sell),
    PROFILE(R.string.nav_profile, R.drawable.ic_account_box),
}

@Composable
fun FavoritesScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pestaña de Favoritos",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
