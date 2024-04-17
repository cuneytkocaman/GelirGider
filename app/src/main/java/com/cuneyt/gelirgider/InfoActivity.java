package com.cuneyt.gelirgider;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.cuneyt.gelirgider.databinding.ActivityInfoBinding;

public class InfoActivity extends AppCompatActivity {

    private ActivityInfoBinding infoBinding;

    private Animation uptodown, downtoup;

    private void anim(){
        uptodown = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_up_to_down);
        downtoup = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim_down_to_up);

        infoBinding.constInfo.setAnimation(uptodown);
        infoBinding.constBottomBar.setAnimation(downtoup);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        infoBinding = DataBindingUtil.setContentView(this, R.layout.activity_info);

        anim();

        infoBinding.imgBtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}