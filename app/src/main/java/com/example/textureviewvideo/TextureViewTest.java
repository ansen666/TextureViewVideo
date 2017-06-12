package com.example.textureviewvideo;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

public class TextureViewTest extends TextureView implements SurfaceTextureListener{
	private Paint paint;//画笔
	private MediaPlayer mediaPlayer;
	
	public TextureViewTest(Context context) {
		super(context);
		init();
	}

	public TextureViewTest(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);
		init();
	}

	public TextureViewTest(Context context, AttributeSet paramAttributeSet,int paramInt) {
		super(context, paramAttributeSet, paramInt);
		init();
	}
	
	private void init() {
		paint=new Paint(6);
		setSurfaceTextureListener(this);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,int height) {
		System.out.println("onSurfaceTextureAvailable onSurfaceTextureAvailable");
		
//	      Rect rect = new Rect(0,0,width,height);
//	      Bitmap bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
//	      Surface s = new Surface(surface);
//	      if (s != null && rect != null && bitmap != null) {
//	          try {
//	              Canvas lockCanvas = s.lockCanvas(rect);
//	              synchronized (bitmap) {
//	                  if (!bitmap.isRecycled()) {
//	                      lockCanvas.drawBitmap(bitmap, null, rect,paint);
//	                  }
//	              }
//	              s.unlockCanvasAndPost(lockCanvas);
//	          } catch (Throwable e) {
//	          		e.printStackTrace();
//	          }
//	      }
		
        try {
 	    		mediaPlayer = new MediaPlayer();
 	    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	    		File file=new File(Environment.getExternalStorageDirectory()+"/ansen.mp4");
	    		mediaPlayer.setDataSource(file.getAbsolutePath());	
	    		Surface s = new Surface(surface);
            mediaPlayer.setSurface(s);
            mediaPlayer.setLooping(true);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					System.out.println("开始播放视频。。。");
					mediaPlayer.start();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,int height) {
		  System.out.println("onSurfaceTextureSizeChanged onSurfaceTextureSizeChanged");
		  
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
		System.out.println("onSurfaceTextureDestroyed onSurfaceTextureDestroyed");
		mediaPlayer.stop();
		mediaPlayer.release();
		surface=null;
		mediaPlayer=null;
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface){
		System.out.println("onSurfaceTextureUpdated onSurfaceTextureUpdated");
	}
}
