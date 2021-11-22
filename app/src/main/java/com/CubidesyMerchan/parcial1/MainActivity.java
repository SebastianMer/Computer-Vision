package com.CubidesyMerchan.parcial1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    Button btnGaleria;
    ImageView imageView;
    ImageView imagen;
    Mat img = new Mat();
    Bitmap gris, imgbitmap;
    String rutaImagen;
    CascadeClassifier dect;
    File haar;

    private static String TAG = "MainActivity";
    static {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamara=findViewById(R.id.btnCamara);
        btnGaleria=findViewById(R.id.btnGaleria);
        imageView=findViewById(R.id.imageView);
        imagen=findViewById(R.id.imageView);

        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseCallback);
        }else{
            baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

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

            try {
                imgbitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
            }catch (IOException e) {
                e.printStackTrace();
            }
            imagen.setImageBitmap(imgbitmap);
            Grises(imageView);
        }
        if (requestCode==1 && resultCode == RESULT_OK){
            /*Bundle extras= data.getExtras();
            Bitmap imgBitmap= (Bitmap) extras.get("data");*/
            Bitmap imgBitmap = BitmapFactory.decodeFile(rutaImagen);
            imagen.setImageBitmap(imgBitmap);
        }
    }

    private File CrearImagen() throws IOException {
        String nombreImagen = "foto_";
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreImagen,".jpg", directorio);
        rutaImagen = imagen.getAbsolutePath();
        return imagen;
    }

    public void Grises(View v){
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inDither = false;
        o.inSampleSize = 4;

        int w = imgbitmap.getWidth(), h = imgbitmap.getHeight();
        gris = Bitmap.createBitmap(h, w, Bitmap.Config.RGB_565);

        //Convertir de BitMap a Mat
        Utils.bitmapToMat(imgbitmap, img);
        //Cambiar de escala
        cvtColor(img, img, COLOR_BGR2GRAY);

        //Detección de rostros
        MatOfRect matOfRect = new MatOfRect();

        dect.detectMultiScale(img, matOfRect);


        /*for(Rect r: rostro.toArray()){
            Imgproc.rectangle(img, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(255,0,0));
        }*/

        //Volver a cambiar a BitMap para poner en pantalla
        Utils.matToBitmap(img, imgbitmap);

        imageView.setImageBitmap(imgbitmap);
    }

}