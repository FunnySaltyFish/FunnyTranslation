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
 * This class consists of the main,essential method for the program. The 
 * JavascriptFormatter holds the format(input) method. The string of any code
 * is entered in the method and the method then returns the formatted string 
 * of code.
 */
/**--------------------------------------------------------------------------*/
public class JavascriptFormatter {

	private JSStack stack;
	private int indentLevel=0;

	/**
	 * The constructor of the JavascriptFormatter initializes the JSStack.
	 */
	public JavascriptFormatter()
	{
		stack=new JSStack();
	}

	/**
	 * This method serves as the main method of formatting a string of code.
	 * 
	 * @param input-the entered string of code to be formatted.
	 * @return the formatted string
	 * 
	 * @throws EmptyStackException-thrown when the stack of BlockTypes is empty
	 * @throws AlignmentException -thrown when there are an odd number of
	 * brackets in the code.
	 */
	//String builder
	public String format(String input)
	{

		String fstring = "";
		try {
			for (int i=0; i < input.length(); i++)
			{

				/**Used to determine the top stack of the iteration
				 *TEST EQUATION
				 *System.out.println("TOP= " + stack.peek());
				 */
				switch (input.charAt(i)) 
				{
				case '{' :
					//When { is found
					indentLevel++;
					stack.push(BlockType.BRACE);
					fstring += input.charAt(i);
					fstring += "\n";
					for (int j=0; j < indentLevel ; j++)
					{
						fstring+="\t";
					}
					break;

				case '}':
					//When '}' is found
					if (stack.peek() != BlockType.BRACE)
					{
						stack.pop();
						indentLevel--;
						fstring += input.charAt(i);
						throw new AlignmentException
						("ERROR: Bracket Allignment");
					}
					else if (i+2 <= input.length() && 
							input.substring(i,i+2).equals("}}"))
					{
						stack.pop();
						indentLevel--;
						fstring += input.charAt(i);
						fstring += "\n";
						for (int j=0; j < indentLevel-1 ; j++)
						{
							fstring+="\t";
						}				
					}
					else if (i == input.length())
					{
						if (stack.peek()==null)
						{
							fstring += input.charAt(i);
						}
						else if (stack.peek()!=null)
						{
							fstring+=input.charAt(i);
							throw new AlignmentException
							("ERROR: Bracket misalignment");
						}
					}
					else
					{
						stack.pop();
						indentLevel--;
						fstring += input.charAt(i);
						fstring += "\n";
						for (int j=0; j < indentLevel ; j++)
						{
							fstring+="\t";
						}
					}
					break;
				case ';':
					//When ';' is found
					/**
					 * One of the rules set for formatted code is that any semi-
					 * colon found inside the parenthesis is not to be touched on.
					 */
					if (stack.peek() == BlockType.PAREN)
					{
						fstring += input.charAt(i);
					}
					else if (i+2 <= input.length() && 
							input.substring(i,i+2).equals(";}"))
					{
						fstring += input.charAt(i);
						fstring += "\n";
						for (int j=0; j < indentLevel-1 ; j++)
						{
							fstring+="\t";
						}				
					}
					else
					{
						fstring += input.charAt(i);
						fstring += "\n";
						for (int j=0; j < indentLevel ; j++)
						{
							fstring+="\t";
						}
					}
					break;

				case '(':
					//When '(' is found
					stack.push(BlockType.PAREN);
					fstring+=input.charAt(i);
					break; 
				case ')':
					//When ')' is found
					if(stack.peek() != BlockType.PAREN)
					{
						stack.pop();
						indentLevel--;
						fstring += input.charAt(i);
						throw new AlignmentException
						("ERROR: Bracket Allignment");
					}
					else if (i == input.length()-1)
					{
						if (indentLevel != 0)
						{
							fstring += input.charAt(i);
							throw new AlignmentException
							("ERROR: Bracket misalignment");
						}
					}
					else
					{
						this.stack.pop();
						fstring += input.charAt(i);
					}
					break;
				case 'f':
					//When 'f' is found
					/**
					 * This case is given when a "for" loop has been detected. This is
					 * done by finding the next 4 characters of the 'f' characters. So
					 * if the for string is found then the case will work as intended.
					 */
					if (input.substring(i,i+4).equals("for("))
					{
						stack.push(BlockType.FOR);
						fstring += input.charAt(i);
					}
					else
					{
						fstring += input.charAt(i);
					}
					break;
				default:
					/**
					 * If no important characters are found, code adds current 
					 * char to the string.
					 */
					fstring+=input.charAt(i);
					break;
				}
			}

			if(stack.isEmpty())
			{
				BlockType extraStack=null;
			}
			else
			{
				BlockType extraStack = stack.pop();
				throw new AlignmentException
				("ERROR: Bracket misalignment");
			}
		}
		catch (EmptyStackException e)
		{
			fstring += ("<ERROR: Empty array\n");	

		}
		catch (AlignmentException e)
		{
			fstring += ("<ERROR: Bracket misalignment\n");	
		}
		//Returns the string formatted.
		return fstring.toString();
	}
}
