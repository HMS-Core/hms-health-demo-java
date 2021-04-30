
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.demo.health.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonParser;
import com.huawei.demo.health.OkHttpUtilCallback;
import com.huawei.health.demo.R;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.function.Consumer;

/**
 * Check authorization result of HUAWEI Health to HUAWEI Health Kit by Restful API
 *
 * @since 2020-09-18
 */
public class HealthKitAuthCloudActivity extends AppCompatActivity {
    private static final String TAG = "HealthKitAuthCloud";

    /**
     * Request code for displaying the sign in authorization screen using the startActivityForResult method.
     * The value can be defined by developers.
     */
    private static final int REQUEST_SIGN_IN_LOGIN = 1002;

    /**
     * Request code for displaying the HUAWEI Health authorization screen using the startActivityForResult method.
     * The value can be defined by developers.
     */
    private static final int REQUEST_HEALTH_AUTH = 1003;

    /**
     * Error Code: can not resolve HUAWEI Health Authorization Activity
     */
    private static final String RESOLVE_ACTIVITY_ERROR = "50033";

    /**
     * Scheme of Huawei Health Authorization Activity
     */
    private static final String HEALTH_APP_SETTING_DATA_SHARE_HEALTHKIT_ACTIVITY_SCHEME =
        "huaweischeme://healthapp/achievement?module=kit";

    /**
     * URL of query Huawei Health Authorization result
     */
    private static final String CLOUD_PRIVACY_URL =
        "https://health-api.cloud.huawei.com/healthkit/v1/profile/privacyRecords";

    /**
     * Huawei Health authorization enabled
     */
    private static final int AUTH_ENABLED = 1;

    private Context mContext;

    // display authorization result
    private TextView authDescTitle;

    // display authorization failure message
    private TextView authFailTips;

    // confirm result
    private Button confirm;

    // retry authorization
    private Button authRetry;

    // Login in to the HUAWEI ID and authorize
    private Button loginAuth;
    
    /**
     * accessToken for http request
     */
    private String accessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_auth);

        initView();
        initService();
    }

    /**
     * Method of handling HUAWEI Health authorization result
     *
     * @param requestCode indicating the request code for Health authorization Activity
     * @param resultCode indicating the authorization result code
     * @param data indicating the authorization result. but data is null, you need to query authorization result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the sign-in response.
        handleSignInResult(requestCode, data);
        // Handle the HAUWEI Health authorization Activity response.
        handleHealthAuthResult(requestCode);
    }

    private void initView() {
        authDescTitle = findViewById(R.id.health_auth_desc_title);
        authFailTips = findViewById(R.id.health_auth_fail_tips);
        loginAuth = findViewById(R.id.health_login_auth);
        confirm = findViewById(R.id.health_auth_confirm);
        authRetry = findViewById(R.id.health_auth_retry);

        authDescTitle.setVisibility(View.GONE);
        authFailTips.setVisibility(View.GONE);
        confirm.setVisibility(View.GONE);
        authRetry.setVisibility(View.GONE);

        // listener to login HUAWEI ID and authorization
        loginAuth.setOnClickListener((view) -> {
            Intent intent = new Intent(this, HealthKitCloudLogin.class);
            startActivityForResult(intent, REQUEST_SIGN_IN_LOGIN);
        });

        // listener to retry authorization
        authRetry.setOnClickListener((view) -> {
            checkOrAuthorizeHealth();
        });

        // finish this Activity
        confirm.setOnClickListener((view) -> {
            this.finish();
        });
    }

    private void initService() {
        mContext = this;
        // get accessToken from intent
        accessToken = getIntent().getStringExtra("accessToken");
    }

    /**
     * Method of handling authorization result responses
     *
     * @param requestCode (indicating the request code for displaying the authorization screen)
     * @param data (indicating the authorization result response)
     */
    private void handleSignInResult(int requestCode, Intent data) {
        if (requestCode != REQUEST_SIGN_IN_LOGIN || data == null) {
            return;
        }
        Log.d(TAG, "HMS handleSignInResult");
        accessToken = data.getStringExtra("accessToken");

        checkOrAuthorizeHealth();
    }

    /**
     * Method of handling the HAUWEI Health authorization Activity response
     *
     * @param requestCode (indicating the request code for displaying the HUAWEI Health authorization screen)
     */
    private void handleHealthAuthResult(int requestCode) {
        // Determine whether request code is HUAWEI Health authorization Activity
        if (requestCode != REQUEST_HEALTH_AUTH) {
            return;
        }

        // Query the authorization result after the HUAWEI health authorization Activity is returned
        queryHealthAuthorization();
    }

    /**
     * Check HUAWEI Health authorization status By restful api.
     * if not, start HUAWEI Health authorization Activity for user authorization.
     */
    private void checkOrAuthorizeHealth() {
        Log.d(TAG, "begint to checkOrAuthorizeHiHealthPrivacy");
        // 1. Build a PopupWindow as progress dialog for time-consuming operation.
        final PopupWindow popupWindow = initPopupWindow();

        // 2. Build restful request to query HUAWEI Health authorization status.
        Request privacyRequest = buildPrivacyRequest(accessToken);

        // 3. Sending an HTTP Request Asynchronously, and build user-defined Callback for response. This Callback init
        // with an anonymous Consumer to handle query result for checkOrAuthorizeHiHealthPrivacy.
        OkHttpClient mClient = new OkHttpClient();
        mClient.newCall(privacyRequest).enqueue(new OkHttpUtilCallback(new Consumer<String>() {
            @Override
            public void accept(String response) {
                Log.i(TAG, "checkOrAuthorizeHiHealthPrivacy success response:" + response);
                // Update View with result, call View.Post() to ensure run on the user interface thread.
                getWindow().getDecorView().post(() -> {
                    // Dismiss the PopupWindow
                    popupWindow.dismiss();

                    // request error
                    if (OkHttpUtilCallback.REQUEST_ERROR.equals(response)) {
                        buildFailView(OkHttpUtilCallback.REQUEST_ERROR);
                        return;
                    }

                    // Parse response Json to get authorization result
                    JsonParser parser = new JsonParser();
                    int opinion =
                        parser.parse(response).getAsJsonArray().get(0).getAsJsonObject().get("opinion").getAsInt();
                    // If HUAWEI Health is authorized, build success View.
                    if (opinion == AUTH_ENABLED) {
                        buildSuccessView();
                        return;
                    }

                    // If not, start HUAWEI Health authorization Activity by schema with User-defined requestCode.
                    Uri healthKitSchemaUri = Uri.parse(HEALTH_APP_SETTING_DATA_SHARE_HEALTHKIT_ACTIVITY_SCHEME);
                    Intent intent = new Intent(Intent.ACTION_VIEW, healthKitSchemaUri);
                    // Before start, Determine whether the HUAWEI health authorization Activity can be opened.
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_HEALTH_AUTH);
                    } else {
                        buildFailView(RESOLVE_ACTIVITY_ERROR);
                    }
                });
            }
        }));
    }

    /**
     * Query Huawei Health authorization result.
     */
    private void queryHealthAuthorization() {
        Log.d(TAG, "begint to queryPrivacyAuthorization");
        // 1. Build a PopupWindow as progress dialog for time-consuming operation
        final PopupWindow popupWindow = initPopupWindow();

        // 2. Build restful request to query HUAWEI Health authorization status.
        Request privacyRequest = buildPrivacyRequest(accessToken);

        // 3. Sending an HTTP Request Asynchronously, and build user-defined Callback for response. This Callback init
        // with an anonymous Consumer to handle query result for queryPrivacyAuthorization.
        OkHttpClient mClient = new OkHttpClient();
        mClient.newCall(privacyRequest).enqueue(new OkHttpUtilCallback(new Consumer<String>() {
            @Override
            public void accept(String response) {
                Log.i(TAG, "queryPrivacyAuthorization success response:" + response);
                // Update View with result, call View.Post() to ensure run on the user interface thread.
                getWindow().getDecorView().post(() -> {
                    // Dismiss the PopupWindow
                    popupWindow.dismiss();

                    // Parse response Json to get authorization result
                    JsonParser parser = new JsonParser();
                    int opinion =
                        parser.parse(response).getAsJsonArray().get(0).getAsJsonObject().get("opinion").getAsInt();

                    // If HUAWEI Health is authorized, build success View. if Not, build fail view.
                    if (opinion == AUTH_ENABLED) {
                        buildSuccessView();
                    } else {
                        buildFailView(null);
                    }
                });
            }
        }));
    }

    private void buildFailView(String errorMessage) {
        authDescTitle.setText(R.string.health_auth_health_kit_fail);
        authFailTips.setVisibility(View.VISIBLE);
        authRetry.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.GONE);

        // If can't resolve HUAWEI Health Authorization Activity, remind the user to install supported version APP.
        if (OkHttpUtilCallback.REQUEST_ERROR.equals(errorMessage)) {
            authFailTips.setText(getResources().getString(R.string.health_auth_health_kit_fail_tips_exception));
        } else if (RESOLVE_ACTIVITY_ERROR.equals(errorMessage)) {
            authFailTips.setText(getResources().getString(R.string.health_auth_health_kit_fail_tips_install));
        } else {
            authFailTips.setText(getResources().getString(R.string.health_auth_health_kit_fail_tips_connect));
        }
    }

    private void buildSuccessView() {
        authDescTitle.setText(R.string.health_auth_health_kit_success);
        authRetry.setVisibility(View.GONE);
        authFailTips.setVisibility(View.GONE);
        confirm.setVisibility(View.VISIBLE);
    }

    /**
     * Build restful request to query HUAWEI Health authorization status
     *
     * @param accessToken header Authorization params for request
     * @return Request to query HUAWEI Health authorization status
     */
    private Request buildPrivacyRequest(Object accessToken) {
        return new Request.Builder().url(CLOUD_PRIVACY_URL)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .get()
            .build();
    }

    /**
     * init popupWindow as progress dialog.
     *
     * @return instance of popupWindow
     */
    private PopupWindow initPopupWindow() {
        final PopupWindow popupWindow = new PopupWindow();
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_waitting, null);
        popupWindow.setContentView(view);

        getWindow().getDecorView().post(() -> {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            authDescTitle.setVisibility(View.VISIBLE);
            loginAuth.setVisibility(View.GONE);
        });
        return popupWindow;
    }
}
