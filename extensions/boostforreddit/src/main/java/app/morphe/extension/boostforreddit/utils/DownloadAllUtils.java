/*
 * Copyright 2026 wchill.
 * https://github.com/wchill/patcheddit
 *
 * See the included NOTICE file for GPLv3 §7(b) and §7(c) terms that apply to this code.
 */

package app.morphe.extension.boostforreddit.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DownloadAllUtils {
    private static final int DOWNLOAD_ALL_OPTION_ID = 0x7f0a7777;

    public static void showMenu(Activity activity, List<Object> options) {
        try {
            Class<?> mediaActivityClass = Class.forName("com.rubenmayayo.reddit.ui.activities.MediaActivity");
            Field gField = mediaActivityClass.getDeclaredField("g");
            gField.setAccessible(true);
            Object submission = gField.get(activity);

            if (submission != null) {
                Class<?> submissionClass = Class.forName("com.rubenmayayo.reddit.models.reddit.SubmissionModel");
                Method o0Method = submissionClass.getMethod("O0");
                List<?> galleryImages = (List<?>) o0Method.invoke(submission);

                // Only show "Download All" if there is more than 1 image in the gallery
                if (galleryImages != null && galleryImages.size() > 1) {
                    Class<?> menuOptionClass = Class.forName("com.rubenmayayo.reddit.ui.customviews.menu.MenuOption");
                    Object downloadAllOption = menuOptionClass.getConstructor().newInstance();

                    // set ID: downloadAllOption.d0(DOWNLOAD_ALL_OPTION_ID)
                    Method d0Method = menuOptionClass.getMethod("d0", int.class);
                    d0Method.invoke(downloadAllOption, DOWNLOAD_ALL_OPTION_ID);

                    // set Text: downloadAllOption.h0("Download All")
                    Method h0Method = menuOptionClass.getMethod("h0", String.class);
                    h0Method.invoke(downloadAllOption, "Download All");

                    // set Icon: downloadAllOption.a0(0x7f080294) (standard download icon ID)
                    Method a0Method = menuOptionClass.getMethod("a0", int.class);
                    a0Method.invoke(downloadAllOption, 0x7f080294);

                    options.add(downloadAllOption);
                }
            }

            // Show menu by invoking the original MediaActivity.J1(List) method
            Method j1Method = mediaActivityClass.getDeclaredMethod("J1", List.class);
            j1Method.setAccessible(true);
            j1Method.invoke(activity, options);

        } catch (Exception e) {
            LoggingUtils.logException(false, () -> "Failed to show menu with Download All: " + e);
            // Fallback: call the original method directly if possible
            try {
                Class<?> mediaActivityClass = Class.forName("com.rubenmayayo.reddit.ui.activities.MediaActivity");
                Method j1Method = mediaActivityClass.getDeclaredMethod("J1", List.class);
                j1Method.setAccessible(true);
                j1Method.invoke(activity, options);
            } catch (Exception ex) {
                LoggingUtils.logException(false, () -> "Failed fallback to J1: " + ex);
            }
        }
    }

    /**
     * Full replacement for MediaActivity.z1's dispatch logic.
     * Handles our custom Download All option AND reflectively calls the original
     * A1/B1/C1 methods for all standard option IDs.
     *
     * Using a void static method avoids any move-result or new local registers
     * in the z1 hook, which would corrupt the register frame and cause VerifyError.
     *
     * Option IDs from the original sparse-switch in z1:
     *   0x7f0a004f -> A1()  (share)
     *   0x7f0a00a0 -> B1()  (unknown)
     *   0x7f0a00a1 -> C1()  (unknown)
     *   0x7f0a7777 -> our Download All
     */
    public static void handleAndDispatch(Activity activity, Object option) {
        try {
            Class<?> menuOptionClass = Class.forName("com.rubenmayayo.reddit.ui.customviews.menu.MenuOption");
            int optionId = (Integer) menuOptionClass.getMethod("q").invoke(option);

            if (optionId == DOWNLOAD_ALL_OPTION_ID) {
                downloadAll(activity);
                return;
            }

            // Reflectively call the original dispatch methods from MediaActivity
            Class<?> mediaActivityClass = Class.forName("com.rubenmayayo.reddit.ui.activities.MediaActivity");
            String methodName = null;
            if (optionId == 0x7f0a004f) {
                methodName = "A1";
            } else if (optionId == 0x7f0a00a0) {
                methodName = "B1";
            } else if (optionId == 0x7f0a00a1) {
                methodName = "C1";
            }
            if (methodName != null) {
                Method m = mediaActivityClass.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(activity);
            }
        } catch (Exception e) {
            LoggingUtils.logException(false, () -> "Failed to dispatch menu click: " + e);
        }
    }

    private static void downloadAll(Activity activity) {
        if (Build.VERSION.SDK_INT >= 30 || activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            performDownload(activity);
        } else {
            Toast.makeText(activity, "Storage permission required to download files.", Toast.LENGTH_SHORT).show();
            activity.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 123);
        }
    }

    private static void performDownload(Activity activity) {
        try {
            Class<?> mediaActivityClass = Class.forName("com.rubenmayayo.reddit.ui.activities.MediaActivity");
            Field gField = mediaActivityClass.getDeclaredField("g");
            gField.setAccessible(true);
            Object submission = gField.get(activity);

            if (submission != null) {
                Class<?> submissionClass = Class.forName("com.rubenmayayo.reddit.models.reddit.SubmissionModel");
                Method o0Method = submissionClass.getMethod("O0");
                List<?> galleryImages = (List<?>) o0Method.invoke(submission);

                if (galleryImages != null && !galleryImages.isEmpty()) {
                    ArrayList<String> urls = new ArrayList<>();
                    Class<?> galleryImageClass = Class.forName("com.rubenmayayo.reddit.models.reddit.GalleryImage");
                    Class<?> imageModelClass = Class.forName("com.rubenmayayo.reddit.models.generic.ImageModel");
                    Method parseMethod = imageModelClass.getMethod("parse", galleryImageClass);
                    Method getDownloadUrlMethod = imageModelClass.getMethod("getDownloadUrl");

                    for (Object galleryImage : galleryImages) {
                        Object imageModel = parseMethod.invoke(null, galleryImage);
                        if (imageModel != null) {
                            String url = (String) getDownloadUrlMethod.invoke(imageModel);
                            if (url != null && !url.trim().isEmpty()) {
                                urls.add(url);
                            }
                        }
                    }

                    if (!urls.isEmpty()) {
                        // Get subfolder name: he.h0.P(context, submission)
                        Class<?> h0Class = Class.forName("he.h0");
                        Method pMethod = h0Class.getMethod("P", Context.class, submissionClass);
                        String subfolder = (String) pMethod.invoke(null, activity, submission);

                        // Show toast message
                        Toast.makeText(activity, "Downloading " + urls.size() + " files...", Toast.LENGTH_SHORT).show();

                        // Start RedditService.r(context, urls, subfolder)
                        Class<?> redditServiceClass = Class.forName("com.rubenmayayo.reddit.services.RedditService");
                        Method rMethod = redditServiceClass.getMethod("r", Context.class, ArrayList.class, String.class);
                        rMethod.invoke(null, activity, urls, subfolder);
                    } else {
                        Toast.makeText(activity, "No downloadable URLs found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "No media files found in this post.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            LoggingUtils.logException(false, () -> "Failed to perform batch download: " + e);
            Toast.makeText(activity, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
