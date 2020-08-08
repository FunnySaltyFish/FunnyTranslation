package com.funny.translation;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BaseActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		//setTheme(R.style.AppTheme_NoActionBar);
	}

	public void moveToActivity(Class activity){
		Intent intent=new Intent();
		intent.setClass(this,activity);
		startActivity(intent);
	}

	public void moveToActivityForResult(Class activity,Intent intent,int startCode){
		intent.setClass(this,activity);
		startActivityForResult(intent,startCode);
	}
	
}
