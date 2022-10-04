package com.techrove.timeclock.security

import com.techrove.timeclock.Settings
import com.techrove.timeclock.database.TimeSheet
import com.techrove.timeclock.database.transactionWithLock
import com.techrove.timeclock.extensions.safeLet
import com.techrove.timeclock.server.cwma.CwmaServer
import com.techrove.timeclock.utils.toHexString
import mu.KotlinLogging
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime

private val logger = KotlinLogging.logger("Key")

object Key {
    var tmsKey = byteArrayOf()
    var photoKey = byteArrayOf()
    var pwdKey = byteArrayOf()
    var pwdSUKey = byteArrayOf()
    var pwdSFKey = byteArrayOf()
    var idsnKey = byteArrayOf()
    var cardKey = byteArrayOf()
    var fingerKey = byteArrayOf()
}

private val messageDigest = MessageDigest.getInstance("SHA-256")

fun ByteArray.digest(): ByteArray = messageDigest.digest(this)

object KeyHelper {

    var masterKey = byteArrayOf()
        private set

    private const val keyDir = "./keys"

    var swIntegrityOk = false
    var keyIntegrityOk = false
    val allIntegrityOk get() = swIntegrityOk && keyIntegrityOk

    private fun writeKeyFile(name: String, key: ByteArray): Boolean {
        try {
            File(keyDir).mkdirs()
            File(keyDir, "${name}.bin").writeBytes(key)
            File(keyDir, "${name}.sha").writeText(key.digest().toHexString())
        } catch (e: Exception) {
            logger.error { "$name key write error" }
            logger.error { e }
            return false
        }
        return true
    }

    private fun readKeyFile(name: String): ByteArray? {
        return try {
            val key = File(keyDir, "${name}.bin").readBytes()
            if (File(keyDir, "${name}.sha").readText() == key.digest().toHexString()) {
                key
            } else {
                logger.error { "$name key sha different" }
                null
            }
        } catch (e: Exception) {
            logger.error { e }
            null
        }
    }

    // Yade1004
    fun verifyKeyFile(dir: String, name: String, key: ByteArray): ByteArray? {
        return try {
//            val key = File(dir, "${name}.bin").readBytes()
//            logger.error { "$name key sha different" }
            val readSha = File(dir, "${name}.sha").readText()
            val madeSha = key.digest().toHexString() // .toStrin(0 String(key.digest())
            logger.info { "$readSha(read) --- $madeSha(made)" }
            if (readSha == madeSha) {
                key
            } else {
                logger.error { "$name key sha different" }
                null
            }
        } catch (e: Exception) {
            logger.error { e }
            null
        }
    }

    private fun checkIntegrityWithMasterKey(name: String): ByteArray? {
        val keyEnc = readKeyFile(name)
        if (keyEnc == null) {
            logger.error { "$name key integrity check error" }
            return null
        }
        return Cipher.decrypt(masterKey, keyEnc) ?: run {
            logger.error { "$name key integrity check decrypt error" }
            return null
        }
    }

    private fun createKeyWithMasterKey(name: String): ByteArray? {
        val key = Cipher.newKey()
        val keyEnc = Cipher.encrypt(masterKey, key)
        if (!writeKeyFile(name, keyEnc)) {
            logger.error { "$name key create error" }
            return null
        }
        return key
    }

    fun checkFileSha(fileName: String): Boolean {
        val file = File(fileName)
        if (!file.exists()) {
            logger.error { "${file.name} doesn't exit" }
            return false
        }
        val sha = File(fileName.replace("""\.[^\.]+$""".toRegex(), ".sha"))
        if (!sha.exists()) {
            logger.error { "${sha.name} doesn't exit" }
            return false
        }
        val content = file.readBytes()
        if (!sha.readText().startsWith(content.digest().toHexString())) {
            logger.error { "$fileName integrity check error" }
            return false
        }
        return true
    }

    fun checkSwIntegrity(): Boolean {
        swIntegrityOk = false
        if (!checkFileSha("./time_clock.jar")) {
            logger.error { "jar integrity check error" }
            return false
        }
        swIntegrityOk = true
        return true
    }

    private fun checkKeyIntegrityInternal(): Boolean {
        keyIntegrityOk = false
        val masterKeyEnc = readKeyFile("master")
        if (masterKeyEnc == null) {
            logger.error { "master key integrity error" }
            return false
        }
        masterKey = Cipher.parseMasterKey(masterKeyEnc)

        Key.pwdKey = checkIntegrityWithMasterKey("pwd") ?: return false
        Key.pwdSUKey = checkIntegrityWithMasterKey("pwd") ?: return false       // Yade0927
        Key.pwdSFKey = checkIntegrityWithMasterKey("pwd") ?: return false       // Yade0927
        Key.tmsKey = checkIntegrityWithMasterKey("tms") ?: return false
        Key.photoKey = checkIntegrityWithMasterKey("photo") ?: return false
        Key.idsnKey = checkIntegrityWithMasterKey("idsn") ?: return false
        Key.cardKey = checkIntegrityWithMasterKey("card") ?: return false
        Key.fingerKey = checkIntegrityWithMasterKey("finger") ?: return false

        logger.info { "integrity check done" }
        keyIntegrityOk = true
        return true
    }

    fun checkKeyIntegrity(): Boolean {
        return checkKeyIntegrityInternal().also {
            if (!it) {
                File("./keys").deleteRecursively()
            }
        }
    }

    private fun deleteAllPictures() {
        ///////////////////////////////////////////////////////////////////////////
        // 사진 폴더 삭제
        ///////////////////////////////////////////////////////////////////////////
        File("./pictures").deleteRecursively()
        File("./pictures").mkdirs()
    }

    fun resetKeys(): Boolean {
        keyIntegrityOk = false
        val masterKeyEnc = Cipher.newMasterKey()
        if (!writeKeyFile("master", masterKeyEnc)) {
            logger.error { "master key create error" }
            return false
        }
        logger.info { "master key created" }

        // backup & restore server key since server key is encrypted with master key
        val cwmaServerKey = CwmaServer.key
        masterKey = Cipher.parseMasterKey(masterKeyEnc)
        CwmaServer.key = cwmaServerKey

        Key.tmsKey = createKeyWithMasterKey("tms") ?: return false
        Key.photoKey = createKeyWithMasterKey("photo") ?: return false
        Key.pwdKey = createKeyWithMasterKey("pwd") ?: return false
        Key.pwdSUKey = createKeyWithMasterKey("pwd") ?: return false       // Yade0927
        Key.pwdSFKey = createKeyWithMasterKey("pwd") ?: return false       // Yade0927
        Key.idsnKey = createKeyWithMasterKey("idsn") ?: return false
        Key.cardKey = createKeyWithMasterKey("card") ?: return false
        Key.fingerKey = createKeyWithMasterKey("finger") ?: return false

        logger.info { "all keys created" }
        keyIntegrityOk = true
        Settings.keyRenewedDate = LocalDateTime.now()

        deleteAllPictures()
        return true
    }

    fun renewKeys(): Boolean {
        if (!keyIntegrityOk) {
            logger.error { "cannot renew keys. key integrity error" }
            return false
        }
        // backup current keys
        val prevTmsKey = Key.tmsKey
        val prevPhotoKey = Key.photoKey
        val prevPwdKey = Key.pwdKey
        val prevIdsnKey = Key.idsnKey
        val prevCardKey = Key.cardKey
        val prevFingerKey = Key.fingerKey

        // create new keys. convert data
        if (resetKeys()) {
            ///////////////////////////////////////////////////////////////////////////
            // 관리자 암호 업데이트
            ///////////////////////////////////////////////////////////////////////////
            logger.info { "update password" }
            val password = Settings.password.decrypt(prevPwdKey, "pw")
            logger.info { "관리자 암호(in plain text): $password" }     // Yade0924
            Settings.password = password.encrypt(Key.pwdKey, "pw")

            ///////////////////////////////////////////////////////////////////////////
            // offline DB 업데이트
            ///////////////////////////////////////////////////////////////////////////
            transactionWithLock {
                TimeSheet.all().forEachIndexed { i, record ->
                    logger.info { "update db [${i + 1}] $record" }
                    record.cardNumber?.let { cardNumber ->
                        transactionWithLock {
                            record.cardNumber = cardNumber.decrypt(prevCardKey, "card").encrypt(Key.cardKey, "card")
                        }
                    }
                    safeLet(record.fingerPrint, record.dob) { fingerPrint, dob ->
                        record.dob = dob.decrypt(prevIdsnKey, "isdn").encrypt(Key.idsnKey, "isdn")
                        record.fingerPrint = fingerPrint.decrypt(prevFingerKey, "finger").encrypt(Key.fingerKey, "finger")
                    }
                }
            }

            ///////////////////////////////////////////////////////////////////////////
            // 사진 업데이트. NOTE: 많은 시간 소요. 동작 불안
            ///////////////////////////////////////////////////////////////////////////
//            File("./pictures").walk().toList()
//                .filter { it.name.endsWith(".jpg", ignoreCase = true) }
//                .forEach { file ->
//                    logger.info { "update file ${file.path}" }
//                    try {
//                        file.decrypt(prevPhotoKey)?.let {
//                            file.writeBytes(encrypt(Key.photoKey, it))
//                        }
//                    } catch (e: Exception) {
//                        logger.error { e }
//                    }
//                }
            return true
        } else {
            logger.error { "cannot renew keys. key reset error" }
            return false
        }
    }
}
