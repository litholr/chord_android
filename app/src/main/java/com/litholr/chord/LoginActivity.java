package com.litholr.chord;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

public class LoginActivity extends AppCompatActivity {
    String TAG = "LoginActivity";

    private final SessionCallback sessionCallback = new SessionCallback();
    Session session;
    private Button btn_custom_login;
    private Button btn_custom_login_out;
    private Button btnBack;

    private String login_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_custom_login = (Button) findViewById(R.id.btn_custom_login);
        btn_custom_login_out = (Button) findViewById(R.id.btn_custom_login_out);
        btnBack = (Button) findViewById(R.id.btnBack);
        try {
            session = Session.getCurrentSession();
            session.addCallback(sessionCallback);
        } catch (IllegalStateException | NullPointerException e) {
            Log.e(TAG, e.toString());
        }


        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);

            }
        });

        btn_custom_login_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                UserManagement.getInstance()
                        .requestLogout(new LogoutResponseCallback() {

                            @Override
                            public void onCompleteLogout() {
                                Toast.makeText(LoginActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = login_nickname;
                Intent intent = new Intent();
                intent.putExtra("nickname", data);
                setResult(0, intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ?????? ?????? ??????
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // ????????????|????????? ??????????????? ?????? ????????? ????????? SDK??? ??????
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            requestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
        }

        private void requestMe() {
            UserManagement.getInstance()
                    .me(new MeV2ResponseCallback() {
                        @Override
                        public void onSessionClosed(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "????????? ?????? ??????: " + errorResult);
                        }

                        @Override
                        public void onFailure(ErrorResult errorResult) {
                            Log.e("KAKAO_API", "????????? ?????? ?????? ??????: " + errorResult);
                        }

                        @Override
                        public void onSuccess(MeV2Response result) {
                            Log.i("KAKAO_API", "????????? ?????????: " + result.getId());

                            UserAccount kakaoAccount = result.getKakaoAccount();
                            if (kakaoAccount != null) {

                                // ?????????
                                String email = kakaoAccount.getEmail();

                                if (email != null) {
                                    Log.i("KAKAO_API", "email: " + email);

                                } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // ?????? ?????? ??? ????????? ?????? ??????
                                    // ???, ?????? ????????? ???????????? ????????? ????????? ?????? ???????????? ????????? ????????? ????????? ???????????? ???????????? ?????????.

                                } else {
                                    // ????????? ?????? ??????
                                }

                                // ?????????
                                Profile profile = kakaoAccount.getProfile();

                                if (profile != null) {
                                    Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                    Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                    Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());
                                    Toast.makeText(LoginActivity.this, profile.getNickname() + "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                    login_nickname = profile.getNickname();

                                } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                    // ?????? ?????? ??? ????????? ?????? ?????? ??????

                                } else {
                                    // ????????? ?????? ??????
                                }
                            }
                        }
                    });

        }
    }
}