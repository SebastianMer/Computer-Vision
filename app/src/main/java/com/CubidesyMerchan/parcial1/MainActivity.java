package com.CubidesyMerchan.parcial1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    ImageView imageView;
    ImageView imagen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamara=findViewById(R.id.btnCamara);
        imageView=findViewById(R.id.imageView);
        imagen=findViewById(R.id.imageView);


        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrircamara();
            }
        });
    }

    /*public void onclick(View view){

    }*/

    private void abrircamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

         startActivityForResult(intent,1);
    }
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==1 && resultCode == RESULT_OK){
            Bundle extras= data.getExtras();
            Bitmap imgBitmap= (Bitmap)extras.get("data");
            imageView.setImageBitmap(imgBitmap);
        }
    }
}