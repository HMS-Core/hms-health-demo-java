
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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.SettingController;
import com.huawei.hms.hihealth.data.Scopes;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.ArrayList;
import java.util.List;

/**
 * Check authorization result of HUAWEI Health to HUAWEI Health Kit by JAVA API
 *
 * @since 2020-09-18
 */
public class HealthKitAuthClientActivity extends AppCompatActivity {
    private static final String TAG = "HealthKitAuthClient";

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
     * Scheme of Huawei Health Authorization Activity
     */
    private static final String HEALTH_APP_SETTING_DATA_SHARE_HEALTHKIT_ACTIVITY_SCHEME =
        "huaweischeme://healthapp/achievement?module=kit";
    /**
     * Error Code: can not resolve HUAWEI Health Authorization Activity
     * The value can be defined by developers.
     */
    private static final String APP_HEALTH_NOT_INSTALLED = "50033";

    private Context mContext;

    // HUAWEI Health kit SettingController
    private SettingController mSettingController;

    // display authorization result
    private TextView authDescTitle;

    // display authorization failure message
    private TextView authFailTips;

    // Login in to the HUAWEI ID and authorize
    private Button loginAuth;

    // confirm result
    private Button confirm;

    // retry HUAWEI health authorization
    private Button authRetry;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_auth);

        initView();
        initService();
    }

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
            signIn();
        });

        // listener to retry HUAWEI Health authorization
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
        Log.i(TAG, "HiHealthKitClient connect to service");
        // Initialize SettingController
        HiHealthOptions fitnessOptions = HiHealthOptions.builder().build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(fitnessOptions);
        mSettingController = HuaweiHiHealth.getSettingController(mContext, signInHuaweiId);
    }

    /**
     * Sign-in and authorization method.
     * The authorization screen will display up if authorization has not granted by the current account.
     */
    private void signIn() {
        Log.i(TAG, "begin sign in");
        List<Scope> scopeList = new ArrayList<>();

        // Add scopes to apply for. The following only shows an example.
        // Developers need to add scopes according to their specific needs.

        // View and save steps in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_STEP_READ));
        scopeList.add(new Scope(Scopes.HEALTHKIT_STEP_WRITE));

        // View and save height and weight in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEIGHTWEIGHT_READ));
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEIGHTWEIGHT_WRITE));

        // View and save the heart rate data in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEARTRATE_READ));
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEARTRATE_WRITE));

        // View and save activityRecord in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_ACTIVITY_RECORD_READ));
        scopeList.add(new Scope(Scopes.HEALTHKIT_ACTIVITY_RECORD_WRITE));

        // View and save sleep in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_SLEEP_READ));
        scopeList.add(new Scope(Scopes.HEALTHKIT_SLEEP_WRITE));

        // Configure authorization parameters.
        HuaweiIdAuthParamsHelper authParamsHelper =
                new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        HuaweiIdAuthParams authParams =
                authParamsHelper.setIdToken().setAccessToken().setScopeList(scopeList).createParams();

        // Initialize the HuaweiIdAuthService object.
        final HuaweiIdAuthService authService =
                HuaweiIdAuthManager.getService(this.getApplicationContext(), authParams);

        // Silent sign-in. If authorization has been granted by the current account,
        // the authorization screen will not display. This is an asynchronous method.
        Task<AuthHuaweiId> authHuaweiIdTask = authService.silentSignIn();

        final Context context = this;

        // Add the callback for the call result.
        authHuaweiIdTask.addOnSuccessListener(huaweiId -> {
            // The silent sign-in is successful.
            Log.i(TAG, "silentSignIn success");
            Toast.makeText(context, "silentSignIn success", Toast.LENGTH_LONG).show();

            // anfter Huawei ID authorization, perform Huawei Health authorization.
            checkOrAuthorizeHealth();
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // The silent sign-in fails.
                // This indicates that the authorization has not been granted by the current account.
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.i(TAG, "sign failed status:" + apiException.getStatusCode());
                    Log.i(TAG, "begin sign in by intent");

                    // Call the sign-in API using the getSignInIntent() method.
                    Intent signInIntent = authService.getSignInIntent();

                    // Display the authorization screen by using the startActivityForResult() method of the activity.
                    // Developers can change HealthKitAuthClientActivity to the actual activity.
                    HealthKitAuthClientActivity.this.startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN);
                }
            }
        });
    }

    /**
     * Method of handling authorization result responses
     *
     * @param requestCode (indicating the request code for displaying the authorization screen)
     * @param data (indicating the authorization result response)
     */
    private void handleSignInResult(int requestCode, Intent data) {
        // Handle only the authorized responses
        if (requestCode != REQUEST_SIGN_IN_LOGIN) {
            return;
        }

        // Obtain the authorization response from the intent.
        HuaweiIdAuthResult result = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
        if (result != null) {
            Log.d(TAG, "handleSignInResult status = " + result.getStatus() + ", result = " + result.isSuccess());
            if (result.isSuccess()) {
                Log.d(TAG, "sign in is success");

                // Obtain the authorization result.
                HuaweiIdAuthResult authResult =
                        HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
                Log.d(TAG, "sign in is success authResult" + authResult);

                // anfter Huawei ID authorization, perform Huawei Health authorization.
                checkOrAuthorizeHealth();
            }
        }
    }

    /**
     * Method of handling the HAUWEI Health authorization Activity response
     *
     * @param requestCode (indicating the request code for displaying the HUAWEI Health authorization screen)
     */
    private void handleHealthAuthResult(int requestCode) {
        if (requestCode != REQUEST_HEALTH_AUTH) {
            return;
        }

        queryHealthAuthorization();
    }

    /**
     * Check HUAWEI Health authorization status.
     * if not, start HUAWEI Health authorization Activity for user authorization.
     */
    private void checkOrAuthorizeHealth() {
        Log.d(TAG, "begint to checkOrAuthorizeHealth");
        // 1. Build a PopupWindow as progress dialog for time-consuming operation
        final PopupWindow popupWindow = initPopupWindow();

        // 2. Calling SettingController to query HUAWEI Health authorization status.
        // This method is asynchronous, so need to build a listener for result.
        Task<Boolean> authTask = mSettingController.getHealthAppAuthorisation();
        authTask.addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                getWindow().getDecorView().post(() -> {
                    popupWindow.dismiss();

                    Log.i(TAG, "checkOrAuthorizeHealth get result success");
                    // If HUAWEI Health is authorized, build success View.
                    if (Boolean.TRUE.equals(result)) {
                        buildSuccessView();
                    } else {
                        // If not, start HUAWEI Health authorization Activity by schema with User-defined requestCode.
                        Uri healthKitSchemaUri = Uri.parse(HEALTH_APP_SETTING_DATA_SHARE_HEALTHKIT_ACTIVITY_SCHEME);
                        Intent intent = new Intent(Intent.ACTION_VIEW, healthKitSchemaUri);
                        // Before start, Determine whether the HUAWEI health authorization Activity can be opened.
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(intent, REQUEST_HEALTH_AUTH);
                        } else {
                            buildFailView(APP_HEALTH_NOT_INSTALLED);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                getWindow().getDecorView().post(() -> {
                    popupWindow.dismiss();

                    // The method has encountered an exception. Show exception tips in the View.
                    if (exception != null) {
                        Log.i(TAG, "checkOrAuthorizeHealth has exception");
                        buildFailView(exception.getMessage());
                    }
                });
            }

        });
    }

    /**
     * Query Huawei Health authorization result.
     */
    private void queryHealthAuthorization() {
        Log.d(TAG, "begint to queryHealthAuthorization");
        // 1. Build a PopupWindow as progress dialog for time-consuming operation
        final PopupWindow popupWindow = initPopupWindow();

        // 2. Calling SettingController to query HUAWEI Health authorization status.
        // This method is asynchronous, so need to build a listener for result.
        Task<Boolean> queryTask = mSettingController.getHealthAppAuthorisation();
        queryTask.addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                getWindow().getDecorView().post(() -> {
                    popupWindow.dismiss();

                    Log.i(TAG, "queryHealthAuthorization result is" + result);
                    // Show authorization result in view.
                    if (Boolean.TRUE.equals(result)) {
                        buildSuccessView();
                    } else {
                        buildFailView(null);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                getWindow().getDecorView().post(() -> {
                    popupWindow.dismiss();

                    // The method has encountered an exception. Show exception tips in the View.
                    if (exception != null) {
                        Log.i(TAG, "queryHealthAuthorization has exception");
                        buildFailView(exception.getMessage());
                    }
                });
            }
        });
    }

    private void buildFailView(String errorMessage) {
        authDescTitle.setText(R.string.health_auth_health_kit_fail);
        authFailTips.setVisibility(View.VISIBLE);
        authRetry.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.GONE);

        // Authentication failure message. if error message is not null, displayed based on the error code.
        if (APP_HEALTH_NOT_INSTALLED.equals(errorMessage)) {
            authFailTips.setText(getResources().getString(R.string.health_auth_health_kit_fail_tips_update));
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
