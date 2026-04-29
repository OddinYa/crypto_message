package com.example.ryptomessage.crypto

import android.util.Base64
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object CryptoManager {
    
    private const val ALGORITHM = "RSA"
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    
    /**
     * Генерирует пару ключей (публичный и приватный)
     */
    fun generateKeyPair(): Pair<String, String> {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val publicKeyBase64 = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        val privateKeyBase64 = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        
        return Pair(publicKeyBase64, privateKeyBase64)
    }
    
    /**
     * Декодирует приватный ключ из публичного ключа и сид-фразы
     * В реальном приложении здесь должна быть более сложная логика
     */
    fun derivePrivateKeyFromPublicKeyAndSeed(publicKey: String, seed: String): String {
        // Это упрощенная реализация - в реальности нужно использовать proper key derivation
        // Для демонстрации используем хеш от комбинации публичного ключа и сида
        val combined = publicKey + seed
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash = sha256.digest(combined.toByteArray())
        
        // Создаем детерминированный приватный ключ на основе хеша
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        val keySpec = PKCS8EncodedKeySpec(hash)
        
        // Возвращаем хеш как строку для простоты
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
    
    /**
     * Шифрует сообщение с использованием публичного ключа получателя
     */
    fun encryptMessage(message: String, publicKeyBase64: String): String {
        try {
            val keyBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            val publicKey = keyFactory.generatePublic(keySpec)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка шифрования: ${e.message}", e)
        }
    }
    
    /**
     * Расшифровывает сообщение с использованием приватного ключа
     */
    fun decryptMessage(encryptedMessage: String, privateKeyBase64: String): String {
        try {
            val keyBytes = Base64.decode(privateKeyBase64, Base64.NO_WRAP)
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            val privateKey = keyFactory.generatePrivate(keySpec)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            
            val encryptedBytes = Base64.decode(encryptedMessage, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Ошибка расшифровки: ${e.message}", e)
        }
    }
}
