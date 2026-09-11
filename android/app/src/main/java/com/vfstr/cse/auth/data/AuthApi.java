package com.vfstr.cse.auth.data;

import com.vfstr.cse.auth.data.model.ChangePasswordRequest;
import com.vfstr.cse.auth.data.model.ForgotPasswordRequest;
import com.vfstr.cse.auth.data.model.LoginRequest;
import com.vfstr.cse.auth.data.model.LoginResponse;
import com.vfstr.cse.core.network.models.ApiResponse;

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
    Call<ApiResponse<Void>> logout(@Body com.vfstr.cse.auth.data.model.LogoutRequest request);

    @POST("auth/refresh-token")
    Call<ApiResponse<Void>> refreshFCMToken(@Body com.vfstr.cse.auth.data.model.RefreshTokenRequest request);
}

