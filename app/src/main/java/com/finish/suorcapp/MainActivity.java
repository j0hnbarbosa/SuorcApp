package com.finish.suorcapp;

//import androidx.appcompat.app.AppCompatActivity;
//
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.ImageView;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
//}


import android.graphics.Bitmap;
        import android.os.Bundle;
        import android.util.Log;

        import org.opencv.android.BaseLoaderCallback;

        import android.view.SurfaceView;
        import android.view.View;
        import android.view.WindowManager;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.SeekBar;
        import android.widget.SeekBar.OnSeekBarChangeListener;
        import android.widget.TextView;
        import android.widget.Toast;

        import org.opencv.android.CameraActivity;
        import org.opencv.android.CameraBridgeViewBase;
        import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
        import org.opencv.android.OpenCVLoader;
        import org.opencv.android.Utils;
        import org.opencv.core.Core;
        import org.opencv.core.CvType;
        import org.opencv.core.Mat;
        import org.opencv.core.MatOfPoint;
        import org.opencv.core.MatOfPoint2f;
        import org.opencv.core.Point;
        import org.opencv.core.Rect;
        import org.opencv.core.Scalar;
        import org.opencv.core.Size;
        import org.opencv.imgproc.Imgproc;

        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;


public class MainActivity extends CameraActivity implements CvCameraViewListener2, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getName();


    private final String BUTTON_GRAY = "GRAY";
    private final String BUTTON_ORIGINAL = "ORIGINAL";
    private final String BUTTON_BLUR = "BLUR";
    private final String BUTTON_DRAWN = "DRAWN";
    private final String BUTTON_SOBEL = "SOBEL";
    private final String BUTTON_DRAWLINEINCONTOURS = "DRAWLINEINCONTOURS";
    private final String BUTTON_CROP = "CROP";

    String switchOption = BUTTON_ORIGINAL;

    Mat mRGBA;
    Mat mRGBAT;

    CameraBridgeViewBase cameraBridgeViewBase;

    BaseLoaderCallback baseLoaderCallback;
    TextView changeText;

    boolean flagSetImage = false;
    ImageView iv;

    SeekBar sldSeekBarWidth;
    SeekBar sldSeekBarHeight;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        changeText = findViewById(R.id.textView);

        changeText.setText("Original");

        sldSeekBarWidth = findViewById(R.id.sldSeekBarWidth_id);
        sldSeekBarHeight = findViewById(R.id.sldSeekBarHeight_id);

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
        changeText.setText(option);
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

    /**
     *  Apply Blur to the Image
     * @param inputFrame Current frame
     * @param dst (Mat) where the new value with blur are stored
     */
    public void blurImage(CameraBridgeViewBase.CvCameraViewFrame inputFrame, Mat dst) {
        int progressWidth = sldSeekBarWidth.getProgress() > 0 ? sldSeekBarWidth.getProgress() : 1;
        int progressHeight =  sldSeekBarHeight.getProgress() > 0 ? sldSeekBarHeight.getProgress() : 1;
        if(inputFrame != null) {
            Imgproc.blur(inputFrame.rgba(), dst, new Size(progressWidth, progressHeight));
        }

    }

    public void drawnRectangle(Mat matRGBA){
        int width = matRGBA.width();

        Point sizeOfFigure =  new Point(width, 150);
        Point positionInTheScreen =  new Point(0, 0);

        Imgproc.rectangle(matRGBA, new Rect(sizeOfFigure, positionInTheScreen), new Scalar(255, 0, 0, 0), 5);
    }

    /**
     * Convert image to show contours
     * @param matRGBA Current frame of the camera
     */
    public void sobel(Mat matRGBA) {
        Mat grayMat = new Mat();
        Mat sobel = new Mat(); //Mat to store the final result

        //Matrices to store gradient and absolute gradient respectively
        Mat grad_x = new Mat();
        Mat abs_grad_x = new Mat();

        Mat grad_y = new Mat();
        Mat abs_grad_y = new Mat();

        //Converting the image to grayscale
        Imgproc.cvtColor(matRGBA, grayMat, Imgproc.COLOR_BGR2GRAY);

        //Calculating gradient in horizontal direction
        Imgproc.Sobel(grayMat, grad_x, CvType.CV_16S, 1, 0, 3, 1, 0);

        //Calculating gradient in vertical direction
        Imgproc.Sobel(grayMat, grad_y, CvType.CV_16S, 0, 1, 3, 1, 0);

        //Calculating absolute value of gradients in both the direction
        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);

        //Calculating the resultant gradient
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 1, matRGBA);


        grayMat.release();
        sobel.release();
        grad_x.release();
        abs_grad_x.release();
        grad_y.release();
        abs_grad_y.release();

        //Converting Mat back to Bitmap
//        Utils.(sobel, currentBitmap);
    }

    public void drawLineInContours(CameraBridgeViewBase.CvCameraViewFrame inputFrame, Mat matRGBA) {

        Mat grayMat= inputFrame.gray();
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurMat, new Size(25,25), 1);
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blurMat, thresh, 255,1,1,11,2);

        grayMat.release();
        blurMat.release();

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(thresh, contours, hier, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        hier.release();


        MatOfPoint2f biggest = new MatOfPoint2f();
        double max_area = 0;
        for (MatOfPoint i : contours) {
            double area = Imgproc.contourArea(i);
            if (area > 100) {
                MatOfPoint2f m = new MatOfPoint2f(i.toArray());
                double peri = Imgproc.arcLength(m, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true);
                if (area > max_area && approx.total() == 4) {
                    biggest = approx;
                    max_area = area;
                }

            }
        }

        // Show all contours
        // Imgproc.drawContours(matRGBA, contours, -1, new Scalar(0, 255, 0));

        // find the outer box
        // Mat displayMat = inputFrame.rgba();
        Point[] points = biggest.toArray();

        if (points.length == 4) {
            // draw the outer box
            for(int i = 0; i < points.length; i++) {
                int nextPos = i < points.length - 1 ?  i+1 : 0;
                Log.d("POINT[]", points[i] + " --- " + points[nextPos]);

                int x1 = (int)Math.ceil(points[i].x / 100.0) * 100;
                int x2 =(int) Math.ceil((int)points[nextPos].x / 100.0) * 100;

                int y1 = (int)Math.ceil((int)points[i].y / 100.0) * 100;
                int y2 = (int)Math.ceil((int)points[nextPos].y / 100.0) * 100;

                int xMax = Math.max(x1, x2);
                int xMin = Math.min(x1, x2) + 250;

                int yMax = Math.max(y1, y2);
                int yMin = Math.min(y1, y2) + 250;

                String str = String.format("x1: %d x2: %d | y1: %d y2: %d ", x1, x2, y1, y2);
                String strM = String.format("xMin: %d > xMax: %d %b | yMin: %d > yMax: %d %b", xMin, xMax, xMin > xMax, yMin, yMax, yMin > yMax);

                Log.d("STR", str);
                Log.d("STRM", strM);

                if(xMin >= xMax && yMin >= yMax) {
                    Imgproc.line(matRGBA, points[i], points[nextPos], new Scalar(255, 0, 0), 5);

                    // crop the image
                    Rect R = new Rect(points[i], points[nextPos]);

                    String matRGBAWH = String.format("matRGBA.width(): %d matRGBA.height(): %d", matRGBA.width(), matRGBA.height());
                    Log.d("matRGBAWH", matRGBAWH);

                    if (matRGBA.height() > R.height && matRGBA.width() > R.width) {
                        Log.d("crop the image", R.toString());
//                        matRGBA = new Mat(matRGBA, R);

//                        Imgproc.rectangle(matRGBA, new Rect(points[i], points[nextPos]), new Scalar(255, 0, 155), 5);
//                        Mat m = new Mat();
//                        matRGBA.copyTo(m);
//                        Imgproc.resize(m, m, new Size(R.width, R.height));
//                        m.copyTo(matRGBA);

                    }
                }


            }


        }

        thresh.release();
        biggest.release();
    }

    /**
     * Crop the image by the specific size of the contours
     * @param matRGBA current frame
     */
    public void cropAndShowImage(Mat matRGBA) {
        if(flagSetImage) {
            Bitmap bi = Bitmap.createBitmap(matRGBA.width(), matRGBA.height(), Bitmap.Config.RGB_565);

            Utils.matToBitmap(matRGBA, bi);

            iv.setImageBitmap(bi);
            flagSetImage = false;
        }

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("changeColorCount", "onCameraFrame");
        Log.d("switchOption", switchOption);

        switch (switchOption){
            case BUTTON_ORIGINAL:
                mRGBA = inputFrame.rgba();
                break;
            case BUTTON_GRAY:
                mRGBA = inputFrame.gray();
                break;
            case BUTTON_BLUR:
                blurImage(inputFrame, mRGBA);
                break;
            case BUTTON_DRAWN:
                mRGBA = inputFrame.rgba();
                drawnRectangle(mRGBA);
                break;
            case BUTTON_SOBEL:
                mRGBA = inputFrame.rgba();
                sobel(mRGBA);
                break;
            case BUTTON_DRAWLINEINCONTOURS:
                mRGBA = inputFrame.rgba();
                drawLineInContours(inputFrame, mRGBA);
                break;
            case BUTTON_CROP:
                mRGBA = inputFrame.rgba();
                drawLineInContours(inputFrame, mRGBA);
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

    public void onChangeOriginal(View view) {
        setSwitchOption(BUTTON_ORIGINAL);
    }

    public void onChangeBlur(View view) {
        setSwitchOption(BUTTON_BLUR);
    }

    public void onChangeGray(View view) {
        setSwitchOption(BUTTON_GRAY);
    }

    public void onChangeDrawn(View view) {
        setSwitchOption(BUTTON_DRAWN);
    }

    public void onChangeSobel(View view) {
        setSwitchOption(BUTTON_SOBEL);
    }

    public void onChangeDrawLineInContours(View view) {
        setSwitchOption(BUTTON_DRAWLINEINCONTOURS);
    }

    public void onChangeCrop(View view) {
        flagSetImage = true;
        setSwitchOption(BUTTON_CROP);
        cropAndShowImage(mRGBA);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d("onProgressChanged", ""+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Toast.makeText(getApplicationContext(), sldSeekBarWidth.getProgress(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Toast.makeText(getApplicationContext(), sldSeekBarWidth.getProgress(), Toast.LENGTH_SHORT).show();
    }
}