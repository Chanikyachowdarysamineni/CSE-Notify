package com.csehub.app.core.network;

import java.io.IOException;

public class NoConnectivityException extends IOException {
    @Override
    public String getMessage() {
        return "No Internet Connection. Please check your network settings.";
    }
}
