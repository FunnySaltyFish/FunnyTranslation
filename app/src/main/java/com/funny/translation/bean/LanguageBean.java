package com.funny.translation.bean;

public class LanguageBean
{
	//选择语言时使用
	private boolean isSelected=false;
	private short checkKind=0;
	public String text;
	
	private short userData;

	public void setUserData(short userData)
	{
		this.userData = userData;
	}

	public short getUserData()
	{
		return userData;
	}
	public void setText(String text){
		this.text=text;
	}
	
	public void setCheckKind(short kind)
	{
		this.checkKind = kind;
	}

	public short getCheckKind()
	{
		return checkKind;
	}//语言种类
	
	public void setIsSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}

	public boolean isSelected()
	{
		return isSelected;
	}
}
