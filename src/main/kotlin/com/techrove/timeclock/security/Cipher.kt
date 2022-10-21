package com.techrove.timeclock.security

import com.techrove.timeclock.utils.toHexString
import mu.KotlinLogging
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val AES_KEY_BIT = 256
private const val IV_LENGTH_BYTE = 12
private const val TAG_LENGTH_BIT = 128
private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"

// Deterministic Random Bit Generator (SP 800-90A)
private const val RANDOM_ALGORITHM = "DRBG"
private const val KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256"
private const val KEY_ALGORITHM = "AES"

private val logger = KotlinLogging.logger("Cipher")

object Cipher {
    private val cipher: Cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    private val secureRandom: SecureRandom = SecureRandom.getInstance(RANDOM_ALGORITHM)
    //private val factory: SecretKeyFactory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)

    private fun getSecureRand(length: Int): ByteArray {
        val rnd = ByteArray(length)
        secureRandom.nextBytes(rnd)
        return rnd
    }

    @Throws(NoSuchAlgorithmException::class)
    fun newKey(): ByteArray = KeyGenerator.getInstance(KEY_ALGORITHM).apply {
        init(AES_KEY_BIT, secureRandom) // SecureRandom.getInstanceStrong())
    }.generateKey().encoded

    fun newMasterKey(): ByteArray {
        val newKey = newKey()
        val randomData = getSecureRand(2048 - AES_KEY_BIT / 8)
        for (i in (0 until AES_KEY_BIT / 8)) {
            randomData[i * 64 + i] = newKey[i]
        }
        return randomData
        //return newKey() + getSecureRand(2048 - AES_KEY_BIT/8)
    }

    fun parseMasterKey(key: ByteArray): ByteArray {
        val parsedKey = ByteArray(AES_KEY_BIT / 8)
//        logger.info("Master Key: ${key.toHexString()}")     // Yade1020
        for (i in (0 until AES_KEY_BIT / 8)) {
            parsedKey[i] = key[i * 64 + i]
        }
//        logger.info("Parsed Key: ${parsedKey.toHexString()}")     // Yade1020
        return parsedKey
        //return key.copyOfRange(0, AES_KEY_BIT/8)
    }

    fun encrypt(key: ByteArray, input: ByteArray): ByteArray {
        val iv = getSecureRand(IV_LENGTH_BYTE)
        val gcm = GCMParameterSpec(TAG_LENGTH_BIT, iv)  // Yade0919
        logger.trace { "ENC key:${key.toHexString()} iv:${iv.toHexString()} " +
                "gcm: ${gcm.toString()}" } // Yade0919
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(key, KEY_ALGORITHM),
            GCMParameterSpec(TAG_LENGTH_BIT, iv)
        )
        return iv + cipher.doFinal(input)
    }

    fun decrypt(key: ByteArray, input: ByteArray): ByteArray? {
        return try {
            val iv = input.copyOfRange(0, IV_LENGTH_BYTE)
            val gcm = GCMParameterSpec(TAG_LENGTH_BIT, iv)  // Yade01018
            logger.trace { "DEC key:${key.toHexString()} iv:${iv.toHexString()}" +
                    "gcm: ${gcm.toString()}" } // Yade01018
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, KEY_ALGORITHM),
                GCMParameterSpec(TAG_LENGTH_BIT, iv)
            )
            cipher.doFinal(input.copyOfRange(IV_LENGTH_BYTE, input.size))
        } catch (e: Exception) {
            null
        }
    }

//    @Throws(GeneralSecurityException::class)
//    private fun getDerivedKey(pwd: CharArray, salt: ByteArray): SecretKey {
//        val key = factory.generateSecret(PBEKeySpec(pwd, salt, 100000, AES_KEY_BIT))
//        return SecretKeySpec(key.encoded, "AES")
//    }
//
//    fun encrypt(password: CharArray, input: ByteArray): CryptoData {
//        val salt = getSecureRand(256)
//        val iv = getSecureRand(IV_LENGTH_BYTE)
//        cipher.init(
//            Cipher.ENCRYPT_MODE,
//            getDerivedKey(password, salt),
//            GCMParameterSpec(TAG_LENGTH_BIT, iv)
//        )
//        return CryptoData(cipher.doFinal(input), iv, salt)
//    }
//
//    fun decrypt(password: CharArray, cryptoData: CryptoData): ByteArray {
//        cipher.init(
//            Cipher.DECRYPT_MODE,
//            getDerivedKey(password, cryptoData.salt),
//            GCMParameterSpec(TAG_LENGTH_BIT, cryptoData.iv)
//        )
//        return cipher.doFinal(cryptoData.data)
//    }
}

