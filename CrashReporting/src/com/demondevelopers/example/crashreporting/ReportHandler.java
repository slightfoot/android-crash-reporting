package com.demondevelopers.example.crashreporting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.Window;


public class ReportHandler
{
	private static final String TAG = ReportHandler.class.getSimpleName();
	
	private static final String[] EVENT_LOG_CMD  = { "logcat", "-d", "-b", "events", "-v", "time" };
	private static final String[] SYSTEM_LOG_CMD = { "logcat", "-d", "-v", "time" };
	
	private static Context sAppContext;
	private static String  sEmailAddress;
	
	private static volatile boolean mCrashing = false;
	
	
	public static void install(Context context, String emailAddress)
	{
		sAppContext   = context.getApplicationContext();
		sEmailAddress = emailAddress;
		// NOTE: It does not generate crash reports when you are debugging your app.
		if(!Debug.waitingForDebugger() && !Debug.isDebuggerConnected()){
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler());
		}
	}
	
	public static String getEmailAddress()
	{
		return sEmailAddress;
	}
	
	
	private static class UncaughtHandler implements UncaughtExceptionHandler
	{
		public void uncaughtException(Thread thread, Throwable ex)
		{
			try{
				// Don't re-enter -- avoid infinite loops if crash-reporting crashes.
				if(mCrashing){
					return;
				}
				mCrashing = true;
				Log.e(TAG, "FATAL EXCEPTION: " + thread.getName(), ex);
				// Attempt to save a screenshot (no permissions required!)
				String screenshot = null;
				Bitmap bm = ReportHandler.getScreenshot();
				if(bm != null){
					screenshot = ReportHandler.saveScreenShot(bm);
					bm.recycle();
				}
				// Bring up crash dialog
				sAppContext.startActivity(ReportActivity
					.createIntent(sAppContext, ex, screenshot));
			}
			catch(Throwable t2){
				try{
					Log.e(TAG, "Error reporting crash", t2);
				}
				catch(Throwable t3){
					// Even Log.e() fails! Oh well.
				}
			}
			finally{
				// Try everything to make sure this process goes away.
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(10); // magic numbers!
			}
		}
	}
	
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void showDefaultReportActivity(Context context, Throwable th)
	{
		String packageName = context.getPackageName();
		PackageManager pm = context.getPackageManager();
		
		// Not perfect.. but it'll have to do.
		ApplicationErrorReport report = new ApplicationErrorReport();
		report.installerPackageName   = pm.getInstallerPackageName(packageName);
		report.packageName = packageName;
		report.processName = packageName;
		report.time        = System.currentTimeMillis();
		report.systemApp   = false;
		report.type        = ApplicationErrorReport.TYPE_CRASH;
		report.crashInfo   = new ApplicationErrorReport.CrashInfo(th);
		
		sAppContext.startActivity(new Intent(Intent.ACTION_APP_ERROR)
			.setPackage("com.google.android.feedback")
			.putExtra(Intent.EXTRA_BUG_REPORT, report)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
	
	public static Bitmap getScreenshot()
	{
		Activity activity = ReportingActivity.getForegroundInstance();
		if(activity == null){
			return null;
		}
		Window window = activity.getWindow();
		if(window == null){
			return null;
		}
		View view = window.getDecorView();
		if(view == null){
			return null;
		}
		view.buildDrawingCache();
		Bitmap cache = view.getDrawingCache();
		Bitmap screenshot = cache.copy(cache.getConfig(), false);
		cache = null;
		if(!view.isDrawingCacheEnabled()){
			view.destroyDrawingCache();
		}
		return screenshot;
	}
	
	public static String saveScreenShot(Bitmap bitmap)
	{
		FileOutputStream stream = null;
		try{
			File temp = File.createTempFile("crash-report", ".jpg");
			stream = new FileOutputStream(temp);
			bitmap.compress(CompressFormat.JPEG, 80, stream);
			return temp.getAbsolutePath();
		}
		catch(IOException e){
			Log.e(TAG, e.getMessage(), e);
		}
		finally{
			if(stream != null){
				try{
					stream.close();
				}
				catch(IOException e){
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		return null;
	}
	
	public static String saveEventLog()
	{
		return captureCommand("event-log", EVENT_LOG_CMD);
	}
	
	public static String saveSystemLog()
	{
		return captureCommand("system-log", SYSTEM_LOG_CMD);
	}
	
	private static String captureCommand(String filePrefix, String[] command)
	{
		Process process = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try{
			File temp = File.createTempFile(filePrefix, ".txt");
			fos = new FileOutputStream(temp);
			process = new ProcessBuilder(command)
				.redirectErrorStream(true)
				.start();
			is = process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
			String line;
			while((line = reader.readLine()) != null){
				writer.append(line).append('\n');
			}
			writer.flush();
			
			return temp.getAbsolutePath();
		}
		catch(IOException e){
			Log.e(TAG, e.getMessage(), e);
		}
		finally{
			if(process != null){
				process.destroy();
			}
			if(fos != null){
				try{
					fos.close();
				}
				catch(IOException e){
					Log.e(TAG, e.getMessage(), e);
				}
			}
			if(is != null){
				try{
					is.close();
				}
				catch(IOException e){
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		
		return null;
	}
}
