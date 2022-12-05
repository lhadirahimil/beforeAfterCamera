package com.hadirahimi.beforeaftercamera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.internal.annotation.CameraExecutor
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.hadirahimi.beforeaftercamera.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityMainBinding
    private lateinit var cameraExecutor : ExecutorService
    //activity result
    lateinit var galleryResult : ActivityResultLauncher<Intent>
    private lateinit var processCameraProvider : ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture : ImageCapture
    
    @SuppressLint("SdCardPath")
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(validatePermissions())
            startCamera()
        else
            Toast.makeText(this@MainActivity , "مجوز های دسترسی را تایید کنید"  , Toast.LENGTH_SHORT).show()
        
        cameraExecutor = Executors.newSingleThreadExecutor()
      
    
        //  receive image from gallery
        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Handle the Intent
                binding.prevPicture.load(intent?.data)
            }
        }
        
        //init views
        binding.apply {
            seekbarOpacity.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0 : SeekBar? , p1 : Int , p2 : Boolean)
                {
                
                }
    
                override fun onStartTrackingTouch(p0 : SeekBar?)
                {
                
                }
    
                override fun onStopTrackingTouch(seekbar : SeekBar?)
                {
                    val alpha = seekbar?.progress?.toDouble()?.div(100)
                    if (alpha != null)
                    {
                        prevPicture.alpha = alpha.toFloat()
                    }
                }
    
            })
    
    
            
            
            //click
            changePhoto.setOnClickListener {
                if (validatePermissions())
                {
                    pickFromGallery()
                }
            }
            setting.setOnClickListener {
            
            }
            //take picture
            takePicture.setOnClickListener {
                
                val timeStamp = System.currentTimeMillis()
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,timeStamp)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
                
                
                imageCapture.takePicture(ImageCapture.OutputFileOptions.Builder(contentResolver,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues).build(),ContextCompat.getMainExecutor(this@MainActivity),object : ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults : ImageCapture.OutputFileResults)
                    {
                        Toast.makeText(this@MainActivity , "تصویر با موفقیت گرفته شد" , Toast.LENGTH_SHORT).show()
                    }
    
                    override fun onError(exception : ImageCaptureException)
                    {
                        Toast.makeText(this@MainActivity ,"عملیات با خطا روبرو شد"+exception.message.toString()  , Toast.LENGTH_SHORT).show()
                        Log.e("HECTOR",exception.message.toString())
                    }
    
                })
                
                showDialogResult(timeStamp)
                
            }
        }
    }
    
    private fun showDialogResult(timeStamp : Long)
    {
        
    }
    
    private fun pickFromGallery()
    {
        val  intent = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResult.launch(intent)
    }
    
  
    
  
    private fun validatePermissions() : Boolean
    {
        var granted = false
        if (ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        granted  = true
        else
        {
            granted = false
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA) ,100)
        }
        return granted
    }
    
    
    private fun startCamera()
    {
        processCameraProvider = ProcessCameraProvider.getInstance(this)
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
        processCameraProvider.addListener({
            try
            {
                val cameraProvider : ProcessCameraProvider = processCameraProvider.get()
                val previewUseCase = buildPreviewUseCase()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,    CameraSelector.DEFAULT_BACK_CAMERA,previewUseCase,imageCapture)
            }catch (e:Exception)
            {
                Toast.makeText(this@MainActivity ,"خطا در راه اندازی دوربین "  , Toast.LENGTH_SHORT).show()
            }
            
        },ContextCompat.getMainExecutor(this@MainActivity))
    }
    
    private fun buildPreviewUseCase() : Preview =
        Preview.Builder().build().also { it.setSurfaceProvider(binding.cameraPrev.surfaceProvider) }
    
    
    private fun openSettingForPermission()
    {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri : Uri = Uri.fromParts("package" , packageName , null)
        intent.data = uri
        startActivity(intent)
    }
    
    
}