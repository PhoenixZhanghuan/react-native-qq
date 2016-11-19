package cn.reactnative.modules.qq;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.connect.UserInfo;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

/**
 * Created by tdzl2_000 on 2015-10-10.
 *
 * Modified by Renguang Dong on 2016-05-25.
 */
public class QQModule extends ReactContextBaseJavaModule implements IUiListener, ActivityEventListener {
    private String appId;
    private Tencent api;
    private final static String INVOKE_FAILED = "QQ API invoke returns false.";
    private static final  String TestEventName = "TestEventName";
    private boolean isLogin;
    private Promise loginPromise;
    private Promise shareToQQPromise;
    private Promise shareToQzonePromise;
    private IUiListener loginListener;
    private IUiListener userInfoListener;
    private UserInfo userInfo;

    private static final String RCTQQShareTypeNews = "news";
    private static final String RCTQQShareTypeImage = "image";
    private static final String RCTQQShareTypeText = "text";
    private static final String RCTQQShareTypeVideo = "video";
    private static final String RCTQQShareTypeAudio = "audio";

    private static final String RCTQQShareType = "type";
    private static final String RCTQQShareText = "text";
    private static final String RCTQQShareTitle = "title";
    private static final String RCTQQShareDescription = "description";
    private static final String RCTQQShareWebpageUrl = "webpageUrl";
    private static final String RCTQQShareImageUrl = "imageUrl";

    private WritableMap params;

    private static final int SHARE_RESULT_CODE_SUCCESSFUL = 0;
    private static final int SHARE_RESULT_CODE_FAILED = 1;
    private static final int SHARE_RESULT_CODE_CANCEL = 2;

    public QQModule(ReactApplicationContext context) {
        super(context);


        this.appId = "1105711546";
    }

    @Override
    public void initialize() {
        super.initialize();

        if (api == null) {
            api = Tencent.createInstance(appId, getReactApplicationContext().getApplicationContext());
        }
        getReactApplicationContext().addActivityEventListener(this);

        loginListener = new IUiListener() {

            @Override
            public void onError(UiError arg0) {
                // TODO Auto-generated method stub

            }

            /**
             * {"ret":0,"pay_token":"D3D678728DC580FBCDE15722B72E7365",
             * "pf":"desktop_m_qq-10000144-android-2002-",
             * "query_authority_cost":448,
             * "authority_cost":-136792089,
             * "openid":"015A22DED93BD15E0E6B0DDB3E59DE2D",
             * "expires_in":7776000,
             * "pfkey":"6068ea1c4a716d4141bca0ddb3df1bb9",
             * "msg":"",
             * "access_token":"A2455F491478233529D0106D2CE6EB45",
             * "login_cost":499}
             */
            @Override
            public void onComplete(Object value) {
                // TODO Auto-generated method stub

                System.out.println("有数据返回..");
                if (value == null) {
                    return;
                }

                try {
                    JSONObject jo = (JSONObject) value;

                    int ret = jo.getInt("ret");

                    System.out.println("json=" + String.valueOf(jo));

                    System.out.println("ret=" + ret);
                    if (ret == 0) {
                        Toast.makeText(getCurrentActivity(), "登录成功",
                                Toast.LENGTH_LONG).show();

                        String openID = jo.getString("openid");
                        String accessToken = jo.getString("access_token");
                        String expires = jo.getString("expires_in");
                        api.setOpenId(openID);
                        api.setAccessToken(accessToken, expires);

                        params = Arguments.createMap();
                        params.putString("openid", openID);
                        params.putString("access_token", accessToken);


                        System.out.println("开始获取用户信息");
                        if(api.getQQToken() == null){
                            System.out.println("qqtoken == null");
                        }
                        userInfo = new UserInfo(getCurrentActivity(), api.getQQToken());
                        userInfo.getUserInfo(userInfoListener);

                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }

            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub

            }
        };

        userInfoListener = new IUiListener() {

            @Override
            public void onError(UiError arg0) {
                // TODO Auto-generated method stub

            }

            /**
             * {"is_yellow_year_vip":"0","ret":0,
             * "figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/1104732758\/015A22DED93BD15E0E6B0DDB3E59DE2D\/40",
             * "figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/1104732758\/015A22DED93BD15E0E6B0DDB3E59DE2D\/100",
             * "nickname":"攀爬←蜗牛","yellow_vip_level":"0","is_lost":0,"msg":"",
             * "city":"黄冈","
             * figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1104732758\/015A22DED93BD15E0E6B0DDB3E59DE2D\/50",
             * "vip":"0","level":"0",
             * "figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1104732758\/015A22DED93BD15E0E6B0DDB3E59DE2D\/100",
             * "province":"湖北",
             * "is_yellow_vip":"0","gender":"男",
             * "figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1104732758\/015A22DED93BD15E0E6B0DDB3E59DE2D\/30"}
             */
            @Override
            public void onComplete(Object arg0) {
                // TODO Auto-generated method stub
                if (arg0 == null) {
                    return;
                }
                try {
                    JSONObject jo = (JSONObject) arg0;
                    int ret = jo.getInt("ret");
                    System.out.println("json=" + String.valueOf(jo));
                    if (ret == 100030) {
                        //权限不够，需要增量授权
                        Runnable r = new Runnable() {
                            public void run() {
                                api.reAuth(getCurrentActivity(), "all", new IUiListener() {

                                    @Override
                                    public void onError(UiError arg0) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void onComplete(Object arg0) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void onCancel() {
                                        // TODO Auto-generated method stub

                                    }
                                });
                            }
                        };

                        getCurrentActivity().runOnUiThread(r);
                    } else {
                        String nickName = jo.getString("nickname");
                        String figureurl_qq_2 = jo.getString("figureurl_qq_2");

                        params.putString("nickName", nickName);
                        params.putString("figureurl_qq_2", figureurl_qq_2);

                        getReactApplicationContext()
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(TestEventName, params);

                        Toast.makeText(getCurrentActivity(), "你好，" + nickName, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }


            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub

            }

        };
    }

    @Override
    public void onCatalystInstanceDestroy() {

        if (api != null){
            api = null;
        }
        getReactApplicationContext().removeActivityEventListener(this);

        super.onCatalystInstanceDestroy();
    }

    @Override
    public String getName() {
        return "RCTQQAPI";
    }
    
    @ReactMethod
    public void isQQInstalled(Promise promise) {
        if (api.isSupportSSOLogin(getCurrentActivity())) {
            promise.resolve(true);
        }
        else {
            promise.reject("not installed");
        }
    }
    
    @ReactMethod
    public void isQQSupportApi(Promise promise) {
        if (api.isSupportSSOLogin(getCurrentActivity())) {
            promise.resolve(true);
        }
        else {
            promise.reject("not support");
        }
    }

    @ReactMethod
    public void logout(Promise promise) {
        api.logout(getCurrentActivity());
    }

    @ReactMethod
    public void login(String scopes, Promise promise){
        this.loginPromise = promise;
        this.isLogin = true;
        if (!api.isSessionValid()){
            api.login(getCurrentActivity(), scopes == null ? "get_simple_userinfo" : scopes, loginListener);
        } else {
            this.loginPromise.reject(INVOKE_FAILED);
            this.loginPromise = null;
        }
    }

    @ReactMethod
    public void shareToQQ(ReadableMap data, Promise promise){
        this.shareToQQPromise = promise;
        this._shareToQQ(data, 0);
    }

    @ReactMethod
    public void shareToQzone(ReadableMap data, Promise promise){
        this.shareToQzonePromise = promise;
        this._shareToQQ(data, 1);
    }

    private void _shareToQQ(ReadableMap data, int scene) {
        this.isLogin = false;
        Bundle bundle = new Bundle();
        if (data.hasKey(RCTQQShareTitle)){
            bundle.putString(QQShare.SHARE_TO_QQ_TITLE, data.getString(RCTQQShareTitle));
        }
        if (data.hasKey(RCTQQShareDescription)){
            bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, data.getString(RCTQQShareDescription));
        }
        if (data.hasKey(RCTQQShareWebpageUrl)){
            bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, data.getString(RCTQQShareWebpageUrl));
        }
        if (data.hasKey(RCTQQShareImageUrl)){
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, data.getString(RCTQQShareImageUrl));
        }
        if (data.hasKey("appName")){
            bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, data.getString("appName"));
        }

        String type = RCTQQShareTypeNews;
        if (data.hasKey(RCTQQShareType)) {
            type = data.getString(RCTQQShareType);
        }

        if (type.equals(RCTQQShareTypeNews)){
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        } else if (type.equals(RCTQQShareTypeImage)){
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, data.getString(RCTQQShareImageUrl));
        } else if (type.equals(RCTQQShareTypeAudio)) {
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
            if (data.hasKey("flashUrl")){
                bundle.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, data.getString("flashUrl"));
            }
        } else if (type.equals("app")){
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
        }

        Log.e("QQShare", bundle.toString());

        if (scene == 0 ) {
            // Share to QQ.
            bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
            api.shareToQQ(getCurrentActivity(), bundle, this);
        }
        else if (scene == 1) {
            // Share to Qzone.
            bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
            api.shareToQQ(getCurrentActivity(), bundle, this);
        }
    }

    private String _getType() {
        return (this.isLogin?"QQAuthorizeResponse":"QQShareResponse");
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, this);
    }

    public void onNewIntent(Intent intent){

    }

    @Override
    public void onComplete(Object o) {

        WritableMap resultMap = Arguments.createMap();
        resultMap.putInt("code", SHARE_RESULT_CODE_SUCCESSFUL);
        resultMap.putString("message", "Share successfully.");
        Log.d("<<<<<<<<<<<", resultMap.toString());
        this.resolvePromise(resultMap);
    }

    @Override
    public void onError(UiError uiError) {
        WritableMap resultMap = Arguments.createMap();
        resultMap.putInt("code", SHARE_RESULT_CODE_FAILED);
        resultMap.putString("message", "Share failed." + uiError.errorDetail);

        this.resolvePromise(resultMap);
    }

    @Override
    public void onCancel() {
        WritableMap resultMap = Arguments.createMap();
        resultMap.putInt("code", SHARE_RESULT_CODE_CANCEL);
        resultMap.putString("message", "Share canceled.");

        this.resolvePromise(resultMap);
    }

    private void resolvePromise(ReadableMap resultMap) {
        if (this.loginPromise != null) {
            this.loginPromise.resolve(resultMap);
            this.loginPromise = null;
        }
        if (this.shareToQQPromise != null) {
            this.shareToQQPromise.resolve(resultMap);
            this.shareToQQPromise = null;
        }
        if (this.shareToQzonePromise != null) {
            this.shareToQzonePromise.resolve(resultMap);
            this.shareToQzonePromise = null;
        }
    }
}
