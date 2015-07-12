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
import android.content.ComponentName;
import android.content.ContentQueryMap;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import rocks.brnmod.setupwizard.R;
import rocks.brnmod.setupwizard.ui.SetupPageFragment;
import rocks.brnmod.setupwizard.util.SetupWizardUtils;

public class FDroidPage extends SetupPage {

    public static final String TAG = "FDroidPage";

    public FDroidPage(Context context, SetupDataCallbacks callbacks) {
        super(context, callbacks);
    }

    @Override
    public Fragment getFragment(FragmentManager fragmentManager, int action) {
        Fragment fragment = fragmentManager.findFragmentByTag(getKey());
        if (fragment == null) {
            Bundle args = new Bundle();
            args.putString(Page.KEY_PAGE_ARGUMENT, getKey());
            args.putInt(Page.KEY_PAGE_ACTION, action);
            fragment = new FDroidFragment();
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
        return R.string.setup_fdroid;
    }

    public static class FDroidFragment extends SetupPageFragment {

        private ContentResolver mContentResolver;

        @Override
        protected void initializePage() {
            mContentResolver = getActivity().getContentResolver();
            TextView summaryView = (TextView) mRootView.findViewById(android.R.id.summary);
            summaryView.setText(R.string.fdroid_summary);
        }

        @Override
        protected int getLayoutResource() {
            return R.layout.fdroid;
        }
    }
}
