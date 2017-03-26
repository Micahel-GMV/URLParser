package com.java.urlparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

// Class to organize logging to file. 
public class Logger {
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSS");
	public boolean echoOn = true;//true - logging on, false - logging off.	
	private BufferedWriter bw = null;
	
	public Logger(File file){//Creates a new logger that will write log to given file
       	FileWriter writer;
       	if (echoOn) try {
			writer = new FileWriter(file, true);
			bw = new BufferedWriter(writer);
		} catch (IOException e) {			
			System.out.println("Logging init error. No logging further.");
			e.printStackTrace();
		}                
	}
	
	public void writeEvent(String text){//Writes given string to a file. 
		String s =dateFormat.format(new java.util.Date()) + ":" + text + "\n";
		System.out.print(s);
		if ( (echoOn)&&(bw!=null) )
			try {				 
				bw.write(s);				
			} catch (IOException e) {
				System.out.print("Logging attempt failed. Log entry: " + text + ". Log output disabled.\n");
				echoOn = false;
				e.printStackTrace();
				
			}		
	}
	
	public void finish(){//Closes files.
		if (echoOn) try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			System.out.println("Logger flushing error.");
			e.printStackTrace();
		}
	}
}
