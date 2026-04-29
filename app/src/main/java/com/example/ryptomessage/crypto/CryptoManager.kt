package com.example.ryptomessage.crypto

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

object CryptoManager {
    
    private const val EC_ALGORITHM = "EC"
    private const val KEY_AGREEMENT_ALGORITHM = "ECDH"
    private const val AES_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val AES_KEY_SIZE = 256
    
    /**
     * Генерирует пару ключей EC (публичный и приватный) для ECDH
     */
    fun generateKeyPair(): Pair<String, String> {
        val keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM, "BC")
        keyPairGenerator.initialize(256) // secp256r1 curve
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val publicKeyBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        val privateKeyBase64 = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        
        return Pair(publicKeyBase64, privateKeyBase64)
    }
    
    /**
     * Вычисляет общий секретный ключ usando ECDH
     * myPrivateKey - мой приватный ключ
     * theirPublicKey - публичный ключ собеседника
     */
    private fun deriveSharedSecret(myPrivateKey: String, theirPublicKey: String): ByteArray {
        // Декодируем ключи
        val myPrivateKeyBytes = Base64.decode(myPrivateKey, Base64.NO_WRAP)
        val theirPublicKeyBytes = Base64.decode(theirPublicKey, Base64.NO_WRAP)
        
        // Восстанавливаем ключи
        val keyFactory = KeyFactory.getInstance(EC_ALGORITHM, "BC")
        val privateKeySpec = PKCS8EncodedKeySpec(myPrivateKeyBytes)
        val publicKeySpec = X509EncodedKeySpec(theirPublicKeyBytes)
        
        val privateKey = keyFactory.generatePrivate(privateKeySpec)
        val publicKey = keyFactory.generatePublic(publicKeySpec)
        
        // Вычисляем общий секрет через ECDH
        val keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM, "BC")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        
        // Генерируем общий секрет
        val sharedSecret = keyAgreement.generateSecret()
        
        // Хэшируем секрет для получения ключа нужной длины
        val sha256 = MessageDigest.getInstance("SHA-256")
        return sha256.digest(sharedSecret)
    }
    
    /**
     * Шифрует сообщение для конкретного получателя
     * Использует ECDH для вычисления общего секрета и AES для шифрования
     * 
     * @param message Сообщение для шифрования
     * @param myPrivateKey Мой приватный ключ
     * @param theirPublicKey Публичный ключ получателя
     * @return Зашифрованное сообщение в формате: IV (16 байт) + зашифрованные данные, всё в Base64
     */
    fun encryptMessage(message: String, myPrivateKey: String, theirPublicKey: String): String {
        try {
            // Вычисляем общий секрет: мой приватный + их публичный
            val sharedSecret = deriveSharedSecret(myPrivateKey, theirPublicKey)
            
            // Создаем AES ключ из общего секрета
            val secretKey = SecretKeySpec(sharedSecret, "AES")
            
            // Генерируем случайный IV
            val iv = ByteArray(16)
            val secureRandom = SecureRandom()
            secureRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            // Шифруем сообщение
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            
            // Объединяем IV и зашифрованные данные
            val result = iv + encryptedBytes
            
            return Base64.encodeToString(result, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка шифрования: ${e.message}", e)
        }
    }
    
    /**
     * Расшифровывает сообщение от конкретного отправителя
     * Использует ECDH для вычисления общего секрета и AES для расшифровки
     * 
     * ВАЖНО: Для корректной расшифровки нужно использовать:
     * - myPrivateKey = приватный ключ ПОЛУЧАТЕЛЯ (того, кто расшифровывает)
     * - theirPublicKey = публичный ключ ОТПРАВИТЕЛЯ (того, кто зашифровал)
     * 
     * Общий секрет будет тем же самым, потому что:
     * - Отправитель вычислил: приватный_отправителя + публичный_получателя
     * - Получатель вычисляет: приватный_получателя + публичный_отправителя
     * 
     * @param encryptedMessage Зашифрованное сообщение (IV + данные в Base64)
     * @param myPrivateKey Приватный ключ ПОЛУЧАТЕЛЯ (для расшифровки)
     * @param theirPublicKey Публичный ключ ОТПРАВИТЕЛЯ (кто зашифровал)
     * @return Расшифрованное сообщение
     */
    fun decryptMessage(encryptedMessage: String, myPrivateKey: String, theirPublicKey: String): String {
        try {
            // Вычисляем общий секрет: мой приватный (получателя) + их публичный (отправителя)
            val sharedSecret = deriveSharedSecret(myPrivateKey, theirPublicKey)
            
            // Создаем AES ключ из общего секрета
            val secretKey = SecretKeySpec(sharedSecret, "AES")
            
            // Декодируем сообщение
            val encryptedBytes = Base64.decode(encryptedMessage, Base64.NO_WRAP)
            
            // Извлекаем IV (первые 16 байт)
            val iv = encryptedBytes.sliceArray(0 until 16)
            val ivSpec = IvParameterSpec(iv)
            
            // Извлекаем зашифрованные данные
            val cipherText = encryptedBytes.sliceArray(16 until encryptedBytes.size)
            
            // Расшифровываем
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decryptedBytes = cipher.doFinal(cipherText)
            
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка расшифровки: ${e.message}", e)
        }
    }
}
