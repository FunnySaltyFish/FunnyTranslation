package com.funny.translation.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileOutputStream;
import java.net.MalformedURLException;

public class HttpUtil{
    /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
										  "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
										connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
				result.append("\n");
				
            }
			System.out.printf("get结果：%s\n",result);
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //1.获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            //2.中文有乱码的需要将PrintWriter改为如下
            //out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8")
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        //System.out.println("post推送结果："+result);
        return result;
    }
	
	public static boolean isAvailable(final String url){
		boolean is=true;
		new Thread(new Runnable(){
				@Override
				public void run()
				{
					// TODO: Implement this method
					try
					{
						URLConnection connection=new URL(url).openConnection();
						connection.connect();
					}
					catch (IOException e)
					{
						
					}
				}
		}).start();
		return is;
	}
	
	public static File downloadFile(String urlPath, String downloadDir) {
        File file = null;
        HttpURLConnection httpURLConnection = null;
        BufferedInputStream bin = null;
        OutputStream out = null;

        try {
            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            httpURLConnection = (HttpURLConnection) urlConnection;
            // 设定请求的方法，默认是GET
            httpURLConnection.setRequestMethod("POST");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();

            //获取相应码
            int responseCode = httpURLConnection.getResponseCode();
            if(responseCode!=200){
                return null;
            }

            // 文件大小
            int fileLength = httpURLConnection.getContentLength();

            // 文件名
            //String filePathUrl = httpURLConnection.getURL().getFile();
            //String fileFullName = filePathUrl.substring(filePathUrl.lastIndexOf(File.separatorChar) + 1);

            System.out.println("file length---->" + fileLength);

            bin = new BufferedInputStream(httpURLConnection.getInputStream());

            //String path = downloadDir + File.separatorChar + fileFullName;
            //文件名称
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String fileName = sdf.format(d)+".mp3";
            String path = downloadDir + File.separatorChar +fileName;
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len = len+size;
                out.write(buf, 0, size);
                // 打印下载百分比
                // System.out.println("下载了-------> " + len * 100 / fileLength +
                // "%\n");
            }
            System.out.println(file.getAbsolutePath()+"--下载成功");
            bin.close();
            out.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //释放资源
            try {
				if (bin!=null) {
					bin.close();
                }
				if (out!=null) {
					out.close();
				}
				if (httpURLConnection!=null) {
					httpURLConnection.disconnect();
					httpURLConnection = null;
				}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
