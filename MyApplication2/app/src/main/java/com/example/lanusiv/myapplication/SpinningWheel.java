package com.example.lanusiv.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class SpinningWheel extends Activity {

	private static String DEBUG_TAG = "Spinning wheel";

	private static Bitmap imageOriginal, imageScaled;

	private static Matrix matrix;

	private ImageView wheel;
	private int wheelHeight, wheelWidth;

	private GestureDetector detector;

	// needed for detecting the inversed rotations
	private boolean[] quadrantTouched;

	private boolean allowRotating;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// load the image only once
		if (imageOriginal == null) {
			imageOriginal = BitmapFactory.decodeResource(getResources(),
					R.drawable.p01);
		}

		// initialize the matrix only once
		if (matrix == null) {
			matrix = new Matrix();
		} else {
			// not needed, you can also post the matrix immediately to restore
			// the old state
			matrix.reset();
		}

		detector = new GestureDetector(this, new MyGestureDetector());

		// there is no 0th quadrant, to keep it simple the first value gets
		// ignored
		quadrantTouched = new boolean[]{false, false, false, false, false};

		allowRotating = true;

		wheel = (ImageView) findViewById(R.id.imageView_ring);
		wheel.setOnTouchListener(new MyOnTouchListener());
		wheel.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// method called more than once, but the values only
						// need to be initialized one time
						if (wheelHeight == 0 || wheelWidth == 0) {
							wheelHeight = wheel.getHeight();
							wheelWidth = wheel.getWidth();

							// resize
							Matrix resize = new Matrix();
							resize.postScale(
									(float) Math.min(wheelWidth, wheelHeight)
											/ (float) imageOriginal.getWidth(),
									(float) Math.min(wheelWidth, wheelHeight)
											/ (float) imageOriginal.getHeight());
							imageScaled = Bitmap.createBitmap(imageOriginal, 0,
									0, imageOriginal.getWidth(),
									imageOriginal.getHeight(), resize, false);

							// translate to the image view's center
							float translateX = wheelWidth / 2
									- imageScaled.getWidth() / 2;
							float translateY = wheelHeight / 2
									- imageScaled.getHeight() / 2;
							matrix.postTranslate(translateX, translateY);

							wheel.setImageBitmap(imageScaled);
							wheel.setImageMatrix(matrix);
						}
					}
				});

	}

	/**
	 * Rotate the wheel.
	 *
	 * @param degrees The degrees, the wheel should get rotated.
	 */
	private void rotateWheel(float degrees) {
		matrix.postRotate(degrees, wheelWidth / 2, wheelHeight / 2);

		if (allowRotating) {
			wheel.setEnabled(false);
		}

		getRewardFromWheelAngle();
		// flip coin effect
		// wheel.setRotationX(degrees); // flip from top to bottom or reversed
		// wheel.setRotationY(degrees); // flip from left to right or reversed

		wheel.setImageMatrix(matrix);
	}

	private int getRewardFromWheelAngle() {
		/**
		 * Get the matrix angle URL: http://stackoverflow.com/a/28307921/3248003
		 */
		float[] v = new float[9];

		matrix.getValues(v);

		// calculate the degree of rotation
		float rAngle = Math.round(Math.atan2(v[Matrix.MSKEW_X],
				v[Matrix.MSCALE_X]) * (180 / Math.PI));
		/**
		 * Convert 0-180 and -180-0 degrees to 0-360 URL:
		 * http://stackoverflow.com/a/25725005/3248003
		 */
		rAngle = (rAngle + 360) % 360;

		return getReward(rAngle);
	}

	private int getReward(float angle) {

		int position = 0;

		// default reward
		int reward = 0;

		//We know that we have 11 sector on our wheel, so 360/11 = ~33
		int numberOfSectors = 11;
		int circle = 360;
		float degreePerSector = (float) circle / numberOfSectors;

		//Then we can get the position of our price
		position = (int) Math.floor(angle / degreePerSector);

		switch (position) {
			case 0:
				reward = 100;
				break;
			case 1:
				reward = 200;
				break;
			case 2:
				reward = 300;
				break;
			case 3:
				reward = 400;
				break;
			case 4:
				reward = 500;
				break;
			case 5:
				reward = 600;
				break;
			case 6:
				reward = 700;
				break;
			case 7:
				reward = 800;
				break;
			case 8:
				reward = 900;
				break;
			case 9:
				reward = 1000;
				break;
			case 10:
				reward = 1100;
				break;
			default:
				break;
		}

		Log.d(DEBUG_TAG, " Position is " + position + " Reward is " + reward + " " + degreePerSector);

		return reward;
	}

	/**
	 * @return The angle of the unit circle with the image view's center
	 */
	private double getAngle(double xTouch, double yTouch) {
		double x = xTouch - (wheelWidth / 2d);
		double y = wheelHeight - yTouch - (wheelHeight / 2d);

		switch (getQuadrant(x, y)) {
			case 1:
				return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

			case 2:
			case 3:
				return 180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

			case 4:
				return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

			default:
				// ignore, does not happen
				return 0;
		}
	}

	/**
	 * @return The selected quadrant.
	 */
	private static int getQuadrant(double x, double y) {
		if (x >= 0) {
			return y >= 0 ? 1 : 4;
		} else {
			return y >= 0 ? 2 : 3;
		}
	}

	/**
	 * wheel's touch events.
	 */
	private class MyOnTouchListener implements View.OnTouchListener {

		private double startAngle;

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN:

					// reset the touched quadrants
					for (int i = 0; i < quadrantTouched.length; i++) {
						quadrantTouched[i] = false;
					}

					allowRotating = false;

					startAngle = getAngle(event.getX(), event.getY());
					break;

				case MotionEvent.ACTION_MOVE:
					double currentAngle = getAngle(event.getX(), event.getY());
					rotateWheel((float) (startAngle - currentAngle));
					startAngle = currentAngle;
					break;

				case MotionEvent.ACTION_UP:
					allowRotating = true;
					break;
			}

			// set the touched quadrant to true
			quadrantTouched[getQuadrant(event.getX() - (wheelWidth / 2),
					wheelHeight - event.getY() - (wheelHeight / 2))] = true;

			detector.onTouchEvent(event);

			return true;
		}
	}

	/**
	 * a fling event.
	 */
	private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
							   float velocityY) {

			// get the quadrant of the start and the end of the fling
			int q1 = getQuadrant(e1.getX() - (wheelWidth / 2), wheelHeight
					- e1.getY() - (wheelHeight / 2));
			int q2 = getQuadrant(e2.getX() - (wheelWidth / 2), wheelHeight
					- e2.getY() - (wheelHeight / 2));

			float velocity = velocityX + velocityY;

			// the inverse rotations
			if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math
					.abs(velocityY))
					|| (q1 == 3 && q2 == 3)
					|| (q1 == 1 && q2 == 3)
					|| (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math
					.abs(velocityY))
					|| ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
					|| ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
					|| (q1 == 2 && q2 == 4 && quadrantTouched[3])
					|| (q1 == 4 && q2 == 2 && quadrantTouched[3])) {

				startTheSpinWithDirection("inversed", velocity);

			} else {

				startTheSpinWithDirection("normal", velocity);

			}

			return true;
		}
	}

	/**
	 * Start the rotation depending on the velocity and direction
	 *
	 * @param direction - String  of normal or inverse wheel direction
	 * @param velocity  - Rotation velocity
	 */
	private void startTheSpinWithDirection(String direction, float velocity) {

		try {

			int minimumVelocityForRotation = 1000;

			//start the rotation if the velocity is more than the value above
			if (Math.abs(velocity) >= minimumVelocityForRotation) {

				Log.d(DEBUG_TAG, "Rotation starts");

				int direct = 1;

				if (direction.equals("inversed")) {
					direct = -1;
				}

				//if the design of tablet requires more then one orientation
				//set the current orientation as constant. The configuration change will reset the wheel
//				if (isTablet()) {
//					setOrientationConstant();
//				}

				//start the runnable process
				wheel.post(new FlingRunnable(direct * velocity));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A {@link Runnable} for animating the the wheel's fling.
	 */
	private class FlingRunnable implements Runnable {

		private float velocity;

		public FlingRunnable(float velocity) {
			this.velocity = velocity;
		}

		@Override
		public void run() {

			if (Math.abs(velocity) > 5 && allowRotating) {

				setOrientationConstant();

				//you can adjust the velocity (bigger value will slowing it down)
				rotateWheel(velocity / 20);

//				 velocity /= 1.0700F; //fast stopping
				velocity /= 1.0100F; // slow stopping

				// post this instance again
				wheel.post(this);

			} else {

				// Rotation ends here
				Log.d(DEBUG_TAG, "Rotation Ends ");

//				getRewardFromWheelAngle();

				//if the design of tablet requires more then one orientation
				//set the needed method for orientation
//				if (isTablet()) {
//					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//
//				}

				allowRotating = false;
				wheel.setEnabled(true);

			}
		}
	}

	@SuppressLint("NewApi")
	/**
	 * Set the current orientation to be constant
	 */
	public void setOrientationConstant() {

		try {

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}