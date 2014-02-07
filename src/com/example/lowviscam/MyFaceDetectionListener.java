package com.example.lowviscam;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;



class MyFaceDetectionListener implements Camera.FaceDetectionListener {
	
    @Override
    public void onFaceDetection(Face[] faces, Camera camera) {
    	
        if (faces.length > 0){
            Log.d("FaceDetection", "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    " Y: " + faces[0].rect.centerY() );
            // Future work to say "X faces detected."

            //ma.tts.speak("Face", TextToSpeech.QUEUE_FLUSH, null);

            /*// from "Implementing Face Detection in Android"
            // http://software.intel.com/en-us/blogs/2013/10/28/implementing-face-detection-in-android
            PointF tmp_point = new PointF();
            Paint tmp_paint = new Paint();
            for (int i = 0; i < faces.length; i++) {
            	Face face = faces[i];
            	tmp_paint.setColor(Color.RED);
            	tmp_paint.setAlpha(100);
            	face.getMidPoint(tmp_point);
            	Object canvas;
            	Point leftEye;
				canvas.drawPoint(leftEye.x, leftEye.y, tmp_paint);
            	}*/
        }
    }
}