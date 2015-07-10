package rocks.brnmod.setupwizard.fdroid;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.ManifestDigest;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.net.Uri;

import android.os.Environment;
import android.os.FileUtils;
import android.os.Parcel;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.jar.JarFile;

public class Installer {

    private Installer() {}

    private static Installer installer = new Installer();

    public static Installer getInstance() {
        return installer;
    }

    private FDroidClient client = new FDroidClient();

    public boolean install(final String apps[], final Context context, final DownloadProgressListener listener) {
        Log.d("SIN", "Start install for " + Arrays.toString(apps));
        if (client.apps.size() == 0) {
            for (int i = 0; i < 3; i++) {
                try {
                    client.load(new DownloadProgressListener() {
                        @Override
                        public void update(float progress) {
                            if (listener != null) listener.update(0.1f * progress);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (client.apps.size() > 0) {
                    break;
                }
            }
        }

        if (client.apps.size() == 0) return false;

        final PackageManager pm = context.getPackageManager();

        final File base;
        {
            File installBase = null;
            File[] installBases = context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
            for (File b : installBases) {
                b = b.getAbsoluteFile();
                try {
                    b = b.getCanonicalFile();
                } catch(IOException ioe) {
                }
                if (b.exists() && b.canWrite() && b.isDirectory()) {
                    installBase = b;
                    break;
                }
            }
            base = installBase;
        }

        float baseline = 0.1f;
        for (final String app : apps) {
            final float b = baseline;
            for (int i = 0 ; i < 3; i++) {
                try {
                    final File apk = client.downloadApk(app, new DownloadProgressListener() {
                        @Override
                        public void update(float progress) {
                            if (listener != null) listener.update(b + (progress * 0.8f) / apps.length);
                        }
                    });

                    if (apk == null) {
                        Log.d("SIN", "APK not downloaded: " + app);
                        continue;
                    }

                    new Thread() {
                        @Override
                        public void run() {
                            synchronized (pm) {
                                // copy it to download location
                                File local = new File(base, apk.getName());
                                Log.d("SIN", "Copy " + app + " to " + local);
                                FileUtils.copyFile(apk, local);
                                Log.d("SIN", "Trigger install of " + app);

                                // trigger install
                                pm.installPackageWithVerification(
                                        Uri.fromFile(local), (IPackageInstallObserver) null,
                                        0, "brn/setup/fdroid",
                                        null, null, null);
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            baseline += 0.8f / apps.length;
        }

        if (listener != null) listener.update(1f);

        return true;
    }

}
