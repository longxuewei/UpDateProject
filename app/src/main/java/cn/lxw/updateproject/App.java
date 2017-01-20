package cn.lxw.updateproject;

import android.app.Application;

import com.blankj.utilcode.utils.Utils;

/**
 * 源代码: Lxw
 * 伊妹儿: China2021@126.com
 * 时间轴: 2017 年 01 月 19 日 15 : 29
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
