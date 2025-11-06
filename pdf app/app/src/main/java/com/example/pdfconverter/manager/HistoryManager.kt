package com.example.pdfconverter.manager

import android.content.Context
import androidx.core.content.edit
import com.example.pdfconverter.dataClasses.ConvertedFiles
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object FileHistoryManager {
    private const val PREFS_NAME = "converted_files_prefs"
    private const val KEY_HISTORY = "file_history"

    private val gson = Gson()
    private val listType = object : TypeToken<MutableList<ConvertedFiles>>() {}.type

    fun saveFile(context: Context, file: ConvertedFiles) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingJson = prefs.getString(KEY_HISTORY, "[]")
        val currentList: MutableList<ConvertedFiles> = gson.fromJson(existingJson, listType)

        currentList.add(0, file)
        prefs.edit { putString(KEY_HISTORY, gson.toJson(currentList)) }
    }

    fun getFiles(context: Context): List<ConvertedFiles> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingJson = prefs.getString(KEY_HISTORY, "[]")
        return gson.fromJson(existingJson, listType)
    }

    fun removeFile(context: Context, file: ConvertedFiles) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingJson = prefs.getString(KEY_HISTORY, "[]")
        val currentList: MutableList<ConvertedFiles> = gson.fromJson(existingJson, listType)

        currentList.removeAll { it.path == file.path }

        prefs.edit { putString(KEY_HISTORY, gson.toJson(currentList)) }

        val delFile = File(file.path)
        if (delFile.exists()) delFile.delete()

    }
}
