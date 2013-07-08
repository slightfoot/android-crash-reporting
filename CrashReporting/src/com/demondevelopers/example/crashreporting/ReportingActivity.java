package com.demondevelopers.example.crashreporting;

import android.app.Activity;


/**
 * Only required if you want the capability to capture screen-shots during the crash.
 */
public class ReportingActivity extends Activity
{
	private static Activity sForegroundInstance;
	
	public static Activity getForegroundInstance()
	{
		return sForegroundInstance;
	}
	
	protected void onPause()
	{
		super.onPause();
		sForegroundInstance = null;
	}
	
	protected void onResume()
	{
		super.onResume();
		sForegroundInstance = this;
	}
}
