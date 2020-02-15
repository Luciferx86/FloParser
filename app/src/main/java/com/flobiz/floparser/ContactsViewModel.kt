package com.flobiz.floparser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.flobiz.floparser.workers.ApiContactsWorker
import com.flobiz.floparser.workers.CsvContactsWorker

/**
 * @created-by: Akshay Arora
 * @date: 2020-02-08
 * @description: ViewModel class
 */

class ContactsViewModel(application: Application) : AndroidViewModel(application) {


    private val workManager: WorkManager = WorkManager.getInstance(application)
    private var csvWorkInfo: LiveData<List<WorkInfo>>
    private var apiWorkInfo: LiveData<List<WorkInfo>>


    /**
     * returns Work Info(CSV)
     */

    fun csvWorkObserver() : LiveData<List<WorkInfo>> = csvWorkInfo

    /**
     * returns Work Info(API)
     */

    fun apiWorkObserver() : LiveData<List<WorkInfo>>{
        return apiWorkInfo
    }

    init {
        //using tags to listen to work status
        csvWorkInfo = workManager.getWorkInfosByTagLiveData("SavingCsvContacts")
        apiWorkInfo = workManager.getWorkInfosByTagLiveData("SavingApiContacts")

    }

    /**
     * function to start the background work(from CSV)
     * @param url: url to pass to the readCSV function
     */

    fun startSavingFromUrl(url: String)  {

        val builder = Data.Builder()
        builder.putString("url", url)
        val saveRequest = OneTimeWorkRequestBuilder<CsvContactsWorker>()
            .setInputData(builder.build())
            .addTag("SavingCsvContacts")
            .build()

        workManager.enqueue(saveRequest)

    }

    /**
     * function to start the background work(from API)
     */
    fun startSavingFromApi() {

        val builder = Data.Builder()
        val saveRequest = OneTimeWorkRequestBuilder<ApiContactsWorker>()
            .setInputData(builder.build())
            .addTag("SavingApiContacts")
            .build()

        workManager.enqueue(saveRequest)
    }


}
