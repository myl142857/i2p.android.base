package net.i2p.android.router.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.i2p.android.router.R;
import net.i2p.data.DataHelper;
import net.i2p.router.Router;
import net.i2p.router.RouterContext;
import net.i2p.router.RouterLaunch;
import net.i2p.util.OrderedProperties;
import net.i2p.util.NativeBigInteger;

class Init {

    private final Context ctx;
    private final String myDir;
    private String _apkPath;

    public Init(Context c) {
        ctx = c;
        myDir = c.getFilesDir().getAbsolutePath();
    }

    void debugStuff() {
        System.err.println("java.io.tmpdir" + ": " + System.getProperty("java.io.tmpdir"));
        System.err.println("java.vendor" + ": " + System.getProperty("java.vendor"));
        System.err.println("java.version" + ": " + System.getProperty("java.version"));
        System.err.println("os.arch" + ": " + System.getProperty("os.arch"));
        System.err.println("os.name" + ": " + System.getProperty("os.name"));
        System.err.println("os.version" + ": " + System.getProperty("os.version"));
        System.err.println("user.dir" + ": " + System.getProperty("user.dir"));
        System.err.println("user.home" + ": " + System.getProperty("user.home"));
        System.err.println("user.name" + ": " + System.getProperty("user.name"));
        System.err.println("getFilesDir()" + ": " + myDir);
        System.err.println("max mem" + ": " + DataHelper.formatSize(Runtime.getRuntime().maxMemory()));
        System.err.println("Package" + ": " + ctx.getPackageName());
        System.err.println("Version" + ": " + getOurVersion());
        System.err.println("MODEL" + ": " + Build.MODEL);
        System.err.println("DISPLAY" + ": " + Build.DISPLAY);
        System.err.println("VERSION" + ": " + Build.VERSION.RELEASE);
        System.err.println("SDK" + ": " + Build.VERSION.SDK);
    }

    private String getOurVersion() {
        PackageManager pm = ctx.getPackageManager();
        String us = ctx.getPackageName();
        try {
            PackageInfo pi = pm.getPackageInfo(us, 0);
            System.err.println("VersionCode" + ": " + pi.versionCode);
            // http://doandroids.com/blogs/2010/6/10/android-classloader-dynamic-loading-of/
            _apkPath = pm.getApplicationInfo(us, 0).sourceDir;
            System.err.println("APK Path" + ": " + _apkPath);
            if (pi.versionName != null)
                return pi.versionName;
        } catch (Exception e) {}
        return "??";
    }

    String getAPKPath() {
        return _apkPath;
    }

    void initialize() {
        Properties props = new Properties();
        props.setProperty("i2p.dir.temp", myDir + "/tmp");
        props.setProperty("i2p.dir.pid", myDir + "/tmp");
        mergeResourceToFile(R.raw.router_config, "router.config", props);
        mergeResourceToFile(R.raw.logger_config, "logger.config", null);
        mergeResourceToFile(R.raw.i2ptunnel_config, "i2ptunnel.config", null);
        // FIXME this is a memory hog to merge this way
        mergeResourceToFile(R.raw.hosts_txt, "hosts.txt", null);
        copyResourceToFile(R.raw.blocklist_txt, "blocklist.txt");

        (new File(myDir, "wrapper.log")).delete();

        // Set up the locations so Router and WorkingDir can find them
        System.setProperty("i2p.dir.base", myDir);
        System.setProperty("i2p.dir.config", myDir);
        System.setProperty("wrapper.logfile", myDir + "/wrapper.log");
    }

    private void copyResourceToFile(int resID, String f) {
        InputStream in = null;
        FileOutputStream out = null;

        System.err.println("Creating file " + f + " from resource");
        byte buf[] = new byte[4096];
        try {
            // Context methods
            in = ctx.getResources().openRawResource(resID);
            out = ctx.openFileOutput(f, 0);
            
            int read = 0;
            while ( (read = in.read(buf)) != -1)
                out.write(buf, 0, read);
            
        } catch (IOException ioe) {
        } catch (Resources.NotFoundException nfe) {
        } finally {
            if (in != null) try { in.close(); } catch (IOException ioe) {}
            if (out != null) try { out.close(); } catch (IOException ioe) {}
        }
    }
    
    /**
     *  Load defaults from resource,
     *  then add props from file,
     *  and write back
     *  For now, do it backwards so we can override with new apks.
     *  When we have user configurable stuff, switch it back.
     *  @param props local overrides or null
     */
    private void mergeResourceToFile(int resID, String f, Properties overrides) {
        InputStream in = null;
        InputStream fin = null;

        byte buf[] = new byte[4096];
        try {
            in = ctx.getResources().openRawResource(resID);
            Properties props = new OrderedProperties();
            // keep user settings
            //DataHelper.loadProps(props,  in);
            
            try {
                fin = ctx.openFileInput(f);
                DataHelper.loadProps(props,  fin);
                System.err.println("Merging resource into file " + f);
            } catch (IOException ioe) {
                System.err.println("Creating file " + f + " from resource");
            }

            // override user settings
            DataHelper.loadProps(props,  in);

            if (overrides != null)
                props.putAll(overrides);
            DataHelper.storeProps(props, ctx.getFileStreamPath(f));
        } catch (IOException ioe) {
        } catch (Resources.NotFoundException nfe) {
        } finally {
            if (in != null) try { in.close(); } catch (IOException ioe) {}
            if (fin != null) try { fin.close(); } catch (IOException ioe) {}
        }
    }
    
}