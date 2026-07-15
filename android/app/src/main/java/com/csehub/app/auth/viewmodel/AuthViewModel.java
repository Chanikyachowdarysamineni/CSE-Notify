package com.csehub.app.auth.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.auth.data.model.LoginResponse;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<String> loginIdError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<String> getLoginIdError() { return loginIdError; }
    public LiveData<String> getEmailError() { return emailError; }
    public LiveData<String> getPasswordError() { return passwordError; }

    public LiveData<AuthRepository.Resource<LoginResponse>> login(String loginId, String password, String fcmToken) {
        loginIdError.setValue(null);
        passwordError.setValue(null);

        boolean isValid = true;

        if (loginId == null || loginId.trim().isEmpty()) {
            loginIdError.setValue("Login ID is required");
            isValid = false;
        }

        if (password == null || password.isEmpty()) {
            passwordError.setValue("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordError.setValue("Password must be at least 6 characters");
            isValid = false;
        }

        if (!isValid) {
            MutableLiveData<AuthRepository.Resource<LoginResponse>> errorRes = new MutableLiveData<>();
            errorRes.setValue(AuthRepository.Resource.error("Please resolve validation errors"));
            return errorRes;
        }

        return authRepository.login(loginId, password, fcmToken);
    }

    public LiveData<AuthRepository.Resource<Void>> forgotPassword(String email) {
        emailError.setValue(null);
        if (email == null || email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Valid email is required");
            MutableLiveData<AuthRepository.Resource<Void>> errorRes = new MutableLiveData<>();
            errorRes.setValue(AuthRepository.Resource.error("Invalid email"));
            return errorRes;
        }
        return authRepository.forgotPassword(email);
    }

    public LiveData<AuthRepository.Resource<Void>> changePassword(String currentPassword, String newPassword) {
        return authRepository.changePassword(currentPassword, newPassword);
    }

    public void logout(Runnable onComplete) {
        authRepository.logout(onComplete);
    }
}
