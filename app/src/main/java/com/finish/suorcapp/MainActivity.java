package com.finish.suorcapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements CvCameraViewListener2 {

    private static final String TAG = MainActivity.class.getName();

    // Used to allow all the methods of the aplication to access the frames of the camera.
    Mat mRGBA;

    // Used to set the width of the rectangle drawn in the screen.
    final int WIDTH_CROP = 120;

    // Used to set the height of the rectangle drawn in the screen.
    final int HEIGHT_CROP = 80;

    // Used to not allow the user try to save a image when the camera is closed.
    boolean changeMethodToSave = false;

    // Used to get the biggest contour area of the image
    MatOfPoint rectPos;

    // Used to allow to draw a rectangle in the screen
    private final String BUTTON_DRAWN = "DRAWN";

    // Used to not allow the user try to save a image when the camera is closed.
    String switchOption = "";

    // Used to initialize the camera of the OpenCv.
    CameraBridgeViewBase cameraBridgeViewBase;

    // Used by the OpenCv to initialize.
    BaseLoaderCallback baseLoaderCallback;

    // used to show the image of the detected Sudoku in the screen
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get the camera view.
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);


        iv =  findViewById(R.id.imageView_id);

        // Initialize the OpenCV
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case BaseLoaderCallback.SUCCESS: {
                        Log.i("TAG", "OpenCV loaded successfully!");
//                        cameraBridgeViewBase.enableView();
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

    /***
     * Used to apply filter to remove traits that is not necessary
     * @param matOriginal The original Mat that will be applied the filters
     * @return Mat With all filter applied
     */
    public Mat applyFilters(Mat matOriginal) {
        // ******* applying filters *****************

        // Gray
        Mat grayMat = new Mat();
        Imgproc.cvtColor(matOriginal, grayMat, Imgproc.COLOR_BGR2GRAY);

        // Blur
        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurMat, new Size(7, 7), 3);

        // Canny
        // Mat cannyMat = new Mat();
        // Imgproc.Canny(blurMat, cannyMat, 0, 255, 3, true);

        // Thresh
        Mat threshMat = new Mat();
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Core.bitwise_not(threshMat, threshMat);

        grayMat.release();
        blurMat.release();
//      cannyMat.release();

        return threshMat;
    }


    /**
     * Crop the image by the specific size of the contours
     * @param matRGBA current frame
     */
    public void handleDrawContoursScreen(Mat matRGBA) {
            int width = matRGBA.width() - WIDTH_CROP * 2;
            int height = matRGBA.height() - HEIGHT_CROP * 2;

            if(width > 0 && height > 0) {
//                ========================================================================

                // Store all contours
                List contours = new ArrayList<MatOfPoint>();

                Mat threshMat = applyFilters(matRGBA);

                // Find all contours
                Imgproc.findContours(threshMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                threshMat.release();

                double maxVal = 0; // Biggest area size
                int maxValIdx = 0; // Index of the biggest area

                // Search for the index with the biggest area.
                for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
                {
                    int countourLenth = ((MatOfPoint) contours.get(contourIdx)).toArray().length;

                    // Limit the amount of contour to calculate the area
                    if(countourLenth >= 4 && countourLenth <= 300) {
                        // Calculate the area.
                        double contourArea = Imgproc.contourArea((MatOfPoint)contours.get(contourIdx));
                        if (maxVal < contourArea)
                        {
                            maxVal = contourArea;
                            maxValIdx = contourIdx;
                        }
                    }
                }

                Imgproc.drawContours(mRGBA, contours, maxValIdx , new Scalar(0, 255, 255), 5);
                rectPos = (MatOfPoint)contours.get(maxValIdx);

            }
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


    /**
     * Draw a rectangle in the screen to limt the area to detect the Sudoku grade.
     * @param matRGBA The original Mat with the current frame
     */
    public void drawnRectangle(Mat matRGBA){

        Point sizeOfFigure =  new Point( WIDTH_CROP - 10, HEIGHT_CROP - 10);
        Point positionInTheScreen =  new Point(matRGBA.width() - WIDTH_CROP  , matRGBA.height() - HEIGHT_CROP);

        Imgproc.rectangle(matRGBA, new Rect(sizeOfFigure, positionInTheScreen), new Scalar(0, 255, 0, 0), 5);
    }

    /***
     * Get frame of the camera.
     * @param inputFrame Frame receive from the camera
     * @return A frame with modified content.
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d("changeColorCount", "onCameraFrame");

        switch (switchOption){
            case BUTTON_DRAWN:
                mRGBA = inputFrame.rgba();
                drawnRectangle(mRGBA);

                if(changeMethodToSave) {
                    handleDrawContoursScreen(mRGBA);
                }

                break;

            default:
                Log.e("INVALID OPTION", switchOption);
                break;
        }

        return mRGBA;
    }

    /***
     * Used to be able to use the camera of the OpenCv
     * @return
     */
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

        } else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public void onOpenGalery(View e) {
        Log.d("Open galery", "Open galery");

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 0);
    }

    public void onOpenCamera(View e) {
        Log.d("Take Photo", "Take Photo");
        if(baseLoaderCallback != null) {
            cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
            cameraBridgeViewBase.enableView();

            changeMethodToSave = true;

            Log.d("DRAW RecTangule", "Draw Rectangule in the image of the camera");
            setSwitchOption(BUTTON_DRAWN);
        }
    }

    public void onTakePhoto(View view) {
        if(changeMethodToSave) {

            List<MatOfPoint> lmop = new ArrayList<>();

            // Used to create an array with the biggest area found
            lmop.add(rectPos);

            // Create a Mat with all the colors black
            Mat matMask = new Mat(mRGBA.rows(), mRGBA.cols(), mRGBA.type(), new Scalar(0, 0, 0));

            // Apply color in the background of the contours in the matMask to know where is the area of the Sudoku
            Imgproc.fillPoly(matMask, lmop, new Scalar(0, 155, 155));

// ============================
            // Store all contours
            List contours = new ArrayList<MatOfPoint>();

            // Apply filters to remove traits not necessary
            Mat threshMat = applyFilters(matMask);

            // Find all contours
            Imgproc.findContours(threshMat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

// ============================


            // Used to set the values to crop the image that was detected in the screen
            Rect rec = Imgproc.boundingRect((MatOfPoint) contours.get(0));

            // Used to create a new Mat with the oject that was detected in the screen
            Mat matSubmat = mRGBA.submat(rec);

            // Used to set the size of the bitmap image
            Bitmap bi = Bitmap.createBitmap(matSubmat.width(), matSubmat.height(), Bitmap.Config.RGB_565);

            //Used to convert Mat to bitmap
                 Utils.matToBitmap(matSubmat, bi);

                 // Used to show in the screen the object that was croped.
                 iv.setImageBitmap(bi);

                 // Used to release memory
                matSubmat.release();

            }
    }

    public void onCloseCamera(View view){
        if(cameraBridgeViewBase != null) {
             mRGBA = Mat.zeros(mRGBA.clone().size(), CvType.CV_8U);
//            cameraBridgeViewBase.setVisibility(SurfaceView.INVISIBLE);
            cameraBridgeViewBase.disableView();
            changeMethodToSave = false;
        }
    }

}