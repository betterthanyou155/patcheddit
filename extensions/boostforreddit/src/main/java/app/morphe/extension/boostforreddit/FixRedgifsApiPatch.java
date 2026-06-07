/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.extension.boostforreddit;

import app.morphe.extension.shared.fixes.redgifs.BaseFixRedgifsApiPatch;
import okhttp3.OkHttpClient;

/**
 * @noinspection unused
 */
public class FixRedgifsApiPatch extends BaseFixRedgifsApiPatch {
    static {
        INSTANCE = new FixRedgifsApiPatch();
    }

    public String getDefaultUserAgent() {
        return "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36";
    }

    public static OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .connectionPool(new okhttp3.ConnectionPool(0, 1, java.util.concurrent.TimeUnit.NANOSECONDS))
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor(INSTANCE)
                .build();
    }
}
