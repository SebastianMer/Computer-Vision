package com.CubidesyMerchan.parcial1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.BitmapCompat;
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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainActivity extends AppCompatActivity {

    Button btnCamara;
    Button btnGaleria;
    Button btnRec;
    ImageView imageView;
    ImageView imagen;
    Mat img = new Mat();
    Bitmap imgbitmap;
    String rutaImagen;
    CascadeClassifier dect;
    File haar;

    int ent;
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

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);

        btnCamara=findViewById(R.id.btnCamara);
        btnGaleria=findViewById(R.id.btnGaleria);
        btnRec=findViewById(R.id.btnRec);

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

                cargarImagen('D');
            }
        });

        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cargarImagen('R');
            }
        });
    }

    private void cargarImagen(char btn){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        switch (btn){
            case 'D': startActivityForResult(intent.createChooser(intent,"Seleccione la aplicacion"),10);
                        break;

            case 'R': startActivityForResult(intent.createChooser(intent,"Seleccione la aplicacion"),15);
                        break;

            default:break;
        }

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
            //imagen.setImageBitmap(imgbitmap);
            Grises(imageView, requestCode);

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

    public void Grises(View v, int requestCode){
        BitmapFactory.Options o = new BitmapFactory.Options();

        //Convertir de BitMap a Mat
        Utils.bitmapToMat(imgbitmap, img);
        //Cambiar de escala
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGRA);

        CascadeClassifier rostro = new CascadeClassifier();

        try{
            InputStream is = this.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File haardir = getDir("cascade", Context.MODE_PRIVATE);
            haar = new File(haardir, "haarcascade_frontalface_alt.xml");
            FileOutputStream os = new FileOutputStream(haar);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while((bytesRead = is.read(buffer)) != -1){
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            rostro = new CascadeClassifier(haar.getAbsolutePath());

        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error cargando cascade", e);
        }
        //Detección de rostros


        MatOfRect matOfRect = new MatOfRect();
        rostro.detectMultiScale(img, matOfRect);

        int numcaras = matOfRect.toArray().length;


        for(Rect cara: matOfRect.toArray()){
            Imgproc.rectangle(img, new Point(cara.x, cara.y), new Point(cara.x + cara.width, cara.y + cara.height), new Scalar(0,0,255), 3);
        }

        //Imgproc.resize(img, img, new Size(1, 1));
        //Volver a cambiar a BitMap para poner en pantalla
        Bitmap gris = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, gris);

        if(requestCode == 10){
            imageView.setImageBitmap(gris);
            Toast.makeText(getApplicationContext(), numcaras+" Caras encontradas", Toast.LENGTH_SHORT).show();
        }else if(requestCode == 15){
            imageView.setImageBitmap(gris);
            Toast.makeText(getApplicationContext(), " Reconociendo ...", Toast.LENGTH_SHORT).show();
        }

    }


}