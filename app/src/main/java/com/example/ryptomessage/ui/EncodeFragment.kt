package com.example.ryptomessage.ui

import android.content.ClipData
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
import com.example.ryptomessage.databinding.FragmentEncodeBinding
import com.google.zxing.integration.android.IntentIntegrator

class EncodeFragment : Fragment() {

    private var _binding: FragmentEncodeBinding? = null
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
        _binding = FragmentEncodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsManager = PreferencesManager(requireContext())
        
        setupUI()
        loadContacts()
    }

    private fun setupUI() {
        // Настройка кнопки сканирования QR
        binding.btnScanQR.setOnClickListener {
            scanQR()
        }
        
        // Настройка кнопки кодирования
        binding.btnEncode.setOnClickListener {
            encodeMessage()
        }
        
        // Обработчик выбора контакта
        binding.spinnerContacts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < contactsList.size) {
                    selectedContact = contactsList[position]
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedContact = null
            }
        }
    }

    private fun loadContacts() {
        contactsList = prefsManager.getContacts()
        
        val contactNames = contactsList.map { it.nickname }.toMutableList()
        if (contactNames.isEmpty()) {
            contactNames.add("Нет контактов - отсканируйте QR")
        }
        
        contactsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            contactNames
        )
        contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerContacts.adapter = contactsAdapter
        
        if (contactsList.isNotEmpty()) {
            selectedContact = contactsList.first()
        }
    }

    private fun scanQR() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Сканировать QR код")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            val scannedContent = scanResult.contents
            if (scannedContent != null) {
                handleScannedQR(scannedContent)
            } else {
                Toast.makeText(requireContext(), "Сканирование отменено", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun encodeMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "Введите сообщение", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedContact == null) {
            Toast.makeText(requireContext(), "Выберите получателя", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Шифруем сообщение ТОЛЬКО публичным ключом получателя
            // Только получатель сможет расшифровать своим приватным ключом
            // Общий секретный ключ генерируется на лету и никогда не хранится
            val encryptedMessage = CryptoManager.encryptMessage(
                message,
                selectedContact!!.publicKey
            )
            
            // Копируем в буфер обмена
            copyToClipboard(encryptedMessage)
            
            // Показываем результат
            binding.tvResult.text = encryptedMessage
            binding.tvResult.visibility = View.VISIBLE
            
            Toast.makeText(requireContext(), "Сообщение зашифровано для ${selectedContact!!.nickname} и скопировано!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка шифрования: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Encrypted Message", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleScannedQR(qrContent: String) {
        try {
            // Формат QR: "nickname|publicKey"
            val parts = qrContent.split("|")
            if (parts.size < 2) {
                Toast.makeText(requireContext(), "Неверный формат QR кода. Ожидалось: никнейм|ключ", Toast.LENGTH_LONG).show()
                return
            }
            
            val nickname = parts[0].trim()
            val publicKey = parts[1].trim()
            
            if (nickname.isEmpty()) {
                Toast.makeText(requireContext(), "Пустой никнейм в QR коде", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (publicKey.isEmpty()) {
                Toast.makeText(requireContext(), "Пустой публичный ключ в QR коде", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Проверяем, не добавлен ли уже контакт с таким же ключом
            val existingContacts = prefsManager.getContacts()
            val existingContact = existingContacts.find { it.publicKey == publicKey }
            if (existingContact != null) {
                Toast.makeText(requireContext(), "Контакт \"$nickname\" уже добавлен!", Toast.LENGTH_LONG).show()
                return
            }
            
            // Создаем контакт
            val contact = Contact(
                id = System.currentTimeMillis().toString(),
                nickname = nickname,
                publicKey = publicKey
            )
            
            // Сохраняем контакт
            prefsManager.addContact(contact)
            
            // Обновляем список
            loadContacts()
            
            Toast.makeText(requireContext(), "Контакт \"$nickname\" добавлен!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при добавлении контакта: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
