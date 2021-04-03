package com.example.photoweather.viewModel

import android.app.Application
import android.content.Context
import android.location.Location
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.photoweather.R
import com.example.photoweather.network.models.Resource
import com.example.photoweather.network.models.WeatherConditions
import com.example.photoweather.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application
    var selectedImageURL: MutableLiveData<String> = MutableLiveData()
    var lastLocation:MutableLiveData<Location> = MutableLiveData()
    val imageUrlLiveData: LiveData<String> = selectedImageURL
    var weatherConditions: WeatherConditions? = null
    var imagesList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    var imagesListLiveData: LiveData<ArrayList<String>> = imagesList
    var newImagePath:String? = null

    fun getAllImages(){
        CoroutineScope(Dispatchers.Default).launch {
            val storageDir =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    context.resources.getString(R.string.file_name)
                )
            if (storageDir.exists()) {
                val list = arrayListOf<String>()
                storageDir.listFiles()?.forEach {
                    if (it.inputStream().channel.size() > 0) {
                        list.add(it.absolutePath)
                    }
                }
                imagesList.postValue(list)
            }else imagesList.postValue(arrayListOf())
        }
    }
    fun getWeatherConditions() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            if (lastLocation.value == null){
                emit(Resource.success( data = BaseRepo.getWeatherConditions(AppUtils.DEFULT_LAT, AppUtils.DEFULT_LON)))
            }else{
                emit(Resource.success( data = BaseRepo.getWeatherConditions(lastLocation.value!!.latitude, lastLocation.value!!.longitude)))
            }
        } catch (ex: Exception) {
            emit(Resource.error( data = null, message = ex.message.toString()))
        }
    }

    fun deleteImage(path:String? = null) {
        if (path != null){

            val delete = File(path).delete()
            Log.i(TAG, "deleteImage: $delete")
        }
        else if (!newImagePath.isNullOrEmpty()){
            File(newImagePath!!).delete()
            newImagePath = ""
        }
    }

    companion object{
        private const val TAG = "BaseViewModel"
    }
}