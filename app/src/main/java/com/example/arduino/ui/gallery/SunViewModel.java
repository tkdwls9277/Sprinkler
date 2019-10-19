package com.example.arduino.ui.gallery;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SunViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SunViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is sun fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}