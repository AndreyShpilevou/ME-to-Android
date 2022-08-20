package app.appsList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.dao.AppRepository

class AppListModel(application: Application) : AndroidViewModel(application) {

    val appRepository: AppRepository = AppRepository(this)

    override fun onCleared() {
        appRepository.close()
    }
}