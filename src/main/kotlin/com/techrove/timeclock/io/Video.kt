package com.techrove.timeclock.io

import com.techrove.timeclock.extensions.runCommand
import com.techrove.timeclock.isLinux
import kotlinx.coroutines.*

object Video {
    private var job: Job? = null
    private var initial = true

    fun play(file: String) {
//        if (!isLinux) return
//        job?.cancel()
//        job = GlobalScope.launch(Dispatchers.Default) {
//            delay(if (initial) 5000 else 1000)
//            if (isActive) {
//                initial = false
//                "/usr/bin/mpv --geometry=768x400+100%+34 --no-config --no-osc --no-osd-bar --ao=alsa --audio-device=alsa/plughw:CARD=OnBoard,DEV=2 --no-input-cursor --loop audio/$file".runCommand()
//            }
//        }
    }

    fun stop() {
//        if (!isLinux) return
//        job?.cancel()
//        GlobalScope.launch(Dispatchers.Default) {
//            "killall -9 mpv".runCommand()
//        }
    }
}
