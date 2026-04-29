package com.example.ryptomessage.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "crypto_message_prefs"
        private const val KEY_NICKNAME = "user_nickname"
        private const val KEY_PUBLIC_KEY = "user_public_key"
        private const val KEY_PRIVATE_KEY = "user_private_key"
        private const val KEY_CONTACTS = "contacts_list"
        private const val KEY_SEED = "user_seed"
    }
    
    var nickname: String?
        get() = prefs.getString(KEY_NICKNAME, null)
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()
    
    var publicKey: String?
        get() = prefs.getString(KEY_PUBLIC_KEY, null)
        set(value) = prefs.edit().putString(KEY_PUBLIC_KEY, value).apply()
    
    var privateKey: String?
        get() = prefs.getString(KEY_PRIVATE_KEY, null)
        set(value) = prefs.edit().putString(KEY_PRIVATE_KEY, value).apply()
    
    var seed: String?
        get() = prefs.getString(KEY_SEED, null)
        set(value) = prefs.edit().putString(KEY_SEED, value).apply()
    
    fun getContacts(): List<Contact> {
        val json = prefs.getString(KEY_CONTACTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Contact>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveContacts(contacts: List<Contact>) {
        val json = gson.toJson(contacts)
        prefs.edit().putString(KEY_CONTACTS, json).apply()
    }
    
    fun addContact(contact: Contact) {
        val contacts = getContacts().toMutableList()
        // Удаляем контакт с таким же id если он есть
        contacts.removeAll { it.id == contact.id }
        contacts.add(contact)
        saveContacts(contacts)
    }
    
    fun deleteContact(contactId: String) {
        val contacts = getContacts().toMutableList()
        contacts.removeAll { it.id == contactId }
        saveContacts(contacts)
    }
    
    fun isInitialized(): Boolean {
        return !publicKey.isNullOrBlank() && !privateKey.isNullOrBlank()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
