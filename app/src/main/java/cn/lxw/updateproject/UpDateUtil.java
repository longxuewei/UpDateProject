package cn.lxw.updateproject;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.blankj.utilcode.utils.AppUtils;
import com.blankj.utilcode.utils.FileUtils;
import com.blankj.utilcode.utils.ShellUtils;
import com.blankj.utilcode.utils.Utils;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.blankj.utilcode.utils.AppUtils.isSystemApp;

/**
 * 源代码: Lxw
 * 伊妹儿: China2021@126.com
 * 时间轴: 2017 年 01 月 19 日 15 : 14
 */

public class UpDateUtil {

    private static final String FILENAME = "ProtectProcess.apk";


    private static UpDateUtil instance;

    public static UpDateUtil getInstance() {
        if (instance == null) {
            synchronized (UpDateUtil.class) {
                if (instance == null)
                    instance = new UpDateUtil();
            }
        }
        return instance;
    }


    /**
     * 检测是否安装了守护程序
     *
     * @param context     上下文
     * @param packageName 包名
     * @return
     */
    public boolean checkProtectProcess(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName))
            return false;
        Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(packageName);
        return launchIntentForPackage != null;
    }


    /**
     * 启动守护程序
     *
     * @param context     上下文
     * @param packageName 包名
     */
    public void launcherProtectProcess(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return;
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.putExtra("PACKAGENAME", AppUtils.getAppPackageName(context));
        }
        context.startActivity(intent);
    }


    /**
     * 静默安装更新包
     * <uses-permission android:name="android.permission.INSTALL_PACKAGES" />
     */
    public boolean installUpDate(String filePath) {
        File file = FileUtils.getFileByPath(filePath);
        if (!FileUtils.isFileExists(file))
            return false;
        String command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + filePath;
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd(command, !isSystemApp(Utils.getContext()), true);
        return commandResult.successMsg != null && commandResult.successMsg.toLowerCase().contains("success");
    }


    /**
     * 将守护程序释放到SD卡
     *
     * @param context
     */
    public File releaseProtectProcessToSdcard(Context context) {
        AssetManager assets = context.getAssets();
        InputStream open = null;
        OutputStream outputStream = null;
        File file = null;
        try {
            open = assets.open(FILENAME);
            File absoluteFile = Environment.getExternalStorageDirectory().getAbsoluteFile();
            file = new File(absoluteFile, FILENAME);
            outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int len;
            while ((len = open.read(bytes)) > 0) {
                outputStream.write(bytes, 0, len);
            }
            open.close();
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            if (open != null)
                try {
                    open.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    CrashReport.postCatchedException(new Throwable("释放守护程序的时候关闭流出现错误!"));
                }
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    CrashReport.postCatchedException(new Throwable("释放守护程序的时候关闭流出现错误!"));
                }
            CrashReport.postCatchedException(new Throwable("释放守护程序的时候出现错误!"));
        }
        return file;
    }


    public Intent installNewApk(File file) {
        if (file == null)
            return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type;
        if (Build.VERSION.SDK_INT < 23) {
            type = "application/vnd.android.package-archive";
        } else {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtils.getFileExtension(file));
        }
        intent.setDataAndType(Uri.fromFile(file), type);
        return intent;
    }
}
