package io.github.dltech21.dy;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ss.android.common.applog.UserInfo;

import java.util.HashMap;
import java.util.Map;

import io.github.dltech21.dlapp.network.RequestCallBack;
import io.github.dltech21.dlapp.network.ResponseContent;
import io.github.dltech21.dy.model.RecommandBean;

public class RecommandAPI {

    public static void fetchDy(final Context context, final int cursor, final RequestCallBack callback) {
        String url = "https://aweme.snssdk.com/aweme/v1/feed/?" + douyinurl(cursor);
        OkGo.<String>get(url)
                .tag(context)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        RecommandBean data = JSON.parseObject(response.body(), RecommandBean.class);
                        if (callback!= null) {
                            ResponseContent responseContent = new ResponseContent();
                            responseContent.setCode(data.getStatus_code());
                            responseContent.setContent(response.body());
                            callback.onSuccess(responseContent);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        if (callback!= null) {
                            callback.onFail(response.message());
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        if (callback!= null) {
                            callback.onAfter();
                        }
                    }

                });
    }

    private static String douyinurl(int cursor) {

        HashMap<String, String>
                paramsMap = new HashMap<String, String>();
        paramsMap.put("iid", Constants.IID);
        paramsMap.put("channel", Constants.CHANNEL);//下载渠道
        paramsMap.put("aid", Constants.AID);
        paramsMap.put("uuid", Constants.UUID); //设备唯一号 需要运行时权限
        paramsMap.put("openudid", Constants.OPEN_UDID); //更账户绑定
        paramsMap.put("app_name", Constants.APP_NAME); //应用名称
        paramsMap.put("version_code", Constants.V_CODE);//版本号
        paramsMap.put("version_name", Constants.V_NAME);//版本名称
        paramsMap.put("ssmix", "a");
        paramsMap.put("manifest_version_code", Constants.V_CODE);//版本号
        paramsMap.put("device_type", DeviceUtils.getModel());//设备类型
        paramsMap.put("device_brand", DeviceUtils.getManufacturer());//手机品牌
        paramsMap.put("os_api", DeviceUtils.getSDKVersionName());//SDK 版本号
        paramsMap.put("os_version", DeviceUtils.getSDKVersionName());//手机系统版本号
        paramsMap.put("resolution", ScreenUtils.getScreenWidth() + "*" + ScreenUtils.getScreenHeight());//分辨率
        paramsMap.put("dpi", ScreenUtils.getScreenDensityDpi() + "");//屏幕密度
        paramsMap.put("device_id", Constants.DEVICE_ID);//设备ID
        paramsMap.put("ac", "wifi");//网络类型
        paramsMap.put("device_platform", "android");//平台
        paramsMap.put("update_version_code", "1592");//更新版本号
        paramsMap.put("app_type", "normal");//应用类型
        paramsMap.put("max_cursor", "0");//应用类型
        paramsMap.put("min_cursor", cursor+"");//应用类型

        int time = (int) (System.currentTimeMillis() / 1000);

        String[] paramsAry = new String[paramsMap.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            paramsAry[i] = entry.getKey();
            i++;
            paramsAry[i] = entry.getValue();
            i++;
        }

        // 添加时间戳
        paramsMap.put("retry_type", "no_retry");
        paramsMap.put("ts", "" + time);//时间戳

        StringBuilder paramsSb = new StringBuilder();

        for (String key : paramsMap.keySet()) {
            paramsSb.append(key + "=" + paramsMap.get(key) + "&");
        }

        String urlStr = paramsSb.toString();

        if (urlStr.endsWith("&")) {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
        }

        String as_cp = UserInfo.getUserInfo(time, urlStr, paramsAry);

        String asStr = as_cp.substring(0, as_cp.length() / 2);
        String cpStr = as_cp.substring(as_cp.length() / 2, as_cp.length());

        String url = urlStr + "&as=" + asStr + "&cp=" + cpStr;
        return url;
    }

}
