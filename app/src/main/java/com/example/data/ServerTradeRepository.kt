package com.example.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

data class TradeBalanceAdjustment(
    val deltaDiem: Double = 0.0,
    val deltaCash: Double = 0.0,
    val tradeCompletedNow: Boolean = false
)

class ServerFullException(serverName: String) : IllegalStateException(serverName)
class EmptyTradeException : IllegalStateException("Trade requires at least one offer > 0")
class InsufficientBagException : IllegalStateException("Offer exceeds player balance")
class OfferLockedException : IllegalStateException("Un-agree before changing your offer")

interface ServerTradeRepository {
    suspend fun ensureSeedServers(): Result<Unit>
    fun observeServers(): Flow<List<GameServer>>
    fun observeServerPlayers(serverId: String): Flow<List<ServerPlayer>>
    fun observeMyTrades(userId: String): Flow<List<TradeSession>>

    suspend fun joinServer(
        userId: String,
        displayName: String,
        targetServerId: String,
        progress: GameProgress,
        previousServerId: String?
    ): Result<GameServer>

    suspend fun leaveServer(userId: String, serverId: String): Result<Unit>
    fun leaveServerFireAndForget(userId: String, serverId: String)

    suspend fun updatePresence(
        userId: String,
        serverId: String,
        displayName: String,
        progress: GameProgress
    ): Result<Unit>

    suspend fun sendTradeInvite(
        serverId: String,
        senderId: String,
        senderName: String,
        receiver: ServerPlayer
    ): Result<TradeSession>

    suspend fun acceptTradeInvite(tradeId: String, userId: String): Result<Unit>
    suspend fun cancelOrDeclineTrade(tradeId: String, userId: String): Result<TradeBalanceAdjustment>
    suspend fun updateTradeOffer(tradeId: String, userId: String, points: Double, cash: Double): Result<Unit>
    suspend fun toggleAgreeTrade(tradeId: String, userId: String, agree: Boolean): Result<TradeBalanceAdjustment>
    suspend fun settleTradeBalanceIfNeeded(tradeId: String, userId: String): Result<TradeBalanceAdjustment>
}

class FirestoreServerTradeRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) : ServerTradeRepository {

    private fun requireUid(expectedUid: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Chưa đăng nhập Google.")
        if (expectedUid != null) {
            check(uid == expectedUid) { "Phiên đăng nhập không khớp." }
        }
        return uid
    }

    override suspend fun ensureSeedServers(): Result<Unit> {
        val path = "servers"
        return try {
            requireUid()
            val existingSnap = db.collection("servers")
                .whereEqualTo("maxPlayers", GameServer.MAX_SERVER_PLAYERS)
                .get()
                .await()
            val existingIds = existingSnap.documents.map { it.id }.toSet()
            GameServer.DEFAULT_SERVERS.forEach { preset ->
                if (preset.id !in existingIds) {
                    runCatching {
                        db.collection("servers").document(preset.id).set(preset.toCreateMap()).await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override fun observeServers(): Flow<List<GameServer>> = flow {
        requireUid()
        val query = db.collection("servers")
            .whereEqualTo("maxPlayers", GameServer.MAX_SERVER_PLAYERS)
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        doc.toObject(GameServer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedBy { it.id }
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, "servers")
                    }
                    throw error
                }
        )
    }

    override fun observeServerPlayers(serverId: String): Flow<List<ServerPlayer>> = flow {
        requireUid()
        val path = "servers/$serverId/players"
        val query = db.collection("servers").document(serverId)
            .collection("players")
            .whereEqualTo("serverId", serverId)
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        doc.toObject(ServerPlayer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.filter { it.online }
                        .sortedWith(compareByDescending<ServerPlayer> { it.restartsLv }.thenByDescending { it.diem })
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser != null) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override fun observeMyTrades(userId: String): Flow<List<TradeSession>> = flow {
        val uid = requireUid(userId)
        val path = "trades"
        val query = db.collection("trades").whereArrayContains("participantIds", uid)
        emitAll(
            query.snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        doc.toObject(TradeSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    }.sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(fsErr ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun joinServer(
        userId: String,
        displayName: String,
        targetServerId: String,
        progress: GameProgress,
        previousServerId: String?
    ): Result<GameServer> {
        val path = "servers/$targetServerId"
        return try {
            val uid = requireUid(userId)
            val targetRef = db.collection("servers").document(targetServerId)
            val targetPlayerRef = targetRef.collection("players").document(uid)
            val prevRef = if (!previousServerId.isNullOrBlank() && previousServerId != targetServerId) {
                db.collection("servers").document(previousServerId)
            } else {
                null
            }
            val prevPlayerRef = prevRef?.collection("players")?.document(uid)

            val joinedServer = db.runTransaction { tx ->
                // All reads MUST precede all writes
                val targetSnap = tx.get(targetRef)
                val prevSnap = prevRef?.let { tx.get(it) }

                val serverObj = if (targetSnap.exists()) {
                    targetSnap.toObject(GameServer::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: GameServer(id = targetServerId, name = targetServerId)
                } else {
                    val preset = GameServer.DEFAULT_SERVERS.firstOrNull { it.id == targetServerId }
                        ?: GameServer(id = targetServerId, name = "Pixie Server $targetServerId")
                    tx.set(targetRef, preset.toCreateMap())
                    preset
                }

                val currentIds = serverObj.activePlayerIds
                val alreadyInTarget = uid in currentIds
                if (!alreadyInTarget && currentIds.size >= serverObj.maxPlayers.toInt()) {
                    throw ServerFullException(serverObj.name)
                }

                // Leave previous server atomically if switching
                if (prevRef != null && prevSnap != null && prevSnap.exists()) {
                    val prevIds = (prevSnap.get("activePlayerIds") as? List<*>)
                        ?.mapNotNull { it as? String }
                        .orEmpty()
                    if (uid in prevIds) {
                        val updatedPrev = prevIds.filterNot { it == uid }
                        tx.update(
                            prevRef,
                            mapOf(
                                "activePlayerIds" to updatedPrev,
                                "playerCount" to updatedPrev.size.toLong(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                }
                if (prevPlayerRef != null) {
                    tx.delete(prevPlayerRef)
                }

                val nextIds = if (alreadyInTarget) currentIds else currentIds + uid
                if (!alreadyInTarget) {
                    tx.update(
                        targetRef,
                        mapOf(
                            "activePlayerIds" to nextIds,
                            "playerCount" to nextIds.size.toLong(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val playerCard = ServerPlayer(
                    userId = uid,
                    serverId = targetServerId,
                    displayName = ServerPlayer.sanitizePlayerName(displayName, uid),
                    diem = progress.diem,
                    cashDo = progress.cashDo,
                    restartsLv = progress.lv3,
                    online = true
                )
                tx.set(targetPlayerRef, playerCard.toWriteMap())

                serverObj.copy(
                    id = targetServerId,
                    activePlayerIds = nextIds,
                    playerCount = nextIds.size.toLong()
                )
            }.await()

            Result.success(joinedServer)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun leaveServer(userId: String, serverId: String): Result<Unit> {
        val path = "servers/$serverId"
        return try {
            val uid = requireUid(userId)
            val serverRef = db.collection("servers").document(serverId)
            val playerRef = serverRef.collection("players").document(uid)
            db.runTransaction { tx ->
                val snap = tx.get(serverRef)
                if (snap.exists()) {
                    val ids = (snap.get("activePlayerIds") as? List<*>)
                        ?.mapNotNull { it as? String }
                        .orEmpty()
                    if (uid in ids) {
                        val nextIds = ids.filterNot { it == uid }
                        tx.update(
                            serverRef,
                            mapOf(
                                "activePlayerIds" to nextIds,
                                "playerCount" to nextIds.size.toLong(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                }
                tx.delete(playerRef)
                Unit
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override fun leaveServerFireAndForget(userId: String, serverId: String) {
        if (auth.currentUser?.uid != userId || serverId.isBlank()) return
        val serverRef = db.collection("servers").document(serverId)
        val playerRef = serverRef.collection("players").document(userId)
        db.runTransaction { tx ->
            val snap = tx.get(serverRef)
            if (snap.exists()) {
                val ids = (snap.get("activePlayerIds") as? List<*>)
                    ?.mapNotNull { it as? String }
                    .orEmpty()
                if (userId in ids) {
                    val nextIds = ids.filterNot { it == userId }
                    tx.update(
                        serverRef,
                        mapOf(
                            "activePlayerIds" to nextIds,
                            "playerCount" to nextIds.size.toLong(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            tx.delete(playerRef)
            Unit
        }
    }

    override suspend fun updatePresence(
        userId: String,
        serverId: String,
        displayName: String,
        progress: GameProgress
    ): Result<Unit> {
        val path = "servers/$serverId/players/$userId"
        return try {
            val uid = requireUid(userId)
            val card = ServerPlayer(
                userId = uid,
                serverId = serverId,
                displayName = ServerPlayer.sanitizePlayerName(displayName, uid),
                diem = progress.diem,
                cashDo = progress.cashDo,
                restartsLv = progress.lv3,
                online = true
            )
            db.document(path).set(card.toWriteMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    override suspend fun sendTradeInvite(
        serverId: String,
        senderId: String,
        senderName: String,
        receiver: ServerPlayer
    ): Result<TradeSession> {
        val tradeId = "trade_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val path = "trades/$tradeId"
        return try {
            val uid = requireUid(senderId)
            require(receiver.userId != uid) { "Không thể tự mời chính mình." }
            val session = TradeSession(
                id = tradeId,
                serverId = serverId,
                senderId = uid,
                senderName = ServerPlayer.sanitizePlayerName(senderName, uid),
                receiverId = receiver.userId,
                receiverName = ServerPlayer.sanitizePlayerName(receiver.displayName, receiver.userId),
                participantIds = listOf(uid, receiver.userId),
                status = TradeStatus.PENDING.firestoreValue
            )
            db.collection("trades").document(tradeId).set(session.toCreateMap()).await()
            Result.success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    override suspend fun acceptTradeInvite(tradeId: String, userId: String): Result<Unit> {
        val path = "trades/$tradeId"
        return try {
            requireUid(userId)
            db.document(path).update(
                mapOf(
                    "status" to TradeStatus.ACTIVE.firestoreValue,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun cancelOrDeclineTrade(tradeId: String, userId: String): Result<TradeBalanceAdjustment> {
        val path = "trades/$tradeId"
        return try {
            val uid = requireUid(userId)
            val tradeRef = db.collection("trades").document(tradeId)
            val userRef = db.collection("users").document(uid)

            val adjustment = db.runTransaction { tx ->
                val tradeSnap = tx.get(tradeRef)
                val userSnap = tx.get(userRef)
                val trade = tradeSnap.toObject(TradeSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: return@runTransaction TradeBalanceAdjustment()

                val status = trade.parsedStatus
                if (status == TradeStatus.COMPLETED || status == TradeStatus.CANCELLED) {
                    return@runTransaction TradeBalanceAdjustment()
                }
                if (status == TradeStatus.PENDING) {
                    tx.update(
                        tradeRef,
                        mapOf(
                            "status" to TradeStatus.CANCELLED.firestoreValue,
                            "cancelledBy" to uid,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    return@runTransaction TradeBalanceAdjustment()
                }

                // Active trade cancel: refund caller's escrowed assets if any
                val isSender = trade.isSender(uid)
                val escrowPoints = trade.myEscrowPoints(uid)
                val escrowCash = trade.myEscrowCash(uid)

                if (escrowPoints > 0.0 || escrowCash > 0.0) {
                    val curDiem = (userSnap.getDouble("diem") ?: 0.0).coerceAtLeast(0.0)
                    val curCash = (userSnap.getDouble("cashDo") ?: 0.0).coerceAtLeast(0.0)
                    tx.update(
                        userRef,
                        mapOf(
                            "diem" to curDiem + escrowPoints,
                            "cashDo" to curCash + escrowCash,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                val tradeUpdates = if (isSender) {
                    mapOf(
                        "status" to TradeStatus.CANCELLED.firestoreValue,
                        "cancelledBy" to uid,
                        "senderAccepted" to false,
                        "senderEscrowPoints" to 0.0,
                        "senderEscrowCash" to 0.0,
                        "senderRefunded" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                } else {
                    mapOf(
                        "status" to TradeStatus.CANCELLED.firestoreValue,
                        "cancelledBy" to uid,
                        "receiverAccepted" to false,
                        "receiverEscrowPoints" to 0.0,
                        "receiverEscrowCash" to 0.0,
                        "receiverRefunded" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
                tx.update(tradeRef, tradeUpdates)
                TradeBalanceAdjustment(deltaDiem = escrowPoints, deltaCash = escrowCash)
            }.await()

            Result.success(adjustment)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun updateTradeOffer(
        tradeId: String,
        userId: String,
        points: Double,
        cash: Double
    ): Result<Unit> {
        val path = "trades/$tradeId"
        return try {
            val uid = requireUid(userId)
            val normPoints = TradeSession.normalizeAmount(points)
            val normCash = TradeSession.normalizeAmount(cash)
            val tradeRef = db.collection("trades").document(tradeId)

            db.runTransaction { tx ->
                val snap = tx.get(tradeRef)
                val trade = snap.toObject(TradeSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: throw IllegalStateException("Trade not found")
                check(trade.parsedStatus == TradeStatus.ACTIVE) { "Trade is no longer active" }
                if (trade.myAccepted(uid)) {
                    throw OfferLockedException()
                }
                val updates = if (trade.isSender(uid)) {
                    mapOf(
                        "senderPoints" to normPoints,
                        "senderCash" to normCash,
                        "receiverAccepted" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                } else {
                    mapOf(
                        "receiverPoints" to normPoints,
                        "receiverCash" to normCash,
                        "senderAccepted" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
                tx.update(tradeRef, updates)
                Unit
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun toggleAgreeTrade(
        tradeId: String,
        userId: String,
        agree: Boolean
    ): Result<TradeBalanceAdjustment> {
        val path = "trades/$tradeId"
        return try {
            val uid = requireUid(userId)
            val tradeRef = db.collection("trades").document(tradeId)
            val userRef = db.collection("users").document(uid)

            val adjustment = db.runTransaction { tx ->
                val tradeSnap = tx.get(tradeRef)
                val userSnap = tx.get(userRef)
                val trade = tradeSnap.toObject(TradeSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: throw IllegalStateException("Trade not found")

                check(trade.parsedStatus == TradeStatus.ACTIVE) { "Trade is not active" }
                val isSender = trade.isSender(uid)
                val curDiem = (userSnap.getDouble("diem") ?: 0.0).coerceAtLeast(0.0)
                val curCash = (userSnap.getDouble("cashDo") ?: 0.0).coerceAtLeast(0.0)
                val oldEscrowPts = trade.myEscrowPoints(uid)
                val oldEscrowCash = trade.myEscrowCash(uid)

                if (agree) {
                    if (!trade.hasAnyOffer()) {
                        throw EmptyTradeException()
                    }
                    val targetPts = trade.myOfferPoints(uid)
                    val targetCash = trade.myOfferCash(uid)
                    val deltaPts = targetPts - oldEscrowPts
                    val deltaCash = targetCash - oldEscrowCash

                    if (deltaPts > curDiem + 1e-6 || deltaCash > curCash + 1e-6) {
                        throw InsufficientBagException()
                    }

                    val nextDiem = (curDiem - deltaPts).coerceAtLeast(0.0)
                    val nextCash = (curCash - deltaCash).coerceAtLeast(0.0)
                    if (abs(deltaPts) > 1e-9 || abs(deltaCash) > 1e-9) {
                        tx.update(
                            userRef,
                            mapOf(
                                "diem" to nextDiem,
                                "cashDo" to nextCash,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }

                    val partnerReadyWithMatchingEscrow = trade.partnerAccepted(uid) &&
                        abs(trade.partnerEscrowPoints(uid) - trade.partnerOfferPoints(uid)) < 1e-6 &&
                        abs(trade.partnerEscrowCash(uid) - trade.partnerOfferCash(uid)) < 1e-6
                    val shouldComplete = partnerReadyWithMatchingEscrow && trade.hasAnyOffer()
                    val nextStatus = if (shouldComplete) {
                        TradeStatus.COMPLETED.firestoreValue
                    } else {
                        TradeStatus.ACTIVE.firestoreValue
                    }

                    val tradeUpdates = if (isSender) {
                        mapOf(
                            "senderAccepted" to true,
                            "senderEscrowPoints" to targetPts,
                            "senderEscrowCash" to targetCash,
                            "status" to nextStatus,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    } else {
                        mapOf(
                            "receiverAccepted" to true,
                            "receiverEscrowPoints" to targetPts,
                            "receiverEscrowCash" to targetCash,
                            "status" to nextStatus,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    }
                    tx.update(tradeRef, tradeUpdates)
                    TradeBalanceAdjustment(
                        deltaDiem = -deltaPts,
                        deltaCash = -deltaCash,
                        tradeCompletedNow = shouldComplete
                    )
                } else {
                    // Un-agree: refund existing escrow back to user's bag
                    if (oldEscrowPts > 0.0 || oldEscrowCash > 0.0) {
                        tx.update(
                            userRef,
                            mapOf(
                                "diem" to curDiem + oldEscrowPts,
                                "cashDo" to curCash + oldEscrowCash,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    val tradeUpdates = if (isSender) {
                        mapOf(
                            "senderAccepted" to false,
                            "senderEscrowPoints" to 0.0,
                            "senderEscrowCash" to 0.0,
                            "status" to TradeStatus.ACTIVE.firestoreValue,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    } else {
                        mapOf(
                            "receiverAccepted" to false,
                            "receiverEscrowPoints" to 0.0,
                            "receiverEscrowCash" to 0.0,
                            "status" to TradeStatus.ACTIVE.firestoreValue,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    }
                    tx.update(tradeRef, tradeUpdates)
                    TradeBalanceAdjustment(deltaDiem = oldEscrowPts, deltaCash = oldEscrowCash)
                }
            }.await()

            Result.success(adjustment)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    override suspend fun settleTradeBalanceIfNeeded(
        tradeId: String,
        userId: String
    ): Result<TradeBalanceAdjustment> {
        val path = "trades/$tradeId"
        return try {
            val uid = requireUid(userId)
            val tradeRef = db.collection("trades").document(tradeId)
            val userRef = db.collection("users").document(uid)

            val adjustment = db.runTransaction { tx ->
                val tradeSnap = tx.get(tradeRef)
                val userSnap = tx.get(userRef)
                val trade = tradeSnap.toObject(TradeSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: return@runTransaction TradeBalanceAdjustment()

                val isSender = trade.isSender(uid)
                val curDiem = (userSnap.getDouble("diem") ?: 0.0).coerceAtLeast(0.0)
                val curCash = (userSnap.getDouble("cashDo") ?: 0.0).coerceAtLeast(0.0)

                when (trade.parsedStatus) {
                    TradeStatus.COMPLETED -> {
                        if (trade.myClaimed(uid)) return@runTransaction TradeBalanceAdjustment()
                        val gainPts = trade.partnerEscrowPoints(uid)
                        val gainCash = trade.partnerEscrowCash(uid)
                        if (gainPts > 0.0 || gainCash > 0.0) {
                            tx.update(
                                userRef,
                                mapOf(
                                    "diem" to curDiem + gainPts,
                                    "cashDo" to curCash + gainCash,
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            )
                        }
                        val claimKey = if (isSender) "senderClaimed" else "receiverClaimed"
                        tx.update(
                            tradeRef,
                            mapOf(
                                claimKey to true,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        TradeBalanceAdjustment(deltaDiem = gainPts, deltaCash = gainCash)
                    }

                    TradeStatus.CANCELLED -> {
                        if (trade.myRefunded(uid)) return@runTransaction TradeBalanceAdjustment()
                        val refundPts = trade.myEscrowPoints(uid)
                        val refundCash = trade.myEscrowCash(uid)
                        if (refundPts > 0.0 || refundCash > 0.0) {
                            tx.update(
                                userRef,
                                mapOf(
                                    "diem" to curDiem + refundPts,
                                    "cashDo" to curCash + refundCash,
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            )
                        }
                        val refundUpdates = if (isSender) {
                            mapOf(
                                "senderRefunded" to true,
                                "senderAccepted" to false,
                                "senderEscrowPoints" to 0.0,
                                "senderEscrowCash" to 0.0,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        } else {
                            mapOf(
                                "receiverRefunded" to true,
                                "receiverAccepted" to false,
                                "receiverEscrowPoints" to 0.0,
                                "receiverEscrowCash" to 0.0,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        }
                        tx.update(tradeRef, refundUpdates)
                        TradeBalanceAdjustment(deltaDiem = refundPts, deltaCash = refundCash)
                    }

                    else -> TradeBalanceAdjustment()
                }
            }.await()

            Result.success(adjustment)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }
}

class OfflineFallbackServerTradeRepository : ServerTradeRepository {
    private val serversFlow = MutableStateFlow(GameServer.DEFAULT_SERVERS)
    private val playersFlow = MutableStateFlow<List<ServerPlayer>>(emptyList())
    private val tradesFlow = MutableStateFlow<List<TradeSession>>(emptyList())

    override suspend fun ensureSeedServers(): Result<Unit> = Result.success(Unit)
    override fun observeServers(): Flow<List<GameServer>> = serversFlow
    override fun observeServerPlayers(serverId: String): Flow<List<ServerPlayer>> = playersFlow
    override fun observeMyTrades(userId: String): Flow<List<TradeSession>> = tradesFlow
    override suspend fun joinServer(
        userId: String,
        displayName: String,
        targetServerId: String,
        progress: GameProgress,
        previousServerId: String?
    ): Result<GameServer> {
        val server = serversFlow.value.firstOrNull { it.id == targetServerId }
            ?: GameServer.DEFAULT_SERVERS.first()
        return Result.success(server.copy(playerCount = 1L, activePlayerIds = listOf(userId)))
    }

    override suspend fun leaveServer(userId: String, serverId: String): Result<Unit> = Result.success(Unit)
    override fun leaveServerFireAndForget(userId: String, serverId: String) {}
    override suspend fun updatePresence(
        userId: String,
        serverId: String,
        displayName: String,
        progress: GameProgress
    ): Result<Unit> = Result.success(Unit)

    override suspend fun sendTradeInvite(
        serverId: String,
        senderId: String,
        senderName: String,
        receiver: ServerPlayer
    ): Result<TradeSession> = Result.failure(IllegalStateException("Offline"))

    override suspend fun acceptTradeInvite(tradeId: String, userId: String): Result<Unit> = Result.success(Unit)
    override suspend fun cancelOrDeclineTrade(tradeId: String, userId: String): Result<TradeBalanceAdjustment> =
        Result.success(TradeBalanceAdjustment())

    override suspend fun updateTradeOffer(tradeId: String, userId: String, points: Double, cash: Double): Result<Unit> =
        Result.success(Unit)

    override suspend fun toggleAgreeTrade(tradeId: String, userId: String, agree: Boolean): Result<TradeBalanceAdjustment> =
        Result.success(TradeBalanceAdjustment())

    override suspend fun settleTradeBalanceIfNeeded(tradeId: String, userId: String): Result<TradeBalanceAdjustment> =
        Result.success(TradeBalanceAdjustment())
}
