package com.java.urlparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


//Class to handle dictionary - create it, read it from file and check availability of word in it.
public class Dictionary {
	
	public static final int firstLetter = 32, lastLetter = 123; //Range of letter codes from CP1251 that can be used in dictionary or file to parse 
	private static int count = 0; //Dictionary length
	private Letter letters[] = new Letter[lastLetter - firstLetter];//Array of links to next letters. Code of char is index of letter in array.
	private static Logger logger; //Link to logger class	
	public static void setLogger(Logger log)
	{
		logger = log;
	}
	
	public int size(){
		return count;
	}
	
	class Letter
	{		
		Dictionary nextLetter; //Link to next letter.
		boolean lastLetter;//Flag that indicates is there a word that ends on this letter. fiX, fixeD - x and d will have 'true'
	}
	
	public Dictionary()//Primary class init. 
	{		
		for (int i = 0; i < letters.length; i++) {
			letters[i] = new Letter();
			letters[i].nextLetter = null;
			letters[i].lastLetter = false;		
		}
	}
	
	public Dictionary(String s)//New branch of dictionary using a word to add to it. 
	{		
		this();		
		this.add(s);
	}	
	
    public Dictionary(File file) throws IOException //Create dictionary from file 
    {
    	this();    	
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;        
        while((line = br.readLine()) != null) {
        	line = line.toLowerCase().trim();
        	add(line);         	
        }
        br.close();
        fr.close();         
    }

	
	private int charCodeAt(String s, int pos) //Calculating index from char code. We use it as index in array of letters.
	{
		return (int)s.charAt(pos) - firstLetter;
	}	

	private int charCode(char c){ //The same but using a char, not a string.
		return (int)c - firstLetter;
	}		
	
	private void soutUnrecognized(char c){ //Reporting char out of range between firstLetter and lastLetter.
		logger.writeEvent("DICTIONARY ERROR: trying to add an unrecognized symbol " + c);
	}	
	
	public void add(String s) //Adds a string to dictionary
	{			
		if (s.length() == 1) {
			int curCode = charCodeAt(s, 0);
			try {
				letters[curCode].lastLetter = true;
				count++;
			} catch (ArrayIndexOutOfBoundsException e) {
				soutUnrecognized(s.charAt(0));
			}
		}
		else 
		{
			try {
				int curCode = charCodeAt(s, 0);
				if ( (letters[curCode].nextLetter) == null) 
					(letters[curCode].nextLetter) = new Dictionary(s.substring(1));
				else (letters[curCode].nextLetter).add(s.substring(1));
			} catch (ArrayIndexOutOfBoundsException e) {
				soutUnrecognized(s.charAt(0));
			}			
		}
	}
	
	
	public boolean check(char[] s, int from, int to){		// Checks is given word in dictionary. char[] is used instead of string to improve performance. It works 8 times faster than string.
		if (to-from == 0) {
			if (letters[charCode(s[from])].lastLetter) return true;
		}		
		else if  (letters[charCode(s[from])].nextLetter != null )  
			return letters[charCode(s[from])].nextLetter.check(s, from+1 , to);
		return false;
	}
}
