package com.techrove.timeclock.utils

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import mu.KotlinLogging
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger("SFTP")

object SshUtil {

    suspend fun getFile(
        remoteUri: String,
        localFile: File,
        defaultUserName: String = "terminal",
        defaultPassword: String = "wpqkfgjrkgowntpdy!",
        defaultPort: Int = 32222,
    ): Boolean {
        var session: Session? = null
        var channel: Channel? = null
        return try {
            logger.info { "downloading $remoteUri to ${localFile.path}" }
            val uri = URI(
                if (!remoteUri.startsWith("sftp://")) {
                    "sftp://$remoteUri"
                } else remoteUri
            )
            val userInfo = uri.userInfo?.split(":")
            val userName = userInfo?.getOrNull(0) ?: defaultUserName
            val password = userInfo?.getOrNull(1) ?: defaultPassword

            session = JSch().getSession(userName, uri.host, if (uri.port < 0) defaultPort else uri.port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                timeout = 20 * 1000
                connect()
            }

            channel = (session?.openChannel("sftp") as? ChannelSftp)?.apply {
                connect()
                get(uri.path, localFile.path)
            }

            logger.info { "download complete" }
            true
        } catch (e: Exception) {
            logger.error { e }
            false
        } finally {
            channel?.disconnect()
            session?.disconnect()
        }
    }
}
