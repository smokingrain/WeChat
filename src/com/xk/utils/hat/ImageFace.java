package com.xk.utils.hat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

public class ImageFace {

	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static List<Rect> findFaces(String filePath) {
		if(StringUtil.isBlank(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if(!file.exists() || file.isDirectory()) {
			return null;
		}
		CascadeClassifier faceCatDetector = new CascadeClassifier("D:/Program Files/opencv/opencv/build/etc/lbpcascades/lbpcascade_frontalcatface.xml");
		CascadeClassifier faceDetector = new CascadeClassifier("D:/Program Files/opencv/opencv/build/etc/lbpcascades/lbpcascade_frontalface.xml");
//		CascadeClassifier faceImproDetector = new CascadeClassifier("D:/Program Files/opencv/opencv/build/etc/lbpcascades/lbpcascade_frontalface_improved.xml");
		CascadeClassifier faceProfDetector = new CascadeClassifier("D:/Program Files/opencv/opencv/build/etc/lbpcascades/lbpcascade_profileface.xml");
		CascadeClassifier faceSliDetector = new CascadeClassifier("D:/Program Files/opencv/opencv/build/etc/lbpcascades/lbpcascade_silverware.xml");
		List<Rect> faces = new ArrayList<Rect>();
		Mat image = Imgcodecs.imread(filePath);
		MatOfRect faceDetections = new MatOfRect();
		faceCatDetector.detectMultiScale(image, faceDetections);
        faces.addAll(faceDetections.toList());
        faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        faces.addAll(faceDetections.toList());
//        faceDetections = new MatOfRect();
//        faceImproDetector.detectMultiScale(image, faceDetections);
//        faces.addAll(faceDetections.toList());
        faceDetections = new MatOfRect();
        faceProfDetector.detectMultiScale(image, faceDetections);
        faces.addAll(faceDetections.toList());
        faceDetections = new MatOfRect();
        faceSliDetector.detectMultiScale(image, faceDetections);
        faces.addAll(faceDetections.toList());
        return faces;
	}
	
}
