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
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.i(Tag,"onStartTrackingTouch");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
				mMediaPlayer.seekTo(seekBar.getProgress());
			}
		}
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
	}
	
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
		surface=null;
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mMediaPlayer=null;
		return true;
	}
	
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
	}

	private class PlayerVideo extends Thread{
		@Override
		public void run(){
			 try {
				  String newFilePath = Environment.getExternalStorageDirectory()+"/ansen.mp4";
				  copyFile(newFilePath);//从assets下复制到sdcard上

				  mMediaPlayer= new MediaPlayer();
				  mMediaPlayer.setDataSource(newFilePath);//设置播放路径
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
			videoImage.setVisibility(View.VISIBLE);
			seekBar.setProgress(0);
			handler.removeCallbacks(mTicker);
		}
	};

	/**
	 * 如果sdcard没有文件就复制过去
	 */
	private void copyFile(String newFilePath) {
	    AssetManager assetManager = this.getAssets();
	    try {
			File file=new File(newFilePath);
			if(!file.exists()){
				InputStream in = assetManager.open("ansen.mp4");
				OutputStream out = new FileOutputStream(file);
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
			}
	    } catch (Exception e) {
	        Log.e(Tag, e.getMessage());
	    }
	}
}
