/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.demo.health;

import java.io.IOException;
import java.util.function.Consumer;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 *  Callback for OkHttp request
 *
 * @since 2020-09-27
 */
public class OkHttpUtilCallback implements Callback {
    private static final String TAG = "OkHttpUtilCallback";

    /**
     * Request error code
     */
    public static final String REQUEST_ERROR = "500";

    private Consumer<String> consumer;

    public OkHttpUtilCallback(Consumer consumer) {
        this.consumer = consumer;
    }

    // If request fail, make a toast to indicate the failure.
    @Override
    public void onFailure(Call call, IOException e) {
        StringBuilder stringBuilder = new StringBuilder("Request error: ").append(call.request().url().toString())
            .append(" ")
            .append(e.getMessage());

        Log.e(TAG, stringBuilder.toString());
        consumer.accept(REQUEST_ERROR);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        Log.d(TAG, "onResponse: " + response.toString());
        if (consumer == null) {
            return;
        }

        // Check whether the request is successful. If yes, invoke the Consumer to process the response. Otherwise, pass
        // REQUEST_ERROR code.
        if (response.isSuccessful() && (response.body() != null)) {
            consumer.accept(response.body().string());
        } else {
            consumer.accept(REQUEST_ERROR);
        }
    }
}