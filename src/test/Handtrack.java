package test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Handtrack {

	static {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	static class ImagePanel extends JPanel {

		public BufferedImage image;

		/**
		 * 
		 */
		private static final long serialVersionUID = 2763509481815891725L;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (image != null)
				g.drawImage(image, 0, 0, null); // see javadoc for more info on
												// the
												// parameters
		}
	}

	private JFrame frame;
	private ScheduledExecutorService timer;

	  private HandDetector detector = null; 
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Handtrack window = new Handtrack();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Handtrack() {
		detector = new HandDetector("gloveHSV.txt", 640, 480);
		initialize();
		initCV();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new ImagePanel(){
			  public void paintComponent(Graphics g)
			  { 
			    super.paintComponent(g);
			    Graphics2D g2d = (Graphics2D) g;
			    if (detector != null)
			      detector.draw(g2d); 
			  }
		};
		frame.getContentPane().add(panel, BorderLayout.CENTER);
	}

	// settings
	private Scalar lower = new Scalar(0, 0, 0);
	private Scalar upper = new Scalar(20, 255, 255);
	private Scalar range = new Scalar(110, 200, 60);
	private Scalar mean = new Scalar(90, 170, 17);


	private Mat cap = new Mat();
	private ImagePanel panel;
	private VideoCapture capture;
	private void calibrate(Mat im) {

		java.awt.Point d = frame.getMousePosition();
		if (d == null)
			d = new java.awt.Point(100, 100);
		Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2HSV);
		Mat rect = new Mat(im, new Rect(new Point(d.getX(), d.getY()), new Point(d.getX() + 50, d.getY() + 50)));
		mean = Core.mean(rect);
		int i;
		i = 0;
		lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
		i = 1;
		lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
		i = 2;
		lower.val[i] = Math.max(0, mean.val[i] - range.val[i]);
		i = 0;
		upper.val[i] = Math.min(179, mean.val[i] + range.val[i]);
		i = 1;
		upper.val[i] = Math.min(255, mean.val[i] + range.val[i]);
		i = 2;
		upper.val[i] = Math.min(255, mean.val[i] + range.val[i]);

		Imgproc.cvtColor(im, im, Imgproc.COLOR_HSV2BGR);

	}

	private void initCV() {
		capture = new VideoCapture();
		capture.open(0);
		if (capture.isOpened()) {
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {
					try {
						capture.read(cap);
						// Core.flip(cap, cap, 1);

						BufferedImage buffer;
						if (panel.image == null) {
							buffer = new BufferedImage(cap.width(), cap.height(), BufferedImage.TYPE_3BYTE_BGR);
							panel.image = buffer;
						}
						buffer = panel.image;
						detector.update(buffer);
						byte[] data = ((DataBufferByte) buffer.getRaster().getDataBuffer()).getData();
						cap.get(0, 0, data);
						frame.repaint();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
		}
	}

}