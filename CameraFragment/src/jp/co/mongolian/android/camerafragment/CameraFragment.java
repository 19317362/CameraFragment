package jp.co.mongolian.android.camerafragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CameraFragment extends Fragment {
	
	private boolean isZoonEnabled;

	/**
	 * PictureCallback
	 *
	 */
	public interface OnPictureTakenListener {

		/**
		 * @param data jpeg image data cropped into fragment view size
		 */
		public void onPictureTaken(Bitmap jpegBitmap);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("zoom_enabled", isZoonEnabled);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			isZoonEnabled = savedInstanceState.getBoolean("zoom_enabled", false);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_camera, null, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		CameraView camera = getCameraView(); 
		if (camera != null) {
			camera.onResume();
			camera.setZoomEnabled(isZoonEnabled);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		CameraView camera = getCameraView(); 
		if (camera != null) camera.onPause();
	}

	/**
	 * start preview
	 */
	public void startPreview() {
		CameraView camera = getCameraView(); 
		if (camera != null) camera.startPreview();
	}

	/**
	 * stop preview
	 */
	public void stopPreview() {
		CameraView camera = getCameraView(); 
		if (camera != null) camera.stopPreview();
	}
	
	
	/**
	 * take a picture
	 * or nothing happen if the camera is not available now. 
	 * @param shutter
	 * @param raw
	 * @param jpeg
	 */
	public void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg) {
		CameraView camera = getCameraView(); 
		if (camera != null) camera.takePicture(shutter, raw, jpeg);
	}

	/**
	 * start auto focus
	 * @param callback
	 */
	public void autoFocus(AutoFocusCallback cb) {
		CameraView camera = getCameraView(); 
		if (camera != null) camera.autoFocus(cb);
	}

	/**
	 * set preview callback on camera 
	 * @param cb
	 */
	public void setPreviewCallback(PreviewCallback cb) {
		CameraView camera = getCameraView(); 
		if (camera != null) camera.setPreviewCallback(cb);
	}
	
	private CameraView getCameraView() {
		View view = getView();
		if (view == null) return null;
		return (CameraView)getView().findViewById(R.id.camera_view); 
	}
}