package com.funny.translation.thread;
import com.funny.translation.utils.UpdateUtil;
import com.funny.translation.MainActivity;
import com.funny.translation.utils.ApplicationUtil;

public class UpdateThread extends Thread
{

	MainActivity ctx;
	boolean isShowResult;

	boolean haveNewVersion=false;
	public UpdateThread(MainActivity ctx)
	{
		this.ctx = ctx;
		isShowResult=false;
	}
	
	@Override
	public void run()
	{
		// TODO: Implement this method
		super.run();
		Thread t=new Thread(new Runnable(){
				@Override
				public void run()
				{
					// TODO: Implement this method
					UpdateUtil.updateDescription=UpdateUtil.getUpdateDescription();
					
				}
			});
		t.start();
		//long curTime=System.currentTimeMillis();
		//System.out.println("t开始");
		try
		{
			t.join();//确保网络访问执行完后再继续
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		haveNewVersion=UpdateUtil.checkNewVersion(ctx);
		//System.out.println("t结束，花费"+(System.currentTimeMillis()-curTime));
		while (!isShowResult){//还没有完成
			
			if(ctx.isFree()){//主Activity闲着
				ctx.runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							// TODO: Implement this method
							if(haveNewVersion){
								ctx.showUpdateDialog();
							}else{
								ApplicationUtil.print(ctx,"自动更新检测完毕，当前已是最新版本！");
							}
						}
				});
				isShowResult=true;
			}else{
				try
				{
					sleep(1000);
					//System.out.println("试图展示更新dialog——");
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
}
