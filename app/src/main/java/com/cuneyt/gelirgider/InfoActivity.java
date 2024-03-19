package com.cuneyt.gelirgider;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.cuneyt.gelirgider.databinding.ActivityInfoBinding;

public class InfoActivity extends AppCompatActivity {

    private ActivityInfoBinding infoBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        infoBinding = DataBindingUtil.setContentView(this, R.layout.activity_info);

        infoBinding.imgBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}