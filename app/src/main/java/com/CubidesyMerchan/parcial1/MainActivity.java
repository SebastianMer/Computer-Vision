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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.Feature2D;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.osgi.OpenCVNativeLoader;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.io.File;
import java.io.Serializable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.security.auth.callback.PasswordCallback;
import javax.xml.parsers.ParserConfigurationException;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC3;
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
    Mat pre_cara;
    int i = 0;

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
            Deteccion_Haar(imageView, requestCode);

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

    public void Deteccion_Haar(View v, int requestCode){
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
        Rect rectCrop=null;

        for(Rect cara: matOfRect.toArray()){
            Imgproc.rectangle(img, new Point(cara.x, cara.y), new Point(cara.x + cara.width, cara.y + cara.height), new Scalar(0,0,255), 3);
            rectCrop = new Rect(cara.x, cara.y, cara.width, cara.height);
        }



        //Imgproc.resize(img, img, new Size(1, 1));
        //Volver a cambiar a BitMap para poner en pantalla
        Bitmap gris = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, gris);

        if(requestCode == 10){
            imageView.setImageBitmap(gris);
            Toast.makeText(getApplicationContext(), numcaras+" Caras encontradas", Toast.LENGTH_SHORT).show();
        }else if(requestCode == 15){
            pre_cara = new Mat(500, 500, CvType.CV_64F);
            pre_cara = new Mat(img,rectCrop);

            Imgproc.resize(pre_cara, pre_cara, new Size(500, 500));
            Bitmap pre = Bitmap.createBitmap(pre_cara.cols(), pre_cara.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(pre_cara, pre);
            imageView.setImageBitmap(pre);
            metodoPca();
        }

    }

    public void metodoPca(){
        /*Mat dataPts = new Mat();
        Mat mean = new Mat();
        Mat eigenvectors = new Mat();
        Mat eigenvalues = new Mat();
        Core.PCACompute2(dataPts, mean, eigenvectors, eigenvalues);*/

        //Lectura de archivo xml

        Mat trian_seb = new Mat(55, 1, CV_16S);

        trian_seb.put(55, 1, 9.97745209e+01, 1.05430511e+02, 1.11519707e+02, 9.59469070e+01,
                    1.03286270e+02, -5.97784653e+01, -6.07402306e+01, 8.10382080e+01, 1.17843781e+02, 8.40067444e+01,
                    8.15825119e+01, -1.12500320e+02, 7.86857147e+01, 9.94324722e+01, 9.99810944e+01, 1.01215935e+02,
                    -9.46645966e+01, -1.02959785e+02, 8.20064240e+01, -1.87276993e+01,
                    -9.46645966e+01, -1.14954643e+02, -8.66395340e+01, 1.01167747e+02,
                    -2.50557690e+01, -6.66079559e+01, -5.42955246e+01, 9.94538422e+01,
                    -1.73107529e+02, 1.00517487e+02, -1.76240723e+02, -1.02473778e+02,
                    -1.11410904e+02, 1.00714836e+02, -1.67855103e+02, 1.00150253e+02,
                    -4.25327225e+01, -2.02810574e+01, -5.44977760e+01, 8.62398529e+01,
                    -5.00738449e+01, 1.11457710e+02, 1.17126442e+02, -9.75823364e+01,
                    9.25721970e+01, 1.05004166e+02, -6.06802864e+01, -9.68201447e+01,
                    -1.71307571e+02, -6.64568024e+01, 1.22038727e+02, -6.98824615e+01,
                    -8.69313889e+01, -1.74588833e+01, -2.10118313e+01);


        pre_cara.convertTo(pre_cara, CV_32FC3, 1/255.0);
        Mat test = new Mat(500, 500, CvType.CV_64F);
        Mat mean = new Mat();
        Mat eigenvectors = new Mat();
        Mat eigenvalues = new Mat();
        Core.PCACompute2(test, mean, eigenvectors, eigenvalues);

        Toast.makeText(getApplicationContext(), " Hasta ahora bien", Toast.LENGTH_SHORT).show();

    }


}