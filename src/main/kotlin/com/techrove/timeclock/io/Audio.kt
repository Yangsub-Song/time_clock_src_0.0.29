package com.techrove.timeclock.io

import com.techrove.timeclock.Settings
import com.techrove.timeclock.extensions.runCommand
import com.techrove.timeclock.isLinux
import javafx.scene.media.AudioClip
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.io.File

object Audio {
    private var mediaPlayer: MediaPlayer? = null
    private var job: Deferred<Unit>? = null
    private var interrupt = true

    fun play(path: String, interruptPlay: Boolean = true, useClipApi: Boolean = false, endOfMedia: () -> Unit = {}) {
        if (path == "beep1.wav" && !Settings.beep) {
            return
        }
        GlobalScope.launch {
            if (isLinux) {
                stop()
                job = GlobalScope.async(Dispatchers.IO) {
                    interrupt = interruptPlay
                    "/usr/bin/aplay -D plughw:0,2".runCommand(
                        argument = File(
                            "audio",
                            path
                        ).absolutePath
                    )
                    withContext(Dispatchers.JavaFx) {
                        endOfMedia()
                    }
                    job = null
                }
            } else {
                stop()
                val source = File("audio", path).toURI().toString()
                if (useClipApi) {
                    AudioClip(source).play()
                } else {
                    mediaPlayer = MediaPlayer(Media(source)).apply {
                        play()
                        setOnEndOfMedia {
                            endOfMedia()
                        }
                    }
                }
            }
        }
    }

    suspend fun stop() {
        if (isLinux) {
            if (job == null) return
            if (interrupt) {
                job?.cancel()
                "killall -9 aplay".runCommand()
            } else {
                job?.await()
            }
            job = null
        } else {
            mediaPlayer?.stop()
        }
    }

    fun setVolume(percent: Int) {
        if (isLinux) {
            GlobalScope.async(Dispatchers.IO) {
                //-c 0 -D 2
                "amixer set Headphone,1 ${percent}%".runCommand()
            }
        }
    }
}
