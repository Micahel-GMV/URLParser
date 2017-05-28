package com.gudzenko;

import java.io.File;
import java.io.IOException;

public class UrlParser {
	public static void main(String[] args) {
		String logFileName = "urlparser.log";
		File logFile = new File(logFileName);
		Logger logger = new Logger(logFile);
		if (args.length<3) {
			System.out.println("Wrong number of parameters. Usage: urlparser <FILENAME_IN> <FILENAME_OUT> <DICTIONARY> [logging] [<NUMBER_OF_THREADS 1..8>]");
			return;
		}		
		if ( (args.length > 3) && (args[3].equals("logging") ) ) logger.echoOn = true;
		else logger.echoOn = false;	
		int threadNumber = 1; 
		final String threadError = "Thread number is wrong or unrecognized. Thread number is set to 1."; 
		if (args.length > 4) try {
			threadNumber = Integer.parseInt(args[4]);
			if ( (threadNumber < 1)||(threadNumber>8) ) {
				System.out.println(threadError);
				threadNumber = 1;
			}
		}
		catch (NumberFormatException e) {
			System.out.println(threadError);
		}
		logger.writeEvent("Program start.");		
		Dictionary dic = null;
		String dictionaryFileName = args[2];		
		String urlsFileName = args[0];		
		String resultFileName = args[1];		
		logger.writeEvent("Dictionary file name: " + dictionaryFileName);
		logger.writeEvent("Input file name: " + urlsFileName);
		logger.writeEvent("Output file name:" + resultFileName);
		try {				
			logger.writeEvent("Loading dictionary.");
			File file = new File(dictionaryFileName);
			Dictionary.setLogger(logger);
			dic= new Dictionary(file);			
			logger.writeEvent("" + dic.size() + " words loaded.");			
			file = new File(urlsFileName);
			File fileOut = new File(resultFileName);
			logger.writeEvent("Starting parsing input file.");
			Processor processor  = new Processor(dic, threadNumber, logger);
			long time = System.currentTimeMillis();
			processor.process(file, fileOut);
			time = System.currentTimeMillis() - time;
			logger.writeEvent("File parsing finished. " + processor.getCount() + " lines processed in " + time/1000 + 
								" secs at speed of " + ( (time!=0)?(1000 * processor.getCount())/time : "n/a " ) 
								+ "lines/sec.");
			
		}
		catch (IOException e){
			e.printStackTrace();
			logger.writeEvent("IO error during processing.");
		}			
		logger.finish();
	}
}
