package com.example.minch.test0518_1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {



    Button buttonB;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonB = (Button) findViewById(R.id.buttonB);

        //앱 기본 스타일 설정
        getSupportActionBar().setElevation(0);




        buttonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,BuleActivity.class);
                startActivity(intent);

            }
        });//b_back



        // 블루투스 활성화하기


    }//onCreate


}
