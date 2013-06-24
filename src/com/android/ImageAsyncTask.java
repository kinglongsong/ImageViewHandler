package com.android;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {

	String imagePath;
	ImageView imageView;

	public ImageAsyncTask(String imagePath, ImageView imageView) {

		this.imagePath = imagePath;
		this.imageView = imageView;

	}

	@Override
	protected Bitmap doInBackground(Void... arg0) {
		return BitmapGenerator.generateCompressedBitmap(imagePath, 800);
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		this.imageView.setImageBitmap(result);
		super.onPostExecute(result);
	}

}
