package com.support.litework.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.support.litework.model.ApplistEntry;

import java.util.ArrayList;
import java.util.List;

public class InstalledApps {
    public static List<ApplistEntry> getInstalledApps(PackageManager packageManager) {
        List<ApplistEntry> installedApps = new ArrayList<>();

        // Get a list of all installed packages
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        // Loop through the packages and add app names to the list
        for (PackageInfo packageInfo : packages) {
            // Check if it's a non-system app
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String packagename = packageInfo.packageName;
                String appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
                Log.d("InstalledApp List", packagename+ "::" +appName);
                installedApps.add(new ApplistEntry(packagename, appName));
            }
        }
        return installedApps;
    }
}
