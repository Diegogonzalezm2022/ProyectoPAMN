package com.ulpgc.proyectopamn

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthViewModel
import com.ulpgc.proyectopamn.adapters.primary.ui.auth.AuthScreen
import com.ulpgc.proyectopamn.adapters.primary.ui.user.UserProfileScreen
import com.ulpgc.proyectopamn.config.AppDependencyContainer
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.ui.theme.ProyectoPAMNTheme

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

    var currentUser by remember { mutableStateOf(authInputPort.getCurrentUser()) }

    if (currentUser == null) {
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
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen(username = currentUser.username)
                    AppDestinations.FAVORITES -> FavoritesScreen()
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

enum class AppDestinations(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    HOME(R.string.nav_home, R.drawable.ic_home),
    FAVORITES(R.string.nav_favorites, R.drawable.ic_favorite),
    MY_PRODUCTS(R.string.nav_my_products, R.drawable.ic_sell),
    PROFILE(R.string.nav_profile, R.drawable.ic_account_box),
}

@Composable
fun HomeScreen(username: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "¡Bienvenido, $username!",
            style = MaterialTheme.typography.headlineSmall
        )
    }
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

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ProyectoPAMNTheme {
        HomeScreen(username = "Usuario")
    }
}
