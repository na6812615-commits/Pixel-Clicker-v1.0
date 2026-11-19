package com.example.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.AppLanguage
import com.example.data.AutoSyncInterval
import com.example.data.CrystalAuraTheme
import com.example.data.VfxIntensity

data class PixieStrings(
    // Auth & Common
    val appTitleUppercase: String,
    val tagline: String,
    val mechanicsHeading: String,
    val featureTap: String,
    val featureDollar: String,
    val featureAuto: String,
    val featureRestart: String,
    val featureCloud: String,
    val signInGoogle: String,
    val signingIn: String,
    val signOut: String,
    val loadingWorld: String,
    val loadErrorTitle: String,
    val retry: String,

    // Navigation Tabs
    val tabTap: String,
    val tabShop: String,
    val tabDollarShop: String,
    val tabRestarts: String,
    val tabSettings: String,

    // Stats Header & Ranks
    val statsTitle: String,
    val statsPoints: String,
    val statsCash: String,
    val statsMultiplier: String,
    val statsPerClick: String,
    val statsClicks: String,
    val statsCountdown: String,
    val saveNow: String,
    val openSettings: String,
    val rankNovice: String,
    val rankMage: String,
    val rankSprite: String,
    val rankSage: String,
    val rankMythic: String,

    // Tap Arena
    val sanctuaryBadge: String,
    val discountLabel: (String) -> String,
    val comboLabel: (Int) -> String,
    val tapHintAccessibility: String,
    val cooldownWait: (String) -> String,
    val tapCrystalCta: (String) -> String,
    val nextDollarTitle: String,
    val canExchangeDollarHint: (Int) -> String,
    val autoLockedTitle: String,
    val autoActiveTitle: String,
    val autoPausedTitle: String,
    val autoUnlockHint: String,
    val autoRateHint: (String, String) -> String,
    val btnTurnOn: String,
    val btnTurnOff: String,
    val btnBuy: String,
    val btnDone: String,

    // Shop
    val shopTitle: String,
    val shopBannerDesc: (String) -> String,
    val shopSectionHeader: String,
    val shopPowerTitle: (String) -> String,
    val shopPowerSub: (String, String) -> String,
    val shopMultiTitle: String,
    val shopMultiSub: (String, String) -> String,
    val shopCooldownTitle: String,
    val shopCooldownSub: (String, String) -> String,
    val shopTip: String,
    val costPoints: (String) -> String,
    val costDollar: (String) -> String,
    val levelBadge: (Long) -> String,

    // $Shop$
    val dollarExchangeTitle: String,
    val exchangeAllDesc: (String) -> String,
    val btnExchange1000: String,
    val btnExchangeAll: String,
    val dollarShopTitle: String,
    val dollarDiscountTitle: String,
    val dollarDiscountSub: (String) -> String,
    val dollarMenuSpeedTitle: String,
    val dollarMenuSpeedSub: (String, String) -> String,
    val dollarToastSpeedTitle: String,
    val dollarToastSpeedSub: (String, String) -> String,
    val dollarAutoTitle: String,
    val dollarAutoSubLocked: String,
    val dollarAutoSubOwned: String,
    val statusNotOwned: String,
    val statusOwned: String,
    val costBought: String,
    val autoUpgradeTitle: String,
    val autoLockedNotice: String,
    val autoToggleTitle: String,
    val autoStatusOn: String,
    val autoStatusOff: String,
    val autoToggleDesc: (Boolean, String, String) -> String,
    val autoPowerTitle: (Long) -> String,
    val autoPowerSub: (Long, Long) -> String,
    val autoSpeedTitle: String,
    val autoSpeedSub: (String, String) -> String,

    // Restarts
    val restartsTitle: String,
    val restartsPermMultiplierLabel: String,
    val restartsRequired: (String) -> String,
    val restartsReadyDesc: String,
    val restartsMissingDesc: (String) -> String,
    val restartsActionBtn: String,
    val restartsRulesTitle: String,
    val restartsRulesBody: (String, String) -> String,
    val restartsConfirmTitle: String,
    val restartsConfirmBody: String,
    val confirmYes: String,
    val confirmCancel: String,

    // Settings
    val settingsTitle: String,
    val settingsSubtitle: String,
    val settingsAutoSavedBadge: String,
    val settingsBack: String,
    val settingsSectionLanguage: String,
    val settingsLanguageTitle: String,
    val settingsLanguageDesc: String,
    val settingsSectionVfx: String,
    val vfxIntensityTitle: String,
    val vfxIntensityDesc: String,
    val vfxLow: String,
    val vfxNormal: String,
    val vfxUltra: String,
    val auraThemeTitle: String,
    val auraThemeDesc: String,
    val auraMint: String,
    val auraRose: String,
    val auraCyan: String,
    val auraGold: String,
    val ambientTitle: String,
    val ambientDesc: String,
    val arcaneRingsTitle: String,
    val arcaneRingsDesc: String,
    val comboTitle: String,
    val comboDesc: String,
    val floatingTitle: String,
    val floatingDesc: String,
    val autoFloatingTitle: String,
    val autoFloatingDesc: String,
    val settingsSectionGameplay: String,
    val soundEffectsTitle: String,
    val soundEffectsDesc: String,
    val hapticTitle: String,
    val hapticDesc: String,
    val shakeTitle: String,
    val shakeDesc: String,
    val upgradeBadgesTitle: String,
    val upgradeBadgesDesc: String,
    val dollarMeterTitle: String,
    val dollarMeterDesc: String,
    val confirmRestartTitle: String,
    val confirmRestartDesc: String,
    val compactNumbersTitle: String,
    val compactNumbersDesc: String,
    val settingsSectionCloud: String,
    val autoSyncTitle: String,
    val autoSyncDesc: String,
    val syncIntervalTitle: String,
    val syncIntervalDesc: String,
    val syncInterval4s: String,
    val syncInterval8s: String,
    val syncInterval20s: String,
    val playerSnapshotTitle: String,
    val playerSnapshotDetail: (String, String, String) -> String,
    val linkedGoogleAccount: String,
    val accountSummary: (Long, Long) -> String,
    val syncNowBtn: String,
    val syncingBtn: String,
    val resetDefaultsBtn: String,

    // Engine & ViewModel messages
    val msgShopPowerOk: String,
    val msgNotEnoughPoints: (Long) -> String,
    val msgShopMultiOk: String,
    val msgShopCooldownOk: String,
    val msgExchange1000Ok: String,
    val msgNotEnoughExchange: (Long) -> String,
    val msgZeroPointsExchange: String,
    val msgExchangeAllOk: (String) -> String,
    val msgDiscountOk: String,
    val msgMenuSpeedOk: String,
    val msgToastSpeedOk: String,
    val msgAutoAlreadyOwned: String,
    val msgAutoUnlocked: String,
    val msgNotEnoughCash: (Long) -> String,
    val msgAutoMissingToggle: String,
    val msgAutoTurnedOn: String,
    val msgAutoTurnedOff: String,
    val msgAutoMissingUpgrade: String,
    val msgAutoPowerOk: String,
    val msgAutoSpeedOk: String,
    val msgRestartOk: (String) -> String,
    val msgNotEnoughRestart: (Long) -> String,
    val toastResetSettings: String,
    val toastSavedCloud: String,
    val errLoadGame: String,
    val errSyncCloud: String
) {
    fun vfxLabel(intensity: VfxIntensity): String = when (intensity) {
        VfxIntensity.LOW -> vfxLow
        VfxIntensity.NORMAL -> vfxNormal
        VfxIntensity.ULTRA -> vfxUltra
    }

    fun auraName(theme: CrystalAuraTheme): String = when (theme) {
        CrystalAuraTheme.PIXIE_MINT -> auraMint
        CrystalAuraTheme.ROSE_NOVA -> auraRose
        CrystalAuraTheme.CYBER_CYAN -> auraCyan
        CrystalAuraTheme.SOLAR_GOLD -> auraGold
    }

    fun syncIntervalLabel(interval: AutoSyncInterval): String = when (interval) {
        AutoSyncInterval.FAST_4S -> syncInterval4s
        AutoSyncInterval.STANDARD_8S -> syncInterval8s
        AutoSyncInterval.RELAXED_20S -> syncInterval20s
    }
}

val LocalPixieStrings = staticCompositionLocalOf<PixieStrings> { ViPixieStrings }

fun pixieStringsFor(language: AppLanguage): PixieStrings = when (language) {
    AppLanguage.VI -> ViPixieStrings
    AppLanguage.EN -> EnPixieStrings
    AppLanguage.JA -> JaPixieStrings
    AppLanguage.KO -> KoPixieStrings
    AppLanguage.ZH -> ZhPixieStrings
    AppLanguage.ES -> EsPixieStrings
    AppLanguage.FR -> FrPixieStrings
    AppLanguage.DE -> DePixieStrings
    AppLanguage.PT -> PtPixieStrings
    AppLanguage.RU -> RuPixieStrings
    AppLanguage.TH -> ThPixieStrings
    AppLanguage.ID -> IdPixieStrings
}
