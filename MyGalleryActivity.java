package org.posenet.compare;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Nullable;
import org.tensorflow.lite.examples.posenet.lib.Device;
import org.tensorflow.lite.examples.posenet.lib.KeyPoint;
import org.tensorflow.lite.examples.posenet.lib.Person;
import org.tensorflow.lite.examples.posenet.lib.Posenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import kotlin.jvm.internal.Intrinsics;

public class MyGalleryActivity extends AppCompatActivity {
    @Nullable
    private TextToSpeech textToSpeech;
    private final int pickImage = 100;
    private final int picId = 123;
    private Uri imageUri;
    private Person testPerson;
    private Person galleryPerson;
    private ImageView imageCamera, imageGallery;
    private Button btnCompare, btnGallery, btnCamera;
    private TextView textResult;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initUI();
        registerClickListener();
    }

    @Nullable
    public final TextToSpeech getTextToSpeech() {
        return this.textToSpeech;
    }

    private void initUI() {
        imageCamera = findViewById(R.id.image_camera);
        imageGallery = findViewById(R.id.image_gallery);

        btnGallery = findViewById(R.id.button_gallery);
        btnCamera = findViewById(R.id.button_camera);

        btnCompare = findViewById(R.id.btn_compare);
        textResult = findViewById(R.id.text_result);

        this.textToSpeech = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
            public final void onInit(int i) {
                if (i != -1) {
                    TextToSpeech textToSpeech = MyGalleryActivity.this.getTextToSpeech();
                    if (textToSpeech == null) {
                        Intrinsics.throwNpe();
                    }

                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    private void registerClickListener() {
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasCameraPermission()) {
//                    Intent camera = new Intent("android.media.action.IMAGE_CAPTURE");
//                    startActivityForResult(camera, MyGalleryActivity.this.picId);
                    Intent myIntent = new Intent(MyGalleryActivity.this, CameraActivity.class);
                    startActivityForResult(myIntent, MyGalleryActivity.this.picId);
                } else {
                    requestCameraPermission();
                }
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent("android.intent.action.PICK", MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, MyGalleryActivity.this.pickImage);
            }
        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compareTwoImages();
            }
        });
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0;
    }

    @TargetApi(23)
    private void requestCameraPermission() {
        this.requestPermissions(new String[]{"android.permission.CAMERA"}, this.picId);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == this.pickImage) {
            this.imageUri = data != null ? data.getData() : null;
            setPosNetGallery(this.imageUri);
        }

        if (resultCode == -1 && requestCode == this.picId) {
            if (data == null) {
                Intrinsics.throwNpe();
            }

            Bundle bundle = data.getExtras();
            Bitmap cameraPic = BitmapFactory.decodeFile(bundle.get("path").toString());
            imageCamera.setImageBitmap(cameraPic);
            setPosNetCamera();
        }
    }

    private void setPosNetCamera() {
        Context context = this.getApplicationContext();
        Device device = Device.CPU;
        Posenet posenet = new Posenet(context, "posenet_model.tflite", device);
        Bitmap imageBitmap = drawableToBitmap(imageCamera.getDrawable());

        this.testPerson = posenet.estimateSinglePose(imageBitmap);
        Paint paint = new Paint();
        paint.setColor(-65536);
        float size = 2.0F;
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Person var10000 = this.testPerson;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("testPerson");
        }

        Iterator var8 = var10000.getKeyPoints().iterator();

        while (var8.hasNext()) {
            KeyPoint keyPoint = (KeyPoint) var8.next();
            canvas.drawCircle((float) keyPoint.getPosition().getX(), (float) keyPoint.getPosition().getY(), size, paint);
        }

        Intrinsics.checkExpressionValueIsNotNull(imageCamera, "image_camera");
        imageCamera.setAdjustViewBounds(true);
        imageCamera.setImageBitmap(mutableBitmap);
    }

    private void setPosNetGallery(Uri imageUri) {
        imageGallery.setImageURI(imageUri);
        Context context = this.getApplicationContext();
        Device device = Device.CPU;
        Posenet posenet = new Posenet(context, "posenet_model.tflite", device);
        Bitmap imageBitmap = drawableToBitmap(imageGallery.getDrawable());

        this.galleryPerson = posenet.estimateSinglePose(imageBitmap);
        Paint paint = new Paint();
        paint.setColor(-65536);
        float size = 2.0F;
        Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Person var10000 = this.galleryPerson;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("galleryPerson");
        }

        Iterator var9 = var10000.getKeyPoints().iterator();

        while (var9.hasNext()) {
            KeyPoint keyPoint = (KeyPoint) var9.next();
            canvas.drawCircle((float) keyPoint.getPosition().getX(), (float) keyPoint.getPosition().getY(), size, paint);
        }

        Intrinsics.checkExpressionValueIsNotNull(imageGallery, "image_gallery");
        imageGallery.setAdjustViewBounds(true);
        imageGallery.setImageBitmap(mutableBitmap);
    }

    private void compareTwoImages() {
        ArrayList galleryPersonList = new ArrayList();
        Person var10000 = this.galleryPerson;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("galleryPerson");
        }

        Iterator var3 = var10000.getKeyPoints().iterator();

        while (var3.hasNext()) {
            KeyPoint keyPoint = (KeyPoint) var3.next();
            galleryPersonList.add((double) keyPoint.getPosition().getX());
            galleryPersonList.add((double) keyPoint.getPosition().getY());
        }

        Log.d("Gallery", String.valueOf(galleryPersonList.size()));
        ArrayList testPersonList = new ArrayList();
        var10000 = this.testPerson;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("testPerson");
        }

        Iterator var4 = var10000.getKeyPoints().iterator();

        while (var4.hasNext()) {
            KeyPoint keyPoint = (KeyPoint) var4.next();
            testPersonList.add((double) keyPoint.getPosition().getX());
            testPersonList.add((double) keyPoint.getPosition().getY());
        }

        double res = CommonUtils.similarity(galleryPersonList, testPersonList);
        Intrinsics.checkExpressionValueIsNotNull(textResult, "text_result");
        textResult.setText((CharSequence) String.valueOf(res));
        TextToSpeech var9;
        if (res > 0.98D) {
            Toast.makeText(this, "POSE given images are matching", Toast.LENGTH_SHORT).show();
            var9 = this.textToSpeech;
            if (var9 == null) {
                Intrinsics.throwNpe();
            }

            var9.speak("POSE given images are matching", 0, (HashMap) null);
        } else {

            Toast.makeText(this, "POSE given images are not matching", Toast.LENGTH_SHORT).show();
            var9 = this.textToSpeech;
            if (var9 == null) {
                Intrinsics.throwNpe();
            }

            var9.speak("POSE given images are not matching", 0, (HashMap) null);
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(257, 257, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        Intrinsics.checkExpressionValueIsNotNull(bitmap, "bitmap");
        return bitmap;
    }

}
