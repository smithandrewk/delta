package com.example.delta.presentation

import  android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import com.example.delta.util.BatteryHandler
import com.example.delta.util.FileHandler
import com.example.delta.util.SensorHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.toByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainActivity : ComponentActivity() {
    private lateinit var mSensorHandler: SensorHandler
    private lateinit var mFileHandler: FileHandler
    private lateinit var mBatteryHandler: BatteryHandler
    private lateinit var filesDir: File
    private lateinit var mMainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d("0000","onCreate")

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTheme(android.R.style.Theme_DeviceDefault)

//        filesDir = getExternalFilesDir(null)!!
        filesDir = getFilesDir()
        mMainViewModel = MainViewModel(filesDir)

        mFileHandler = FileHandler(filesDir)
        mSensorHandler = SensorHandler(mFileHandler,getSystemService(SENSOR_SERVICE) as SensorManager)
        mBatteryHandler = BatteryHandler(::registerReceiver,::unregisterReceiver, mFileHandler, mMainViewModel::updateBatteryLevel, mMainViewModel::setIsCharging)

        setContent {
            WearApp(mMainViewModel)
        }
    }
    override fun onPause() {
        super.onPause()
        Log.d("0000","onPause")
        runBlocking {
            launch {
                mMainViewModel.sendRequest("onPause")
            }
        }
    }
    override fun onResume() {
        super.onResume()
        Log.d("0000","onResume")
        runBlocking {
            launch {
                mMainViewModel.sendRequest("onResume")
            }
        }
    }
    override fun onStop() {
        super.onStop()
        Log.d("0000","onStop")
        runBlocking {
            launch {
                mMainViewModel.sendRequest("onStop")
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("0000","onStart")
        runBlocking {
            launch {
                mMainViewModel.sendRequest("onStart")
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("0000","onDestroy")
        runBlocking {
            launch {
                mMainViewModel.sendRequest("onDestroy")
            }
        }
        mSensorHandler.unregisterAll()
        mFileHandler.closeFiles()
        mBatteryHandler.unregister()
        listDirectories(filesDir)
    }
    private fun listDirectories(root: File) {
        val fileSizes = mutableMapOf<String, Long>()
        val files = root.listFiles()
        files?.filter { it.isDirectory }?.forEach { directory ->
            fileSizes[directory.name] = getDirectorySize(directory)
        }
//        zipDirectory("${filesDir.absolutePath}","${filesDir.absolutePath}/data.zip")
        runBlocking {
            launch {
                mMainViewModel.sendFiles(fileSizes)
            }
        }
    }
//            GlobalScope.launch(Dispatchers.IO) {
//                zipDirectory("${filesDir.absolutePath}/${directory.name}","${filesDir.absolutePath}/${directory.name}.zip")
//            }

    private fun getDirectorySize(directory: File): Long {
        var size: Long = 0
        val files = directory.listFiles()
        files?.forEach { file ->
            size += if (file.isDirectory) getDirectorySize(file) else file.length()
        }
        return size
    }
    fun zipDirectory(directoryPath: String, zipPath: String) {
        Log.d("0000","zipping $directoryPath")
        val sourceFile = File(directoryPath)
        FileOutputStream(zipPath).use { fos ->
            ZipOutputStream(fos).use { zos ->
                zipFile(sourceFile, sourceFile.name, zos)
            }
        }
    }
    fun zipFile(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isHidden) {
            return
        }
        if (fileToZip.isDirectory) {
            if (fileName.endsWith("/").not()) {
                zipOut.putNextEntry(ZipEntry("$fileName/"))
                zipOut.closeEntry()
            }
            fileToZip.listFiles()?.forEach { childFile ->
                zipFile(childFile, fileName + "/" + childFile.name, zipOut)
            }
            return
        }
        FileInputStream(fileToZip).use { fis ->
            BufferedInputStream(fis).use { bis ->
                val zipEntry = ZipEntry(fileName)
                zipOut.putNextEntry(zipEntry)
                bis.copyTo(zipOut, 1024)
                zipOut.closeEntry()
            }
        }
    }
}

@Serializable
data class Message(val msg: String)
@Serializable
data class FileList(val files: MutableMap<String, Long>)
class MainViewModel(private val filesDir: File): ViewModel() {
    var currentBatteryLevel by mutableStateOf(0f)
    private var isCharging by mutableStateOf(0)

    fun updateBatteryLevel(newLevel: Float) {
        currentBatteryLevel = newLevel
    }
    fun setIsCharging(newState: Int) {
        println("setIsCharging")
        println(newState)
        // if newState == 1 and isCharging == 0, wait a minute, then start uploading data

        if (newState == 1 && isCharging == 0) {
            isCharging = newState
            Log.d("0000","making request")
            runBlocking {
                launch {
//                    uploadFile()
                }
            }
        } else if (isCharging == 1) {
            // temporary

        }
//        else if (newState == 0) {
//            isCharging = newState
//        }
    }
    suspend fun sendRequest(msg: String) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val response: HttpResponse = client.post("http://192.168.1.115:8000/data") {
            contentType(ContentType.Application.Json)
            setBody(Message(msg))
        }
        println(response.status)
        client.close()
    }
    suspend fun sendFiles(files: MutableMap<String, Long>) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val response: HttpResponse = client.post("http://192.168.1.115:8000/data") {
            contentType(ContentType.Application.Json)
            setBody(FileList(files))
        }
        println(response.status)
        client.close()
    }
    suspend fun uploadFile(filename: String) {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "http://192.168.1.115:8000/file",
            formData = formData {
                append("description", "Data")
                append("filename", File("${filesDir.absolutePath}/${filename}.zip").readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "application/zip")
                    append(HttpHeaders.ContentDisposition, "filename=\"${filename}.zip\"")
                })
            }
        )
        println(response.status)
        client.close()
    }
    suspend fun sendLargeFile(client: HttpClient, url: String, filePath: String): HttpResponse {
        val file = File(filePath)

        return client.post(url) {
            // Set content type if necessary, e.g., application/octet-stream
            contentType(ContentType.Application.OctetStream)
            setBody(object : OutgoingContent.WriteChannelContent() {
                override suspend fun writeTo(channel: ByteWriteChannel) {
                    file.inputStream().use { inputStream ->
                        inputStream.toByteReadChannel().copyTo(channel)
                    }
                }
            })
        }
    }

}