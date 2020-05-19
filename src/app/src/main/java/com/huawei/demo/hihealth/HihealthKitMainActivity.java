/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.demo.hihealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
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
 * functional description
 *
 * @since 2020-03-19
 */
public class HihealthKitMainActivity extends AppCompatActivity {
    private static final String TAG = "KitConnectActivity";

    // Request code for displaying the authorization screen using the startActivityForResult method.
    // The value can be defined by developers.
    private static final int REQUEST_SIGN_IN_LOGIN = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hihealth_kit_main);

        // The authorization and sign-in API is called each time the app is started.
        signIn();
    }

    /**
     * Data Controller
     *
     * @param view UI object
     */
    public void hihealthDataControllerOnclick(View view) {
        Intent intent = new Intent(this, HihealthKitDataManagerActivity.class);
        startActivity(intent);
    }

    /**
     * Setting Controller
     *
     * @param view UI object
     */
    public void hihealthSettingControllerOnclick(View view) {
        Intent intent = new Intent(this, HihealthKitSettingControllerActivity.class);
        startActivity(intent);
    }

    /**
     * Auto Recorder
     *
     * @param view UI object
     */
    public void hihealthAutoRecorderOnClick(View view) {
        Intent intent = new Intent(this, HiHealthKitAutoRecorderControllerActivity.class);
        startActivity(intent);
    }

    /**
     * Sensor Controller
     *
     * @param view UI object
     */
    public void hihealthSensorOnClick(View view) {
        Intent intent = new Intent(this, HiHealthKitSensorsControllerActivity.class);
        startActivity(intent);
    }

    /**
     * Activity Records Controller
     *
     * @param view UI object
     */
    public void hihealthActivityRecordOnClick(View view) {
        Intent intent = new Intent(this, HihealthKitActivityRecordControllerActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the sign-in response.
        handleSignInResult(requestCode, data);
    }

    /**
     * signing In and applying for Scopes
     *
     * @param view UI object
     */
    public void onLoginClick(View view) {
        signIn();
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
        scopeList.add(new Scope(Scopes.HEALTHKIT_STEP_BOTH));

        // View and save height and weight in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEIGHTWEIGHT_BOTH));

        // View and save the heart rate data in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_HEARTRATE_BOTH));

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

        // Add the callback for the call result.
        authHuaweiIdTask.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId huaweiId) {
                // The silent sign-in is successful.
                Log.i(TAG, "silentSignIn success");
            }
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
                    // Developers can change HihealthKitMainActivity to the actual activity.
                    HihealthKitMainActivity.this.startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN);
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
        Log.d(TAG, "handleSignInResult status = " + result.getStatus() + ", result = " + result.isSuccess());
        if (result.isSuccess()) {
            Log.d(TAG, "sign in is success");

            // Obtain the authorization result.
            HuaweiIdAuthResult authResult = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
        }
    }
}
