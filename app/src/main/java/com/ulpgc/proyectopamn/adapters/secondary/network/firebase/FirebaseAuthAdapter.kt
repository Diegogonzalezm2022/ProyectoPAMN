package com.ulpgc.proyectopamn.adapters.secondary.network.firebase

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ulpgc.proyectopamn.domain.model.AuthResult
import com.ulpgc.proyectopamn.domain.model.UserProfile
import com.ulpgc.proyectopamn.domain.ports.output.AuthOutputPort
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Adaptador Secundario (Driven Adapter) para Autenticación e Infraestructura con Firebase.
 *
 * Implementa [AuthOutputPort] utilizando Firebase Auth, Firestore y Firebase Storage.
 * Sube automáticamente las imágenes seleccionadas de la galería a la nube de Firebase Storage.
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

    private val storage: FirebaseStorage?
        get() = if (isFirebaseAvailable) try { FirebaseStorage.getInstance() } catch (_: Exception) { null } else null

    override suspend fun getCurrentUser(): UserProfile? {
        val auth = firebaseAuth
        val db = firestore
        val firebaseUser = auth?.currentUser
        if (db != null && firebaseUser != null) {
            // Firebase Auth recuerda la sesión, pero el nombre, teléfono, foto y descripción
            // solo están en Firestore: hay que leerlos de ahí, no de `displayName` (que nunca se rellena).
            return try {
                val doc = db.collection("users").document(firebaseUser.uid).get().awaitTask()
                UserProfile(
                    id = firebaseUser.uid,
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: firebaseUser.email ?: "",
                    phone = doc.getString("phone") ?: "",
                    photoUrl = doc.getString("photoUrl") ?: "",
                    description = doc.getString("description") ?: ""
                )
            } catch (_: Exception) {
                // Sin conexión y sin caché: al menos se mantiene la sesión con el email.
                UserProfile(id = firebaseUser.uid, email = firebaseUser.email ?: "")
            }
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
                fallbackCurrentUser = userProfile
                AuthResult.Success(userProfile)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error de autenticación en Firebase")
            }
        }

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

                // Subir foto a Firebase Storage si es una URI de galería.
                // La cuenta ya está creada: si la foto falla, se registra sin foto (se puede poner luego).
                val uploadedPhotoUrl = uploadProfileImageIfNeeded(firebaseUser.uid, user.photoUrl) ?: ""
                val registeredProfile = user.copy(id = firebaseUser.uid, photoUrl = uploadedPhotoUrl)

                val userMap = mapOf(
                    "username" to registeredProfile.username,
                    "email" to registeredProfile.email,
                    "phone" to registeredProfile.phone,
                    "photoUrl" to registeredProfile.photoUrl,
                    "description" to registeredProfile.description
                )

                db.collection("users").document(firebaseUser.uid).set(userMap).awaitTask()
                fallbackCurrentUser = registeredProfile

                AuthResult.Success(registeredProfile)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error al registrar en Firebase")
            }
        }

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

                // Subir nueva foto a Firebase Storage si proviene de la galería
                val uploadedPhotoUrl = uploadProfileImageIfNeeded(uid, user.photoUrl)
                    ?: return AuthResult.Error("No se pudo subir la foto. Inténtalo de nuevo.")
                val updatedProfile = user.copy(id = uid, photoUrl = uploadedPhotoUrl)

                val userMap = mapOf(
                    "username" to updatedProfile.username,
                    "email" to updatedProfile.email,
                    "phone" to updatedProfile.phone,
                    "photoUrl" to updatedProfile.photoUrl,
                    "description" to updatedProfile.description
                )
                db.collection("users").document(uid).update(userMap).awaitTask()
                fallbackCurrentUser = updatedProfile

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

    override suspend fun deleteAccount(password: String): AuthResult<Unit> {
        val auth = firebaseAuth
        val db = firestore
        val firebaseUser = auth?.currentUser

        if (db != null && firebaseUser != null) {
            val email = firebaseUser.email ?: return AuthResult.Error("No se pudo identificar la cuenta")

            // 1. Reautenticar ANTES de borrar nada. Firebase exige un inicio de sesión reciente
            //    para borrar la cuenta; si lo comprobáramos al final, el perfil ya estaría borrado.
            try {
                firebaseUser.reauthenticate(EmailAuthProvider.getCredential(email, password)).awaitTask()
            } catch (_: Exception) {
                return AuthResult.Error("Contraseña incorrecta")
            }

            return try {
                val uid = firebaseUser.uid
                // 2. Foto (puede no existir: si falla, seguimos).
                try {
                    storage?.reference?.child("profile_images/$uid.jpg")?.delete()?.awaitTask()
                } catch (_: Exception) {}
                // 3. Perfil en Firestore (necesita la sesión, por eso va antes que la cuenta).
                db.collection("users").document(uid).delete().awaitTask()
                // 4. Por último, la cuenta de Auth.
                firebaseUser.delete().awaitTask()
                fallbackCurrentUser = null

                AuthResult.Success(Unit)
            } catch (e: Exception) {
                AuthResult.Error(e.localizedMessage ?: "Error al borrar cuenta en Firebase")
            }
        }

        val current = fallbackCurrentUser ?: return AuthResult.Error("No hay sesión activa")
        if (fallbackUsers[current.id]?.second != password) {
            return AuthResult.Error("Contraseña incorrecta")
        }
        fallbackUsers.remove(current.id)
        fallbackCurrentUser = null
        return AuthResult.Success(Unit)
    }

    /**
     * Sube la imagen de perfil a Firebase Storage si es una URI local (`content://` o `file://`).
     * Retorna la URL pública de descarga, o `null` si no se pudo subir.
     *
     * Nunca devuelve la ruta local: `content://...` solo existe en este móvil y caduca,
     * así que guardarla en Firestore dejaría una foto rota para todos los demás.
     */
    private suspend fun uploadProfileImageIfNeeded(uid: String, photoUrl: String): String? {
        if (photoUrl.isBlank()) return ""
        if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
            return photoUrl // Ya es una URL pública almacenada en la nube
        }

        val firebaseStorage = storage ?: return null
        return try {
            val uri = Uri.parse(photoUrl)
            val ref = firebaseStorage.reference.child("profile_images/$uid.jpg")
            ref.putFile(uri).awaitTask()
            ref.downloadUrl.awaitTask().toString()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result -> continuation.resume(result) }
        addOnFailureListener { exception -> continuation.resumeWithException(exception) }
    }
}
