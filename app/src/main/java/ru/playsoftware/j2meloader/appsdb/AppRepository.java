package ru.playsoftware.j2meloader.appsdb;

import static ru.playsoftware.j2meloader.util.Constants.PREF_EMULATOR_DIR;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import ru.playsoftware.j2meloader.applist.AppListModel;
import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.util.AppUtils;

public class AppRepository implements SharedPreferences.OnSharedPreferenceChangeListener {

	private final Context context;
	private final MutableLiveData<List<AppItem>> listLiveData = new MutableLiveData<>();
	private final MutableLiveData<Throwable> errorsLiveData = new MutableLiveData<>();
	private final CompositeDisposable compositeDisposable = new CompositeDisposable();
	private final ErrorObserver errorObserver = new ErrorObserver(errorsLiveData);

	private AppDatabase db;
	private AppItemDao appItemDao;

	public AppRepository(AppListModel model) {
		if (model.getAppRepository() != null) {
			throw new IllegalStateException("You must get instance from 'AppListModel'");
		}
		this.context = model.getApplication();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		preferences.registerOnSharedPreferenceChangeListener(this);
		String emulatorDir = Config.getEmulatorDir();
		File dir = new File(emulatorDir);
		if (dir.isDirectory() && dir.canWrite()) {
			initDb(emulatorDir);
		}
	}

	public void initDb(String path) {
		db = AppDatabase.open(context, path);
		appItemDao = db.appItemDao();
		ConnectableFlowable<List<AppItem>> listConnectableFlowable = getAll()
				.subscribeOn(Schedulers.io())
				.publish();
		compositeDisposable.add(listConnectableFlowable
				.firstElement()
				.subscribe(list -> AppUtils.updateDb(this, new ArrayList<>(list)), errorsLiveData::postValue));
		compositeDisposable.add(listConnectableFlowable.subscribe(listLiveData::postValue, errorsLiveData::postValue));
		compositeDisposable.add(listConnectableFlowable.connect());
	}

	public void observeApps(LifecycleOwner owner, Observer<List<AppItem>> observer) {
		listLiveData.observe(owner, observer);
	}

	public Flowable<List<AppItem>> getAll() {
		return appItemDao.getAll();
	}

	public void insert(AppItem item) {
		Completable.fromAction(() -> appItemDao.insert(item))
				.subscribeOn(Schedulers.io())
				.subscribe(errorObserver);
	}

	public void insert(List<AppItem> items) {
		Completable.fromAction(() -> appItemDao.insert(items))
				.subscribeOn(Schedulers.io())
				.subscribe();
	}

	public void update(AppItem item) {
		Completable.fromAction(() -> appItemDao.update(item))
				.subscribeOn(Schedulers.io())
				.subscribe(errorObserver);
	}

	public void delete(AppItem item) {
		Completable.fromAction(() -> appItemDao.delete(item))
				.subscribeOn(Schedulers.io())
				.subscribe(errorObserver);
	}

	public void delete(List<AppItem> items) {
		Completable.fromAction(() -> appItemDao.delete(items))
				.subscribeOn(Schedulers.io())
				.subscribe(errorObserver);
	}

	public void deleteAll() {
		Completable.fromAction(appItemDao::deleteAll)
				.subscribeOn(Schedulers.io())
				.subscribe(errorObserver);
	}

	public AppItem get(String name, String vendor) {
		return appItemDao.get(name, vendor);
	}

	public AppItem get(int id) {
		return appItemDao.get(id);
	}

	public void close() {
		if (db != null) {
			db.close();
		}
		compositeDisposable.clear();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		if (PREF_EMULATOR_DIR.equals(key)) {
			String newPath = sp.getString(key, null);
			if (db != null) {
				String databaseName = db.getOpenHelper().getDatabaseName();
				if (databaseName != null) {
					String dbDir = new File(databaseName).getParent();
					if (dbDir != null) {
						if (dbDir.equals(newPath)) {
							return;
						}
					}
				}
				db.close();
				compositeDisposable.clear();
			}
			initDb(newPath);
		}
	}

	public void observeErrors(LifecycleOwner owner, Observer<Throwable> observer) {
		errorsLiveData.observe(owner, observer);
	}

	public void onWorkDirReady() {
		if (db == null) {
			initDb(Config.getEmulatorDir());
		}
	}

	private static class ErrorObserver implements CompletableObserver {
		private final MutableLiveData<Throwable> callback;

		public ErrorObserver(MutableLiveData<Throwable> callback) {
			this.callback = callback;
		}

		@Override
		public void onSubscribe(@NotNull Disposable d) {
		}

		@Override
		public void onComplete() {
		}

		@Override
		public void onError(@NotNull Throwable e) {
			callback.postValue(e);
		}
	}
}
