package com.demondevelopers.example.crashreporting;

import android.os.Bundle;
import android.view.View;


public class MainActivity extends ReportingActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.crash_app).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				throw new IllegalStateException("THIS IS A CRASH!");
			}
		});
	}
}
