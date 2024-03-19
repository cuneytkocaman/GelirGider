package com.cuneyt.gelirgider.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cuneyt.gelirgider.R;
import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.entities.EarningSpendingModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class TabloRvAdapter extends RecyclerView.Adapter<TabloRvAdapter.DesignListObjectHolder> {

    private Context context;
    private ArrayList<EarningSpendingModel> ggModelLists;
    private EarningSpendingModel earSpeModel = new EarningSpendingModel();
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceTable, referenceUser;
    private MonthYear monthYear = new MonthYear();
    private String month = monthYear.currentlyDateTime("MMMM"); // Geçerli ay değişkene atandı.
    private String year = monthYear.currentlyDateTime("yyyy"); // Geçerli yıl değişkene atandı.
    private static final int VIEW_TYPE_SALARY = 1;
    private static final int VIEW_TYPE_EARNING = 2;
    private static final int VIEW_TYPE_SAVING = 3;
    private static final int VIEW_TYPE_SPENDING = 4; // Her türün (maaş, gider, birikim, gelir) tasarımı int değişkene atandı. Bu değişkenler, int kontrolüyle 'DesignListObjectHolder' sınıfından tasarıma bağlandı.
                                                     // Bu numaralar ekleme işlemi sırasında türler için atanmış ve DB'ye yazılmıştı. Maaş=1, Gelir=2, Birikim=3, Gider=4

    public TabloRvAdapter(Context context, ArrayList<EarningSpendingModel> ggModelLists) {
        this.context = context;
        this.ggModelLists = ggModelLists;
    }

    public class DesignListObjectHolder extends RecyclerView.ViewHolder {
        TextView textRvBaslik, textRvMiktar;
        ConstraintLayout constTableRow;
        CheckBox cbPayment;

        public DesignListObjectHolder(@NonNull View itemView) {
            super(itemView);

            textRvBaslik = itemView.findViewById(R.id.textRvTitle);
            textRvMiktar = itemView.findViewById(R.id.textRvAmounth);
            constTableRow = itemView.findViewById(R.id.constTableRow);
            cbPayment = itemView.findViewById(R.id.cbPayment);
        }
    }

    @NonNull
    @Override
    public DesignListObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View viewDesign;
        switch (viewType) { // Her tür için ayrı ayrı tasarımlar tanıtıldı.
            case 1: // Maaş
                viewDesign = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_row_salary, parent, false);
                return new DesignListObjectHolder(viewDesign);

            case 2: // Gelir
                viewDesign = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_row_earning, parent, false);
                return new DesignListObjectHolder(viewDesign);

            case 3: // Birikim
                viewDesign = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_row_saving, parent, false);
                return new DesignListObjectHolder(viewDesign);

            default: // Gider
                viewDesign = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_row_spending, parent, false);
                return new DesignListObjectHolder(viewDesign);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DesignListObjectHolder holder, int position) {

        String id = ggModelLists.get(position).getId();
        String currYear = ggModelLists.get(position).getYear();
        String currMonth = ggModelLists.get(position).getMonth();
        String title = ggModelLists.get(position).getTitle();
        int amounth = ggModelLists.get(position).getAmounth();
        String pay = ggModelLists.get(position).getPayment();

        holder.textRvBaslik.setText(title);
        holder.textRvMiktar.setText(String.valueOf(amounth));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
        referenceTable = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_table));

        update(holder, title, amounth, id, currYear, currMonth);

        delete(holder, id, currYear, currMonth);
    }

    @Override
    public int getItemCount() {
        return ggModelLists.size();
    }

    public void update(DesignListObjectHolder holder, String title, int amounth, String id, String cYear, String cMonth){
        referenceUser.addValueEventListener(new ValueEventListener() { // Güncelleme işlemleri
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcı Id'si.

                /*holder.cbPayment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // Ödendi, ödenmedi checkbox kontrolü
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked){
                            String payStatus = "ödendi";

                            HashMap<String, Object> updatePay = new HashMap<>();
                            updatePay.put("payment", payStatus);

                            referenceTable.child(currentUser).child(cYear).child(cMonth).child(id).updateChildren(updatePay); // Güncelleme yapıldı.

                            Log.e("CHECKBOX: ", title);

                        } else if (!isChecked){
                            String payStatus = "ödendi";

                            HashMap<String, Object> updatePay = new HashMap<>();
                            updatePay.put("payment", payStatus);

                            referenceTable.child(currentUser).child(cYear).child(cMonth).child(id).updateChildren(updatePay); // Güncelleme yapıldı.

                            Log.e("CHECKBOX: ", title);
                        }
                    }
                });*/

                holder.constTableRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builderUpdate = new AlertDialog.Builder(context);
                        View viewUpdate = LayoutInflater.from(v.getContext()).inflate(R.layout.alert_add_update, null);

                        EditText editAmounth = viewUpdate.findViewById(R.id.editAmounth); // Alert dialog görsel objeleri
                        EditText editTitle = viewUpdate.findViewById(R.id.editTitle);
                        RadioGroup radioGroupAdd = viewUpdate.findViewById(R.id.radioGroupAdd);
                        TextView textBtYes = viewUpdate.findViewById(R.id.textBtYes);
                        TextView textBtNo = viewUpdate.findViewById(R.id.textBtNo);

                        builderUpdate.setView(viewUpdate);

                        AlertDialog dialogUpdate = builderUpdate.create();
                        dialogUpdate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        editTitle.setText(title);
                        editAmounth.setText(String.valueOf(amounth));

                        textBtYes.setText("Güncelle");

                        textBtYes.setOnClickListener(new View.OnClickListener() { // Güncelle butonu
                            @Override
                            public void onClick(View v) {
                                if (TextUtils.isEmpty(editTitle.getText().toString()) ||
                                        TextUtils.isEmpty(editAmounth.getText().toString())) {

                                    Toast.makeText(context, "Bilgileri giriniz.", Toast.LENGTH_SHORT).show();

                                } else {
                                    radioGroupAdd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                                            RadioButton radioButton = group.findViewById(checkedId);
                                        }
                                    });

                                    int radioSelectId = radioGroupAdd.getCheckedRadioButtonId();

                                    if (radioSelectId == -1) { // Radio Button'lardan seçim yapılmadıysa uyarıldı.

                                        Toast.makeText(context, "Seçim yapılmadı.", Toast.LENGTH_SHORT).show();

                                    } else {
                                        RadioButton radioButtonAdd = radioGroupAdd.findViewById(radioSelectId);

                                        Integer intAmounth = Integer.parseInt(editAmounth.getText().toString());
                                        String radioType = radioButtonAdd.getText().toString(); // Harcamanın veya gelirin yeni türü alındı.

                                        HashMap<String, Object> updateData = new HashMap<>();
                                        updateData.put("title", editTitle.getText().toString());
                                        updateData.put("amounth", intAmounth);
                                        updateData.put("type", radioButtonAdd.getText().toString());

                                        if (radioType.equals("Maaş")) { // Güncellemede tür değiştirildiğinde, türe atanan numaralar da değiştirildi.
                                            String no = "1" + title;
                                            updateData.put("sort", no);

                                        } else if (radioType.equals("Gelir")) {
                                            String no = "2" + title;
                                            updateData.put("sort", no);

                                        } else if (radioType.equals("Birikim")) {
                                            String no = "3" + title ;
                                            updateData.put("sort", no);

                                        } else if (radioType.equals("Gider")) {
                                            String no = "4" + title;
                                            updateData.put("sort", no);
                                        }

                                        referenceTable.child(currentUser).child(cYear).child(cMonth).child(id).updateChildren(updateData, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                if (error != null){
                                                    Toast.makeText(context, "Hata" + error.getMessage(), Toast.LENGTH_SHORT).show();

                                                } else{
                                                    Toast.makeText(context, "Güncellendi.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }); // Güncelleme yapıldı.

                                        dialogUpdate.dismiss();
                                    }
                                }
                            }
                        });
                        textBtNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogUpdate.dismiss();
                            }
                        });

                        dialogUpdate.show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void delete(DesignListObjectHolder holder, String id, String cYear, String cMonth){
        holder.constTableRow.setOnLongClickListener(new View.OnLongClickListener() { // Silme işlemleri
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builderDelete = new AlertDialog.Builder(context);
                View viewDelete = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.alert_delete, null);

                TextView textAlertYes = viewDelete.findViewById(R.id.textAlertYes);
                TextView textAlertNo = viewDelete.findViewById(R.id.textAlertNo);

                builderDelete.setView(viewDelete);
                AlertDialog dialogDelete = builderDelete.create();
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                textAlertYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
                        referenceTable = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_table));

                        referenceUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcının ID'di alındı.

                                referenceTable.child(currentUser).child(cYear).child(cMonth).child(id).removeValue();

                                Toast.makeText(context, "Veri silindi.", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        dialogDelete.dismiss();
                    }
                });

                textAlertNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialogDelete.dismiss();
                    }
                });

                dialogDelete.show();

                return true;
            }
        });
    }

    public int getItemViewType(int position) { // Tek Recycler View'de birden çok tasarım göstermek için 'getItemViewType' sınıfı oluşturuldu. EarningSpendingModel içinde her türe göre (maaş, gider, birikim, gelir) tasarımlara yönlendirildi.

        if (ggModelLists.get(position).getType().equals("Maaş")) {
            return VIEW_TYPE_SALARY;

        } else if (ggModelLists.get(position).getType().equals("Gelir")) {
            return VIEW_TYPE_EARNING;

        } else if (ggModelLists.get(position).getType().equals("Birikim")) {
            return VIEW_TYPE_SAVING;

        } else {
            return VIEW_TYPE_SPENDING;
        }
    }

}
