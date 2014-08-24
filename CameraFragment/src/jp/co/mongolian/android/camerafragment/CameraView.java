package jp.co.mongolian.android.camerafragment;

import static android.opengl.GLES10.GL_FLOAT;
import static android.opengl.GLES10.GL_MODELVIEW;
import static android.opengl.GLES10.GL_PROJECTION;
import static android.opengl.GLES10.GL_TEXTURE_COORD_ARRAY;
import static android.opengl.GLES10.GL_TRIANGLE_STRIP;
import static android.opengl.GLES10.GL_VERTEX_ARRAY;
import static android.opengl.GLES10.glBindTexture;
import static android.opengl.GLES10.glDisable;
import static android.opengl.GLES10.glDisableClientState;
import static android.opengl.GLES10.glDrawArrays;
import static android.opengl.GLES10.glEnable;
import static android.opengl.GLES10.glEnableClientState;
import static android.opengl.GLES10.glFlush;
import static android.opengl.GLES10.glGenTextures;
import static android.opengl.GLES10.glLoadIdentity;
import static android.opengl.GLES10.glMatrixMode;
import static android.opengl.GLES10.glRotatef;
import static android.opengl.GLES10.glTexCoordPointer;
import static android.opengl.GLES10.glVertexPointer;
import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
	private static final int CAMERA_ID = 0;

	private Camera.Size mPreviewSize = null;

	protected FloatBuffer vertBuff, texBuff;

	private int textureId;
	private SurfaceTexture surfaceTexture;
	private boolean isZoomEnabled = false;

	private Camera camera;

	protected int orientation;
	private ScaleGestureDetector gestureDetector;

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);

		vertBuff = ByteBuffer.allocateDirect(4 * 4 * 3)
				.order(ByteOrder.nativeOrder()).asFloatBuffer()
				.put(new float[] { -1, -1, 0, 1, -1, 0, -1, 1, 0, 1, 1, 0 });
		texBuff = ByteBuffer.allocateDirect(4 * 4 * 2)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		setEGLConfigChooser(8, 8, 8, 0, 0, 0);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		int[] textureIds = new int[1];
		glGenTextures(1, textureIds, 0);
		textureId = textureIds[0];
	}

	@Override
	public void onResume() {
		super.onResume();

		synchronized (this) {
			surfaceTexture = new SurfaceTexture(textureId);
			surfaceTexture.setOnFrameAvailableListener(this);

			camera = Camera.open(CAMERA_ID);
			Camera.Parameters parameters = camera.getParameters();

			List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
			Collections.sort(sizes, new SizeComparator());
			mPreviewSize = sizes.get(0);

			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			parameters.setRotation(getCameraDisplay());

			camera.setParameters(parameters);

			try {
				camera.setPreviewTexture(surfaceTexture);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			camera.startPreview();

			gestureDetector = new ScaleGestureDetector(getContext(), this);
			setOnTouchListener(this);
		}
	}

	@Override
	public void onPause() {
		synchronized (this) {
			camera.stopPreview(); 
			camera.release();
			camera = null;
			surfaceTexture = null;
			gestureDetector = null;
		}

		super.onPause();
	}
	
	public void autoFocus(AutoFocusCallback callback) {
    	if (camera != null) camera.autoFocus(callback);
    }
    
    public void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg) {
    	if (camera != null) {
    		camera.cancelAutoFocus();
    		camera.takePicture(shutter, raw, jpeg);
    	}
    }

    public void setZoomEnabled(boolean enabled) {
    	isZoomEnabled = enabled;
    }

    public void setPreviewCallback(PreviewCallback cb) {
    	camera.setPreviewCallback(cb);
    }
    
    public Camera.Size getPreviewSize() {
    	return mPreviewSize;
    }
    
    public Camera.Size getPictureSize() {
    	return camera.getParameters().getPictureSize();
    }

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		boolean isPortrait = orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180;
		float prevRatio = (float)mPreviewSize.width / (float)mPreviewSize.height;
		float viewRatio =  isPortrait ? height / (float) width : width / (float) height;
		float xOffset = 0, yOffset = 0;

		if (viewRatio < prevRatio) xOffset = (1 - viewRatio / prevRatio) / 2;
		else yOffset = (1 - prevRatio / viewRatio) / 2;

		texBuff.position(0);
		texBuff.put(new float[] { xOffset, 1 - yOffset, 1 - xOffset,
				1 - yOffset, xOffset, yOffset, 1 - xOffset, yOffset });
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glEnable(GL_TEXTURE_EXTERNAL_OES);
		glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

		synchronized (this) {
			if (surfaceTexture != null) surfaceTexture.updateTexImage();
		}

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glRotatef((orientation - 1) * 90, 0, 0, 1);

		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, 0, vertBuff.position(0));

		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_FLOAT, 0, texBuff.position(0));

		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisable(GL_TEXTURE_EXTERNAL_OES);

		glFlush();
	}
	
	public void startPreview() {
		if (camera != null) camera.startPreview();
	}
	
	public void stopPreview() {
		if (camera != null) camera.stopPreview();
	}

	public void setFlashMode(String flashMode) {
		if (camera != null) {
			Camera.Parameters params = camera.getParameters();
			params.setFlashMode(flashMode);
			camera.setParameters(params);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) { // on the UI thread
		super.onSizeChanged(w, h, oldw, oldh);

		orientation = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
	}

	public int getCameraDisplay() {
		int result;

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(0, info);

		int rotation = orientation;
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;
		} else {
			result = (info.orientation - degrees + 360) % 360;
		}
		return result;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		//Measure Width
		if (widthMode == MeasureSpec.EXACTLY
				|| widthMode == MeasureSpec.AT_MOST) {
			width = widthSize;
		} else {
			width = 0;
		}

		//Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = (int)(width * 0.75);
		} else {
			height = 0;
		}

		//MUST CALL THIS
		setMeasuredDimension(width, height);
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		CameraView.this.requestRender();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (gestureDetector != null) {
			gestureDetector.onTouchEvent(event);
			return true;
		}
		return false;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		if (isZoomEnabled && camera != null) {
			float factor = gestureDetector.getScaleFactor();
			float dif = factor - 1.0f;
			int absPoint = Math.abs((int)(dif * 100));

			Camera.Parameters params = camera.getParameters();
			int zoom = params.getZoom();
			int maxZoom = params.getMaxZoom();

			int futureZoom = zoom + absPoint * (dif > 0 ? 1 : -1);

			if (futureZoom <= maxZoom && futureZoom >= 0) {
				params.setZoom(futureZoom);
				camera.startSmoothZoom(futureZoom);
			}
			camera.setParameters(params);
			return true;
		}
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		// NO-OP
		return false;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// NO-OP
	}
	
	private static class SizeComparator implements Comparator<Camera.Size> {
		@Override
		public int compare(Camera.Size a, Camera.Size b) {
			return b.width - a.width;
		}
	}
}