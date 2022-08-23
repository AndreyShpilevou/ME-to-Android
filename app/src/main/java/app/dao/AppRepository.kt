package app.dao

import android.content.Context
import app.dao.AppDatabase.Companion.open
import app.appsList.AppListModel
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import app.utils.AppUtils
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Flowable
import io.reactivex.Completable
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import app.profile.Config
import app.utils.Constants
import java.io.File
import java.util.ArrayList

class AppRepository(model: AppListModel) : OnSharedPreferenceChangeListener {

    private val context: Context
    private val listLiveData = MutableLiveData<List<AppItem>>()
    private val errorsLiveData = MutableLiveData<Throwable>()
    private val compositeDisposable = CompositeDisposable()
    private val errorObserver = ErrorObserver(errorsLiveData)
    private var db: AppDatabase? = null
    private var appItemDao: AppItemDao? = null

    fun initDb(path: String?) {

        db = open(context, path!!)
        appItemDao = db!!.appItemDao()
        val listConnectableFlowable = getAll()
            .subscribeOn(Schedulers.io())
            .publish()
        compositeDisposable.add(listConnectableFlowable
            .firstElement()
            .subscribe({ list: List<AppItem?>? ->
                AppUtils.updateDb(
                    this,
                    ArrayList(list)
                )
            }) { value: Throwable -> errorsLiveData.postValue(value) })
        compositeDisposable.add(listConnectableFlowable.subscribe({ value: List<AppItem>? ->
            listLiveData.postValue(
                value
            )
        }) { value: Throwable -> errorsLiveData.postValue(value) })
        compositeDisposable.add(listConnectableFlowable.connect())
    }

    fun observeApps(owner: LifecycleOwner?, observer: (List<AppItem>) -> Unit) {
        listLiveData.observe(owner!!, observer!!)
    }

    fun getAll(): Flowable<List<AppItem>> = appItemDao!!.getAll()

    fun insert(item: AppItem?) {
        Completable.fromAction { appItemDao!!.insert(item!!) }
            .subscribeOn(Schedulers.io())
            .subscribe(errorObserver)
    }

    fun insert(items: List<AppItem>) {
        Completable.fromAction { appItemDao!!.insert(items) }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun update(item: AppItem?) {
        Completable.fromAction { appItemDao!!.update(item!!) }
            .subscribeOn(Schedulers.io())
            .subscribe(errorObserver)
    }

    fun delete(item: AppItem?) {
        Completable.fromAction { appItemDao!!.delete(item!!) }
            .subscribeOn(Schedulers.io())
            .subscribe(errorObserver)
    }

    fun delete(items: List<AppItem>) {
        Completable.fromAction { appItemDao!!.delete(items) }
            .subscribeOn(Schedulers.io())
            .subscribe(errorObserver)
    }

    fun deleteAll() {
        Completable.fromAction { appItemDao!!.deleteAll() }
            .subscribeOn(Schedulers.io())
            .subscribe(errorObserver)
    }

    operator fun get(name: String, vendor: String): AppItem? {
        return appItemDao!![name, vendor]
    }

    operator fun get(id: Int): AppItem? {
        return appItemDao!![id]
    }

    fun close() {
        if (db != null) {
            db!!.close()
        }
        compositeDisposable.clear()
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        if (Constants.PREF_EMULATOR_DIR == key) {
            val newPath = sp.getString(key, null)
            if (db != null) {
                val databaseName = db!!.openHelper.databaseName
                if (databaseName != null) {
                    val dbDir = File(databaseName).parent
                    if (dbDir != null) {
                        if (dbDir == newPath) {
                            return
                        }
                    }
                }
                db!!.close()
                compositeDisposable.clear()
            }
            initDb(newPath)
        }
    }

    fun observeErrors(owner: LifecycleOwner?, observer: Observer<Throwable>?) {
        errorsLiveData.observe(owner!!, observer!!)
    }

    fun onWorkDirReady() {
        if (db == null) {
            initDb(Config.emulatorDir)
        }
    }

    private class ErrorObserver(private val callback: MutableLiveData<Throwable>) :
        CompletableObserver {
        override fun onSubscribe(d: Disposable) {}
        override fun onComplete() {}
        override fun onError(e: Throwable) {
            callback.postValue(e)
        }
    }

    init {
        check(model.appRepository == null) { "You must get instance from 'AppListModel'" }
        context = model.getApplication()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this)
        val emulatorDir = Config.emulatorDir
        val dir = File(emulatorDir)
        if (dir.isDirectory && dir.canWrite()) {
            initDb(emulatorDir)
        }
    }
}