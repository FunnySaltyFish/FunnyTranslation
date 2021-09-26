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
 * This class serves as the driver for the objects and classes that takes an
 * javascript (.js) file or a text (.txt) file and formats the code inside
 * the file. The user is prompted to input the name of the file first. Then 
 * the program will run, reading the file entered and reformatting it. The
 * formatted program will print in the console along with errors found in the
 * code. The runner itself uses the StringBuilder to import the file and read
 * it. If no file is found the program promps the user to try again until one
 * is found.
 * The default location where the class reads files is the "hw3sample" file in 
 * the current workspace of the program.
 */
/**--------------------------------------------------------------------------*/
import java.io.*;
import java.nio.*;
import java.util.*;

public class JavascriptFormatterRunner {

	public static void main(String[] args) {

		System.out.println("Welcome to the Javascript Formatter.");
		System.out.println();

		//Initializes the objects
		Scanner scan = new Scanner(System.in);
		JavascriptFormatter format = new JavascriptFormatter();
		StringBuilder sb = new StringBuilder();

		//Sets the condition for the do-while loop
		boolean fileCheck = false;

		/**
		 * This is the start of the do-while loop which will continuously run
		 * until a readable, file is found. 
		 */
		do {
			System.out.print("Please enter a filename: ");
			String fileName = scan.nextLine();

			/**
			 * Use as an easy way to test files without typing the name
			 * repeatedly.
			 *TEST
			 *fileName = "gcds-unformat.js";
			 */

			if (fileName == "")
			{
				fileCheck=true;
			}
			else if(fileName == null)
			{
				fileCheck=true;
			}

			BufferedReader bufferedRead = null;
			try 
			{
				/**
				 * Here is the directory of the file read path. 
				 * If the path needs to be changed it needs to be done here.
				 */
				bufferedRead = new BufferedReader
						(new FileReader("hw3sample\\"+fileName));
				String line;
				while ((line = bufferedRead.readLine()) != null) 
				{
					if (sb.length() > 0) 
					{
						sb.append("\n");
					}
					sb.append(line);
				}
				fileCheck=false;
			} 
			catch (IOException e) 
			{
				System.out.println("File not found please try again");
				fileCheck=true;
			}
			System.out.println();
		}
		while (fileCheck);

		String contents = sb.toString();
		System.out.println("Unformatted string...");
		System.out.println(contents);

		System.out.println();
		System.out.println("Formatted string...");

		/**
		 * Formatting of the string is done along with all error catching.
		 */

			contents = format.format(contents);			
		

		System.out.println(contents);

		System.out.println("Thank you for using Javascript Formatter!");
	}

}
