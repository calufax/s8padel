package com.calufax.s8padel.data

import android.content.Context
import androidx.core.util.AtomicFile
import com.calufax.s8padel.model.TournamentState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repositorio local usando AtomicFile para garantizar que aunque se cierre la app
 * abruptamente o el sistema corte el proceso, jamás se pierda ni corrompa el estado cargado.
 */
class TournamentRepository(context: Context) {

    private val storageFile = File(context.applicationContext.filesDir, FILE_NAME)
    private val atomicFile = AtomicFile(storageFile)
    private val writeMutex = Mutex()

    fun loadImmediate(): TournamentState {
        if (!storageFile.exists()) {
            return TournamentState()
        }
        return runCatching {
            val bytes = atomicFile.readFully()
            val json = bytes.toString(Charsets.UTF_8)
            TournamentJsonSerializer.decode(json)
        }.getOrElse {
            TournamentState()
        }
    }

    suspend fun save(state: TournamentState) {
        withContext(Dispatchers.IO) {
            writeMutex.withLock {
                val json = TournamentJsonSerializer.encode(state)
                val bytes = json.toByteArray(Charsets.UTF_8)
                var stream: java.io.FileOutputStream? = null
                try {
                    stream = atomicFile.startWrite()
                    stream.write(bytes)
                    stream.flush()
                    atomicFile.finishWrite(stream)
                } catch (e: Exception) {
                    if (stream != null) {
                        atomicFile.failWrite(stream)
                    }
                    throw e
                }
            }
        }
    }

    companion object {
        private const val FILE_NAME = "super8_tournament_v1.json"
    }
}
