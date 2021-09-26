package com.funny.translation.js.formatter.JavascriptFormatter; /**-------------------------------------Info---------------------------------*/
/**
 *		Name: 						Date:					Current Version:
 *		Martin Barcelon				10-02-2016				1.0
 *
 *		Email:					
 *		martin.barcelon@stonybrook.edu
 *
 *		Student ID:					
 *		110250249					
 */
/**-------------------------------Short Description--------------------------*/
/**
 * This object serves to connect the JSNode stacks. Therefore, this object
 * consists of a top Node or the tail, and a temporary node for use in any of
 * the methods. Outside of the constructor, which sets the top node to null,
 * this object consists of a push method, a pop method, a peek method and a
 * isEmpty method since this object acts as a stacked list. Therefore, the
 * topmost node gets removed first and added on top of first.
 */
/**--------------------------------------------------------------------------*/

public class JSStack {

	private JSNode top;
	private JSNode temp;
	
	public JSStack()
	{
		this.top=null;
	}
	
	/**
	 * This method adds a new node to the stacked list. Note that when the 
	 * method is empty (has no top node), then the new node is set to the 
	 * top. 
	 * 
	 * @param b-the entered BlockType to be entered in the node.
	 */
	public void push(BlockType b)
	{
		JSNode newNode = new JSNode();
		newNode.setBlockType(b);
		
		if (this.top == null)
		{
			this.top=newNode;
		}
		else
		{
			newNode.setNext(top);
			this.top = newNode;
		}
	}
	//top = top. 
	//4 <--
	//3
	//2 
	//1
	
	/**
	 * This method removes the top node and sets the previous one (next node)
	 * to the top.
	 * 
	 * @return -the BlockType of the deleted node
	 * @throws EmptyStackException -thrown when there is no stacks to be removed
	 */
	public BlockType pop() throws EmptyStackException
	{
		JSNode topElement=this.top; 
		if(this.top==null)
		{
			throw new EmptyStackException("ERROR: No node to remove");
		}
		else
		{
			this.top=this.top.getNext();
		}
		return topElement.getData();
	}
	
	/**
	 * This methods returns the BlockType of the topmost stack.
	 * 
	 * @return the BlockType of the top most stack.
	 */
	public BlockType peek()
	{
		if(this.top==null)
		{
			return null;
		}
		else
		{
			return this.top.getData();
		}
	}
	
	/**
	 * This method checks if the stack is empty, and returns a boolean value.
	 * 
	 * @return -true or false, depending on the state of the stacked list.
	 */
	public boolean isEmpty()
	{
		boolean isEmpty=true;
		if (this.top==null)
		{
			isEmpty=false;
		}
		return isEmpty;
	}
	
}
