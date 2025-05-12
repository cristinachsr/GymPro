package edu.pmdm.gympro;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private final Runnable onItemSelected;

    public SimpleItemSelectedListener(Runnable onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onItemSelected.run();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
