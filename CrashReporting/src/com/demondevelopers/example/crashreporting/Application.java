package com.demondevelopers.example.crashreporting;

public class Application extends android.app.Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		ReportHandler.install(this, "report@example.com");
	}
}

