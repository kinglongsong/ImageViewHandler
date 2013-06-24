package com.android.imageviewhandler;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ImageViewHandler implements OnTouchListener, OnDoubleTapListener,
		OnGestureListener, OnClickListener {

	ImageView imageView;
	GestureDetector detector;

	Matrix currentMatrix = new Matrix();
	Matrix processMatrix = new Matrix();

	RectF currentRect;

	float current_point_x;
	float current_point_y;

	float start_distance;

	int mShortAnimationDuration;

	ImageViewHandlerListener imageViewHandlerListener;

	enum shape {
		landscape, vertical
	}

	private boolean scale_switch_on = true;
	private boolean drag_switch_on = true;

	public ImageViewHandler(ImageView imageView) {

		this.imageView = imageView;
		this.imageView.setOnTouchListener(this);
		this.imageView.setScaleType(ScaleType.FIT_CENTER);
		detector = new GestureDetector(this.imageView.getContext(), this);
		detector.setOnDoubleTapListener(this);

		mShortAnimationDuration = imageView.getContext().getResources()
				.getInteger(android.R.integer.config_shortAnimTime);

		this.allowParentInterceptTouchEvent(false);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getPointerCount() > 2) {
			return true;
		}

		this.update(event);

		return true;

	}

	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void scale(MotionEvent event) {

		if (!this.scale_switch_on) {
			return;
		}

		if (this.imageViewHandlerListener != null) {
			this.imageViewHandlerListener.onScale();
		}

		this.allowParentInterceptTouchEvent(false);

		this.imageView.setScaleType(ScaleType.MATRIX);

		float newDistance = this.getDistance(event);

		if (newDistance > 10f) {
			this.processMatrix.set(this.currentMatrix);
			float scale = newDistance / start_distance;
			processMatrix.postScale(scale, scale,
					this.getImageViewCenterPointF().x,
					this.getImageViewCenterPointF().y);
			this.imageView.setImageMatrix(processMatrix);
		}

	}

	// private PointF getMidPointF(MotionEvent event) {
	//
	// float x = event.getX(0) + event.getX(1);
	// float y = event.getY(0) + event.getY(1);
	// PointF f = new PointF();
	// f.set(x / 2, y / 2);
	// return f;
	//
	// }

	private void drag(MotionEvent event) {

		if (!this.drag_switch_on) {
			return;
		}

		if (this.imageViewHandlerListener != null) {
			this.imageViewHandlerListener.onDrag();
		}

		if (this.imageView.getScaleType() == ScaleType.FIT_CENTER) {
			return;
		}

		this.imageView.setScaleType(ScaleType.MATRIX);

		float dx = event.getX() - this.current_point_x;
		float dy = event.getY() - this.current_point_y;
		float moveDistance = (float) Math.sqrt(dx * dx + dy * dy);
		if ((moveDistance) > ViewConfiguration.get(this.imageView.getContext())
				.getScaledTouchSlop()) {

			if (this.isFullScreen()) {

				if (!this.fullScreenReachBound(dx, dy)) {
					this.processMatrix.set(this.currentMatrix);
					this.processMatrix.postTranslate(dx, dy);
					this.imageView.setImageMatrix(processMatrix);

					this.allowParentInterceptTouchEvent(false);

				} else {

					this.allowParentInterceptTouchEvent(true);
				}

			} else {

				if (this.getImageShape() == shape.landscape.ordinal()) {
					if (!this.landscapeReachBound(dx)) {
						this.processMatrix.set(this.currentMatrix);
						this.processMatrix.postTranslate(dx, 0);
						this.imageView.setImageMatrix(processMatrix);

						this.allowParentInterceptTouchEvent(false);

					} else {

						this.allowParentInterceptTouchEvent(true);
					}

				} else if (this.getImageShape() == shape.vertical.ordinal()) {
					if (!this.verticalReachBound(dy)) {
						this.processMatrix.set(this.currentMatrix);
						this.processMatrix.postTranslate(0, dy);
						this.imageView.setImageMatrix(processMatrix);

						this.allowParentInterceptTouchEvent(false);

					} else {

						this.allowParentInterceptTouchEvent(true);
					}
				}

			}

		}

	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (this.imageView.getScaleType() == ScaleType.FIT_CENTER) {
			this.imageView.setScaleType(ScaleType.CENTER);
			this.allowParentInterceptTouchEvent(false);
		} else {
			this.imageView.setScaleType(ScaleType.FIT_CENTER);
			this.allowParentInterceptTouchEvent(true);
		}

		if (this.imageViewHandlerListener != null) {
			this.imageViewHandlerListener.onDoubleTap();
		}

		Log.d("test", "onDoubleTap");
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.d("test", "onDoubleTapEvent");
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.d("test", "onSingleTapConfirmed");

		if (this.imageViewHandlerListener != null) {
			this.imageViewHandlerListener.onOneTap();
		}

		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.d("test", "onDown");
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.d("test", "onFling");
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.d("test", "onScroll");
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.d("test", "onSingleTapUp");

		return true;
	}

	private boolean isTooSmall() {
		boolean result = false;
		if ((this.currentRect.left > 0) && (currentRect.top > 0)
				&& (currentRect.right < this.imageView.getWidth())
				&& (currentRect.bottom < this.imageView.getHeight())) {
			result = true;
		}

		return result;
	}

	private RectF getRelatedRect() {
		int width = this.imageView.getDrawable().getIntrinsicWidth();
		int height = this.imageView.getDrawable().getIntrinsicHeight();
		RectF rectF = new RectF(0, 0, width, height);
		this.currentMatrix.mapRect(rectF);
		return rectF;
	}

	private void adjustDisplay() {

		if (this.isTooSmall()) {

			this.allowParentInterceptTouchEvent(true);

			this.animateToDisplay(new Runnable() {

				@Override
				public void run() {
					imageView.setScaleType(ScaleType.FIT_CENTER);
					allowParentInterceptTouchEvent(false);
				}
			});

			return;
		}

		if (!this.isFullScreen()) {

			RectF imageViewRectF = new RectF(0, 0, this.imageView.getWidth(),
					this.imageView.getHeight());

			float dx = 0;
			float dy = 0;

			float x_to_move = 0;
			float y_to_move = 0;

			if (this.getImageShape() == shape.landscape.ordinal()) {

				dy = imageViewRectF.centerY() - this.currentRect.centerY();

				if ((this.currentRect.left > 0)
						|| (this.currentRect.right < imageViewRectF.right)) {
					if ((Math.abs(this.currentRect.left)) >= (Math
							.abs(this.currentRect.right - imageViewRectF.right))) {

						x_to_move = imageViewRectF.right
								- this.currentRect.right;

					} else {
						x_to_move = -this.currentRect.left;
					}
				}

			} else {

				dx = imageViewRectF.centerX() - this.currentRect.centerX();

				if ((this.currentRect.top > 0)
						|| (this.currentRect.bottom < imageViewRectF.bottom)) {
					if ((Math.abs(this.currentRect.top)) >= (Math
							.abs(this.currentRect.bottom
									- imageViewRectF.bottom))) {

						y_to_move = imageViewRectF.bottom
								- this.currentRect.bottom;

					} else {
						y_to_move = -this.currentRect.top;
					}
				}

			}

			this.processMatrix.set(this.currentMatrix);
			this.processMatrix.postTranslate(dx, dy);
			this.processMatrix.postTranslate(x_to_move, y_to_move);

			this.animateToDisplay(new Runnable() {

				@Override
				public void run() {
					imageView.setImageMatrix(processMatrix);
				}
			});

		}
	}

	private void update(MotionEvent event) {
		switch (MotionEventCompat.getActionMasked(event)) {
		case MotionEvent.ACTION_DOWN:
			Log.d("test", "ACTION_DOWN");
			this.current_point_x = event.getX(this.getActionIndex(event));
			this.current_point_y = event.getY(this.getActionIndex(event));
			this.currentMatrix.set(this.imageView.getImageMatrix());
			this.currentRect = this.getRelatedRect();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			Log.d("test", "ACTION_POINTER_DOWN");
			this.current_point_x = event.getX(this.getActionIndex(event));
			this.current_point_y = event.getY(this.getActionIndex(event));
			this.currentMatrix.set(this.imageView.getImageMatrix());
			this.currentRect = this.getRelatedRect();
			this.start_distance = this.getDistance(event);
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 2) {
				this.scale(event);
			} else if (event.getPointerCount() == 1) {
				this.drag(event);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.d("test", "ACTION_POINTER_UP");
			int index = 0;
			index = this.getActionIndex(event);
			if (index == 0) {
				index = 1;
			} else {
				index = 0;
			}
			this.current_point_x = event.getX(index);
			this.current_point_y = event.getY(index);
			this.currentMatrix.set(this.imageView.getImageMatrix());
			this.currentRect = this.getRelatedRect();
			this.adjustDisplay();
			break;
		case MotionEvent.ACTION_UP:
			Log.d("test", "ACTION_UP");
			this.currentMatrix.set(this.imageView.getImageMatrix());
			this.currentRect = this.getRelatedRect();
			this.adjustDisplay();
			break;
		}

	}

	private int getActionIndex(MotionEvent event) {

		return MotionEventCompat.getActionIndex(event);

	}

	private void animateToDisplay(Runnable runnable) {

		this.imageView.postDelayed(runnable, 100);

	}

	public void setImageViewHandlerListener(ImageViewHandlerListener listener) {
		this.imageViewHandlerListener = listener;
	}

	private void allowParentInterceptTouchEvent(boolean allow) {
		this.imageView.getParent().requestDisallowInterceptTouchEvent(!allow);
	}

	private boolean fullScreenReachBound(float dx, float dy) {
		boolean reach = false;

		Matrix expectedProcessMatrix = new Matrix();
		expectedProcessMatrix.set(this.currentMatrix);
		expectedProcessMatrix.postTranslate(dx, dy);

		int width = this.imageView.getDrawable().getIntrinsicWidth();
		int height = this.imageView.getDrawable().getIntrinsicHeight();
		RectF rectF = new RectF(0, 0, width, height);
		expectedProcessMatrix.mapRect(rectF);

		if ((rectF.left > 0) || (rectF.top > 0)
				|| (rectF.right < this.imageView.getWidth())
				|| (rectF.bottom < this.imageView.getHeight())) {
			reach = true;
		}

		return reach;
	}

	private boolean landscapeReachBound(float dx) {
		boolean reach = false;

		Matrix expectedProcessMatrix = new Matrix();
		expectedProcessMatrix.set(this.currentMatrix);
		expectedProcessMatrix.postTranslate(dx, 0);

		int width = this.imageView.getDrawable().getIntrinsicWidth();
		int height = this.imageView.getDrawable().getIntrinsicHeight();
		RectF rectF = new RectF(0, 0, width, height);
		expectedProcessMatrix.mapRect(rectF);

		if ((rectF.left > 0) || (rectF.right < this.imageView.getWidth())) {
			reach = true;
		}

		return reach;
	}

	private boolean verticalReachBound(float dy) {
		boolean reach = false;

		Matrix expectedProcessMatrix = new Matrix();
		expectedProcessMatrix.set(this.currentMatrix);
		expectedProcessMatrix.postTranslate(0, dy);

		int width = this.imageView.getDrawable().getIntrinsicWidth();
		int height = this.imageView.getDrawable().getIntrinsicHeight();
		RectF rectF = new RectF(0, 0, width, height);
		expectedProcessMatrix.mapRect(rectF);

		if ((rectF.top > 0) || (rectF.bottom < this.imageView.getHeight())) {
			reach = true;
		}

		return reach;
	}

	private boolean isFullScreen() {
		boolean reach = false;

		if ((this.currentRect.left <= 0) && (currentRect.top <= 0)
				&& (currentRect.right >= this.imageView.getWidth())
				&& (currentRect.bottom >= this.imageView.getHeight())) {
			reach = true;
		}

		return reach;
	}

	private int getImageShape() {
		int width = this.imageView.getDrawable().getIntrinsicWidth();
		int height = this.imageView.getDrawable().getIntrinsicHeight();
		if (width >= height) {
			return shape.landscape.ordinal();
		} else {
			return shape.vertical.ordinal();
		}
	}

	private PointF getImageViewCenterPointF() {
		RectF imageViewRectF = new RectF(0, 0, this.imageView.getWidth(),
				this.imageView.getHeight());
		PointF pointF = new PointF(imageViewRectF.centerX(),
				imageViewRectF.centerY());
		return pointF;
	}

	public void setScaleSwitch(boolean on) {
		this.scale_switch_on = on;
	}

	public void setDragSwitch(boolean on) {
		this.drag_switch_on = on;
	}

	public boolean getScaleSwitch() {
		return this.scale_switch_on;
	}

	public boolean getDragSwitch() {
		return this.drag_switch_on;
	}

	@Override
	public void onClick(View v) {

		if (this.imageViewHandlerListener != null) {
			this.imageViewHandlerListener.onClick();
		}
	}

}
