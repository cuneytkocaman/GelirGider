package com.cuneyt.gelirgider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.assistantclass.RandomId;
import com.cuneyt.gelirgider.databinding.ActivityRegisterBinding;
import com.cuneyt.gelirgider.entities.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding regBinding;
    private UserModel userModel = new UserModel();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceUser;

    private MonthYear monthYear = new MonthYear();
    private RandomId randomId = new RandomId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

       // internetCheck();

        regBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        regBinding.textBtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String regName = regBinding.editRegName.getText().toString();
                String pass = regBinding.editRegPassword.getText().toString();
                String passAgain = regBinding.editRegPasswordAgain.getText().toString();

                int passLenght = pass.length();
                int regNameLehght = regName.length(); // İsmin ve parolanın uzunlukları alındı.

                if (TextUtils.isEmpty(regBinding.editRegName.getText().toString()) ||
                        TextUtils.isEmpty(regBinding.editRegPassword.getText().toString()) ||
                        TextUtils.isEmpty(regBinding.editRegPasswordAgain.getText().toString())) {

                    Toast.makeText(RegisterActivity.this, "Alanlar boş geçilemez.", Toast.LENGTH_SHORT).show();

                } else if (passLenght < 6){

                    Toast.makeText(RegisterActivity.this, "Parola en az 6 karakter olmalıdır.", Toast.LENGTH_SHORT).show();

                } else if (!pass.equals(passAgain)){

                    Toast.makeText(RegisterActivity.this, "Parolalar aynı değil.", Toast.LENGTH_SHORT).show();

                } else if (regNameLehght <= 2){

                    Toast.makeText(RegisterActivity.this, "Kullanıcı adı 3 karakterden uzun olmalıdır.", Toast.LENGTH_SHORT).show();

                } else {

                    registerUser();
                }
            }
        });
    }

    private void registerUser(){

        String date = monthYear.currentlyDateTime("dd.MM.yyyy HH:mm:ss").toString();
        String inputUserName = regBinding.editRegName.getText().toString() + "@gg.com";
        String inputPass = regBinding.editRegPassword.getText().toString();

        userModel.setName(inputUserName);
        userModel.setPassword(inputPass);
        userModel.setMemberDateTime(date);

        firebaseAuth.createUserWithEmailAndPassword(userModel.getName(), userModel.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // addCompleteListener: İşlemin durumu hakkında bilgi verir.
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        
                        if (task.isSuccessful()){ // Kayıt başarılı ise çalışır ve kullanıcı kaydı tamamlanır.

                            firebaseUser = firebaseAuth.getCurrentUser();
                            referenceUser = FirebaseDatabase.getInstance().getReference().child("User");

                            referenceUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String userName = regBinding.editRegName.getText().toString();
                                    String id = firebaseUser.getUid().toString();
                                    String member = userModel.getMemberDateTime().toString();

                                    userModel = new UserModel(id, userName, member);

                                    referenceUser.child(id).setValue(userModel); // Kullanıcı DB'ye eklendi.

                                    Intent intentLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intentLogin);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            Toast.makeText(RegisterActivity.this, "Kayıt başarılı.", Toast.LENGTH_LONG).show();

                        } else {

                            Toast.makeText(RegisterActivity.this, "Kayıt başarısız.", Toast.LENGTH_LONG).show();
                            
                        }
                    }
                });
    }

   /* private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {

            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RegisterActivity.this);
            builder.setMessage("İnternet bağlantınızı kontrol edin.");
            builder.setCancelable(true);

            AlertDialog.Builder builderClose = new AlertDialog.Builder(RegisterActivity.this);
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