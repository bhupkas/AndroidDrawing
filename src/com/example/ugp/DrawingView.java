package com.example.ugp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.view.View;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

public class DrawingView extends View {
	
	//drawing path
	private Path drawPath;
	//drawing and canvas paint
	private Paint drawPaint, drawPaint2 ;
	private Paint  canvasPaint2,canvasPaint;
	//initial color
	private int paintColor = this.getResources().getColor(com.example.ugp.R.color.Black);//0xFF660000;
	//canvas
	private Canvas drawCanvas,projection;
	//canvas bitmap
	private Bitmap canvasBitmap,canvasBitmap2;
	
	private boolean draw=false;
	
	private float brushSize, lastBrushSize;
	
	private AssetManager assetManager;
	
	private nativeShadow ns;
	
	
	public DrawingView(Context context, AttributeSet attrs){
	    super(context, attrs);
	    setupDrawing();
	    assetManager = context.getAssets();
	    ns = new nativeShadow(assetManager);
	     
	}
	
	private void setupDrawing(){
		//get drawing area setup for interaction 
		brushSize = getResources().getInteger(R.integer.small_size);
		lastBrushSize = brushSize;
		drawPath = new Path();
		
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		
		drawPaint2 = new Paint();
		drawPaint2.setColor(Color.BLACK);
		drawPaint2.setAntiAlias(true);
		drawPaint2.setStrokeWidth(brushSize);
		drawPaint2.setStyle(Paint.Style.STROKE);
		drawPaint2.setStrokeJoin(Paint.Join.ROUND);
		drawPaint2.setStrokeCap(Paint.Cap.ROUND);
		
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		canvasPaint2 = new Paint(Paint.DITHER_FLAG);
		
	}
	public void startNew(){
	    drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    projection.drawColor(0, PorterDuff.Mode.CLEAR);
	    
	    invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	//view given size
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		canvasBitmap2 = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		projection = new Canvas(canvasBitmap2);
	//	projectShadow();
	}
	
	protected void projectShadow(float x,float y){
		//create bitmap;
		drawCanvas.drawColor(Color.WHITE);
		//super.onDraw(projection);

		/*String pathName =  Environment.getExternalStorageDirectory().getAbsolutePath() + ns.getShadow(); 
              //  "/Download/sketchpad3.png";
		//String pathName = "/storage/emulated/0/Download/aPRvK.jpg";
		Bitmap b = BitmapFactory.decodeFile(pathName);
		*/
		Bitmap b ;
		Log.d("reached", "shadow1");
		int possible= 1;//ns.Shadow(canvasBitmap2);
		if(possible==1)
			  b = BitmapFactory.decodeResource(getResources(), R.drawable.invert1);
		  else if(possible==2)
			  b = BitmapFactory.decodeResource(getResources(), R.drawable.invert2);
		  else
			  b = BitmapFactory.decodeResource(getResources(), R.drawable.invert3);
		Paint p=new Paint();
		p.setColor(Color.BLACK);
		
        drawCanvas.drawBitmap(b, x-(b.getHeight()/2), y-(b.getWidth()/2), p);
        drawCanvas.drawBitmap(canvasBitmap2, 0, 0, canvasPaint2);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	//draw view
		if(draw == false){
			canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
			canvas.drawPath(drawPath, drawPaint);
			//saveImage(canvasBitmap);
		}
		else{
			canvas.drawBitmap(canvasBitmap2, 0, 0, canvasPaint2);
			canvas.drawPath(drawPath, drawPaint2);
			saveImage(canvasBitmap2);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//detect user touch     
		float touchX = event.getX();
		float touchY = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		    drawPath.moveTo(touchX, touchY);
		    break;
		case MotionEvent.ACTION_MOVE:
		    drawPath.lineTo(touchX, touchY);
		    //projectShadow(touchX,touchY);
		    break;
		case MotionEvent.ACTION_UP:
		    drawCanvas.drawPath(drawPath, drawPaint);
		    draw = true;
		    projection.drawPath(drawPath, drawPaint2);
		    saveImage(canvasBitmap2);
		    draw = false;
		    drawPath.reset();
		    projectShadow(touchX,touchY);
		    //projectShadow();
		    break;
		default:
		    return false;
		}
		invalidate();
		return true;
	}
	
	public void setBrushSize(float newSize){
		//update size
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
			    newSize, getResources().getDisplayMetrics());
			brushSize=pixelAmount;
			drawPaint.setStrokeWidth(brushSize);
	}
	public void setLastBrushSize(float lastSize){
	    lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
	    return lastBrushSize;
	}
	
	private void saveImage(Bitmap bmp){
		
		
		String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + 
                "/Download";
		File dir = new File(file_path);
		if(!dir.exists())
		dir.mkdirs();
		File file = new File(dir, "sketchpad"  + ".png");
		FileOutputStream fOut;
		try {
			this.setDrawingCacheEnabled(true);
			//this.buildDrawingCache();
			fOut = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			this.setDrawingCacheEnabled(false);
			Log.d("This is the output", file_path);
			try {
				fOut.flush();
				fOut.close();
			}catch (IOException e) {
		        e.printStackTrace();
		    }
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
