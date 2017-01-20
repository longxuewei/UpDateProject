package cn.lxw.updateproject;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String PROTECT_PROCESS_PACKAGE_NAME = "cn.lxw.protectprocess";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpDateUtil instance = UpDateUtil.getInstance();
        boolean b = instance.checkProtectProcess(this, PROTECT_PROCESS_PACKAGE_NAME);
        if (!b) {
            File file = instance.releaseProtectProcessToSdcard(this);
            if (file.exists()) {
                boolean b1 = instance.installUpDate(file.getAbsolutePath());
                if (b1) {
                    installUpdatePackage(instance);
                } else {
                    Toast.makeText(this, "守护程安装失败", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "守护程序释放失败", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "守护程序已存在", Toast.LENGTH_SHORT).show();
            installUpdatePackage(instance);
        }
        Log.i(TAG, "更新");
    }

    private void installUpdatePackage(UpDateUtil instance) {
        instance.launcherProtectProcess(this, PROTECT_PROCESS_PACKAGE_NAME);
        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File upDate = new File(absolutePath, "update.apk");
        installUpdate(upDate);
    }


    private void installUpdate(File file) {
        UpDateUtil instance = UpDateUtil.getInstance();
        boolean b = instance.installUpDate(file.getAbsolutePath());
        Toast.makeText(this, b + "", Toast.LENGTH_LONG).show();
    }

    private void testInstallUpdate(File file) {
        String cmd = "pm install -r " + file.getAbsolutePath();
        DataOutputStream os = null;
        try {
            //静默安装需要root权限
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();
            //执行命令
            process.waitFor();
        } catch (Exception e) {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
}
