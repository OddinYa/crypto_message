package com.example.ryptomessage.ui

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ryptomessage.crypto.CryptoManager
import com.example.ryptomessage.data.PreferencesManager
import com.example.ryptomessage.databinding.FragmentDecodeBinding

class DecodeFragment : Fragment() {

    private var _binding: FragmentDecodeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsManager = PreferencesManager(requireContext())
        
        setupUI()
    }

    private fun setupUI() {
        // Кнопка вставки из буфера
        binding.btnPaste.setOnClickListener {
            pasteFromClipboard()
        }
        
        // Кнопка декодирования
        binding.btnDecode.setOnClickListener {
            decodeMessage()
        }
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text.toString()
            binding.etEncodedMessage.setText(text)
            Toast.makeText(requireContext(), "Текст вставлен", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Буфер обмена пуст", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeMessage() {
        val encodedMessage = binding.etEncodedMessage.text.toString().trim()
        if (encodedMessage.isEmpty()) {
            Toast.makeText(requireContext(), "Введите зашифрованное сообщение", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Получаем приватный ключ пользователя
            val privateKey = prefsManager.privateKey
            if (privateKey.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Нет приватного ключа. Создайте профиль на вкладке 'Мой QR'", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Расшифровываем сообщение
            val decryptedMessage = CryptoManager.decryptMessage(encodedMessage, privateKey)
            
            // Показываем результат
            binding.tvDecodedMessage.text = decryptedMessage
            binding.cardResult.visibility = View.VISIBLE
            
            // Обновляем информацию об отправителе (если есть в сообщении)
            binding.tvSender.text = "Сообщение расшифровано успешно!"
            
            Toast.makeText(requireContext(), "Сообщение расшифровано!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            binding.cardResult.visibility = View.GONE
            Toast.makeText(requireContext(), "Ошибка расшифровки: ${e.message}\n\nУбедитесь, что сообщение было зашифровано вашим публичным ключом.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
