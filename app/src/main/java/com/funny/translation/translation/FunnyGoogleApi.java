package com.funny.translation.translation;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import com.funny.translation.bean.Consts;public class FunnyGoogleApi
{
	//部分参考自https://blog.csdn.net/u013070165/article/details/85112935
	// 实现js的charAt方法
	public static char charAt(Object obj, int index) {
		return obj.toString().charAt(index);
	}

	// 实现js的charCodeAt方法
	public static int charCodeAt(Object obj, int index) {
		return obj.toString().charAt(index);
	}

	// 实现js的Number方法
	public static int Number(Object cc) {
		try {
			long a = Long.parseLong(cc.toString());
			int b = a > 2147483647 ? (int) (a - 4294967296L) : a < -2147483647 ? (int) (a + 4294967296L) : (int) a;
			return b;
		} catch (Exception ex) {
			return 0;
		}
	}
	
	public static String b(long a, String b) {
		for (int d = 0; d < b.length() - 2; d += 3) {
			char c = b.charAt(d + 2);
			int c0 = 'a' <= c ? charCodeAt(c, 0) - 87 : Number(c);
			long c1 = '+' == b.charAt(d + 1) ? a >> c0 : a << c0;
			a = '+' == b.charAt(d) ? a + c1 & 4294967295L : a ^ c1;
		}
		a = Number(a);
		return a + "";
	}

	//关键，获取tk
	public static String tk(String a, String TKK) {
		String[] e = TKK.split("\\.");
		int d = 0;
		int h = 0;
		int[] g = new int[a.length() * 3];
		h = Number(e[0]);
		for (int f = 0; f < a.length(); f++) {
			int c = charCodeAt(a, f);
			if (128 > c) {
				g[d++] = c;
			} else {
				if (2048 > c) {
					g[d++] = c >> 6 | 192;
				} else {
					if (55296 == (c & 64512) && f + 1 < a.length() && 56320 == (charCodeAt(a, f + 1) & 64512)) {
						c = 65536 + ((c & 1023) << 10) + charCodeAt(a, ++f) & 1023;
						g[d++] = c >> 18 | 240;
						g[d++] = c >> 12 & 63 | 128;
					} else {
						g[d++] = c >> 12 | 224;
						g[d++] = c >> 6 & 63 | 128;
						g[d++] = c & 63 | 128;
					}
				}
			}
		}
		int gl = 0;
		for (int b : g) {
			if (b != 0) {
				gl++;
			}
		}
		int[] g0 = new int[gl];
		gl = 0;
		for (int c : g) {
			if (c != 0) {
				g0[gl] = c;
				gl++;
			}
		}
		long aa = h;
		for (d = 0; d < g0.length; d++) {
			aa += g0[d];
			aa = Number(b(aa, "+-a^+6"));
		}
		aa = Number(b(aa, "+-3^+b+-f"));
		long bb = aa ^ Number(e[1]);
		aa = bb;
		aa = aa + bb;
		bb = aa - bb;
		aa = aa - bb;
		if (0 > aa) {
			aa = (aa & 2147483647) + 2147483648L;
		}
		aa %= (long) 1e6;
		return aa + "" + "." + (aa ^ h);
	}
	
	public static TranslationResult formatResultGoogleComplex(String json)
	{
		TranslationResult result=new TranslationResult(Consts.ENGINE_GOOGLE);
		try
		{
			//用jsonArray解析js数组

			JSONArray all=new JSONArray(json);
			//JSONArray mainResultObj=all.getJSONArray(0);
			String basicResult=all.getJSONArray(0).getJSONArray(0).getString(0);
			System.out.println("bR:" + basicResult);
			result.setBasicResult(basicResult);
			
			if(all.getJSONArray(0).length()>1){//有注音
				JSONArray phoneticNotationArr=all.getJSONArray(0).getJSONArray(1);
				String phoneticNotation=phoneticNotationArr.getString(phoneticNotationArr.length()-1);//all.getJSONArray(1).getString(3);
				System.out.println("pN:" + phoneticNotation);
				result.setPhoneticNotation(phoneticNotation);
			}
			Object detail=all.get(1);
			System.out.println("detail:"+detail.getClass());
			if (detail.toString().equals("null"))
			{
				System.out.println("detail is null");
				
				JSONArray detailArr=all.getJSONArray(5).getJSONArray(0).getJSONArray(2);
				int length;
				String[][] detailTexts = new String[detailArr.length()][];
				for (int i=0;i <(length = detailArr.length());i++)
				{
					JSONArray eachDetail=detailArr.getJSONArray(i); //5 i
					System.out.println("eachDetail:"+eachDetail);
					
					if (eachDetail instanceof JSONArray)
					{
						String text=eachDetail.getString(0);
						detailTexts[i]=new String[1];
						detailTexts[i][0]=text;
						//System.out.println(text);
					}
				}
				showArray(detailTexts);
			}
			else if (detail instanceof JSONArray)
			{
				JSONArray detailArr=all.getJSONArray(1).getJSONArray(0).getJSONArray(2);
				System.out.println("detailArr:" + detailArr);

				int length;
				String[][] detailTexts=new String[detailArr.length()][];
				for (int i=0;i < (length = detailArr.length());i++)
				{
					JSONArray eachDetail=detailArr.getJSONArray(i);
					System.out.println("eachDetail:" + eachDetail);

					if (eachDetail instanceof JSONArray)
					{
						String text=eachDetail.getString(0);
						JSONArray explaination=eachDetail.getJSONArray(1);
						//detailTexts=new String[explaination.length()+1][];
						System.out.println("explanation:" + explaination);

						if (explaination instanceof JSONArray)
						{
							detailTexts[i] = new String[explaination.length() + 1];//多一个，第一个放文字
							detailTexts[i][0] = text;
							for (int j=0;j < explaination.length();j++)
							{
								detailTexts[i][j + 1] = explaination.getString(j);
							}
						}
					}
					//showArray(detailTexts);
				}
				result.setResultTexts(detailTexts);
				showArray(detailTexts);
			}
			
			//System.out.println(obj);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static TranslationResult formatGoogleSimple(String json){
		TranslationResult result=new TranslationResult(Consts.ENGINE_GOOGLE);
		try
		{
			JSONArray all=new JSONArray(json);
			String basicResult=all.getJSONArray(0).getJSONArray(0).getString(0);
			result.setBasicResult(basicResult);
			System.out.println(basicResult);
			
			JSONArray detailArr=all.getJSONArray(5);
			int length;
			String[][] detailTexts;
			for(int i=0;i<(length=detailArr.length());i++){
				JSONArray eachDetail=detailArr.getJSONArray(i); //5 i
				detailTexts=new String[1][];
				if(eachDetail instanceof JSONArray){
					String text=eachDetail.getString(0);
					JSONArray explaination=eachDetail.getJSONArray(2); //5 i 2
					detailTexts[i]=new String[explaination.length()+1];//多一个，第一个放文字
					detailTexts[i][0]=text;
					for(int j=0;j<explaination.length();j++){
						detailTexts[i][j+1]=explaination.getJSONArray(j).getString(0);// 5 i 2 j 0
					}
					showArray(detailTexts);
				}
			}
			System.out.println(all);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static void showArray(String[][] arr){
		for(String[] arr1:arr){
			for(String str:arr1){
				System.out.print(str);
				System.out.print(" ");
			}
			System.out.println("");
		}
	}
}
