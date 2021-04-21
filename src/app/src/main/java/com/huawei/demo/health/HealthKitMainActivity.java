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

package com.huawei.demo.health;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.demo.health.auth.HealthKitAuthActivity;
import com.huawei.health.demo.R;

/**
 * functional description
 *
 * @since 2020-03-19
 */
public class HealthKitMainActivity extends AppCompatActivity {
    private static final String TAG = "KitConnectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_kit_main);
    }

    /**
     * Data Controller
     *
     * @param view UI object
     */
    public void hihealthDataControllerOnclick(View view) {
        Intent intent = new Intent(this, HealthKitDataControllerActivity.class);
        startActivity(intent);
    }

    /**
     * Setting Controller
     *
     * @param view UI object
     */
    public void hihealthSettingControllerOnclick(View view) {
        Intent intent = new Intent(this, HealthKitSettingControllerActivity.class);
        startActivity(intent);
    }

    /**
     * Auto Recorder
     *
     * @param view UI object
     */
    public void hihealthAutoRecorderOnClick(View view) {
        Intent intent = new Intent(this, HealthKitAutoRecorderControllerActivity.class);
        startActivity(intent);
    }

    /**
     * Activity Records Controller
     *
     * @param view UI object
     */
    public void hihealthActivityRecordOnClick(View view) {
        Intent intent = new Intent(this, HealthKitActivityRecordControllerActivity.class);
        startActivity(intent);
    }

    /**
     * signing In and applying for Scopes, and enable the health app authorization.
     *
     * @param view UI object
     */
    public void onLoginClick(View view) {
        Intent intent = new Intent(this, HealthKitAuthActivity.class);
        startActivity(intent);
    }
}
