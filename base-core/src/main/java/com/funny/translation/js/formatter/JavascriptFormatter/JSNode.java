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
 * This node consists of the data field and the next node, since this is used
 * in a stack array. Meaning only the next node must be specified. Each node
 * holds the data for the type of BlockType. This carries its own, set and 
 * get methods.
 */
/**--------------------------------------------------------------------------*/
public class JSNode {

	/**
	 * Sets the properties of the node like block and next.
	 */
	private BlockType block;
	private JSNode next;
	
	/**
	 * The constructor of the node first sets both the next node and the
	 * data carried as null.
	 */
	public JSNode()
	{
		this.block=null;
		this.next=null;
	}
	
	/**
	 * This method sets the BlockType to the node.
	 * 
	 * @param input-the entered BlockType of the node.
	 */
	public void setBlockType(BlockType input)
	{
		this.block=input;
	}
	
	/**
	 * This method sets the next node in the linked list.
	 * 
	 * @param next-the entered next node.
	 */
	public void setNext(JSNode next)
	{
		this.next = next;
	}
	
	/**
	 * This methods returns the next node in the singly linked list.
	 * 
	 * @return-the next node in the list.
	 */
	public JSNode getNext()
	{
		return this.next;
	}
	
	/**
	 * This method returns the BlockType data of the node/
	 * 
	 * @return-the node BlockType.
	 */
	public BlockType getData()
	{
		return this.block;
	}

}
