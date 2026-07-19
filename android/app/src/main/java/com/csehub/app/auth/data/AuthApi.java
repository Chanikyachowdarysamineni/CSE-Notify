package com.csehub.app.auth.data;

import com.csehub.app.auth.data.model.ChangePasswordRequest;
import com.csehub.app.auth.data.model.ForgotPasswordRequest;
import com.csehub.app.auth.data.model.LoginRequest;
import com.csehub.app.auth.data.model.LoginResponse;
import com.csehub.app.core.network.models.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Retrofit endpoints for Auth module
 */
public interface AuthApi {

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest request);

    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);

    @PUT("auth/change-password")
    Call<ApiResponse<LoginResponse>> changePassword(@Body ChangePasswordRequest request);

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(@Body com.csehub.app.auth.data.model.LogoutRequest request);

    @POST("auth/refresh-token")
    Call<ApiResponse<Void>> refreshFCMToken(@Body com.csehub.app.auth.data.model.RefreshTokenRequest request);
}
