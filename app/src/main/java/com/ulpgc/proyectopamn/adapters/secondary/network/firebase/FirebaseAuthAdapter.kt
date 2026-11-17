package com.ulpgc.proyectopamn.adapters.secondary.network.firebase

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Adaptador Secundario (Driven Adapter) para Autenticación e Infraestructura con Firebase.
 *
 * Implementa [AuthOutputPort] utilizando Firebase Auth y Firestore.
 * Incluye un repositorio en memoria de respaldo si Firebase no está configurado aún
 * en el entorno del dispositivo.
 */
class FirebaseAuthAdapter(
    private val context: Context
) : AuthOutputPort {

    // Repositorio de respaldo en memoria si no hay `google-services.json`
    private val fallbackUsers = mutableMapOf<String, Pair<UserProfile, String>>() // id -> (profile, password)
    private var fallbackCurrentUser: UserProfile? = null

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Exception) {
            false
        }

    private val firebaseAuth: FirebaseAuth?
        get() = if (isFirebaseAvailable) try { FirebaseAuth.getInstance() } catch (_: Exception) { null } else null

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) try { FirebaseFirestore.getInstance() } catch (_: Exception) { null } else null

    override fun getCurrentUser(): UserProfile? {
        val auth = firebaseAuth
        val db = firestore
        if (auth != null && db != null && auth.currentUser != null) {
            val firebaseUser = auth.currentUser!!
            return UserProfile(
                id = firebaseUser.uid,
                username = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                phone = firebaseUser.phoneNumber ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                description = ""
            )
        }
        return fallbackCurrentUser
    }

    override suspend fun loginWithEmail(email: String, password: String): AuthResult<UserProfile> {
        val auth = firebaseAuth
        val db = firestore

        if (auth != null && db != null) {
            return try {
                val result = auth.signInWithEmailAndPassword(email, password).awaitTask()
                val firebaseUser = result.user ?: return AuthResult.Error("Error al iniciar sesión")
                
                // Obtener perfil desde Firestore
                val doc = db.collection("users").document(firebaseUser.uid).get().awaitTask()
                val userProfile = if (doc.exists()) {
                    UserProfile(
                        id = firebaseUser.uid,
                        username = doc.getString("username") ?: firebaseUser.displayName ?: "",
                        email = doc.getString("email") ?: firebaseUser.email ?: "",
                        phone = doc.getString("phone") ?: firebaseUser.phoneNumber ?: "",
                        photoUrl = doc.getString("photoUrl") ?: firebaseUser.photoUrl?.toString() ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } else {
                    UserProfile(
                        id = firebaseUser.uid,
                        username = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: email,
                        phone = firebaseUser.phoneNumber ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: ""
                    )
                }
                AuthResult.Success(userProfile)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error de autenticación en Firebase")
            }
        }

        // Modo de respaldo en memoria
        val userPair = fallbackUsers.values.find { it.first.email.equals(email, ignoreCase = true) }
            ?: return AuthResult.Error("Credenciales incorrectas")

        if (userPair.second != password) {
            return AuthResult.Error("Contraseña incorrecta")
        }

        fallbackCurrentUser = userPair.first
        return AuthResult.Success(userPair.first)
    }

    override suspend fun findUserByUsername(username: String): UserProfile? {
        val db = firestore
        if (db != null) {
            return try {
                val query = db.collection("users").whereEqualTo("username", username).get().awaitTask()
                if (!query.isEmpty) {
                    val doc = query.documents.first()
                    UserProfile(
                        id = doc.id,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }

        return fallbackUsers.values.find { it.first.username.equals(username, ignoreCase = true) }?.first
    }

    override suspend fun findUserByPhone(phone: String): UserProfile? {
        val db = firestore
        if (db != null) {
            return try {
                val query = db.collection("users").whereEqualTo("phone", phone).get().awaitTask()
                if (!query.isEmpty) {
                    val doc = query.documents.first()
                    UserProfile(
                        id = doc.id,
                        username = doc.getString("username") ?: "",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: "",
                        photoUrl = doc.getString("photoUrl") ?: "",
                        description = doc.getString("description") ?: ""
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }

        return fallbackUsers.values.find { it.first.phone == phone }?.first
    }

    override suspend fun register(user: UserProfile, password: String): AuthResult<UserProfile> {
        val auth = firebaseAuth
        val db = firestore

        if (auth != null && db != null) {
            return try {
                val result = auth.createUserWithEmailAndPassword(user.email, password).awaitTask()
                val firebaseUser = result.user ?: return AuthResult.Error("Error al registrar usuario")

                val registeredProfile = user.copy(id = firebaseUser.uid)

                val userMap = mapOf(
                    "username" to registeredProfile.username,
                    "email" to registeredProfile.email,
                    "phone" to registeredProfile.phone,
                    "photoUrl" to registeredProfile.photoUrl,
                    "description" to registeredProfile.description
                )

                db.collection("users").document(firebaseUser.uid).set(userMap).awaitTask()

                AuthResult.Success(registeredProfile)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error al registrar en Firebase")
            }
        }

        // Modo de respaldo en memoria
        val newUid = "local_" + System.currentTimeMillis()
        val createdProfile = user.copy(id = newUid)
        fallbackUsers[newUid] = Pair(createdProfile, password)
        fallbackCurrentUser = createdProfile
        return AuthResult.Success(createdProfile)
    }

    override suspend fun updateProfile(user: UserProfile): AuthResult<UserProfile> {
        val auth = firebaseAuth
        val db = firestore

        if (auth != null && db != null && auth.currentUser != null) {
            return try {
                val uid = auth.currentUser!!.uid
                val updatedProfile = user.copy(id = uid)
                val userMap = mapOf(
                    "username" to updatedProfile.username,
                    "email" to updatedProfile.email,
                    "phone" to updatedProfile.phone,
                    "photoUrl" to updatedProfile.photoUrl,
                    "description" to updatedProfile.description
                )
                db.collection("users").document(uid).update(userMap).awaitTask()
                AuthResult.Success(updatedProfile)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error al actualizar perfil en Firebase")
            }
        }

        val current = fallbackCurrentUser ?: return AuthResult.Error("No hay sesión activa")
        val updated = user.copy(id = current.id)
        val password = fallbackUsers[current.id]?.second ?: ""
        fallbackUsers[current.id] = Pair(updated, password)
        fallbackCurrentUser = updated
        return AuthResult.Success(updated)
    }

    override suspend fun logout() {
        val auth = firebaseAuth
        auth?.signOut()
        fallbackCurrentUser = null
    }

    override suspend fun deleteAccount(): AuthResult<Unit> {
        val auth = firebaseAuth
        val db = firestore

        if (auth != null && db != null && auth.currentUser != null) {
            return try {
                val uid = auth.currentUser!!.uid
                db.collection("users").document(uid).delete().awaitTask()
                auth.currentUser!!.delete().awaitTask()
                AuthResult.Success(Unit)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error al borrar cuenta en Firebase")
            }
        }

        val current = fallbackCurrentUser ?: return AuthResult.Error("No hay sesión activa")
        fallbackUsers.remove(current.id)
        fallbackCurrentUser = null
        return AuthResult.Success(Unit)
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
}
