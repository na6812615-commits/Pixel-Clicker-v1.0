package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class PixieSoundEffect {
    MANUAL_TAP,
    PURCHASE_SUCCESS,
    PURCHASE_FAIL
}

object PixieSoundSynthesizer {
    const val SAMPLE_RATE = 44_100

    fun buildWavBytes(effect: PixieSoundEffect): ByteArray {
        val pcm = when (effect) {
            PixieSoundEffect.MANUAL_TAP -> synthesizeManualTap()
            PixieSoundEffect.PURCHASE_SUCCESS -> synthesizePurchaseSuccess()
            PixieSoundEffect.PURCHASE_FAIL -> synthesizePurchaseFail()
        }
        return encodePcm16MonoWav(pcm, SAMPLE_RATE)
    }

    internal fun synthesizeManualTap(): ShortArray {
        val durationSec = 0.09
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val out = ShortArray(totalSamples)
        var phaseMain = 0.0
        var phaseHarmonic = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val glide = (t / 0.018).coerceIn(0.0, 1.0)
            val freq = 987.77 + (1479.98 - 987.77) * glide // B5 -> F#6 crystal pluck
            phaseMain += 2.0 * PI * freq / SAMPLE_RATE
            phaseHarmonic += 2.0 * PI * (freq * 2.0) / SAMPLE_RATE

            val attack = (t / 0.003).coerceIn(0.0, 1.0)
            val decay = exp(-t * 40.0)
            val env = attack * decay

            val sample = (0.72 * sin(phaseMain) + 0.28 * sin(phaseHarmonic)) * env * 0.65
            out[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    internal fun synthesizePurchaseSuccess(): ShortArray {
        val notes = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00) // C6 - E6 - G6 - C7
        val noteDur = 0.062
        val tailDur = 0.09
        val totalSamples = (SAMPLE_RATE * (notes.size * noteDur + tailDur)).toInt()
        val out = ShortArray(totalSamples)

        val mix = DoubleArray(totalSamples)
        notes.forEachIndexed { idx, freq ->
            val startIdx = (idx * noteDur * SAMPLE_RATE).toInt()
            val len = ((noteDur + tailDur) * SAMPLE_RATE).toInt()
            var phase = 0.0
            var shimmerPhase = 0.0
            for (j in 0 until len) {
                val pos = startIdx + j
                if (pos >= totalSamples) break
                val t = j.toDouble() / SAMPLE_RATE
                phase += 2.0 * PI * freq / SAMPLE_RATE
                shimmerPhase += 2.0 * PI * (freq * 2.0) / SAMPLE_RATE

                val attack = (t / 0.004).coerceIn(0.0, 1.0)
                val decayRate = if (idx == notes.lastIndex) 16.0 else 28.0
                val env = attack * exp(-t * decayRate)
                val wave = 0.78 * sin(phase) + 0.22 * sin(shimmerPhase)
                mix[pos] += wave * env * 0.48
            }
        }

        for (i in 0 until totalSamples) {
            out[i] = (mix[i].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    internal fun synthesizePurchaseFail(): ShortArray {
        val notes = doubleArrayOf(220.0, 155.56) // A3 -> Eb3 classic descending deny
        val noteDurs = doubleArrayOf(0.095, 0.125)
        val totalDur = noteDurs.sum()
        val totalSamples = (SAMPLE_RATE * totalDur).toInt()
        val out = ShortArray(totalSamples)

        var cursor = 0
        notes.forEachIndexed { idx, freq ->
            val len = (noteDurs[idx] * SAMPLE_RATE).toInt()
            var phase = 0.0
            for (j in 0 until len) {
                val pos = cursor + j
                if (pos >= totalSamples) break
                val t = j.toDouble() / SAMPLE_RATE
                phase += 2.0 * PI * freq / SAMPLE_RATE

                val attack = (t / 0.005).coerceIn(0.0, 1.0)
                val decay = exp(-t * 14.0)
                val env = attack * decay
                // Warm buzz: fundamental + odd harmonics softly shaped
                val raw = 0.65 * sin(phase) + 0.25 * sin(phase * 3.0) + 0.12 * sin(phase * 2.0)
                val sample = raw * env * 0.58
                out[pos] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            }
            cursor += len
        }
        return out
    }

    private fun encodePcm16MonoWav(samples: ShortArray, sampleRate: Int): ByteArray {
        val dataBytes = samples.size * 2
        val byteRate = sampleRate * 2
        val out = ByteArrayOutputStream(44 + dataBytes)

        fun writeAscii(text: String) {
            out.write(text.toByteArray(Charsets.US_ASCII))
        }

        fun writeIntLe(value: Int) {
            out.write(value and 0xFF)
            out.write((value ushr 8) and 0xFF)
            out.write((value ushr 16) and 0xFF)
            out.write((value ushr 24) and 0xFF)
        }

        fun writeShortLe(value: Int) {
            out.write(value and 0xFF)
            out.write((value ushr 8) and 0xFF)
        }

        writeAscii("RIFF")
        writeIntLe(36 + dataBytes)
        writeAscii("WAVE")
        writeAscii("fmt ")
        writeIntLe(16) // PCM chunk size
        writeShortLe(1) // PCM format
        writeShortLe(1) // Mono
        writeIntLe(sampleRate)
        writeIntLe(byteRate)
        writeShortLe(2) // Block align
        writeShortLe(16) // Bits per sample
        writeAscii("data")
        writeIntLe(dataBytes)

        for (s in samples) {
            writeShortLe(s.toInt())
        }
        return out.toByteArray()
    }
}

class PixieSoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val soundIds = ConcurrentHashMap<PixieSoundEffect, Int>()
    private val loadedIds = ConcurrentHashMap.newKeySet<Int>()

    @Volatile
    private var released = false

    private val soundPool: SoundPool? = runCatching {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()
            .apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedIds.add(sampleId)
                    }
                }
            }
    }.getOrNull()

    init {
        preloadAll()
    }

    private fun preloadAll() {
        val pool = soundPool ?: return
        scope.launch {
            runCatching {
                val dir = File(appContext.cacheDir, "pixie_sfx")
                if (!dir.exists()) dir.mkdirs()

                PixieSoundEffect.entries.forEach { effect ->
                    if (released) return@forEach
                    val file = File(dir, "${effect.name.lowercase()}.wav")
                    val bytes = PixieSoundSynthesizer.buildWavBytes(effect)
                    FileOutputStream(file).use { it.write(bytes) }
                    val id = pool.load(file.absolutePath, 1)
                    if (id > 0) {
                        soundIds[effect] = id
                    }
                }
            }
        }
    }

    fun play(effect: PixieSoundEffect) {
        if (released) return
        val pool = soundPool ?: return
        val sampleId = soundIds[effect] ?: return
        if (!loadedIds.contains(sampleId)) return
        val volume = when (effect) {
            PixieSoundEffect.MANUAL_TAP -> 0.80f
            PixieSoundEffect.PURCHASE_SUCCESS -> 0.90f
            PixieSoundEffect.PURCHASE_FAIL -> 0.85f
        }
        runCatching {
            pool.play(sampleId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun release() {
        released = true
        scope.cancel()
        runCatching { soundPool?.release() }
        soundIds.clear()
        loadedIds.clear()
    }
}
