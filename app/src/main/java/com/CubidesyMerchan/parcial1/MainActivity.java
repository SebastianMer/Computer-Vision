package com.CubidesyMerchan.parcial1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    Button btnGaleria;
    ImageView imageView;
    ImageView imagen;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamara=findViewById(R.id.btnCamara);
        btnGaleria=findViewById(R.id.btnGaleria);
        imageView=findViewById(R.id.imageView);
        imagen=findViewById(R.id.imageView);


        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrircamara();
            }
        });
    }

    public void onclick(View view){
        cargarImagen();
    }
    private void cargarImagen(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent,"selecione la aplicacion"),10);
    }

    private void abrircamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

         startActivityForResult(intent,1);
    }
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            Uri path =data.getData();
            imagen.setImageURI(path);
        }
        if (requestCode==1 && resultCode == RESULT_OK){
            Bundle extras= data.getExtras();
            Bitmap imgBitmap= (Bitmap)extras.get("data");
            imageView.setImageBitmap(imgBitmap);
        }
    }
}