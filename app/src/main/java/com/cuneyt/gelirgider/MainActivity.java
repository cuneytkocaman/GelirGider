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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.gelirgider.adapter.TabloRvAdapter;
import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.assistantclass.RandomId;
import com.cuneyt.gelirgider.databinding.ActivityMainBinding;
import com.cuneyt.gelirgider.entities.EarningSpendingModel;
import com.cuneyt.gelirgider.entities.RemainingModel;
import com.google.android.material.textfield.TextInputEditText;
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

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mainBinding;
    private EarningSpendingModel earningSpendingModel;
    private DatabaseReference referenceTablo, referenceUser, referenceNote;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private TabloRvAdapter tabloRvAdapter;
    private ArrayList<EarningSpendingModel> ggLists = new ArrayList<>();
    private MonthYear monthYear = new MonthYear();
    private RandomId randomId = new RandomId();

    private String month = monthYear.currentlyDateTime("MMMM"); // Geçerli ay değişkene atandı.
    private String year = monthYear.currentlyDateTime("yyyy"); // Geçerli yıl değişkene atandı.

    private int earningTotal = 0; // Gelir toplamları için değişken oluşturuldu.
    private int spendingTotal = 0; // Gider toplamları için değişken oluşturuldu.
    private int salaryTotal = 0; // Maaş toplamları için değişken oluşturuldu.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); // Oturum açmış kullanıcı alındı.
        referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user)); // Kullanıcı tablosu oluşturuldu.

        // mainBinding.textMonth.setText(month); // Geçerli ay editText'te gösterildi.
        mainBinding.textYear.setText(year); // Geçerli yıl editText'te gösterildi.

        show(); // Kayıtlar gösterildi.

        mainBinding.imgBtAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });

        mainBinding.textMonth.setOnClickListener(new View.OnClickListener() { // Ay seçme işlemi
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderMonth = new AlertDialog.Builder(MainActivity.this);
                View viewMonth = getLayoutInflater().inflate(R.layout.alert_month_choice, null);

                TextView textBtYes = viewMonth.findViewById(R.id.textBtYes);
                TextView textBtNo = viewMonth.findViewById(R.id.textBtNo);
                RadioGroup radioGroupMonth = viewMonth.findViewById(R.id.radioGroupMonth);

                builderMonth.setView(viewMonth);

                AlertDialog dialogMonth = builderMonth.create();
                dialogMonth.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialogMonth.show();

                radioGroupMonth.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton rbMonth = group.findViewById(checkedId);
                    }
                });

                textBtYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //  showRemaining();

                        int radioSelectId = radioGroupMonth.getCheckedRadioButtonId();

                        if (radioSelectId == -1) {
                            Toast.makeText(MainActivity.this, "Seçim yapılmadı.", Toast.LENGTH_SHORT).show();

                        } else {
                            mainBinding.textTotalSalary.setVisibility(View.VISIBLE);
                            mainBinding.constTotal.setVisibility(View.VISIBLE);
                            mainBinding.imgBtAdd.setVisibility(View.VISIBLE);
                            mainBinding.imgBtNote.setVisibility(View.VISIBLE);

                            RadioButton radioButtonMonth = radioGroupMonth.findViewById(radioSelectId);

                            mainBinding.textMonth.setText(radioButtonMonth.getText().toString());

                            show();

                            mainBinding.textTotalSalary.setText("0");
                            mainBinding.textTotalSpending.setText("0");
                            mainBinding.textRemaining.setText("0");

                            dialogMonth.dismiss();
                        }
                    }
                });

                textBtNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogMonth.dismiss();
                    }
                });


            }
        });

        mainBinding.imgBtNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteAdd();
            }
        });

        mainBinding.imgBtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    public void add() {

        String txtMonth = mainBinding.textMonth.getText().toString(); // TextView'deki ay adı değişkene atandı. Metod tetiklenince ilk olarak ay adını görüp, son ekleme yapılan ay ile karıştırmaması için en tepeye yazıldı.

        AlertDialog.Builder builderAdd = new AlertDialog.Builder(MainActivity.this); // Alert Dialog MainActivity üzerinde oluşturuldu.
        View viewAdd = getLayoutInflater().inflate(R.layout.alert_add_update, null); // Açılması istenen alert dialog tasarımı bağlandı.

        EditText editAmounth = viewAdd.findViewById(R.id.editAmounth); // Alert dialog görsel objeleri
        EditText editTitle = viewAdd.findViewById(R.id.editTitle);
        RadioGroup radioGroupAdd = viewAdd.findViewById(R.id.radioGroupAdd);
        TextView textBtYes = viewAdd.findViewById(R.id.textBtYes);
        TextView textBtNo = viewAdd.findViewById(R.id.textBtNo);

        builderAdd.setView(viewAdd);

        AlertDialog dialogAdd = builderAdd.create();
        dialogAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        referenceTablo = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_table)); // Table isimli tablo oluşturuldu.

        textBtYes.setText("Ekle"); // Ekle ve güncelleme işlemleri için tek Alert Dialog tasarımı yapıldığı için, ekleme butonu tıklayınca Alert Dialog'daki onay butonuna 'Ekle' ifadesi yazdırıldı.

        textBtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(editTitle.getText().toString()) ||
                        TextUtils.isEmpty(editAmounth.getText().toString())) { // Başlık ve Tutar bilgilerinin boş geçilmemesi sağlandı.

                    Toast.makeText(MainActivity.this, "Bilgileri giriniz.", Toast.LENGTH_SHORT).show();

                } else {

                    /*Intent intentRv = new Intent(MainActivity.this, TabloRvAdapter.class);
                    String sMonth = mainBinding.textMonth.toString();
                    intentRv.putExtra("m", sMonth);
                    startActivity(intentRv);*/

                    String ranId = randomId.randomUUID(); // Her kök için ID üretildi. Kontrolü kolaylaştırmak için ID oluşturuldu. ID aynı zamanda düğüm olarak aktarıldı.

                    radioGroupAdd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            RadioButton radioButton = group.findViewById(checkedId);
                        }
                    });

                    referenceTablo.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            int radioSelectId = radioGroupAdd.getCheckedRadioButtonId();

                            if (radioSelectId == -1) { // Radio Button'lardan seçim yapılmadıysa uyarıldı.

                                Toast.makeText(MainActivity.this, "Seçim yapılmadı.", Toast.LENGTH_SHORT).show();

                            } else {
                                RadioButton radioButtonAdd = radioGroupAdd.findViewById(radioSelectId);

                                String currentUserId = firebaseUser.getUid().toString();
                                String title = editTitle.getText().toString();
                                String amounth = editAmounth.getText().toString();
                                String type = radioButtonAdd.getText().toString();

                                Integer intAmaounth = Integer.parseInt(amounth); // EarningSpendingModel sınıfındaki değişken integer olduğundan editAmounth ile gelen veri integer'a çevrildi.

                                Spannable spTitle = new SpannableString(title); // String parçalama sınıfı.
                                String firstLetterTitle = spTitle.subSequence(0, 1).toString(); // Her kök ID'sinin başına eklenmesi için başlığın 1. harfi alındı. Bu işlem Firebase'de okumayı kolaylaştırmak için yapıldı.

                                String uniqueId = firstLetterTitle + "-" + ranId; // Her kök için benzersiz ID oluşturuldu.

                                if (type.equals("Maaş")) { // Recycler View'de sıralama yapmak için her gelir ve gider türüne numara verildi.
                                    int number = 1;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, year, txtMonth);

                                } else if (type.equals("Gelir")) {
                                    int number = 2;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, year, txtMonth);

                                } else if (type.equals("Birikim")) {
                                    int number = 3;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, year, txtMonth);

                                } else if (type.equals("Gider")) {
                                    int number = 4;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, year, txtMonth);

                                }

                                referenceTablo.child(currentUserId).child(year).child(txtMonth).child(uniqueId).setValue(earningSpendingModel); // Veri Firebase'e eklendi.

                            }
                            dialogAdd.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        textBtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAdd.dismiss();
            }
        });

        dialogAdd.show();
    }
    public void show() {
        mainBinding.rvTable.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, true); // VERTICAL, true: RecyclerView'e eklenen veri en alttan üste doğru eklenir.
        linearLayoutManager.setStackFromEnd(true); // RecyclerView'e eklenen veri sayfayı otomatik kaydırır.
        mainBinding.rvTable.setLayoutManager(linearLayoutManager);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInflater = inflater.inflate(R.layout.design_row_salary, null); // Tasarım bağlandı.

        tabloRvAdapter = new TabloRvAdapter(this, ggLists);
        mainBinding.rvTable.setAdapter(tabloRvAdapter);

        String txtMonth = mainBinding.textMonth.getText().toString(); // Seçilen ay değişkene atandı.
        String userId = firebaseUser.getUid().toString(); // Online kullanıcı ID'si alındı. Her kullanıcının kendi tablosu, kendi ID'si altında görüntülendi.
        referenceTablo = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_table)).child(userId).child(year).child(txtMonth); // Seçilen ayın verisi listelendi.

        referenceTablo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ggLists.clear();

                int totalSalary = 0, totalSpending = 0, totalEarning = 0; //Gelir ve giderin toplanması için başlangıç değişkenine 0 verildi.

                for (DataSnapshot d : snapshot.getChildren()) {

                    EarningSpendingModel earningSpending = d.getValue(EarningSpendingModel.class);
                    String tur = earningSpending.getType().toString();

                    if (tur.equals("Maaş") || tur.equals("Gelir")) { // Türü maaş ve gelir ise ayrı topla.

                        Integer amountSalary = earningSpending.getAmounth(); // Türü maaş olan miktarlar değişkene atandı.
                        totalSalary = totalSalary + amountSalary; // Türü maaş olanlar toplandı.

                        mainBinding.textTotalSalary.setText(String.valueOf(totalSalary));

                    } else if (tur.equals("Gider") || tur.equals("Birikim")) { // Türü gider ise ayrı topla.

                        Integer amounthSpending = earningSpending.getAmounth(); // Türü gider olan miktarlar değişkene atandı.
                        totalSpending = totalSpending + amounthSpending; // Türü gider olanlar toplandı.

                        mainBinding.textTotalSpending.setText(String.valueOf(totalSpending));

                    }

                    int remainig = totalSalary - totalSpending; // Toplam gelir ve toplam gider arasındaki fark hesaplandı. Değişkene atandı.
                    mainBinding.textRemaining.setText(String.valueOf(remainig)); // Fark text view'de görüntülendi.

                    ggLists.add(earningSpending);
                }

                Collections.sort(ggLists, new Comparator<EarningSpendingModel>() { //RecyclerView A->Z sıralama
                    @Override
                    public int compare(EarningSpendingModel ggModel, EarningSpendingModel t1) {
                        //  return t1.getNumber().compareToIgnoreCase(ggModel.getNumber());
                        return Integer.compare(t1.getNumber(), ggModel.getNumber());
                    }
                });

                tabloRvAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void noteAdd() { // Not ekleme işlemleri

        AlertDialog.Builder builderNote = new AlertDialog.Builder(MainActivity.this);
        View viewNote = getLayoutInflater().inflate(R.layout.alert_note, null);

        TextView textNoteBtYes = viewNote.findViewById(R.id.textNoteBtYes);
        TextView textNoteBtNo = viewNote.findViewById(R.id.textNoteBtNo);
        TextInputEditText inputEarningNote = viewNote.findViewById(R.id.inputEarningNote);
        TextInputEditText inputSpendingNote = viewNote.findViewById(R.id.inputSpendingNote);

        builderNote.setView(viewNote);

        AlertDialog dialogNote = builderNote.create();
        dialogNote.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String txtMonth = mainBinding.textMonth.getText().toString(); // Seçilen ay değişkene atandı.
        String currentUser = firebaseUser.getUid().toString(); // Online kullanıcı ID'si alındı. Her kullanıcının kendi notu, kendi ID'si altında görüntülendi.
        String noteId = txtMonth + "Note"; // Devamlı güncelleme yapılacağı için sabit bir ID oluşturuldu.

        referenceNote = FirebaseDatabase.getInstance().getReference("Note").child(currentUser).child(year).child(txtMonth);

        referenceNote.addValueEventListener(new ValueEventListener() { // Notu görüntüleme
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot d : snapshot.getChildren()) {

                    if (d.equals(null)) { // Not henüz girilmemişse EditText'te herhangi bir bilgi gösterilmedi

                    } else {
                        EarningSpendingModel esModel = d.getValue(EarningSpendingModel.class);
                        String earNot = esModel.getEarningNote().toString(); // Modelden not bilgisi alındı.
                        String speNot = esModel.getSpendingNote().toString(); // Modelden not bilgisi alındı.

                        inputEarningNote.setText(earNot); // DB'den gelen not bilgisi Alert dialog'daki Text'e atandı.
                        inputSpendingNote.setText(speNot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceNote = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_note)); // Note isimli tablo oluşturuldu.

        textNoteBtYes.setOnClickListener(new View.OnClickListener() { // Not ekleme işlemleri
            @Override
            public void onClick(View v) {

                String eNote = inputEarningNote.getText().toString(); // Gelir notu eNote değişkenine atandı.
                String spNote = inputSpendingNote.getText().toString(); // Gider notu spNote değişkenine atandı

                earningSpendingModel = new EarningSpendingModel(noteId, eNote, spNote);

                if (TextUtils.isEmpty(eNote) && TextUtils.isEmpty(spNote)) {
                    Toast.makeText(MainActivity.this, "Not girişi yapmadınız.", Toast.LENGTH_SHORT).show();

                } else {

                    referenceUser.addValueEventListener(new ValueEventListener() { // Not bilgisi devamlı güncelleneceği için ekleme değil güncelleme işlemi yaptırıldı.
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcı ID'si alındı.

                            HashMap<String, Object> updateNote = new HashMap<>();
                            updateNote.put("id", noteId);
                            updateNote.put("earningNote", eNote); // Gelir notu güncellendi.
                            updateNote.put("spendingNote", spNote); // Gider notu güncellendi.

                            referenceNote.child(currentUser).child(year).child(txtMonth).child(noteId).updateChildren(updateNote);

                            dialogNote.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });


        textNoteBtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNote.dismiss();
            }
        });

        dialogNote.show();
    }
    public void logout() {
        AlertDialog.Builder builderLogout = new AlertDialog.Builder(MainActivity.this);
        View viewLogout = getLayoutInflater().inflate(R.layout.alert_sign_out, null);

        TextView textBtLogoutYes = viewLogout.findViewById(R.id.textBtLogoutYes);
        TextView textBtLogoutNo = viewLogout.findViewById(R.id.textBtLogoutNo);

        builderLogout.setView(viewLogout);

        AlertDialog dialog = builderLogout.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        textBtLogoutYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                Intent intentLogout = new Intent(MainActivity.this, LoginActivity.class);
                intentLogout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentLogout);
                finish();

                dialog.dismiss();
            }
        });

        textBtLogoutNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}