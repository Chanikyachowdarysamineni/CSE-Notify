package com.csehub.app.auth.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.csehub.app.auth.data.model.ChangePasswordRequest;
import com.csehub.app.auth.data.model.ForgotPasswordRequest;
import com.csehub.app.auth.data.model.LoginRequest;
import com.csehub.app.auth.data.model.LoginResponse;
import com.csehub.app.core.network.ApiClient;
import com.csehub.app.core.network.models.ApiResponse;
import com.csehub.app.core.security.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository to bridge auth requests from ViewModels to the network API
 */
public class AuthRepository {

    private final AuthApi authApi;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        this.authApi = ApiClient.createService(AuthApi.class);
        this.tokenManager = TokenManager.getInstance(context);
    }

    public LiveData<Resource<LoginResponse>> login(String email, String password, String fcmToken) {
        MutableLiveData<Resource<LoginResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading());

        authApi.login(new LoginRequest(email, password, fcmToken)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LoginResponse loginRes = response.body().getData();
                    // Save user session securely
                    tokenManager.saveUserSession(
                            loginRes.getUser().getId(),
                            loginRes.getUser().getEmail(),
                            loginRes.getUser().getName(),
                            loginRes.getUser().getRole(),
                            loginRes.getToken()
                    );
                    data.setValue(Resource.success(loginRes));
                } else {
                    String msg = "Login failed";
                    if (response.body() != null) {
                        msg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            msg = response.errorBody().string();
                        } catch (Exception e) { /* ignored */ }
                    }
                    data.setValue(Resource.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                data.setValue(Resource.error("Connection failed: " + t.getMessage()));
            }
        });

        return data;
    }

    public LiveData<Resource<Void>> forgotPassword(String email) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading());

        authApi.forgotPassword(new ForgotPasswordRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    data.setValue(Resource.success(null));
                } else {
                    data.setValue(Resource.error("Failed to send reset link"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                data.setValue(Resource.error("Connection failed"));
            }
        });

        return data;
    }

    public LiveData<Resource<Void>> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<Resource<Void>> data = new MutableLiveData<>();
        data.setValue(Resource.loading());

        authApi.changePassword(new ChangePasswordRequest(currentPassword, newPassword)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Update saved token
                    tokenManager.saveToken(response.body().getData().getToken());
                    data.setValue(Resource.success(null));
                } else {
                    String msg = "Failed to change password";
                    if (response.body() != null) {
                        msg = response.body().getMessage();
                    }
                    data.setValue(Resource.error(msg));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                data.setValue(Resource.error("Connection failed"));
            }
        });

        return data;
    }

    public void logout(Runnable onComplete) {
        authApi.logout().enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                tokenManager.clearSession();
                ApiClient.reset();
                onComplete.run();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                tokenManager.clearSession();
                ApiClient.reset();
                onComplete.run();
            }
        });
    }

    // Generic resource helper
    public static class Resource<T> {
        public enum Status { SUCCESS, ERROR, LOADING }
        public final Status status;
        public final T data;
        public final String message;

        private Resource(Status status, T data, String message) {
            this.status = status;
            this.data = data;
            this.message = message;
        }

        public static <T> Resource<T> success(T data) {
            return new Resource<>(Status.SUCCESS, data, null);
        }

        public static <T> Resource<T> error(String msg) {
            return new Resource<>(Status.ERROR, null, msg);
        }

        public static <T> Resource<T> loading() {
            return new Resource<>(Status.LOADING, null, null);
        }
    }
}
