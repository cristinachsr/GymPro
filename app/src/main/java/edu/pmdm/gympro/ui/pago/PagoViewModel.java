package edu.pmdm.gympro.ui.pago;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import edu.pmdm.gympro.model.Pago;

public class PagoViewModel extends ViewModel {
    private final MutableLiveData<List<Pago>> listaPagos = new MutableLiveData<>();

    public LiveData<List<Pago>> getListaPagos() {
        return listaPagos;
    }

    public void setListaPagos(List<Pago> pagos) {
        listaPagos.setValue(pagos);
    }
}
