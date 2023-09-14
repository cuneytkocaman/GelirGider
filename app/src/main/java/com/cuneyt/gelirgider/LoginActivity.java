package com.cuneyt.gelirgider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.gelirgider.databinding.ActivityLoginBinding;
import com.cuneyt.gelirgider.entities.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    private UserModel userModel = new UserModel();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

     //   internetCheck();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        loginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        loginBinding.textBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        loginBinding.textBtRegisterPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });
    }

    public void loginUser(){
        String inputUser = loginBinding.editLoginName.getText().toString() + "@gg.com";
        String inputPass = loginBinding.editLoginPassword.getText().toString();

        userModel.setName(inputUser);
        userModel.setPassword(inputPass);

        if (!TextUtils.isEmpty(inputUser) && !TextUtils.isEmpty(inputPass)){

            firebaseAuth.signInWithEmailAndPassword(userModel.getName(), userModel.getPassword())
                    .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intentMain);
                            finish();

                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Kullanıcı adı veya parola hatalı.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Kullancı adı veya Parola boş geçilemez.", Toast.LENGTH_LONG).show();
        }
    }

    public void onStart() {

        if (firebaseUser != null) {

            Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }

        super.onStart();
    }

    /*private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {

            *//*final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("İnternet bağlantınızı kontrol edin.");
            builder.setCancelable(true);*//*

            AlertDialog.Builder builderClose = new AlertDialog.Builder(LoginActivity.this);
            View viewClose = getLayoutInflater().inflate(R.layout.alert_internet_check, null);

            ImageView imgBtAlertClose = viewClose.findViewById(R.id.imgBtAlertClose);

            builderClose.setView(viewClose);

            AlertDialog alertDialog = builderClose.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            imgBtAlertClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            *//*builder.setPositiveButton("Çıkış", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish(); //Uygulamayı sonlandırıldı.
                }
            });

            android.app.AlertDialog alertDialog = builder.create();*//*

            alertDialog.show();
        }
    }*/
}

