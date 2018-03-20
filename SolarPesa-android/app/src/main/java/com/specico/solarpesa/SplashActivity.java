package com.specico.solarpesa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.specico.solarpesa.Utils.Constants;
import com.specico.solarpesa.Utils.SharedPrefUtil;

public class SplashActivity extends AppCompatActivity {

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean login_status = SharedPrefUtil.getBooleanPreference(SplashActivity.this, Constants.LOGIN_STATUS);

                if(login_status)
                {
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finishAffinity();
                }
                else {
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finishAffinity();
                }

            }
        },2000);
    }
}
