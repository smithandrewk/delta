package com.example.delta.util

import android.content.Context
import com.example.delta.submodules.util.FilesHandlerCore
import java.io.File

class FilesHandler(private val applicationContext: Context,
                   private val filesDir: File,
                   private val appStartTimeMillis: Long,
                   private val appStartTimeReadable: String) :
    FilesHandlerCore(applicationContext, filesDir, appStartTimeMillis, appStartTimeReadable) {

    // Override any functions from FilesHandlerCore to change functionality
}