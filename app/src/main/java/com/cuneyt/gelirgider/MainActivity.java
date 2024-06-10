package com.cuneyt.gelirgider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.RelativeDateTimeFormatter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cuneyt.gelirgider.adapter.TabloRvAdapter;
import com.cuneyt.gelirgider.assistantclass.DateTime;
import com.cuneyt.gelirgider.assistantclass.MobileDeviceName;
import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.assistantclass.RandomId;
import com.cuneyt.gelirgider.databinding.ActivityMainBinding;
import com.cuneyt.gelirgider.entities.EarningSpendingModel;
import com.cuneyt.gelirgider.entities.ErrorLogModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mainBinding;
    private EarningSpendingModel earningSpendingModel;
    private ErrorLogModel errorLogModel = new ErrorLogModel();
    private MobileDeviceName deviceName = new MobileDeviceName();
    private DateTime dateTime = new DateTime();
    private DatabaseReference referenceTablo, referenceUser, referenceNote, referenceError;
    final private DatabaseReference referenceTablo1 = FirebaseDatabase.getInstance().getReference("Tablo");
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private TabloRvAdapter tabloRvAdapter;
    private ArrayList<EarningSpendingModel> ggLists = new ArrayList<>();
    private MonthYear monthYear = new MonthYear();
    private RandomId randomId = new RandomId();
    private String currentlyMonth = monthYear.currentlyDateTime("MMMM"); // Geçerli ay değişkene atandı.
    private String currentlyYear = monthYear.currentlyDateTime("yyyy"); // Geçerli yıl değişkene atandı.
    private Animation uptodown, downtoup, alpha;

    private void anim() {
        uptodown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_up_to_down);
        downtoup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_down_to_up);
        alpha = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_alpha);

        mainBinding.constYearMonth.setAnimation(uptodown);
        mainBinding.constBottom.setAnimation(downtoup);
      //  mainBinding.constTotal.setAnimation(downtoup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internetCheck();

        try {
            mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

            anim();

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser(); // Oturum açmış kullanıcı alındı.
            referenceUser = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_user)); // Kullanıcı tablosu oluşturuldu.

            mainBinding.textMonth.setText(currentlyMonth); // Geçerli ay editText'te gösterildi.
            mainBinding.textYear.setText(currentlyYear); // Geçerli yıl editText'te gösterildi.

            show(currentlyMonth, currentlyYear); // Uygulama ilk gösterildiğinde geçerli ayın verileri listelendi. currenlyMonth geçerli ayı yukarıda tutan değişken.

            mainBinding.imgBtAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String txtMonth = mainBinding.textMonth.getText().toString(); // TextView'deki ay ve yıl adı değişkenlere atandı. Metod tetiklenince ilk olarak ay ve yıl adını görüp, son ekleme yapılan ay ve yıl ile karıştırmaması için en tepeye yazıldı.
                    String txtYear = mainBinding.textYear.getText().toString();
                    add(txtMonth, txtYear);
                }
            });
            mainBinding.textYear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    yearChoise();
                }
            });

            mainBinding.textMonth.setOnClickListener(new View.OnClickListener() { // Ay seçme işlemi
                @Override
                public void onClick(View view) {
                    monthChoise();
                }
            });
            mainBinding.imgBtNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noteAdd();
                }
            });
            mainBinding.imgBtLoanCredit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intenLoanCredit = new Intent(MainActivity.this, LoanActivity.class);
                    startActivity(intenLoanCredit);
                }
            });
            mainBinding.imgBtInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentInfo = new Intent(MainActivity.this, InfoActivity.class);
                    startActivity(intentInfo);
                }
            });
            mainBinding.textTotalSalary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calculation();
                }
            });
            mainBinding.imgBtLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });

        } catch (Exception e) {

            String mobileDevName = deviceName.deviceName().toString();
            String err = String.valueOf(e);
            String date = dateTime.currentlyDateTime("dd.MM.yyyy HH:mm:ss");

            errorLogModel = new ErrorLogModel(mobileDevName, "MainActivity", err, date);
            referenceError = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_error));
            referenceError.push().setValue(errorLogModel);
        }
    }

    public void yearChoise() {
        AlertDialog.Builder builderYear = new AlertDialog.Builder(MainActivity.this);
        View viewYear = getLayoutInflater().inflate(R.layout.alert_year, null);

        TextView textBtYes = viewYear.findViewById(R.id.textBtYes);
        TextView textBtNo = viewYear.findViewById(R.id.textBtNo);
        RadioGroup radioGroupYear = viewYear.findViewById(R.id.radioGroupYear);

        builderYear.setView(viewYear);

        AlertDialog dialogYear = builderYear.create();
        dialogYear.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogYear.show();

        radioGroupYear.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rbYear = group.findViewById(checkedId);
            }
        });

        textBtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int radioSelectId = radioGroupYear.getCheckedRadioButtonId();

                if (radioSelectId == -1) {
                    Toast.makeText(MainActivity.this, "Seçim yapılmadı.", Toast.LENGTH_SHORT).show();

                } else {

                    RadioButton radioButtonYear = radioGroupYear.findViewById(radioSelectId);
                    mainBinding.textYear.setText(radioButtonYear.getText().toString());
                    monthChoise();

                    dialogYear.dismiss();
                }

            }
        });

        textBtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogYear.dismiss();
            }
        });

        builderYear.setView(viewYear);

    }

    public void monthChoise() {
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

                int radioSelectId = radioGroupMonth.getCheckedRadioButtonId();

                if (radioSelectId == -1) {
                    Toast.makeText(MainActivity.this, "Seçim yapılmadı.", Toast.LENGTH_SHORT).show();

                } else {

                    RadioButton radioButtonMonth = radioGroupMonth.findViewById(radioSelectId);

                    mainBinding.textMonth.setText(radioButtonMonth.getText().toString());

                    String radioMonth = radioButtonMonth.getText().toString();
                    String textYear = mainBinding.textYear.getText().toString();

                    show(radioMonth, textYear);

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

    public void add(String choiseMonth, String choiseYear) {

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

        textBtYes.setText("Ekle"); // Ekle ve güncelleme işlemleri için tek Alert Dialog tasarımı yapıldığı için, ekleme butonu tıklayınca Alert Dialog'daki onay butonuna 'Ekle' ifadesi yazdırıldı.

        textBtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(editTitle.getText().toString()) ||
                        TextUtils.isEmpty(editAmounth.getText().toString())) { // Başlık ve Tutar bilgilerinin boş geçilmemesi sağlandı.

                    Toast.makeText(MainActivity.this, "Bilgileri giriniz.", Toast.LENGTH_SHORT).show();

                } else {

                    String ranId = randomId.randomUUID(); // Her kök için ID üretildi. Kontrolü kolaylaştırmak için ID oluşturuldu. ID aynı zamanda düğüm olarak aktarıldı.

                    radioGroupAdd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            RadioButton radioButton = group.findViewById(checkedId);
                        }
                    });

                    referenceUser.addValueEventListener(new ValueEventListener() {
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
                                boolean pay = false;

                                Integer intAmaounth = Integer.parseInt(amounth); // EarningSpendingModel sınıfındaki değişken integer olduğundan editAmounth ile gelen veri integer'a çevrildi.

                                Spannable spTitle = new SpannableString(title); // String parçalama sınıfı.
                                String firstLetterTitle = spTitle.subSequence(0, 1).toString(); // Her kök ID'sinin başına eklenmesi için başlığın 1. harfi alındı. Bu işlem Firebase'de okumayı kolaylaştırmak için yapıldı.

                                String uniqueId = firstLetterTitle + "-" + ranId; // Her kök için benzersiz ID oluşturuldu.

                                if (type.equals("Maaş")) { // Recycler View'de sıralama yapmak için her gelir ve gider verisinin başına bir rakam ve verinin adı verildi. Bu sayede her tür kendi içinde alfabetik olarak sıralanabildi.
                                    String number = "1" + title;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, choiseYear, choiseMonth);

                                } else if (type.equals("Gelir")) {
                                    String number = "2" + title;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, choiseYear, choiseMonth);

                                } else if (type.equals("Birikim")) {
                                    String number = "3" + title;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, choiseYear, choiseMonth);

                                } else if (type.equals("Gider")) {
                                    String number = "4" + title;
                                    earningSpendingModel = new EarningSpendingModel(uniqueId, title, intAmaounth, type, number, choiseYear, choiseMonth);

                                }

                                referenceTablo1.child(currentUserId).child(choiseYear).child(choiseMonth).child(uniqueId).setValue(earningSpendingModel); // Veri Firebase'e eklendi.

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

    public void show(String choiseMonth, String choiseYear) {

        mainBinding.rvTable.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, true); // VERTICAL, true: RecyclerView'e eklenen veri en alttan üste doğru eklenir.
        linearLayoutManager.setStackFromEnd(true); // RecyclerView'e eklenen veri sayfayı otomatik kaydırır.
        mainBinding.rvTable.setItemAnimator(new DefaultItemAnimator());
        mainBinding.rvTable.setLayoutManager(linearLayoutManager);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewInflater = inflater.inflate(R.layout.design_row_spending, null); // Tasarım bağlandı.

        tabloRvAdapter = new TabloRvAdapter(this, ggLists);
        mainBinding.rvTable.setAdapter(tabloRvAdapter);

        // String txtMonth = mainBinding.textMonth.getText().toString(); // Seçilen ay değişkene atandı.
        String userId = firebaseUser.getUid().toString(); // Online kullanıcı ID'si alındı. Her kullanıcının kendi tablosu, kendi ID'si altında görüntülendi.

        referenceTablo = FirebaseDatabase.getInstance().getReference(getResources().getString(R.string.db_table)).child(userId).child(choiseYear).child(choiseMonth); // Seçilen yıl ve ayın verisi listelendi.

        referenceTablo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ggLists.clear();
                int totalSalary = 0, totalSpending = 0, totalEarning = 0; //Gelir ve giderin toplanması için başlangıç değişkenine 0 verildi.

                for (DataSnapshot d : snapshot.getChildren()) {

                    EarningSpendingModel earningSpending = d.getValue(EarningSpendingModel.class);
                    String typeEs = earningSpending.getType().toString();

                    if (typeEs.equals("Maaş") || typeEs.equals("Gelir")) { // Türü maaş ve gelir ise ayrı topla.

                        Integer amountSalary = earningSpending.getAmounth(); // Türü maaş olan miktarlar değişkene atandı.
                        totalSalary = totalSalary + amountSalary; // Türü maaş olanlar toplandı.

                        mainBinding.textTotalSalary.setText(String.valueOf(totalSalary));

                    } else if (typeEs.equals("Gider") || typeEs.equals("Birikim")) { // Türü gider ise ayrı topla.

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
                        return t1.getSort().compareToIgnoreCase(ggModel.getSort());
                        // return Integer.compare(t1.getNumber(), ggModel.getNumber());
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
        TextInputEditText inputSavingNote = viewNote.findViewById(R.id.inputSavingNote);

        builderNote.setView(viewNote);

        AlertDialog dialogNote = builderNote.create();
        dialogNote.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        String txtMonth = mainBinding.textMonth.getText().toString(); // Seçilen ay değişkene atandı.
        String currentUser = firebaseUser.getUid().toString(); // Online kullanıcı ID'si alındı. Her kullanıcının kendi notu, kendi ID'si altında görüntülendi.
        String noteId = txtMonth + "Note"; // Devamlı güncelleme yapılacağı için sabit bir ID oluşturuldu.

        referenceNote = FirebaseDatabase.getInstance().getReference("Note").child(currentUser).child(currentlyYear).child(txtMonth);

        referenceNote.addValueEventListener(new ValueEventListener() { // Notu görüntüleme
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot d : snapshot.getChildren()) {

                    if (d.equals(null)) { // Not henüz girilmemişse EditText'te herhangi bir bilgi gösterilmedi

                    } else {
                        EarningSpendingModel esModel = d.getValue(EarningSpendingModel.class);
                        String earNot = esModel.getEarningNote().toString(); // Modelden not bilgisi alındı.
                        String speNot = esModel.getSpendingNote().toString(); // Modelden not bilgisi alındı.
                        String svnNot = esModel.getSavingNote().toString(); // Modelden not bilgisi alındı.

                        inputEarningNote.setText(earNot); // DB'den gelen not bilgisi Alert dialog'daki Text'e atandı.
                        inputSpendingNote.setText(speNot);
                        inputSavingNote.setText(svnNot);
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
                String savNote = inputSavingNote.getText().toString();
                earningSpendingModel = new EarningSpendingModel(noteId, eNote, spNote, savNote);

                if (TextUtils.isEmpty(eNote) && TextUtils.isEmpty(spNote) && TextUtils.isEmpty(savNote)) {
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
                            updateNote.put("savingNote", savNote); // Birikim notu güncellendi.

                            referenceNote.child(currentUser).child(currentlyYear).child(txtMonth).child(noteId).updateChildren(updateNote);

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

    public void calculation(){
        AlertDialog.Builder builderCalc = new AlertDialog.Builder(MainActivity.this);
        View viewNote = getLayoutInflater().inflate(R.layout.alert_salary_calc, null);

        EditText editSalaryCuneyt = viewNote.findViewById(R.id.editSalaryCuneyt);
        EditText editSalarySibel = viewNote.findViewById(R.id.editSalarySibel);
        EditText editPercent = viewNote.findViewById(R.id.editPercent);
        TextView textAlertCuneyt = viewNote.findViewById(R.id.textAlertCuneyt);
        TextView textAlertSibel = viewNote.findViewById(R.id.textAlertSibel);
        TextView textCuneyt = viewNote.findViewById(R.id.textCuneyt);
        TextView textSibel = viewNote.findViewById(R.id.textSibel);
        TextView textBtCalc = viewNote.findViewById(R.id.textBtCalc);
        TextView textBtClose = viewNote.findViewById(R.id.textBtClose);

        builderCalc.setView(viewNote);

        AlertDialog dialogNote = builderCalc.create();
        dialogNote.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        textBtCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editSalaryCuneyt.getText().toString()) || TextUtils.isEmpty(editSalarySibel.getText().toString()) || TextUtils.isEmpty(editPercent.getText().toString())){

                    Toast.makeText(MainActivity.this, "Bilgileri giriniz.", Toast.LENGTH_SHORT).show();

                } else {
                    Integer cuneyt = Integer.parseInt(editSalaryCuneyt.getText().toString());
                    Integer sibel = Integer.parseInt(editSalarySibel.getText().toString());
                    Integer percent = Integer.parseInt(editPercent.getText().toString());

                    int cuneytNew = (cuneyt * (100 + percent)) / 100;
                    int sibelNew = (sibel * (100 + percent)) / 100;

                    textAlertCuneyt.setText(String.valueOf(cuneytNew));
                    textAlertSibel.setText(String.valueOf(sibelNew));

                    textAlertCuneyt.setVisibility(View.VISIBLE);
                    textAlertSibel.setVisibility(View.VISIBLE);
                    textCuneyt.setVisibility(View.VISIBLE);
                    textSibel.setVisibility(View.VISIBLE);
                }


            }
        });

        textBtClose.setOnClickListener(new View.OnClickListener() {
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

    private void internetCheck() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {


        } else {
            AlertDialog.Builder builderNet = new AlertDialog.Builder(MainActivity.this);
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