package edu.pmdm.gympro.ui.analisis;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnalisisViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AnalisisViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}