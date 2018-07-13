package io.github.dltech21.dy;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.meituan.android.walle.WalleChannelReader;
import com.ss.android.common.applog.GlobalContext;
import com.ss.android.common.applog.UserInfo;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;


public class DYApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        GlobalContext.setContext(getApplicationContext());
        loadSo();
        Utils.init(this);
        initOkgo();
        initLog();
        initBugly();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        Beta.installTinker();
    }

    public void loadSo() {
        try {
            System.loadLibrary("userinfo");
        } catch (Exception e) {
            System.out.println("load so err:" + Log.getStackTraceString(e));
        }
        UserInfo.setAppId(2);
        int result = UserInfo.initUser("a3668f0afac72ca3f6c1697d29e0e1bb1fef4ab0285319b95ac39fa42c38d05f");
        System.out.println("result:" + result);
    }

    private void initOkgo() {

        HttpHeaders headers = new HttpHeaders();
        headers.put("User-Agent", "okhttp/3.10.0");
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.readTimeout(10000, TimeUnit.MILLISECONDS);
            builder.writeTimeout(10000, TimeUnit.MILLISECONDS);
            builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
            HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
            builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("xporn");
                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
                loggingInterceptor.setColorLevel(Level.WARNING);
                builder.addInterceptor(loggingInterceptor);
                builder.addNetworkInterceptor(new StethoInterceptor());
            }
            OkGo.getInstance()
                    .init(this)
                    .setOkHttpClient(builder.build())
                    .setCacheMode(CacheMode.NO_CACHE)
                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                    .setRetryCount(0)
                    .addCommonHeaders(headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initLog() {
        final LogUtils.Config config = LogUtils.getConfig()
                .setLogSwitch(BuildConfig.DEBUG)// 设置 log 总开关，包括输出到控制台和文件，默认开
                .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
                .setGlobalTag(null)// 设置 log 全局标签，默认为空
                // 当全局标签不为空时，我们输出的 log 全部为该 tag，
                // 为空时，如果传入的 tag 为空那就显示类名，否则显示 tag
                .setLogHeadSwitch(true)// 设置 log 头信息开关，默认为开
                .setLog2FileSwitch(false)// 打印 log 时是否存到文件的开关，默认关
                .setDir("")// 当自定义路径为空时，写入应用的/cache/log/目录中
                .setFilePrefix("")// 当文件前缀为空时，默认为"util"，即写入文件为"util-MM-dd.txt"
                .setBorderSwitch(true)// 输出日志是否带边框开关，默认开
                .setSingleTagSwitch(true)// 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
                .setConsoleFilter(LogUtils.V)// log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
                .setFileFilter(LogUtils.V)// log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
                .setStackDeep(1);// log 栈深度，默认为 1
        LogUtils.d(config.toString());
    }

    private void initBugly() {
        Beta.enableHotfix = true;
        Beta.canAutoDownloadPatch = true;
        Beta.canNotifyUserRestart = false;
        Beta.canAutoPatch = true;
        Beta.canShowUpgradeActs.add(MainActivity.class);
        String channel = WalleChannelReader.getChannel(getApplicationContext());
        Bugly.setAppChannel(getApplicationContext(), channel);
        Bugly.init(getApplicationContext(), "6590822359", false);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppChannel(channel);
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            @Override
            public Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                          String errorMessage, String errorStack) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Key", "Value");
                return map;
            }

            @Override
            public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                           String errorMessage, String errorStack) {
                try {
                    return "Extra data.".getBytes("UTF-8");
                } catch (Exception e) {
                    return null;
                }
            }

        });
        CrashReport.setAppChannel(getApplicationContext(), channel);
        CrashReport.initCrashReport(getApplicationContext(), strategy);
    }
}
