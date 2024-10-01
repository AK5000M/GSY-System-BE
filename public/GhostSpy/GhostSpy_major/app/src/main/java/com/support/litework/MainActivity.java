package com.support.litework;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mLayoutUpdate;
    private Boolean isClicked = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayoutUpdate = (LinearLayout) findViewById(R.id.layout_update);
        mLayoutUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateApp();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        boolean canInstall = getPackageManager().canRequestPackageInstalls();
        if (canInstall && isClicked) {
            File apkFile = copyApkFromAssets("update.apk");
            installApk(apkFile);
        }
    }
    private void updateApp() {
        isClicked = true;
        boolean canInstall = getPackageManager().canRequestPackageInstalls();
        if (!canInstall) {
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + getPackageName())));
        } else {
            File apkFile = copyApkFromAssets("update.apk");
            installApk(apkFile);
        }
    }
    public File copyApkFromAssets(String fileName) {
        File apkFile = null;
        try {
            InputStream is = getAssets().open(fileName);
            apkFile = new File(getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(apkFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return apkFile;
    }
    public void installApk(File apkFile) {
        if (apkFile != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", apkFile);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }
}