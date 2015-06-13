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
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import rocks.brnmod.setupwizard.R;
import rocks.brnmod.setupwizard.ui.SetupPageFragment;

public class SoundSettingsPage extends SetupPage {

    private static final String TAG = "SoundSettingsPage";

    public SoundSettingsPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new SoundSettingsFragment();
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
        return R.string.setup_sound;
    }

    public static class SoundSettingsFragment extends SetupPageFragment {

        private View mSoundEffectsRow;
        private View mLockscreenSoundsRow;
        private View mDtmfToneRow;
        private View mVibrateInputDevicesRow;
        private CheckBox mSoundEffects;
        private CheckBox mLockscreenSounds;
        private CheckBox mDtmfTone;
        private CheckBox mVibrateInputDevices;

        private ContentResolver mContentResolver;

        private View.OnClickListener mSoundEffectsRowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.SOUND_EFFECTS_ENABLED, !mSoundEffects.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        private View.OnClickListener mLockscreenSoundsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.LOCKSCREEN_SOUNDS_ENABLED, !mLockscreenSounds.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        private View.OnClickListener mDtmfToneClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.DTMF_TONE_WHEN_DIALING, !mDtmfTone.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        private View.OnClickListener mVibrateInputDevicesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.System.putInt(mContentResolver,
                        Settings.System.HAPTIC_FEEDBACK_ENABLED, !mVibrateInputDevices.isChecked() ? 1 : 0);

                updateSettingsToggles();
            }
        };

        @Override
        protected void initializePage() {
            mContentResolver = getActivity().getContentResolver();
            TextView summaryView = (TextView) mRootView.findViewById(android.R.id.summary);
            summaryView.setText(R.string.sound_settings_summary);

            mSoundEffectsRow = mRootView.findViewById(R.id.sound_effects);
            mSoundEffectsRow.setOnClickListener(mSoundEffectsRowClickListener);
            mSoundEffects = (CheckBox) mRootView.findViewById(R.id.sound_effects_checkbox);

            mLockscreenSoundsRow = mRootView.findViewById(R.id.lockscreen_sounds);
            mLockscreenSoundsRow.setOnClickListener(mLockscreenSoundsClickListener);
            mLockscreenSounds = (CheckBox) mRootView.findViewById(R.id.lockscreen_sounds_checkbox);

            mDtmfToneRow = mRootView.findViewById(R.id.dtmf_tone);
            mDtmfToneRow.setOnClickListener(mDtmfToneClickListener);
            mDtmfTone = (CheckBox) mRootView.findViewById(R.id.dtmf_tone_checkbox);

            mVibrateInputDevicesRow = mRootView.findViewById(R.id.vibrate_input_devices);
            mVibrateInputDevicesRow.setOnClickListener(mVibrateInputDevicesClickListener);
            mVibrateInputDevices = (CheckBox) mRootView.findViewById(R.id.vibrate_input_devices_checkbox);
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.sound_settings_page;
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
            boolean touchSoundsEnabled = Settings.System.getInt(mContentResolver,
                    Settings.System.SOUND_EFFECTS_ENABLED, 0) == 1;
            boolean lockscreenSoundsEnabled = Settings.System.getInt(mContentResolver,
                    Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 0) == 1;
            boolean DtmfToneEnabled = Settings.System.getInt(mContentResolver,
                    Settings.System.DTMF_TONE_WHEN_DIALING, 0) == 1;
            boolean vibrateInputDevicesEnabled = Settings.System.getInt(mContentResolver,
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 1;

            mSoundEffects.setChecked(touchSoundsEnabled);
            mLockscreenSounds.setChecked(lockscreenSoundsEnabled);
            mDtmfTone.setChecked(DtmfToneEnabled );
            mVibrateInputDevices.setChecked(vibrateInputDevicesEnabled);
        }

    }
}
