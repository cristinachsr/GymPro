package edu.pmdm.gympro.ui.monitores;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MonitoresViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MonitoresViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}