package edu.pmdm.gympro.ui.grupos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GruposViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GruposViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}