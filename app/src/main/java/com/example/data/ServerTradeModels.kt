package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlin.math.round

@Keep
@IgnoreExtraProperties
data class GameServer(
    @DocumentId val id: String = "",
    val name: String = "",
    val region: String = "Global",
    val maxPlayers: Long = MAX_SERVER_PLAYERS.toLong(),
    val playerCount: Long = 0L,
    val activePlayerIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val isFull: Boolean
        get() = playerCount >= maxPlayers || activePlayerIds.size >= maxPlayers

    fun containsPlayer(uid: String): Boolean = uid in activePlayerIds

    fun toCreateMap(): Map<String, Any> = mapOf(
        "name" to name.trim().take(48).ifEmpty { "Pixie Realm" },
        "region" to region.trim().take(20).ifEmpty { "Global" },
        "maxPlayers" to MAX_SERVER_PLAYERS.toLong(),
        "playerCount" to 0L,
        "activePlayerIds" to emptyList<String>(),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        const val MAX_SERVER_PLAYERS = 20

        val DEFAULT_SERVERS: List<GameServer> = listOf(
            GameServer(id = "server_1", name = "Pixie Forest #1", region = "SEA"),
            GameServer(id = "server_2", name = "Crystal Sanctuary #2", region = "SEA"),
            GameServer(id = "server_3", name = "Starlight Valley #3", region = "Global"),
            GameServer(id = "server_4", name = "Aurora Peak #4", region = "Global"),
            GameServer(id = "server_5", name = "Moonbeam Cove #5", region = "Asia"),
            GameServer(id = "server_6", name = "Solar Citadel #6", region = "EU"),
            GameServer(id = "server_7", name = "Emerald Glade #7", region = "NA"),
            GameServer(id = "server_8", name = "Astral Rift #8", region = "Global")
        )
    }
}

@Keep
@IgnoreExtraProperties
data class ServerPlayer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val serverId: String = "",
    val displayName: String = "Pixie Player",
    val diem: Double = 0.0,
    val cashDo: Double = 0.0,
    val restartsLv: Long = 0L,
    val online: Boolean = true,
    val updatedAt: Timestamp? = null
) {
    fun toWriteMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "serverId" to serverId,
        "displayName" to sanitizePlayerName(displayName, userId),
        "diem" to diem.coerceAtLeast(0.0),
        "cashDo" to cashDo.coerceAtLeast(0.0),
        "restartsLv" to restartsLv.coerceAtLeast(0L),
        "online" to online,
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun sanitizePlayerName(rawName: String?, uid: String): String {
            val cleaned = rawName
                ?.substringBefore('@')
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
                .orEmpty()
            return if (cleaned.isNotEmpty()) {
                cleaned.take(40)
            } else {
                "Pixie-${uid.takeLast(4).ifEmpty { "0001" }}"
            }
        }
    }
}

enum class TradeStatus(val firestoreValue: String) {
    PENDING("pending"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        fun fromRaw(raw: String?): TradeStatus =
            entries.firstOrNull { it.firestoreValue == raw } ?: CANCELLED
    }
}

@Keep
@IgnoreExtraProperties
data class TradeSession(
    @DocumentId val id: String = "",
    val serverId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val participantIds: List<String> = emptyList(),
    val status: String = TradeStatus.PENDING.firestoreValue,
    val cancelledBy: String = "",
    val senderPoints: Double = 0.0,
    val senderCash: Double = 0.0,
    val receiverPoints: Double = 0.0,
    val receiverCash: Double = 0.0,
    val senderEscrowPoints: Double = 0.0,
    val senderEscrowCash: Double = 0.0,
    val receiverEscrowPoints: Double = 0.0,
    val receiverEscrowCash: Double = 0.0,
    val senderAccepted: Boolean = false,
    val receiverAccepted: Boolean = false,
    val senderClaimed: Boolean = false,
    val receiverClaimed: Boolean = false,
    val senderRefunded: Boolean = false,
    val receiverRefunded: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    val parsedStatus: TradeStatus
        get() = TradeStatus.fromRaw(status)

    fun isSender(uid: String): Boolean = uid == senderId

    fun partnerIdFor(uid: String): String = if (isSender(uid)) receiverId else senderId

    fun partnerNameFor(uid: String): String = if (isSender(uid)) receiverName else senderName

    fun myOfferPoints(uid: String): Double = if (isSender(uid)) senderPoints else receiverPoints

    fun myOfferCash(uid: String): Double = if (isSender(uid)) senderCash else receiverCash

    fun partnerOfferPoints(uid: String): Double = if (isSender(uid)) receiverPoints else senderPoints

    fun partnerOfferCash(uid: String): Double = if (isSender(uid)) receiverCash else senderCash

    fun myEscrowPoints(uid: String): Double = if (isSender(uid)) senderEscrowPoints else receiverEscrowPoints

    fun myEscrowCash(uid: String): Double = if (isSender(uid)) senderEscrowCash else receiverEscrowCash

    fun partnerEscrowPoints(uid: String): Double = if (isSender(uid)) receiverEscrowPoints else senderEscrowPoints

    fun partnerEscrowCash(uid: String): Double = if (isSender(uid)) receiverEscrowCash else senderEscrowCash

    fun myAccepted(uid: String): Boolean = if (isSender(uid)) senderAccepted else receiverAccepted

    fun partnerAccepted(uid: String): Boolean = if (isSender(uid)) receiverAccepted else senderAccepted

    fun myClaimed(uid: String): Boolean = if (isSender(uid)) senderClaimed else receiverClaimed

    fun myRefunded(uid: String): Boolean = if (isSender(uid)) senderRefunded else receiverRefunded

    fun hasAnyOffer(): Boolean =
        (senderPoints + senderCash + receiverPoints + receiverCash) > 0.0001

    fun toCreateMap(): Map<String, Any> = mapOf(
        "serverId" to serverId,
        "senderId" to senderId,
        "senderName" to ServerPlayer.sanitizePlayerName(senderName, senderId),
        "receiverId" to receiverId,
        "receiverName" to ServerPlayer.sanitizePlayerName(receiverName, receiverId),
        "participantIds" to listOf(senderId, receiverId),
        "status" to TradeStatus.PENDING.firestoreValue,
        "cancelledBy" to "",
        "senderPoints" to 0.0,
        "senderCash" to 0.0,
        "receiverPoints" to 0.0,
        "receiverCash" to 0.0,
        "senderEscrowPoints" to 0.0,
        "senderEscrowCash" to 0.0,
        "receiverEscrowPoints" to 0.0,
        "receiverEscrowCash" to 0.0,
        "senderAccepted" to false,
        "receiverAccepted" to false,
        "senderClaimed" to false,
        "receiverClaimed" to false,
        "senderRefunded" to false,
        "receiverRefunded" to false,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        fun normalizeAmount(raw: Double): Double {
            if (!raw.isFinite() || raw <= 0.0) return 0.0
            return round(raw * 100.0) / 100.0
        }
    }
}

sealed interface TradeOfferValidation {
    data class Valid(val points: Double, val cash: Double) : TradeOfferValidation
    data object InvalidNumber : TradeOfferValidation
    data object ExceedsPoints : TradeOfferValidation
    data object ExceedsCash : TradeOfferValidation
}

object TradeRules {
    fun filterServers(servers: List<GameServer>, query: String): List<GameServer> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return servers
        return servers.filter { server ->
            server.name.contains(trimmed, ignoreCase = true) ||
                server.region.contains(trimmed, ignoreCase = true)
        }
    }

    fun validateOfferInputs(
        pointsInput: String,
        cashInput: String,
        availablePoints: Double,
        availableCash: Double
    ): TradeOfferValidation {
        val parsedPoints = if (pointsInput.isBlank()) {
            0.0
        } else {
            pointsInput.trim().replace(',', '.').toDoubleOrNull()
        }
        val parsedCash = if (cashInput.isBlank()) {
            0.0
        } else {
            cashInput.trim().replace(',', '.').toDoubleOrNull()
        }
        if (parsedPoints == null || parsedCash == null ||
            !parsedPoints.isFinite() || !parsedCash.isFinite() ||
            parsedPoints < 0.0 || parsedCash < 0.0
        ) {
            return TradeOfferValidation.InvalidNumber
        }
        val normalizedPoints = TradeSession.normalizeAmount(parsedPoints)
        val normalizedCash = TradeSession.normalizeAmount(parsedCash)
        if (normalizedPoints > availablePoints + 1e-6) {
            return TradeOfferValidation.ExceedsPoints
        }
        if (normalizedCash > availableCash + 1e-6) {
            return TradeOfferValidation.ExceedsCash
        }
        return TradeOfferValidation.Valid(normalizedPoints, normalizedCash)
    }

    fun canCompleteTrade(trade: TradeSession): Boolean {
        return trade.parsedStatus == TradeStatus.ACTIVE &&
            trade.senderAccepted &&
            trade.receiverAccepted &&
            trade.hasAnyOffer()
    }
}
