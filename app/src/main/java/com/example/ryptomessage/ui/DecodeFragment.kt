package com.example.ryptomessage.ui

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ryptomessage.crypto.CryptoManager
import com.example.ryptomessage.data.Contact
import com.example.ryptomessage.data.PreferencesManager
import com.example.ryptomessage.databinding.FragmentDecodeBinding

class DecodeFragment : Fragment() {

    private var _binding: FragmentDecodeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var contactsAdapter: ArrayAdapter<String>
    private var contactsList = listOf<Contact>()
    private var selectedContact: Contact? = null

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
        loadContacts()
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
        
        // Кнопка удаления контакта
        binding.btnDeleteContact.setOnClickListener {
            deleteSelectedContact()
        }
        
        // Обработчик выбора контакта
        binding.spinnerContacts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0 && position <= contactsList.size) {
                    selectedContact = contactsList[position - 1]
                    binding.btnDeleteContact.visibility = View.VISIBLE
                } else {
                    selectedContact = null
                    binding.btnDeleteContact.visibility = View.GONE
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedContact = null
                binding.btnDeleteContact.visibility = View.GONE
            }
        }
    }

    private fun loadContacts() {
        contactsList = prefsManager.getContacts()
        
        val contactNames = mutableListOf<String>()
        contactNames.add("Неизвестный отправитель")
        contactNames.addAll(contactsList.map { it.nickname })
        
        contactsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            contactNames
        )
        contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerContacts.adapter = contactsAdapter
        
        if (contactsList.isNotEmpty()) {
            selectedContact = contactsList.first()
        } else {
            selectedContact = null
        }
        
        // Скрываем кнопку удаления если нет контактов
        binding.btnDeleteContact.visibility = View.GONE
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

    private fun deleteSelectedContact() {
        if (selectedContact == null) {
            Toast.makeText(requireContext(), "Выберите контакт для удаления", Toast.LENGTH_SHORT).show()
            return
        }
        
        prefsManager.deleteContact(selectedContact!!.id)
        Toast.makeText(requireContext(), "Контакт \"${selectedContact!!.nickname}\" удален", Toast.LENGTH_SHORT).show()
        
        // Обновляем список контактов
        loadContacts()
        selectedContact = null
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
            
            // Проверяем, есть ли в сообщении публичный ключ отправителя
            // Формат: "senderPublicKey|encryptedMessage"
            val parts = encodedMessage.split("|", limit = 2)
            val actualEncryptedMessage: String
            val senderPublicKey: String?
            
            if (parts.size == 2 && parts[0].length > 100) {
                // Предполагаем, что это публичный ключ (он достаточно длинный)
                senderPublicKey = parts[0]
                actualEncryptedMessage = parts[1]
            } else {
                senderPublicKey = null
                actualEncryptedMessage = encodedMessage
            }
            
            // Расшифровываем сообщение ТОЛЬКО своим приватным ключом
            // Это работает ТОЛЬКО если сообщение было зашифровано НАШИМ публичным ключом
            // Контакт может прочитать сообщение ТОЛЬКО если у него есть наш публичный ключ
            val decryptedMessage = CryptoManager.decryptMessage(actualEncryptedMessage, privateKey)
            
            // Показываем результат
            binding.tvDecodedMessage.text = decryptedMessage
            binding.cardResult.visibility = View.VISIBLE
            
            // Обновляем информацию об отправителе
            if (!senderPublicKey.isNullOrBlank()) {
                // Пытаемся найти контакт по публичному ключу
                val contacts = prefsManager.getContacts()
                val senderContact = contacts.find { it.publicKey == senderPublicKey }
                
                if (senderContact != null) {
                    binding.tvSenderLabel.text = "Отправитель: ${senderContact.nickname} (из контактов)"
                } else {
                    binding.tvSenderLabel.text = "Отправитель: Неизвестный (нет в контактах)"
                }
            } else {
                binding.tvSenderLabel.text = "Отправитель: Неизвестный (ключ не передан)"
            }
            
            Toast.makeText(requireContext(), "Сообщение расшифровано!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            binding.cardResult.visibility = View.GONE
            Toast.makeText(requireContext(), "Ошибка расшифровки: ${e.message}\n\nУбедитесь, что сообщение было зашифровано ВАШИМ публичным ключом.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
