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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cuneyt.gelirgider.assistantclass.DateTime;
import com.cuneyt.gelirgider.assistantclass.MobileDeviceName;
import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.assistantclass.RandomId;
import com.cuneyt.gelirgider.databinding.ActivityRegisterBinding;
import com.cuneyt.gelirgider.entities.ErrorLogModel;
import com.cuneyt.gelirgider.entities.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
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
    private DatabaseReference referenceUser, referenceError;
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private DateTime dateTime = new DateTime();
    MobileDeviceName deviceName = new MobileDeviceName();
    private MonthYear monthYear = new MonthYear();
    private RandomId randomId = new RandomId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        internetCheck();

        try {
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
        } catch (Exception e){

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime("dd.MM.yyyy HH:mm:ss");

            errorLogModel = new ErrorLogModel(mobileDevName, "RegisterActivity", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }

    }

    private void registerUser(){

        String date = monthYear.currentlyDateTime("dd.MM.yyyy HH:mm:ss").toString();
        String inputUserName = regBinding.editRegName.getText().toString() + "@gg.com";
        String inputPass = regBinding.editRegPassword.getText().toString();

        userModel.setName(inputUserName);
        userModel.setPassword(inputPass);
        userModel.setMemberDateTime(date);

        checkUserName(inputUserName); // Aynı isimli kullanıcı var mı yok mu kontrol edildi.

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

                            /*referenceUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot d : snapshot.getChildren()){
                                        UserModel user = d.getValue(UserModel.class);
                                        String userName = user.getName().toString();

                                        if (d.equals(null)){



                                        } else if (regBinding.editRegName.equals(userName)){

                                            Toast.makeText(RegisterActivity.this, "Böyle bir kullanıcı var.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });*/

                        } else {

                           // Toast.makeText(RegisterActivity.this, "Kayıt başarısız. Böyle bir kullanıcı mevcut.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserName(String againName){
        firebaseAuth.fetchSignInMethodsForEmail(againName).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()){

                    boolean check = !task.getResult().getSignInMethods().isEmpty();

                    if (!check){

                   //     Toast.makeText(RegisterActivity.this, "Başarılı", Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(RegisterActivity.this, "Böyle bir kullanıcı mevcut.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {
            AlertDialog.Builder builderNet = new AlertDialog.Builder(RegisterActivity.this);
            //  builderNet.setMessage("İnternet bağlantınızı kontrol edin.");
            builderNet.setCancelable(true);
            View viewClose = getLayoutInflater().inflate(R.layout.alert_internet_check, null);

            TextView textBtAlertYes = viewClose.findViewById(R.id.textBtAlertNetYes);

            builderNet.setView(viewClose);

            AlertDialog alertDialog = builderNet.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            textBtAlertYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            alertDialog.show();
        }
    }
}