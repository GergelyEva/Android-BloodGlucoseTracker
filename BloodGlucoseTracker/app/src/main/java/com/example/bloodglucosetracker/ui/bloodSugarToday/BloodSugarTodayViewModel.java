package com.example.bloodglucosetracker.ui.bloodSugarToday;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BloodSugarTodayViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BloodSugarTodayViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Blood Sugar Today fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}