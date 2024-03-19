package com.cuneyt.gelirgider.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cuneyt.gelirgider.LoanActivity;
import com.cuneyt.gelirgider.R;
import com.cuneyt.gelirgider.assistantclass.DateTime;
import com.cuneyt.gelirgider.assistantclass.MonthYear;
import com.cuneyt.gelirgider.entities.LoanModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LoanRvAdapter extends RecyclerView.Adapter<LoanRvAdapter.DesignObjectHolder> {

    private Context context;
    private ArrayList<LoanModel> loanModelLists;
    private LoanModel loanModel = new LoanModel();
    private FirebaseUser firebaseUser;
    private DatabaseReference referenceLoan, referenceUser;
    private DateTime dateTime = new DateTime();
    private static final int VIEW_TYPE_LOAN = 1;
    private static final int VIEW_TYPE_CREDIT = 2;
    private int sum = 1, sub = 1;

    public LoanRvAdapter(Context context, ArrayList<LoanModel> loanModelLists) {
        this.context = context;
        this.loanModelLists = loanModelLists;
    }

    public class DesignObjectHolder extends RecyclerView.ViewHolder {
        TextView textRvPerson, textRvAmounth, textRvName, textRvUpdDate, textBtClose;
        ImageButton imgBtUp, imgBtDown;
        ConstraintLayout constRvLoanInfo, constRvPerson;

        public DesignObjectHolder(@NonNull View itemView) {
            super(itemView);

            textRvPerson = itemView.findViewById(R.id.textRvPerson);
            textRvAmounth = itemView.findViewById(R.id.textRvAmounth);
            textRvName = itemView.findViewById(R.id.textRvName);
            textRvUpdDate = itemView.findViewById(R.id.textRvUpdDate);
            textBtClose = itemView.findViewById(R.id.textBtClose);
            imgBtUp = itemView.findViewById(R.id.imgBtUp);
            imgBtDown = itemView.findViewById(R.id.imgBtDown);
            constRvLoanInfo = itemView.findViewById(R.id.constRvLoanInfo);
            constRvPerson = itemView.findViewById(R.id.constRvPerson);
        }
    }

    @NonNull
    @Override
    public DesignObjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View viewDesign;
        switch (viewType) {
            case 1: // Verecek Başlığı
                viewDesign = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_row_loan, parent, false);
                return new DesignObjectHolder(viewDesign);
            default: // Alacak Başlığı
                viewDesign = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_row_credit, parent, false);
                return new DesignObjectHolder(viewDesign);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DesignObjectHolder holder, int position) {

        String id = loanModelLists.get(position).getId().toString();
        String person = loanModelLists.get(position).getPerson().toString();
        String loanName = loanModelLists.get(position).getLoan().toString();
        int amounth = loanModelLists.get(position).getAmounth();
        String loanType = loanModelLists.get(position).getLoanType().toString();
        String date = loanModelLists.get(position).getUpdateDate().toString();

        holder.textRvPerson.setText(person);
        holder.textRvName.setText(loanName);
        holder.textRvAmounth.setText(String.valueOf(amounth));
        holder.textRvUpdDate.setText(date);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
        referenceLoan = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_loan));

        updateLoanCredit(holder, amounth, id, loanName);

        upDown(holder, id);

        delete(holder, id);
    }

    public int getItemViewType(int position) { // Tek Recycler View'de birden çok tasarım göstermek için 'getItemViewType' sınıfı oluşturuldu. LoanModel içinde her türe göre (Verecek, alacak) tasarımlara yönlendirildi.

        if (loanModelLists.get(position).getLoanType().equals("Verecek")) {
            return VIEW_TYPE_LOAN;

        } else {
            return VIEW_TYPE_CREDIT;
        }
    }

    @Override
    public int getItemCount() {
        return loanModelLists.size();
    }

    public void updateLoanCredit(DesignObjectHolder holder, int amounth, String id, String loanName) {
        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcı Id'si.

                holder.constRvLoanInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builderUpdate = new AlertDialog.Builder(context);
                        View viewUpd = LayoutInflater.from(view.getContext()).inflate(R.layout.alert_loan_credit_update, null);

                        EditText editLoanCreditUpdate = viewUpd.findViewById(R.id.editLoanCreditUpdate);
                        TextView textCurrentTitle = viewUpd.findViewById(R.id.textCurrentTitle);
                        TextView textCurrentLoanCredit = viewUpd.findViewById(R.id.textCurrentLoanCredit);
                        TextView textBtLoanCreditAdd = viewUpd.findViewById(R.id.textBtLoanCreditAdd);
                        TextView textBtLoanCreditSub = viewUpd.findViewById(R.id.textBtLoanCreditSub);
                        TextView textBtLoanCreditNew = viewUpd.findViewById(R.id.textBtLoanCreditNew);
                        TextView textBtClose = viewUpd.findViewById(R.id.textBtClose);

                        builderUpdate.setView(viewUpd);

                        AlertDialog dialogUpd = builderUpdate.create();
                        dialogUpd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        textCurrentLoanCredit.setText(String.valueOf(amounth));
                        textCurrentTitle.setText(loanName);


                        textBtLoanCreditAdd.setOnClickListener(new View.OnClickListener() { // Girilen değer ile geçerli (eski) değeri toplama
                            @Override
                            public void onClick(View view) {

                                if (TextUtils.isEmpty(editLoanCreditUpdate.getText().toString())) {
                                    Toast.makeText(context, "Miktar giriniz.", Toast.LENGTH_SHORT).show();

                                } else {

                                    Integer currAmounth = Integer.parseInt(textCurrentLoanCredit.getText().toString()); // Güncel olan değer alert dialog'daki textCurrentLoanCredit'e atanmıştı. O değer toplama işlemi için integer'a döndürüldü.
                                    Integer newAmounth = Integer.parseInt(editLoanCreditUpdate.getText().toString()); // Yeni girilen miktar integer'a döndürüldü.

                                    int endAmounth = currAmounth + newAmounth; // Geçerli miktar ile yeni girilen miktar toplandı.

                                    HashMap<String, Object> updateAmounth = new HashMap<>();
                                    updateAmounth.put("amounth", endAmounth);
                                    updateAmounth.put("updateDate", dateTime.currentlyDateTime("dd.MM.yyyy"));

                                    referenceLoan.child(currentUser).child(id).updateChildren(updateAmounth); // Güncelleme yapıldı.

                                    dialogUpd.dismiss();
                                }
                            }
                        });

                        textBtLoanCreditSub.setOnClickListener(new View.OnClickListener() { // Girilen değeri ile geçerli (eski) değerden çıkarma
                            @Override
                            public void onClick(View view) {

                                if (TextUtils.isEmpty(editLoanCreditUpdate.getText().toString())) {
                                    Toast.makeText(context, "Miktar giriniz.", Toast.LENGTH_SHORT).show();

                                } else {

                                    Integer currAmounth = Integer.parseInt(textCurrentLoanCredit.getText().toString()); // Güncel olan değer alert dialog'daki textCurrentLoanCredit'e atanmıştı. O değer toplama işlemi için integer'a döndürüldü.
                                    Integer newAmounth = Integer.parseInt(editLoanCreditUpdate.getText().toString()); // Yeni girilen miktar integer'a döndürüldü.

                                    int endAmounth = currAmounth - newAmounth; // Geçerli miktar ile yeni girilen miktar toplandı.

                                    HashMap<String, Object> updateAmounth = new HashMap<>();
                                    updateAmounth.put("amounth", endAmounth);
                                    updateAmounth.put("updateDate", dateTime.currentlyDateTime("dd.MM.yyyy"));

                                    referenceLoan.child(currentUser).child(id).updateChildren(updateAmounth); // Güncelleme yapıldı.

                                    dialogUpd.dismiss();
                                }
                            }
                        });

                        textBtLoanCreditNew.setOnClickListener(new View.OnClickListener() { // Yeni değer ekleme
                            @Override
                            public void onClick(View view) {

                                if (TextUtils.isEmpty(editLoanCreditUpdate.getText().toString())) {
                                    Toast.makeText(context, "Miktar giriniz.", Toast.LENGTH_SHORT).show();

                                } else {

                                    Integer newAmounth = Integer.parseInt(editLoanCreditUpdate.getText().toString()); // Yeni girilen miktar integer'a döndürüldü.

                                    HashMap<String, Object> updateAmounth = new HashMap<>();
                                    updateAmounth.put("amounth", newAmounth);
                                    updateAmounth.put("updateDate", dateTime.currentlyDateTime("dd.MM.yyyy"));

                                    referenceLoan.child(currentUser).child(id).updateChildren(updateAmounth); // Güncelleme yapıldı.

                                    dialogUpd.dismiss();
                                }
                            }
                        });

                        textBtClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogUpd.dismiss();
                            }
                        });

                        dialogUpd.show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void upDown(DesignObjectHolder holder, String id) {
        referenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcı Id'si.

                holder.imgBtDown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Integer textAmounth = Integer.parseInt(holder.textRvAmounth.getText().toString());

                        textAmounth = textAmounth - sub;

                        HashMap<String, Object> updateAmounth = new HashMap<>();
                        updateAmounth.put("amounth", textAmounth);
                        updateAmounth.put("updateDate", dateTime.currentlyDateTime("dd.MM.yyyy"));

                        referenceLoan.child(currentUser).child(id).updateChildren(updateAmounth); // Güncelleme yapıldı.

                        holder.textRvAmounth.setText(String.valueOf(textAmounth));
                    }
                });

                holder.imgBtUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Integer textAmounth = Integer.parseInt(holder.textRvAmounth.getText().toString());

                        textAmounth = textAmounth + sub;

                        HashMap<String, Object> updateAmounth = new HashMap<>();
                        updateAmounth.put("amounth", textAmounth);
                        updateAmounth.put("updateDate", dateTime.currentlyDateTime("dd.MM.yyyy"));

                        referenceLoan.child(currentUser).child(id).updateChildren(updateAmounth); // Güncelleme yapıldı.

                        holder.textRvAmounth.setText(String.valueOf(textAmounth));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void delete(LoanRvAdapter.DesignObjectHolder holder, String id) {
        holder.constRvPerson.setOnLongClickListener(new View.OnLongClickListener() { // Silme işlemleri
            @Override
            public boolean onLongClick(View v) {

                androidx.appcompat.app.AlertDialog.Builder builderDelete = new androidx.appcompat.app.AlertDialog.Builder(context);
                View viewDelete = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.alert_delete, null);

                TextView textAlertYes = viewDelete.findViewById(R.id.textAlertYes);
                TextView textAlertNo = viewDelete.findViewById(R.id.textAlertNo);

                builderDelete.setView(viewDelete);
                androidx.appcompat.app.AlertDialog dialogDelete = builderDelete.create();
                dialogDelete.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                textAlertYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        referenceUser = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_user));
                        referenceLoan = FirebaseDatabase.getInstance().getReference(context.getResources().getString(R.string.db_loan));

                        referenceUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String currentUser = snapshot.child(firebaseUser.getUid()).child("id").getValue().toString(); // Online kullanıcının ID'di alındı.

                                referenceLoan.child(currentUser).child(id).removeValue();

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
}
