/*
 * Copyright (C) 2015 BRNmod Team
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rocks.brnmod.setupwizard.setup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import rocks.brnmod.setupwizard.R;
import rocks.brnmod.setupwizard.ui.SetupPageFragment;
import rocks.brnmod.setupwizard.util.SetupWizardUtils;

public class CommonSettingsPage extends SetupPage {

    private static final String TAG = "CommonSettingsPage";

    public CommonSettingsPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new CommonSettingsFragment();
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public int getTitleResId() {
        return R.string.setup_common;
    }

    public static class CommonSettingsFragment extends SetupPageFragment {

        private View mPrivacyGuardRow;
        private View mAdvancedRebootRow;
        private View mDevelopmentSettingsRow;
        private CheckBox mPrivacyGuard;
        private CheckBox mAdvancedReboot;
        private CheckBox mDevelopmentSettings;

        private ContentResolver mContentResolver;

        private View.OnClickListener mPrivacyGuardClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.Secure.putInt(mContentResolver,
                        Settings.Secure.PRIVACY_GUARD_DEFAULT, !mPrivacyGuard.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        private View.OnClickListener mAdvancedRebootClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.Secure.putInt(mContentResolver,
                        Settings.Secure.ADVANCED_REBOOT, !mAdvancedReboot.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        private View.OnClickListener mDevelopmentSettingsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.Global.putInt(mContentResolver,
                        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, !mDevelopmentSettings.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        @Override
        protected void initializePage() {
            mContentResolver = getActivity().getContentResolver();
            TextView summaryView = (TextView) mRootView.findViewById(android.R.id.summary);
            summaryView.setText(R.string.common_settings_summary);

            mPrivacyGuardRow = mRootView.findViewById(R.id.privacy_guard);
            mPrivacyGuardRow.setOnClickListener(mPrivacyGuardClickListener);
            mPrivacyGuard = (CheckBox) mRootView.findViewById(R.id.privacy_guard_checkbox);

            mAdvancedRebootRow = mRootView.findViewById(R.id.advanced_reboot);
            mAdvancedRebootRow.setOnClickListener(mAdvancedRebootClickListener);
            mAdvancedReboot = (CheckBox) mRootView.findViewById(R.id.advanced_reboot_checkbox);

            mDevelopmentSettingsRow = mRootView.findViewById(R.id.development_settings);
            mDevelopmentSettingsRow.setOnClickListener(mDevelopmentSettingsClickListener);
            mDevelopmentSettings = (CheckBox) mRootView.findViewById(R.id.development_settings_checkbox);
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.common_settings_page;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateSettingsToggles();
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        private void updateSettingsToggles() {
            boolean privacyGuardEnabled = Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.PRIVACY_GUARD_DEFAULT, 0) == 1;
            boolean advancedRebootEnabled = Settings.Secure.getInt(mContentResolver,
                    Settings.Secure.ADVANCED_REBOOT, 0) == 1;
            boolean developmentSettingsEnabled = Settings.Global.getInt(mContentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;

            mPrivacyGuard.setChecked(privacyGuardEnabled);
            mAdvancedReboot.setChecked(advancedRebootEnabled);
            mDevelopmentSettings.setChecked(developmentSettingsEnabled);
        }

    }
}
