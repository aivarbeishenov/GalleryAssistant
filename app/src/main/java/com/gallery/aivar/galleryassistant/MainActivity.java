package com.gallery.aivar.galleryassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.gallery.aivar.galleryassistant.adapter.RemainsModelAdapter;
import com.gallery.aivar.galleryassistant.pojo.RemainsModel;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraPreview;
    TextView txtResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    ListView listView;
    final int RequestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        txtResult = (TextView) findViewById(R.id.txtResult);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        //Обработка тапа по изображению для активации сканированияя
        cameraPreview.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());

                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            listView.setAdapter(null);
                            txtResult.setText(R.string.result_text_default);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, RequestCameraPermissionID);

                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if (qrcodes.size()!=0)
                {
                    txtResult.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent;
                            String url,toGoUrl= "http://www.gallery.kg/api/assistant/остатки-магазина?";
                            String[] values;
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                            url = qrcodes.valueAt(0).displayValue;
                            values = url.split("/");
                            toGoUrl += ("магазин="+values[5]);
                            toGoUrl += ("&номенклатура="+values[6]);
                            cameraSource.stop();

                            /*txtResult.setText(toGoUrl);
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(toGoUrl));
                            startActivity(intent);*/

                            OkHttpClient client = new OkHttpClient();
                            final Request request = new Request.Builder()
                                    .url(toGoUrl)
                                    .build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                    if (response.isSuccessful())
                                    {
                                        final String apiResponse = response.body().string();
                                        final List<RemainsModel> list = new ArrayList<RemainsModel>();


                                        try {
                                            JSONObject jsonObject = new JSONObject(apiResponse);

                                            JSONArray jsonArray = jsonObject.getJSONArray("Остатки");
                                            for (int i=0;i<jsonArray.length();i++){
                                                JSONObject jObj = jsonArray.getJSONObject(i);
                                                list.add(new RemainsModel(jObj.getString("Характеристика"),jObj.getString("СвободныйОстаток")));
                                            }
                                            MainActivity.this.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    RemainsModelAdapter adapter = new RemainsModelAdapter(getApplicationContext(), list);
                                                    listView.setAdapter(adapter);

                                                    txtResult.setText(R.string.result_text_alternative);
                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });


                        }
                    });
                }
            }
        });
    }
}
