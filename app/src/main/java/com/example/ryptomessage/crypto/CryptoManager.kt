package com.example.ryptomessage.crypto

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object CryptoManager {
    
    private const val RSA_ALGORITHM = "RSA"
    private const val RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    
    /**
     * Генерирует пару ключей (публичный и приватный)
     */
    fun generateKeyPair(): Pair<String, String> {
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val publicKeyBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        val privateKeyBase64 = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        
        return Pair(publicKeyBase64, privateKeyBase64)
    }
    
    /**
     * Шифрует сообщение ТОЛЬКО для конкретного контакта
     * Использует публичный ключ получателя
     * Общий секретный ключ генерируется на лету и никогда не хранится
     * Только владелец соответствующего приватного ключа сможет расшифровать
     */
    fun encryptMessage(message: String, recipientPublicKeyBase64: String): String {
        try {
            // Декодируем публичный ключ получателя
            val recipientKeyBytes = Base64.decode(recipientPublicKeyBase64, Base64.NO_WRAP)
            val recipientKeySpec = X509EncodedKeySpec(recipientKeyBytes)
            val recipientKeyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
            val recipientPublicKey = recipientKeyFactory.generatePublic(recipientKeySpec)
            
            // Создаем Cipher для шифрования
            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
            
            // Шифруем сообщение публичным ключом получателя
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка шифрования: ${e.message}", e)
        }
    }
    
    /**
     * Расшифровывает сообщение ТОЛЬКО если у получателя есть приватный ключ
     * Соответствующий публичному ключу, которым было зашифровано сообщение
     * Может расшифровать только тот, у кого есть правильный приватный ключ
     */
    fun decryptMessage(encryptedMessage: String, privateKeyBase64: String): String {
        try {
            // Декодируем приватный ключ
            val keyBytes = Base64.decode(privateKeyBase64, Base64.NO_WRAP)
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
            val privateKey = keyFactory.generatePrivate(keySpec)
            
            // Создаем Cipher для расшифровки
            val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            
            // Расшифровываем сообщение
            val encryptedBytes = Base64.decode(encryptedMessage, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка расшифровки: ${e.message}", e)
        }
    }
}
