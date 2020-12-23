package com.ls.videoapp.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ls.libnetwork.ApiResponse;
import com.ls.libnetwork.ApiService;
import com.ls.libnetwork.JsonCallback;
import com.ls.videoapp.R;
import com.ls.videoapp.model.User;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private View mActionClose;
    private View mActionLogin;
    private Tencent mTencent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mActionClose = findViewById(R.id.action_close);
        mActionLogin = findViewById(R.id.action_login);

        mActionLogin.setOnClickListener(this);
        mActionClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_close){
            finish();
        }else if (v.getId() == R.id.action_login){
            login();
        }
    }

    private void login() {
        if (mTencent == null){
            mTencent = Tencent.createInstance("101921737", getApplicationContext());
        }
        mTencent.login(this, "all", new IUiListener() {
            @Override
            public void onComplete(Object o) {
                //登录成功
                JSONObject response = (JSONObject) o;
                try {
                    //取出登录后的信息
                    String openid = response.getString("openid");
                    String access_token = response.getString("access_token");
                    String expires_in = response.getString("expires_in");
                    long expires_time = response.getLong("expires_time");

                    mTencent.setAccessToken(access_token,expires_in);
                    mTencent.setOpenId(openid);
                    //通过qqtoken去获取用户信息
                    QQToken qqToken = mTencent.getQQToken();
                    getUserInfo(qqToken,expires_time,openid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(getApplicationContext(),"登录失败:reason"+uiError.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"登录取消",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWarning(int i) {

            }
        });
    }

    private void getUserInfo(QQToken qqToken, long expires_time, String openid) {
        UserInfo userInfo = new UserInfo(getApplicationContext(), qqToken);
        userInfo.getUserInfo(new IUiListener() {
            @Override
            public void onComplete(Object o) {
                JSONObject response = (JSONObject)o;

                //获取用户信息
                try {
                    String nickname = response.getString("nickname");
                    String figureurl_2 = response.getString("figureurl_2");

                    //将当前登录者的身份提交到自己的后台，由后台来保存用户的信息
                    save(nickname,figureurl_2,openid,expires_time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(getApplicationContext(),"登录失败:reason"+uiError.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"登录取消",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWarning(int i) {

            }
        });
    }

    private void save(String nickname, String avatar, String openid, long expires_time) {
        ApiService.get("/user/insert")
                .addParam("name",nickname)
                .addParam("avatar",avatar)
                .addParam("qqOpenId",openid)
                .addParam("expires_time",expires_time)
                .execute(new JsonCallback<User>(){
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        //把用户登录的信息，持久化存储起来，但是考虑到多个地方会调用登录，所以需要一个UserManager进行统一的存储，方便回传给调用方
                        if (response.body != null){
                            UserManager.get().save(response.body);
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"登录失败,msg:"+response.message,Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(ApiResponse<User> response) {
                        super.onError(response);
                    }

                    @Override
                    public void onCacheSuccess(ApiResponse<User> response) {
                        super.onCacheSuccess(response);
                    }
                });
    }

}
