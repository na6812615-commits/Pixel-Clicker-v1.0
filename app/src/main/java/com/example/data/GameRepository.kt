package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

interface GameProgressRepository {
    suspend fun loadOrCreateProgress(userId: String): Result<GameProgress>
    suspend fun saveProgress(userId: String, progress: GameProgress): Result<Unit>
}

class GameRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) : GameProgressRepository {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUid: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Người chơi cần đăng nhập Google trước khi truy cập dữ liệu.")
        if (expectedUid != null) {
            check(uid == expectedUid) { "Phiên đăng nhập đã thay đổi; từ chối ghi chéo tài khoản." }
        }
        return uid
    }

    fun observeProgress(userId: String): Flow<GameProgress?> = flow {
        val uid = requireUserId(userId)
        val docRef = db.collection("users").document(uid)
        val path = docRef.path
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            GameProgress::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun loadOrCreateProgress(userId: String): Result<GameProgress> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val existing = snapshot.toObject(
                    GameProgress::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: GameProgress(id = uid, userId = uid)
                Result.success(existing)
            } else {
                val initial = GameProgress(id = uid, userId = uid)
                docRef.set(initial.toCreateMap(uid)).await()
                val createdSnap = docRef.get().await()
                val created = createdSnap.toObject(
                    GameProgress::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: initial
                Result.success(created)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    override suspend fun saveProgress(userId: String, progress: GameProgress): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val docRef = db.collection("users").document(uid)
            if (progress.createdAt == null) {
                val existingSnap = docRef.get().await()
                if (!existingSnap.exists()) {
                    docRef.set(progress.toCreateMap(uid)).await()
                } else {
                    docRef.update(progress.toUpdateMap()).await()
                }
            } else {
                docRef.update(progress.toUpdateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}
