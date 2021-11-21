package com.CubidesyMerchan.parcial1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    Button btnGaleria;
    ImageView imageView;
    ImageView imagen;
    String rutaImagen;

    private static String TAG = "MainActivity";
    static {
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV instalado exitosamente.");
        }else{
            Log.d(TAG, "OpenCV no se instalo Error..");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamara=findViewById(R.id.btnCamara);
        btnGaleria=findViewById(R.id.btnGaleria);
        imageView=findViewById(R.id.imageView);
        imagen=findViewById(R.id.imageView);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                                                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1000);
        }

        btnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var1) {

                abrirCamara();
            }
        });

        btnGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cargarImagen();
            }
        });
    }

    /*public void onclick(View view){
        cargarImagen();
    }*/
    private void cargarImagen(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent,"Seleccione la aplicacion"),10);
    }

    private void abrirCamara(){

        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent1.resolveActivity(getPackageManager()) != null){

            File imagenArchivo = null;

            try {
                imagenArchivo = CrearImagen();

            }catch (IOException ex){
                Log.e("Error", ex.toString());

            }

            if(imagenArchivo != null){
                Uri fotoUri = FileProvider.getUriForFile(this, "com.ingelist.mycamera.fileprovider", imagenArchivo);
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(intent1, 1);
            }


        }

    }
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            Uri path =data.getData();
            imagen.setImageURI(path);
        }
        if (requestCode==1 && resultCode == RESULT_OK){
            /*Bundle extras= data.getExtras();
            Bitmap imgBitmap= (Bitmap) extras.get("data");*/
            Bitmap imgBitmap = BitmapFactory.decodeFile(rutaImagen);
            imageView.setImageBitmap(imgBitmap);
        }
    }

    private File CrearImagen() throws IOException {
        String nombreImagen = "foto_";
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreImagen,".jpg", directorio);
        rutaImagen = imagen.getAbsolutePath();
        return imagen;
    }
}