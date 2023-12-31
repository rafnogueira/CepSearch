package com.orion.cepsearch.ui.search;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.orion.cepsearch.R;
import com.orion.cepsearch.core.model.local.Cep;
import com.orion.cepsearch.core.model.local.CepResultItem;
import com.orion.cepsearch.core.repository.CepRepository;
import com.orion.cepsearch.core.utils.AppConstants;

import io.reactivex.disposables.Disposable;

public class SearchCepViewModel extends ViewModel {
    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Integer> toastMessageById;
    private MutableLiveData<String> cepSearchParams = null;
    private MutableLiveData<CepResultItem> results = null;
    private MutableLiveData<Boolean> showLoading = null;
    private CepRepository cepRepository = null;
    private Disposable disposable = null;

    public SearchCepViewModel() {
        cepSearchParams = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        toastMessageById = new MutableLiveData<>();
        results = new MutableLiveData<>();
        showLoading = new MutableLiveData<>();
    }

    public void injectCepRepositoryContext(Context mContext) {
        cepRepository = new CepRepository(mContext);
    }

    public void saveCep(Cep cep) {
        cepRepository.saveCepLocal(cep)
                .subscribe(
                        () -> {
                            sendToastMessageById(R.string.local_cep_saved);
                        },
                        throwable -> {
                            sendToastMessageById(R.string.local_cep_save_error);
                        }
                );
    }

    public void searchCepClick(String cep) {
        disposable = cepRepository.searchCep(cep)
                .doOnError(error -> {
                            if (cepRepository.hasApisToUse()) {
                                searchCepClick(cep);
                            }
                        }
                )
                .doFinally(() -> {
                            cepRepository.resetApiFlags();
                            hideLoading();
                        }
                )
                .subscribe(this::showResults,
                        throwable -> {
                            String message = AppConstants.UNKNOW_API_ERROR + throwable.getLocalizedMessage();
                            showErrorMessage(message);
                            Log.e(AppConstants.APP_API_RUNTIME_ERROR, AppConstants.UNKNOW_API_ERROR + throwable.getLocalizedMessage());
                        });
    }

    public LiveData<CepResultItem> getResult() {
        return results;
    }

    public void showLoading() {
        showLoading.postValue(true);
    }

    public void hideLoading() {
        showLoading.postValue(false);
    }

    public LiveData<Boolean> getLoading() {
        return showLoading;
    }

    public void showResults(CepResultItem result) {
        results.postValue(result);
    }

    public void showErrorMessage(String message) {
        errorMessage.postValue(message);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void sendToastMessageById(Integer id) {
        toastMessageById.postValue(id);
    }

    public LiveData<Integer> getToastMessageById() {
        return toastMessageById;
    }

    public void dispose(LifecycleOwner lifecycleOwner) {
        if (this.disposable != null)
            this.disposable.dispose();

        errorMessage.removeObservers(lifecycleOwner);
        cepSearchParams.removeObservers(lifecycleOwner);
        errorMessage.removeObservers(lifecycleOwner);
        toastMessageById.removeObservers(lifecycleOwner);
        results.removeObservers(lifecycleOwner);
        showLoading.removeObservers(lifecycleOwner);
    }

}