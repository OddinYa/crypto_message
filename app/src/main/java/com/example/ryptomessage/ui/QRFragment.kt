package com.example.ryptomessage.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ryptomessage.R
import com.example.ryptomessage.crypto.CryptoManager
import com.example.ryptomessage.data.PreferencesManager
import com.example.ryptomessage.databinding.FragmentQrBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class QRFragment : Fragment() {

    private var _binding: FragmentQrBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsManager = PreferencesManager(requireContext())
        
        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        binding.btnSaveNickname.setOnClickListener {
            saveNickname()
        }
        
        binding.btnCopyPublicKey.setOnClickListener {
            copyPublicKeyToClipboard()
        }
    }

    private fun loadUserData() {
        // Загружаем никнейм
        val nickname = prefsManager.nickname
        binding.etNickname.setText(nickname ?: "")
        
        // Проверяем, есть ли уже ключи
        var publicKey = prefsManager.publicKey
        var privateKey = prefsManager.privateKey
        
        if (publicKey.isNullOrBlank() || privateKey.isNullOrBlank()) {
            // Генерируем новую пару ключей
            val keyPair = CryptoManager.generateKeyPair()
            publicKey = keyPair.first
            privateKey = keyPair.second
            
            prefsManager.publicKey = publicKey
            prefsManager.privateKey = privateKey
            
            // Генерируем seed для пользователя
            if (prefsManager.seed.isNullOrBlank()) {
                prefsManager.seed = generateSeed()
            }
        }
        
        // Отображаем публичный ключ
        binding.tvPublicKey.text = "Публичный ключ:\n${publicKey.take(50)}..."
        
        // Генерируем QR код
        generateQRCode(publicKey)
    }

    private fun saveNickname() {
        val nickname = binding.etNickname.text.toString().trim()
        if (nickname.isEmpty()) {
            Toast.makeText(requireContext(), "Введите никнейм", Toast.LENGTH_SHORT).show()
            return
        }
        
        prefsManager.nickname = nickname
        Toast.makeText(requireContext(), "Никнейм сохранен", Toast.LENGTH_SHORT).show()
        
        // Перегенерируем QR с новым никнеймом
        prefsManager.publicKey?.let { generateQRCode(it) }
    }

    private fun generateQRCode(data: String) {
        try {
            val nickname = prefsManager.nickname ?: "User"
            val qrData = "$nickname|$data"
            
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                qrData,
                BarcodeFormat.QR_CODE,
                500,
                500
            )
            
            val bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565)
            for (x in 0 until 500) {
                for (y in 0 until 500) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            
            binding.ivQRCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка генерации QR: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyPublicKeyToClipboard() {
        val publicKey = prefsManager.publicKey
        if (publicKey.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Нет публичного ключа", Toast.LENGTH_SHORT).show()
            return
        }
        
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Public Key", publicKey)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(requireContext(), "Публичный ключ скопирован", Toast.LENGTH_SHORT).show()
    }

    private fun generateSeed(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { chars.random() }
            .joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
