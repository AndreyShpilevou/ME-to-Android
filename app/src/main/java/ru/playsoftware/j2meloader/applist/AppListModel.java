package ru.playsoftware.j2meloader.applist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import app.dao.AppRepository;

public class AppListModel extends AndroidViewModel {
	private final AppRepository appRepository;

	public AppListModel(@NonNull Application application) {
		super(application);
		appRepository = new AppRepository(this);
	}

	public AppRepository getAppRepository() {
		return appRepository;
	}

	@Override
	protected void onCleared() {
		appRepository.close();
	}
}
