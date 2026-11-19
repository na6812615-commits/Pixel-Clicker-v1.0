package com.example.ui.game

import com.example.data.GameProgress
import com.example.ui.i18n.PixieStrings
import com.example.ui.i18n.ViPixieStrings
import kotlin.math.roundToLong

data class GameActionResult(
    val nextProgress: GameProgress,
    val message: String,
    val succeeded: Boolean,
    val shouldSyncNow: Boolean
)

object GameEngine {

    fun effectiveCost(basePrice: Double, giamgia: Double): Long {
        return Math.round(basePrice / giamgia.coerceAtLeast(1.0))
    }

    fun pointsPerManualClick(progress: GameProgress): Double {
        return round2(progress.x * progress.muti)
    }

    fun pointsPerAutoClick(progress: GameProgress): Double {
        return round2(progress.x2 * progress.muti)
    }

    fun round2(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }

    fun manualClick(progress: GameProgress): GameProgress {
        val gained = progress.x * progress.muti
        return progress.copy(
            diem = progress.diem + gained,
            clickCount = progress.clickCount + 1L
        )
    }

    fun autoClickTick(progress: GameProgress): GameProgress {
        if (!progress.autoEnabled || progress.gioihan) return progress
        val gained = progress.x2 * progress.muti
        return progress.copy(
            diem = progress.diem + gained,
            clickCount = progress.clickCount + 1L
        )
    }

    // Shop Item 1: +x diem / gia (initial 25, x3) / lv
    fun buyShopPower(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        val cost = effectiveCost(progress.gia, progress.giamgia)
        return if (progress.diem >= cost) {
            val next = progress.copy(
                diem = (progress.diem - (progress.gia / progress.giamgia)).coerceAtLeast(0.0),
                x = progress.x * 2.0,
                gia = progress.gia * 3.0,
                lv = progress.lv + 1L
            )
            GameActionResult(next, strings.msgShopPowerOk, true, true)
        } else {
            val missing = (cost - progress.diem).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughPoints(missing),
                false,
                false
            )
        }
    }

    // Shop Item 2: +0.1 muti / gia2 (initial 50, x3) / lv2
    fun buyShopMultiplier(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        val cost = effectiveCost(progress.gia2, progress.giamgia)
        return if (progress.diem >= cost) {
            val next = progress.copy(
                diem = (progress.diem - (progress.gia2 / progress.giamgia)).coerceAtLeast(0.0),
                muti = round2(progress.muti + 0.1),
                gia2 = progress.gia2 * 3.0,
                lv2 = progress.lv2 + 1L
            )
            GameActionResult(next, strings.msgShopMultiOk, true, true)
        } else {
            val missing = (cost - progress.diem).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughPoints(missing),
                false,
                false
            )
        }
    }

    // Shop Item 3: giam countdown / gia4 (initial 75, x3) / lv4
    fun buyShopCooldown(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        val cost = effectiveCost(progress.gia4, progress.giamgia)
        return if (progress.diem >= cost) {
            val nextCd = (progress.cd - progress.thay).coerceAtLeast(1e-6)
            val nextThay = (progress.thay / 2.0).coerceAtLeast(1e-6)
            val next = progress.copy(
                diem = (progress.diem - (progress.gia4 / progress.giamgia)).coerceAtLeast(0.0),
                gia4 = progress.gia4 * 3.0,
                lv4 = progress.lv4 + 1L,
                cd = nextCd,
                thay = nextThay
            )
            GameActionResult(next, strings.msgShopCooldownOk, true, true)
        } else {
            val missing = (cost - progress.diem).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughPoints(missing),
                false,
                false
            )
        }
    }

    // Exchange 1: 1000 diem -> $1
    fun exchange1000(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        return if (progress.diem >= 1000.0) {
            val next = progress.copy(
                diem = (progress.diem - 1000.0).coerceAtLeast(0.0),
                cashDo = progress.cashDo + 1.0
            )
            GameActionResult(next, strings.msgExchange1000Ok, true, true)
        } else {
            val missing = (1000.0 - progress.diem).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughExchange(missing),
                false,
                false
            )
        }
    }

    // Exchange 2: all diem -> diem / 1000 $
    fun exchangeAll(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        if (progress.diem <= 0.0) {
            return GameActionResult(progress, strings.msgZeroPointsExchange, false, false)
        }
        val gainedCash = progress.diem / 1000.0
        val next = progress.copy(
            cashDo = progress.cashDo + gainedCash,
            diem = 0.0
        )
        return GameActionResult(
            next,
            strings.msgExchangeAllOk(formatCash(gainedCash)),
            true,
            true
        )
    }

    // $Shop$ 1: giam gia / gia5 (initial 5$, x3) / lv5
    fun buyDollarDiscount(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        return if (progress.cashDo >= progress.gia5) {
            val next = progress.copy(
                cashDo = (progress.cashDo - progress.gia5).coerceAtLeast(0.0),
                gia5 = progress.gia5 * 3.0,
                giamgia = progress.giamgia + progress.thay2,
                thay2 = progress.thay2 * 2.0,
                lv5 = progress.lv5 + 1L
            )
            GameActionResult(next, strings.msgDiscountOk, true, true)
        } else {
            val missing = (progress.gia5 - progress.cashDo).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughCash(missing),
                false,
                false
            )
        }
    }

    // $Shop$ 2: giam thoi gian roi khoi menu / gia6 (initial 3$, x3) / lv6
    fun buyDollarMenuSpeed(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        return if (progress.cashDo >= progress.gia6) {
            val next = progress.copy(
                cashDo = (progress.cashDo - progress.gia6).coerceAtLeast(0.0),
                cd3 = (progress.cd3 / 2.0).coerceAtLeast(1e-6),
                gia6 = progress.gia6 * 3.0,
                lv6 = progress.lv6 + 1L
            )
            GameActionResult(next, strings.msgMenuSpeedOk, true, true)
        } else {
            val missing = (progress.gia6 - progress.cashDo).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughCash(missing),
                false,
                false
            )
        }
    }

    // $Shop$ 3: giam thoi gian thong bao / gia7 (initial 3$, x3) / lv7
    fun buyDollarToastSpeed(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        return if (progress.cashDo >= progress.gia7) {
            val next = progress.copy(
                cashDo = (progress.cashDo - progress.gia7).coerceAtLeast(0.0),
                cd4 = (progress.cd4 / 2.0).coerceAtLeast(1e-6),
                gia7 = progress.gia7 * 3.0,
                lv7 = progress.lv7 + 1L
            )
            GameActionResult(next, strings.msgToastSpeedOk, true, true)
        } else {
            val missing = (progress.gia7 - progress.cashDo).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughCash(missing),
                false,
                false
            )
        }
    }

    // $Shop$ 4: auto click / 25$ (one-time)
    fun buyDollarAutoUnlock(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        if (!progress.gioihan) {
            return GameActionResult(progress, strings.msgAutoAlreadyOwned, false, false)
        }
        return if (progress.cashDo >= 25.0) {
            val next = progress.copy(
                cashDo = (progress.cashDo - 25.0).coerceAtLeast(0.0),
                gioihan = false,
                autoEnabled = true
            )
            GameActionResult(next, strings.msgAutoUnlocked, true, true)
        } else {
            val missing = (25.0 - progress.cashDo).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughCash(missing),
                false,
                false
            )
        }
    }

    // Auto Click Upgrade 1: bat / tat auto click
    fun toggleAutoClick(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        if (progress.gioihan) {
            return GameActionResult(
                progress,
                strings.msgAutoMissingToggle,
                false,
                false
            )
        }
        val nextEnabled = !progress.autoEnabled
        val next = progress.copy(autoEnabled = nextEnabled)
        val msg = if (nextEnabled) strings.msgAutoTurnedOn else strings.msgAutoTurnedOff
        return GameActionResult(next, msg, true, true)
    }

    // Auto Click Upgrade 2: +x2fake diem cho auto click / gia8 (initial 3$, x3) / lv8
    fun buyAutoPower(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        if (progress.gioihan) {
            return GameActionResult(progress, strings.msgAutoMissingUpgrade, false, false)
        }
        return if (progress.cashDo >= progress.gia8) {
            val next = progress.copy(
                cashDo = (progress.cashDo - progress.gia8).coerceAtLeast(0.0),
                x2 = progress.x2 + progress.x2fake,
                x2fake = progress.x2fake * 3.0,
                gia8 = progress.gia8 * 3.0,
                lv8 = progress.lv8 + 1L
            )
            GameActionResult(next, strings.msgAutoPowerOk, true, true)
        } else {
            val missing = (progress.gia8 - progress.cashDo).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughCash(missing),
                false,
                false
            )
        }
    }

    // Auto Click Upgrade 3: giam cd2 / gia9 (initial 2$, x3) / lv9
    fun buyAutoSpeed(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        if (progress.gioihan) {
            return GameActionResult(progress, strings.msgAutoMissingUpgrade, false, false)
        }
        return if (progress.cashDo >= progress.gia9) {
            val next = progress.copy(
                cashDo = (progress.cashDo - progress.gia9).coerceAtLeast(0.0),
                cd2 = (progress.cd2 / 2.0).coerceAtLeast(1e-6),
                gia9 = progress.gia9 * 3.0,
                lv9 = progress.lv9 + 1L
            )
            GameActionResult(next, strings.msgAutoSpeedOk, true, true)
        } else {
            val missing = (progress.gia9 - progress.cashDo).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughCash(missing),
                false,
                false
            )
        }
    }

    // Restarts (Prestige): reset run state, permanently increase muti2 by +0.5
    fun performRestart(progress: GameProgress, strings: PixieStrings = ViPixieStrings): GameActionResult {
        val cost = effectiveCost(progress.gia3, progress.giamgia)
        return if (progress.diem >= cost) {
            val nextBaseMuti = round2(progress.muti2 + 0.5)
            val next = GameProgress(
                id = progress.id,
                userId = progress.userId,
                diem = 0.0,
                cashDo = 0.0,
                clickCount = 0L,
                muti = nextBaseMuti,
                muti2 = nextBaseMuti,
                x = 1.0,
                cd = 0.25,
                thay = 0.125,
                giamgia = 1.0,
                thay2 = 0.1,
                cd3 = 1.0,
                cd4 = 1.0,
                lv = 0L,
                gia = 25.0,
                lv2 = 0L,
                gia2 = 50.0,
                lv3 = progress.lv3 + 1L,
                gia3 = progress.gia3 * 3.0,
                lv4 = 0L,
                gia4 = 75.0,
                lv5 = 0L,
                gia5 = 5.0,
                lv6 = 0L,
                gia6 = 3.0,
                lv7 = 0L,
                gia7 = 3.0,
                gioihan = true,
                autoEnabled = false,
                x2 = 1.0,
                x2fake = 1.0,
                cd2 = 1.0,
                lv8 = 0L,
                gia8 = 3.0,
                lv9 = 0L,
                gia9 = 2.0,
                createdAt = progress.createdAt,
                updatedAt = progress.updatedAt
            )
            GameActionResult(
                next,
                strings.msgRestartOk(nextBaseMuti.toString()),
                true,
                true
            )
        } else {
            val missing = (cost - progress.diem).roundToLong().coerceAtLeast(1L)
            GameActionResult(
                progress,
                strings.msgNotEnoughRestart(missing),
                false,
                false
            )
        }
    }

    fun formatCash(cash: Double): String {
        val rounded = Math.round(cash * 1000.0) / 1000.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    fun formatSeconds(sec: Double): String {
        val rounded = Math.round(sec * 10000.0) / 10000.0
        return rounded.toString()
    }

    fun formatGameNumber(value: Double, compact: Boolean): String {
        val whole = value.roundToLong()
        return formatGameCount(whole, compact)
    }

    fun formatGameCount(value: Long, compact: Boolean): String {
        if (!compact || value < 1_000L) return value.toString()
        val abs = value.toDouble()
        return when {
            abs >= 1_000_000_000_000.0 -> String.format(java.util.Locale.US, "%.2fT", abs / 1_000_000_000_000.0)
            abs >= 1_000_000_000.0 -> String.format(java.util.Locale.US, "%.2fB", abs / 1_000_000_000.0)
            abs >= 1_000_000.0 -> String.format(java.util.Locale.US, "%.2fM", abs / 1_000_000.0)
            else -> String.format(java.util.Locale.US, "%.2fK", abs / 1_000.0)
        }
    }
}
