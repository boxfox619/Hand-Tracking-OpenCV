package test;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;

public class Mini {

	public static void main(String[] args) {
		// Set image path
		String filename1 = "C:\\Users\\±è¼º·¡\\Desktop\\gesture\\gesture1.png";
		String filename2 = "C:\\Users\\±è¼º·¡\\Desktop\\gesture\\gesture1.png";

		int ret;
		ret = compareFeature(filename1, filename2);
		
		if (ret > 0) {
			System.out.println("Two images are same.");
		} else {
			System.out.println("Two images are different.");
		}
	}

	/**
	 * Compare that two images is similar using feature mapping  
	 * @author minikim
	 * @param filename1 - the first image
	 * @param filename2 - the second image
	 * @return integer - count that has the similarity within images 
	 */
	public static Mat bufferedImageToMat(BufferedImage bi) {
		  Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		  mat.put(0, 0, data);
		  return mat;
		}
	
	public static int compareFeature(String filename1, String filename2) {
		int retVal = 0;
		long startTime = System.currentTimeMillis();
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Load images to compare
		Mat img3 = null,img1 = null;
		try {
			img1 = bufferedImageToMat(ImageIO.read(new File(filename1)));
			img3 = bufferedImageToMat(ImageIO.read(new File(filename2)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Declare key point of images
		MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
		Mat descriptors1 = new Mat();

		Mat descriptors2 = new Mat();
		MatOfKeyPoint keypoints3 = new MatOfKeyPoint();
		FeatureDetector detector2 = FeatureDetector.create(FeatureDetector.ORB); 
		DescriptorExtractor extractor2 = DescriptorExtractor.create(DescriptorExtractor.ORB);
		detector2.detect(img3, keypoints3);
		extractor2.compute(img3, keypoints3, descriptors2);

		// Definition of ORB key point detector and descriptor extractors
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB); 
		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

		// Detect key points
		detector.detect(img1, keypoints1);
		
		// Extract descriptors
		extractor.compute(img1, keypoints1, descriptors1);

		// Definition of descriptor matcher
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		// Match points of two images
		MatOfDMatch matches = new MatOfDMatch();
//		System.out.println("Type of Image1= " + descriptors1.type() + ", Type of Image2= " + descriptors2.type());
//		System.out.println("Cols of Image1= " + descriptors1.cols() + ", Cols of Image2= " + descriptors2.cols());
		
		// Avoid to assertion failed
		// Assertion failed (type == src2.type() &amp;&amp; src1.cols == src2.cols &amp;&amp; (type == CV_32F || type == CV_8U)
		if (descriptors2.cols() == descriptors1.cols()) {
			matcher.match(descriptors1, descriptors2 ,matches);
	
			// Check matches of key points
			DMatch[] match = matches.toArray();
			double max_dist = 0; double min_dist = 100;
			
			for (int i = 0; i < descriptors1.rows(); i++) { 
				double dist = match[i].distance;
			    if( dist < min_dist ) min_dist = dist;
			    if( dist > max_dist ) max_dist = dist;
			}
			System.out.println("max_dist=" + max_dist + ", min_dist=" + min_dist);
			
		    // Extract good images (distances are under 10)
			for (int i = 0; i < descriptors1.rows(); i++) {
				
				if (match[i].distance <= 10) {
					retVal++;
				}
			}
			System.out.println("matching count=" + retVal);
		}
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("estimatedTime=" + estimatedTime + "ms");
		
		return retVal;
	}
}