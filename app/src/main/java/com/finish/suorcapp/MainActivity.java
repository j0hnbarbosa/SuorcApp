package com.finish.suorcapp;


        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;
        import android.net.Uri;
        import android.os.Bundle;
        import android.util.Log;

        import org.opencv.android.BaseLoaderCallback;

        import android.view.SurfaceView;
        import android.view.View;
        import android.view.WindowManager;
        import android.widget.ImageView;
        import android.widget.Toast;

        import org.opencv.android.CameraActivity;
        import org.opencv.android.CameraBridgeViewBase;
        import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
        import org.opencv.android.OpenCVLoader;
        import org.opencv.android.Utils;
        import org.opencv.core.Core;
        import org.opencv.core.CvType;
        import org.opencv.core.Mat;
        import org.opencv.core.Point;
        import org.opencv.core.Rect;
        import org.opencv.core.Scalar;
        import org.opencv.core.Size;
        import org.opencv.imgproc.Imgproc;

        import java.io.FileNotFoundException;
        import java.io.InputStream;
        import java.net.URI;
        import java.util.Collections;
        import java.util.List;


public class MainActivity extends CameraActivity implements CvCameraViewListener2 {

    private static final String TAG = MainActivity.class.getName();

    Mat mRGBA;
    Mat mRGBAT;
    final int WIDTH_CROP = 150;


    private final String BUTTON_DRAWN = "DRAWN";
    private final String BUTTON_TAKE_PHOTO = "TAKE_PHOTO";

    String switchOption = "";

    CameraBridgeViewBase cameraBridgeViewBase;

    BaseLoaderCallback baseLoaderCallback;

    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);


        iv =  findViewById(R.id.imageView_id);

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS: {
                        Log.i("TAG", "OpenCV loaded successfully!");
                        cameraBridgeViewBase.enableView();
                        break;
                    }
                    default: {
                        super.onManagerConnected(status);
                        break;
                    }
                }
            }
        };

    }


    @Override
    public void onResume() {
        super.onResume();
        // OpenCV manager initialization


        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
            Toast.makeText(getApplicationContext(), "OpenCv loaded!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, MainActivity.this, baseLoaderCallback);
            Toast.makeText(getApplicationContext(), "Theres a problem in loading OpenCv", Toast.LENGTH_SHORT).show();
        }

    }

    public void setSwitchOption(String option) {
        this.switchOption = option;
    }

    /**
     * Crop the image by the specific size of the contours
     * @param matRGBA current frame
     */
    public void handleTakeAndShowPhoto(Mat matRGBA) {
            int width = matRGBA.width() - WIDTH_CROP * 2;
            int height = matRGBA.height() - 200;

            if(width > 0 && height > 0) {
                Bitmap bi = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                Rect rect = new Rect(WIDTH_CROP, 100, width, height);

                Utils.matToBitmap(matRGBA.submat(rect) , bi);

                iv.setImageBitmap(bi);
            }

    }

    /**
     * Apply blur to the image
     * @param matRGBA current frame
     */
    public Mat applyBlur(Mat matRGBA) {
        Mat dst = new Mat();
        Imgproc.blur(matRGBA, dst, new Size(50, 1.5));
        return dst;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        Toast.makeText(getApplicationContext(), "Enterd onCameraViewStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraViewStopped() {
        cameraBridgeViewBase.disableView();
        mRGBA.release();
    }


    public void flipImage() {
        mRGBAT = mRGBA.t();
        Core.flip(mRGBA.t(), mRGBAT, 1);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());
    }



    public void drawnRectangle(Mat matRGBA){
        int value = matRGBA.width();


        Point sizeOfFigure =  new Point( WIDTH_CROP, 100);
        Point positionInTheScreen =  new Point(matRGBA.width() - WIDTH_CROP  , matRGBA.height() - 100 );

        Imgproc.rectangle(matRGBA, new Rect(sizeOfFigure, positionInTheScreen), new Scalar(0, 255, 0, 0), 5);
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("changeColorCount", "onCameraFrame");

        switch (switchOption){
            case BUTTON_DRAWN:
                mRGBA = inputFrame.rgba();
                drawnRectangle(mRGBA);
                break;

            default:
                Log.e("INVALID OPTION", switchOption);
                break;

        }

        return mRGBA;
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
            Toast.makeText(getApplicationContext(), "Entered in onDestroy", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
            Toast.makeText(getApplicationContext(), "Entered in onPause", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                iv.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public void onOpenGalery(View e) {
        Log.d("Open galery", "Open galery");

        Log.d("DRAW RecTangule", "Draw Rectangule in the image of the camera");
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 0);


    }

    public void onOpenCamera(View e) {
        Log.d("Take Photo", "Take Photo");
        if(baseLoaderCallback != null) {
            cameraBridgeViewBase.enableView();
            setSwitchOption(BUTTON_DRAWN);
        }
    }

    public void onTakePhoto(View view) {
        if(cameraBridgeViewBase.isEnabled()) {
            handleTakeAndShowPhoto(mRGBA);
        }

    }

    public void onCloseCamera(View view){
        if(cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

}