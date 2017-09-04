package com.example.textureviewvideo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends Activity implements SurfaceTextureListener{
	private final String Tag = MainActivity.class.getSimpleName();
	private MediaPlayer mMediaPlayer;
	private Surface surface;
	
	private ImageView videoImage;
	private SeekBar seekBar;

	private Handler handler=new Handler();

	private final Runnable mTicker = new Runnable() {
		public void run() {
			long now = SystemClock.uptimeMillis();
			long next = now + (1000 - now % 1000);

			handler.postAtTime(mTicker,next);//延迟一秒再次执行runnable,就跟计时器一样效果

			if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
				seekBar.setProgress(mMediaPlayer.getCurrentPosition());//更新播放进度
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextureView textureView=(TextureView) findViewById(R.id.textureview);
		textureView.setSurfaceTextureListener(this);//设置监听函数  重写4个方法

		videoImage=(ImageView) findViewById(R.id.video_image);

		seekBar= (SeekBar) findViewById(R.id.seekbar);
		seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
	}

	private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
				mMediaPlayer.seekTo(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	};
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,int height) {
		Log.i(Tag,"onSurfaceTextureAvailable");
		surface=new Surface(surfaceTexture);
		new PlayerVideo().start();//开启一个线程去播放视频

		handler.post(mTicker);//更新进度
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,int height) {
		Log.i(Tag,"onSurfaceTextureSizeChanged");
	}
	
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
		Log.i(Tag,"onSurfaceTextureDestroyed");
		surface=null;
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer=null;
		return true;
	}
	
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
		Log.i(Tag,"onSurfaceTextureUpdated");
	}

	private class PlayerVideo extends Thread{
		@Override
		public void run(){
			 try {
				  File file=new File(Environment.getExternalStorageDirectory()+"/ansen.mp4");
				  if(!file.exists()){//文件不存在 从assets下复制到sdcard上
					  copyFile();
				  }
				  mMediaPlayer= new MediaPlayer();
				  mMediaPlayer.setDataSource(file.getAbsolutePath());//设置播放路径
				  mMediaPlayer.setSurface(surface);
				  mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				  mMediaPlayer.setOnCompletionListener(onCompletionListener);//播放完成监听
				  mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp){//预加载完成
						videoImage.setVisibility(View.GONE);
						mMediaPlayer.start();//开始播放

						seekBar.setMax(mMediaPlayer.getDuration());//设置总进度
					}
				  });
				  mMediaPlayer.prepare();
			  } catch (Exception e) {  
				  e.printStackTrace();
			  }
	    }
	}

	private MediaPlayer.OnCompletionListener onCompletionListener=new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			Log.i(Tag,"播放完成,显示图片");
			videoImage.setVisibility(View.VISIBLE);
			seekBar.setProgress(0);
			handler.removeCallbacks(mTicker);
		}
	};

	/**
	 * 如果sdcard没有文件就复制过去
	 */
	private void copyFile() {
	    AssetManager assetManager = this.getAssets();
	    try {
			InputStream in = assetManager.open("ansen.mp4");
	        String newFileName = Environment.getExternalStorageDirectory()+"/ansen.mp4";
			OutputStream out = new FileOutputStream(newFileName);
	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        Log.e(Tag, e.getMessage());
	    }
	}
}
