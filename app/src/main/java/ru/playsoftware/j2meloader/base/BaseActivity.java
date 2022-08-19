package ru.playsoftware.j2meloader.base;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ru.playsoftware.j2meloader.R;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		if (getSupportActionBar() != null) {
			getSupportActionBar().setElevation(getResources().getDisplayMetrics().density * 2);
		}
		super.onCreate(savedInstanceState);
	}
}
