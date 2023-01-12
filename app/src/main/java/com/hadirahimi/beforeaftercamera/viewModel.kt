package com.hadirahimi.beforeaftercamera

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

class viewModel :ViewModel()
{
    val liveImageAddress = MutableLiveData<Uri>()
    val liveOpacity = MutableLiveData<Float>()
    
    suspend fun saveImage(uri:Uri)
    {
        liveImageAddress.postValue(uri)
    }
    
    suspend fun saveOpacity(opacity:Float)
    {
        liveOpacity.postValue(opacity)
    }
}