package com.funny.translation.translation;;

public class TranslationResult
{
//	public final static short DETAIL_NONE=0;//非常简单的结果，没有详细信息
//	public final static short DETAIL_MANY_RESULTS=1;//有多个返回结果
//	public final static short 

	public final static short TRANSLATE_STATUE_SUCCESS =0;
	public final static short TRANSLATE_STATUE_FAIL=1;

	private short engineKind;
	private String basicResult;//最简单的翻译结果，只有结果
	private String[][] resultTexts;//额外的详细翻译结果，可能有多个结果
	private short statue;
	//你好——Hello!你好，喂
	/*
		{
			{Hello}{你好}{喂},
			{Hi}{...}{...}
		}
	*/
	private String phoneticNotation;//注音

	private String sourceString;
	private String partOfSpeech;//词性

	private int index;//该任务被执行时的顺序

	public TranslationResult(short engineKind,String sourceString) {
		this(engineKind,sourceString,null);
	}

	public TranslationResult(short engineKind)
	{
		this(engineKind,"",null);
	}

	public TranslationResult(short engineKind, String basicResult, String[][] resultTexts)
	{
		this.engineKind = engineKind;
		this.basicResult = basicResult;
		this.resultTexts = resultTexts;
	}
	
	//Setters and getters

	public void setStatue(short statue)
	{
		this.statue = statue;
	}

	public short getStatue()
	{
		return statue;
	}

	public void setPartOfSpeech(String partOfSpeech)
	{
		this.partOfSpeech = partOfSpeech;
	}

	public String getPartOfSpeech()
	{
		return partOfSpeech;
	}

	public void setBasicResult(String basicResult)
	{
		this.basicResult = basicResult;
	}

	public String getBasicResult()
	{
		return basicResult==null?"":basicResult;
	}

	public void setResultTexts(String[][] resultTexts)
	{
		this.resultTexts = resultTexts;
	}

	public String[][] getResultTexts()
	{
		return resultTexts;
	}

	public void setPhoneticNotation(String phoneticNotation)
	{
		this.phoneticNotation = phoneticNotation;
	}

	public String getPhoneticNotation()
	{
		return phoneticNotation;
	}

	public String getSourceString() {
		return sourceString;
	}

	public void setSourceString(String sourceString) {
		this.sourceString = sourceString;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public short getEngineKind() {
		return engineKind;
	}

	@Override
	public String toString() {
		return "TranslationResult{" +
				"basicResult='" + basicResult + '\'' +
				", sourceString='" + sourceString + '\'' +
				'}';
	}
}
