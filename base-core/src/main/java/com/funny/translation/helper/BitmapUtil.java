package com.funny.translation.helper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.BitmapFactory;
import android.content.res.Resources ;
import android.graphics.Matrix;
import android.graphics.Path;

import java.io.ByteArrayOutputStream;

public class BitmapUtil
{
	public static Bitmap getSquareBitmap(Bitmap bitmap){
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		if(w==h){return bitmap;}
		Bitmap result=null;
		Canvas canvas=new Canvas(bitmap);
		Paint p=new Paint();
		p.setAntiAlias(true);
		if(w>h){
			result=Bitmap.createBitmap(h,h,Bitmap.Config.ARGB_8888);
			int x=(w-h)/2;//宽高差值的一半
			canvas.drawRect(w>>1-x,0,w>>1+x,h,p);
		}else{
			result=Bitmap.createBitmap(w,w,Bitmap.Config.ARGB_8888);
			int x=(h-w)/2;//宽高差值的一半
			canvas.drawRect(0,h>>1-x,w,h>>1+x,p);
		}
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap,0,0,p);
		return result;
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap){
		int l=Math.max(bitmap.getHeight(),bitmap.getWidth());
		Bitmap result=Bitmap.createBitmap(l,l,Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(result);
		Paint p=new Paint();
		p.setAntiAlias(true);
		canvas.drawCircle(l/2,l/2,l/2,p);
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap,0,0,p);
		return result;
	}

	public static Bitmap getBitmapFromResources(Resources re,int id){
		Bitmap b=BitmapFactory.decodeResource(re,id);
		return b;
	}
	
	public static Bitmap getBitmapFromResources(Resources re,int id,int targetWidth,int targetHeight){
		Bitmap b=getScaledBitmap(BitmapFactory.decodeResource(re,id),targetWidth,targetHeight);
		return b;
	}
	

	public static Bitmap getScaledBitmap(Bitmap bitmap,int targetWidth,int targetHeight){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		float scaleWidth = ((float)targetWidth)/width;
		float scaleHeight = ((float)targetHeight)/height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
		return scaledBitmap;
	}

	public static Bitmap getBigBitmapFromResources(Resources re,int id,int targetWidth,int targetHeight){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(re, id, options);
		//现在原始宽高以存储在了options对象的outWidth和outHeight实例域中
		int rawWidth = options.outWidth;
		int rawHeight = options.outHeight;
		int inSampleSize = 1;
		if (rawWidth > targetWidth || rawHeight > targetHeight) {
			float ratioHeight = (float) rawHeight / targetHeight;
			float ratioWidth = (float) rawWidth / targetWidth;
			inSampleSize = (int) Math.min(ratioWidth, ratioHeight);
		}
		options.inSampleSize=inSampleSize;
		options.inJustDecodeBounds=false;
		Bitmap b=BitmapFactory.decodeResource(re,id,options);
		return b;
	}

	/**
	 * 质量压缩方法
	 *
	 * @param image
	 * @return
	 */
	public static byte[] compressImage(Bitmap image, Long maxSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		byte[] bytes;
		while ((bytes = baos.toByteArray()).length > maxSize) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();//重置baos即清空baos
			//第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差 ，第三个参数：保存压缩后的数据的流
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		image.recycle();
		return bytes;
	}

	public static Bitmap getHexagonBitmap(Bitmap bitmap){
		int l=Math.max(bitmap.getHeight(),bitmap.getWidth());
		Bitmap result=Bitmap.createBitmap(l,l,Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(result);
		Path path=new Path();
		Paint p=new Paint();
		p.setAntiAlias(true);
		l=l/2;//六边形边长
		float h=l*0.866f;
		float d=l/2;
		path.moveTo(d,0);
		path.rLineTo(l,0);
		path.rLineTo(d,h);
		path.rLineTo(-d,h);
		path.rLineTo(-l,0);
		path.rLineTo(-d,-h);
		path.close();
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap,0,0,p);
		return result;
	}

}
