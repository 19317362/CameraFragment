package jp.co.mongolian.android.camerafragmentsample;

import jp.co.mongolian.android.camerafragment.CameraFragment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener, PictureCallback {

	private View mButton;
	private ImageView mPicture;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mButton = findViewById(R.id.TakePicture); 
		mButton.setOnClickListener(this);
		
		mPicture = (ImageView)findViewById(R.id.PictureTaken);
	}

	@Override
	public void onClick(View v) {
		v.setEnabled(false);
		CameraFragment cf = (CameraFragment)getFragmentManager().findFragmentById(R.id.CameraFragment);
		cf.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Drawable d = mPicture.getDrawable();
		if (d != null && d instanceof BitmapDrawable) {
			mPicture.setImageBitmap(null);
			((BitmapDrawable)d).getBitmap().recycle();
		}
		
		Bitmap btmp = BitmapFactory.decodeByteArray(data, 0, data.length);

		mPicture.setImageBitmap(btmp);
		mButton.setEnabled(true);
		
		CameraFragment cf = (CameraFragment)getFragmentManager().findFragmentById(R.id.CameraFragment);
		cf.startPreview();
	}

}
