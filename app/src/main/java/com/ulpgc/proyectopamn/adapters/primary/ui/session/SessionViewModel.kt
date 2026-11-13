package com.ulpgc.proyectopamn.adapters.primary.ui.session

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ulpgc.proyectopamn.R
import com.ulpgc.proyectopamn.domain.model.User
import com.ulpgc.proyectopamn.domain.ports.input.DeleteAccountInputPort
import com.ulpgc.proyectopamn.domain.ports.input.GetCurrentUserInputPort
import com.ulpgc.proyectopamn.domain.ports.input.LogoutInputPort
import kotlinx.coroutines.launch

/**
 * Sabe quién tiene la sesión iniciada en toda la app.
 *
 * MainActivity lo mira para decidir si enseña el acceso o la app con el footer.
 */
class SessionViewModel(
    getCurrentUser: GetCurrentUserInputPort,
    private val logout: LogoutInputPort,
    private val deleteAccount: DeleteAccountInputPort,
) : ViewModel() {

    // Con Firebase, al reabrir la app aquí ya vendrá el usuario: la sesión se recuerda sola.
    var currentUser by mutableStateOf(getCurrentUser.execute())
        private set

    var isBusy by mutableStateOf(false)
        private set

    @get:StringRes
    var errorMessage by mutableStateOf<Int?>(null)
        private set

    fun onLoggedIn(user: User) {
        currentUser = user
    }

    fun onLogout() {
        viewModelScope.launch {
            logout.execute()
            currentUser = null
        }
    }

    fun onDeleteAccount() {
        if (isBusy) return
        isBusy = true
        errorMessage = null
        viewModelScope.launch {
            deleteAccount.execute()
                .onSuccess { currentUser = null }
                .onFailure { errorMessage = R.string.profile_delete_error }
            isBusy = false
        }
    }
}
