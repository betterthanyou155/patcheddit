/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.extension.boostforreddit.http;

import java.util.List;

import app.morphe.extension.boostforreddit.http.arcticshift.ArcticShiftThrottlingInterceptor;
import app.morphe.extension.boostforreddit.http.imgur.ImgurUndeleteInterceptor;
import app.morphe.extension.boostforreddit.http.reddit.RedditFixAudioInDownloadsInterceptor;
import app.morphe.extension.boostforreddit.http.reddit.RedditMediaUndeleteInterceptor;
import app.morphe.extension.boostforreddit.http.reddit.RedditSubmissionUndeleteInterceptor;
import app.morphe.extension.boostforreddit.http.reddit.RedditSubredditUndeleteInterceptor;
import app.morphe.extension.boostforreddit.http.wayback.WaybackThrottlingInterceptor;
import app.morphe.extension.shared.fixes.feed.RAllPatch;
import app.morphe.extension.shared.requests.BaseOkHttpRequestHook;
import okhttp3.Interceptor;


/**
 * @noinspection unused
 */
public class OkHttpRequestHook extends BaseOkHttpRequestHook {
    private OkHttpRequestHook() {}

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new OkHttpRequestHook();
        }
    }


    @Override
    protected List<Interceptor> getInterceptors() {
        return List.of(
                new RedditMediaUndeleteInterceptor(),
                new RedditSubmissionUndeleteInterceptor(),
                new RedditSubredditUndeleteInterceptor(),
                new RedditFixAudioInDownloadsInterceptor(),
                new ImgurUndeleteInterceptor(),
                new RAllPatch(),
                new RedgifsUserAgentInterceptor()
        );
    }

    @Override
    protected List<Interceptor> getNetworkInterceptors() {
        return List.of(
                new ArcticShiftThrottlingInterceptor(),
                new WaybackThrottlingInterceptor()
        );
    }

    private static class RedgifsUserAgentInterceptor implements Interceptor {
        @androidx.annotation.NonNull
        @Override
        public okhttp3.Response intercept(@androidx.annotation.NonNull Chain chain) throws java.io.IOException {
            okhttp3.Request request = chain.request();
            String host = request.url().host();
            if (host != null && host.contains("redgifs.com")) {
                okhttp3.Request modifiedRequest = request.newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                        .build();
                return chain.proceed(modifiedRequest);
            }
            return chain.proceed(request);
        }
    }
}
