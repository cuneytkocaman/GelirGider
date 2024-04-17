package com.cuneyt.gelirgider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.gelirgider.adapter.LoanRvAdapter;
import com.cuneyt.gelirgider.assistantclass.DateTime;
import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.assistantclass.RandomId;
import com.cuneyt.gelirgider.databinding.ActivityLoanBinding;
import com.cuneyt.gelirgider.entities.EarningSpendingModel;
import com.cuneyt.gelirgider.entities.LoanModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class LoanActivity extends AppCompatActivity {
    private ActivityLoanBinding loanBinding;
    private LoanModel loanModel;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceLoan, referenceUser;
    private LoanRvAdapter loanRvAdapter;
    private ArrayList<LoanModel> loanLists = new ArrayList<>();
    private RandomId randomId = new RandomId();
    private DateTime dateTime = new DateTime();
    private MonthYear monthYear = new MonthYear();
    private String month = monthYear.currentlyDateTime("MMMM"); // Geçerli ay değişkene atandı.
    private String year = monthYear.currentlyDateTime("yyyy"); // Geçerli yıl değişkene atandı.
    private Animation uptodown, downtoup, alpha;

    private void anim(){
        uptodown = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_up_to_down);
        downtoup = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_down_to_up);
        alpha = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_alpha);

        loanBinding.constList.setAnimation(uptodown);
        loanBinding.constBottomBar.setAnimation(downtoup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);

        loanBinding = DataBindingUtil.setContentView(this, R.layout.activity_loan);

        anim();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user));

        loanBinding.imgBtAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLoanCredit();
            }
        });

        loanBinding.imgBtLoanNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addLoanNote();
            }
        });

        loanBinding.imgBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        show();
    }

    public void addLoanCredit() {
        AlertDialog.Builder builderPerson = new AlertDialog.Builder(LoanActivity.this);
        View viewAdd = getLayoutInflater().inflate(R.layout.alert_loan_person, null);

        EditText editLoanPerson = viewAdd.findViewById(R.id.editLoanPerson);
        EditText editLoanCreditTitle = viewAdd.findViewById(R.id.editLoanCreditTitle);
        EditText editLoanCreditAmounth = viewAdd.findViewById(R.id.editLoanCreditAmounth);
        TextView textAlertYes = viewAdd.findViewById(R.id.textAlertYes);
        TextView textBtClose = viewAdd.findViewById(R.id.textBtClose);
        RadioGroup radioGroupLoanCredit = viewAdd.findViewById(R.id.radioGroupLoanCredit);

        builderPerson.setView(viewAdd);

        AlertDialog dialogPerson = builderPerson.create();
        dialogPerson.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogPerson.show();

        referenceLoan = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_loan));

        textAlertYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editLoanPerson.getText().toString())) {
                    Toast.makeText(LoanActivity.this, "Kişi adını giriniz.", Toast.LENGTH_SHORT).show();

                } else {
                    String ranId = randomId.randomUUID(); // Her kök için ID üretildi. Kontrolü kolaylaştırmak için ID oluşturuldu. ID aynı zamanda düğüm olarak aktarıldı.

                    radioGroupLoanCredit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            RadioButton radioButton = radioGroup.findViewById(i);
                        }
                    });

                    referenceLoan.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            int radioSelectId = radioGroupLoanCredit.getCheckedRadioButtonId();

                            if (radioSelectId == -1) { // Radio Button'lardan seçim yapılmadıysa uyarıldı.

                                Toast.makeText(LoanActivity.this, "Seçim yapılmadı.", Toast.LENGTH_SHORT).show();

                            } else {

                                RadioButton radioButtonLoanCredit = radioGroupLoanCredit.findViewById(radioSelectId);

                                String currentUserId = firebaseUser.getUid().toString();
                                String person = editLoanPerson.getText().toString();
                                String loan = editLoanCreditTitle.getText().toString();
                                int amounth = Integer.parseInt(editLoanCreditAmounth.getText().toString());
                                String loanType = radioButtonLoanCredit.getText().toString();
                                String date = monthYear.currentlyDateTime("dd.MM.yyyy");
                                String sort = person + loan;

                                Spannable spTitle = new SpannableString(person); // String parçalama sınıfı.
                                String firstLetterTitle = spTitle.subSequence(0, 1).toString(); // Her kök ID'sinin başına eklenmesi için başlığın 1. harfi alındı. Bu işlem Firebase'de okumayı kolaylaştırmak için yapıldı.

                                String uniqeId = firstLetterTitle + "-" + ranId; // Her kök için benzersiz ID oluşturuldu.

                                loanModel = new LoanModel(uniqeId, person, loan, amounth, loanType, date, sort);

                                referenceLoan.child(currentUserId).child(uniqeId).setValue(loanModel); // Firebase'e eklendi.
                            }

                            dialogPerson.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        textBtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPerson.dismiss();
            }
        });
    }

    public void show() {
        loanBinding.rvLoan.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, true); // VERTICAL, true: RecyclerView'e eklenen veri en alttan üste doğru eklenir.
        linearLayoutManager.setStackFromEnd(true); // RecyclerView'e eklenen veri sayfayı otomatik kaydırır.
        loanBinding.rvLoan.setLayoutManager(linearLayoutManager);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInflater = inflater.inflate(R.layout.design_row_loan, null); // Tasarım bağlandı.

        loanRvAdapter = new LoanRvAdapter(this, loanLists);
        loanBinding.rvLoan.setAdapter(loanRvAdapter);

        String userId = firebaseUser.getUid().toString(); // Online kullanıcı ID'si alındı. Her kullanıcının kendi tablosu, kendi ID'si altında görüntülendi.

        referenceLoan = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_loan)).child(userId);
        referenceLoan.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                loanLists.clear();

                for (DataSnapshot d : snapshot.getChildren()) {

                    LoanModel loanM = d.getValue(LoanModel.class);
                    loanLists.add(loanM);
                }

                Collections.sort(loanLists, new Comparator<LoanModel>() { //RecyclerView A->Z sıralama
                    @Override
                    public int compare(LoanModel loanModel, LoanModel t1) {
                        return t1.getSort().compareToIgnoreCase(loanModel.getSort());
                        // return Integer.compare(t1.getNumber(), ggModel.getNumber());
                    }
                });

                loanRvAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void addLoanNote() { // Not ekleme ve görüntüleme
        AlertDialog.Builder builderLoanNote = new AlertDialog.Builder(LoanActivity.this);
        View viewNote = getLayoutInflater().inflate(R.layout.alert_loan_note, null);

        EditText editLoanCreditNote = viewNote.findViewById(R.id.editLoanCreditNote);
        TextView textNoteBtYes = viewNote.findViewById(R.id.textNoteBtYes);
        TextView textNoteBtNo = viewNote.findViewById(R.id.textNoteBtNo);

        builderLoanNote.setView(viewNote);

        AlertDialog dialogNote = builderLoanNote.create();
        dialogNote.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogNote.show();

        String noteId = "Note"; // Devamlı güncelleme yapılacağı için sabit bir ID oluşturuldu.
        String currentUser = firebaseUser.getUid().toString(); // Online kullanıcı ID'si alındı. Her kullanıcının kendi notu, kendi ID'si altında görüntülendi.

        referenceLoan = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_loan_note)).child(currentUser);
        referenceLoan.addValueEventListener(new ValueEventListener() { // Not görüntüleme
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot d : snapshot.getChildren()) {

                    if (d.equals(null)) { // Not henüz girilmemişse EditText'te herhangi bir bilgi gösterilmedi.

                    } else {
                        LoanModel lModel = d.getValue(LoanModel.class);
                        String note = lModel.getNote().toString(); // Modelden not bilgisi alındı.

                        editLoanCreditNote.setText(note); // DB'den gelen not bilgisi Alert dialog'daki Text'e atandı
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceLoan = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_loan_note));
        textNoteBtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String note = editLoanCreditNote.getText().toString();
                String updDate = dateTime.currentlyDateTime("dd.MM.yyyy");

                loanModel = new LoanModel(noteId, note, updDate);

                if (TextUtils.isEmpty(editLoanCreditNote.getText().toString())) {
                    Toast.makeText(LoanActivity.this, "Not bilgisi giriniz.", Toast.LENGTH_SHORT).show();

                } else {
                    referenceUser.addValueEventListener(new ValueEventListener() { // Not bilgisi devamlı güncelleneceği için ekleme değil güncelleme işlemi yaptırıldı.
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //    String currentUserId = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString();
                            String currentUserId = firebaseUser.getUid().toString();

                            HashMap<String, Object> updateNote = new HashMap<>();
                            updateNote.put("id", noteId);
                            updateNote.put("note", note);
                            updateNote.put("updateDate", updDate);

                            referenceLoan.child(currentUserId).child(noteId).updateChildren(updateNote);

                            dialogNote.dismiss();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                dialogNote.show();
            }
        });

        textNoteBtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNote.dismiss();
            }
        });
    }
}