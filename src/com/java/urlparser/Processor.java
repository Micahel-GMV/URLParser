package com.java.urlparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//Class that makes parsing of given file using dictionary.
public class Processor {
	
	private long count = 0;//Number of processed strings
	private StrigParser[] threads;//Array of processing threads
	private int threadNumber;//Number of threads
	private ParserResult[] parserResults;//Each thread will return results of processing here
	private Dictionary dict;//Dictionary that will be used when processing
	private Logger logger;//Logger to write log
	private final long reportInterval = 50_000;//How much lines we will process before reporting performance.
	
	private class ParserResult{
		char[] s; //Result of processing
		boolean processed;//Flag that we can read result
		int length;
	}
	
	//Primary init of processor class. Dictionary, number of threads and logger.
	public Processor(Dictionary dict, int threadNumber, Logger logger){
		this.dict = dict;
		this.threadNumber = threadNumber;
		this.logger = logger;
		parserResults = new ParserResult[threadNumber];
		threads = new StrigParser[threadNumber];
	}
	
	public void setLogger(Logger logger){
		this.logger = logger;
	}
	
	public long getCount(){
		return count;
	}
	
	//Objects of this class will work in their own threads and process strings parsing
	private class StrigParser extends Thread{
		
		private char[] line; //String to process. char[] instead of String for better performance
		private Dictionary dict;
		private int index;//Index of current thread. 	
		private boolean pause = false;//Flag to pause thread while line is set.
		private Future<?> threadLink;		
		
		public void setThreadLink(Future<?> future) {
			this.threadLink = future;
		}
		
		public boolean isDone(){
			return threadLink.isDone();
		}

		public StrigParser(Dictionary dict, int index){ //Primary init of object
			this.dict = dict;
			this.index = index;
		}
		
		public void setString(char[] line) {//Here we send string to parse
			pause = true;
			this.line = line.clone();
			pause = false;
		}
		
		@Override
		public void run(){ // Method that runs in thread and processes lines  
        	int lastChar = 0;
        	parserResults[index].s = null;
        	while ( (pause)&&(!parserResults[index].processed) ) ;
        	if (line!=null) {
        		char[] result = new char[3 * line.length + 1];        		
        		for (int i = 0; i < line.length; i++)
        			for (int j = line.length - 1; j>=i; j--)
        				if ( dict.check(line,i, j) ) {
        					for (int k = i; k<=j; k++) {        						        						
        						result[lastChar] = line[k];        						        						
        						lastChar++;
        					}
        					i = j;
        					result[lastChar++] = ' ';        					
        				}
        		if (lastChar>0) {
        			parserResults[index].s = result;
        			parserResults[index].length = lastChar-1;        					
        			parserResults[index].processed = true;
        		}
        		        		
        	}
        	
		}
		
	}	
	
	
	//Main method of this class. Here we set buffered readers and writes to work with files, creating threads, processing each line 
	//with its own thread and write result to file.
	public void process (File fileIn, File fileOut) throws IOException {
	       	FileReader reader = new FileReader(fileIn);
	       	FileWriter writer = new FileWriter(fileOut,false);
	        BufferedReader br = new BufferedReader(reader);
	        BufferedWriter bw = new BufferedWriter(writer);
	        ExecutorService executorService = Executors.newFixedThreadPool(threadNumber);
	        for (int i = 0; i < threadNumber; i++) {
	        	threads[i] = new StrigParser(dict, i);	        	
	        	parserResults[i] = new ParserResult();	        	
	        	parserResults[i].processed = true;	        	
	        }		        
	        logger.writeEvent("" + threadNumber + " threads created.");
	        String line;
	        long curTime,prevTime = System.currentTimeMillis();
	        while(br.ready()) {	        	
	        	for (int i = 0; i < threadNumber; i++){
		        	line = br.readLine();		        	
		        	if (line != null) {
			        	count++;			        	
			        	if (count%reportInterval == 0){
			        		curTime = System.currentTimeMillis();
			        		System.out.println("" + count/1000 + "k lines processed at speed of " + 
			        				reportInterval*1000 /(curTime - prevTime) + " lines/sec.");
			        		prevTime = curTime;
			        	}			        	
			        	line = line.trim().toLowerCase();			        	
			        	threads[i].setString(line.toCharArray());
			        	parserResults[i].processed = false;
			        	threads[i].setThreadLink(executorService.submit(threads[i]));			        	

		        	} else parserResults[i].s = null;		        	
	        	}
	        	for (int i = 0; i < threadNumber; i++){
	        		while ( ! threads[i].isDone() ) ;	        		
	        		if ( (parserResults[i].s != null) && (parserResults[i].s!=null) ) {
	        				bw.write( new String(parserResults[i].s, 0 , parserResults[i].length));
	        				bw.write("\n");	        			
	        			}
	        	}
	        }
	        br.close();
	        reader.close();
	        bw.flush();
	        writer.flush();
	        bw.close();	        
	        writer.close();
	}
}
