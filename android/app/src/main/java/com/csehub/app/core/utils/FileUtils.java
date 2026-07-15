package com.csehub.app.core.utils;

import com.csehub.app.BuildConfig;

/**
 * File utility class for handling size formatting, file extensions, and server URL resolution
 */
public final class FileUtils {

    private FileUtils() {} // Prevent instantiation

    /**
     * Resolves the server base path to form complete URLs for images, events, and document attachments
     */
    public static String resolveUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }

        // Clean relative path leading slash
        String path = relativePath;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // Extract base server domain (e.g., http://10.0.2.2:5000/) from BuildConfig.BASE_URL
        String baseUrl = BuildConfig.BASE_URL;
        if (baseUrl.endsWith("/api/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 4); // Remove "api/"
        } else if (baseUrl.endsWith("/api")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 3); // Remove "api"
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        return baseUrl + path;
    }

    /**
     * Copy input stream content from uri to temporary local file under app cache.
     * Essential for safe Scoped Storage access on API 29+.
     */
    public static java.io.File getFileFromUri(android.content.Context context, android.net.Uri uri) {
        if (uri == null) return null;
        try {
            String name = "upload_temp";
            android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex);
                }
                cursor.close();
            }
            java.io.File tempFile = new java.io.File(context.getCacheDir(), name);
            java.io.InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper to format file sizes in bytes to human readable formats (e.g., KB, MB)
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
