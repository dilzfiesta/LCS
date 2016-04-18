package xyrality;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/*	External library to read and convert CSV to POJO	*/
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;

import xyrality.Participant;

public class LCS {

	private static Path filePath = Paths.get("");
	private static String winningNumber = null;
	private static List<Participant> participants = null;
	private static boolean debug = false;
	
	/*	/Users/gyro/Downloads/participants.csv 456000123	*/
	
	public static void main(String[] args) throws IOException {
		
		readInputParameters();
		debugPrint("Filename: "+ filePath.getFileName());
		debugPrint("Winning number: "+ winningNumber);
        
        convertFileToPojo();
        debugPrint("Total Participants: " + participants.size());
	    
	    findCredits();
	    debugPrint("Participants credits before merge:");
	    for(Participant p: participants) {
			debugPrint(p.getFirstName() +"::"+ p.getCredits());
		}
	    
	    mergeAndPrintParticipants();
	    
	}
	
	/**
	 * 	Merge the credits of the duplicate participants	
	 *  And print the list to STDOUT
	 */
	public static void mergeAndPrintParticipants() {

		String key = "";
		Integer value = 0;
		Map<String, Integer> output = new TreeMap<String, Integer>();
		
		for(Participant p: participants) {
			key = p.getLastName().concat(",").concat(p.getFirstName()).concat(",").concat(p.getCountry());
			value = p.getCredits();
			
			/*	Do not add participants who didn't won anything	*/
			if(value == 0) continue;
			
			/*	Merge credits of duplicate participants	*/
			if(output.containsKey(key))
				value += output.get(key);
			
			/*	Add/replace participants	*/
			output.put(key, value);
			
		}
		
		/*	Print everything to standard output	*/
		for (Map.Entry<String, Integer> entry : output.entrySet()) { 
			System.out.println(entry.getKey().concat(",").concat(entry.getValue().toString())); 
		}
		
	}
	
	/**
	 * 	Find credits of the participants based on the winning lottery
	 */
	public static void findCredits() {
		
		int[] winningNumberArray = convertStringToArray(winningNumber);
		int position, creditCount;
		
		/*	Loop over each participants	*/
		for(Participant p: participants) {
			int[] ticketNumberArray = convertStringToArray(p.getTicketNumber());
			
			/*	Loop over each element of the winning lottery	*/
			for(int i=0; i<winningNumberArray.length; i++) {
				position = -1;
				creditCount = 0;
				debugPrint("Wining Number: "+ winningNumber.substring(i));
				
				/*	Loop over each subsection of the winning lottery
				 *  Example: If winning ticket is 456000123, then each subsections will be -
				 *  	456000123, 56000123, 6000123, 000123, 00123, 0123, 123, 23, 3	*/
				for(int j=i; j<winningNumberArray.length; j++) {
					
					/*	Loop over each element of the participant's ticket	*/
					for(int k=position+1; k<ticketNumberArray.length; k++) {
						
						/*	Compare if the elements of the winning ticket and participant's ticket matches or not	*/
						if(winningNumberArray[j] == ticketNumberArray[k]) {
							
							/*	Add credit only for entries present in ascending order.
							 * 	And break from the loop to avoid adding credits again for the same element from winning ticket 	*/
							if(position < k) {
								creditCount++;
								position = k;
								debugPrint(p.getTicketNumber() + "::"+ ticketNumberArray[k]);
								break;
							}
						}
					}
				}
				debugPrint("----------------");
				
				/*	Compare and add the highest credit that the participant has earned for for each subsection of the wining ticket	*/
				if(creditCount > p.getCredits()) 
					p.setCredits(creditCount);
			}
		}
		
	}
	
	/**
	 * 	Utility method to convert string to array of integers
	 */
	public static int[] convertStringToArray(String str) {
		
		return Arrays.stream(str.split(""))
	    	.mapToInt(Integer::parseInt)
	    	.toArray();
		
	}
	
	/**
	 * 	Utility to convert the participants list into Participant POJO
	 */
	public static void convertFileToPojo() throws IOException {
		
		File file = filePath.toFile();
		
		ColumnPositionMappingStrategy<Participant> stratagy = new ColumnPositionMappingStrategy<Participant>();
	    stratagy.setType(Participant.class);
	    String[] columns = new String[] {"lastName", "firstName", "country", "ticketNumber"};
	    stratagy.setColumnMapping(columns);

	    CsvToBean<Participant> csv = new CsvToBean<Participant>();
	    participants = csv.parse(stratagy, new FileReader(file));
	    
	}
	
	/**
	 * 	Read valid file path and winning lottery number
	 */
	@SuppressWarnings("resource")
	public static void readInputParameters() {
		
		Boolean found = false;
		String tmp = null;
		
		/*	Read participant file	*/
		Scanner input = new Scanner(System.in);
        System.out.println("Absolute path to participants file:");
        do {
        	while(input.hasNextLine()) { 
        		filePath = Paths.get(input.nextLine().trim());
        		if(new File(filePath.toString()).isFile()) {
        			found = true;
            		break;
            	} else {
            		System.out.println("Invalid file !!");
            		System.out.println("Absolute path to participants file:");
            	}
        	}
        } while (!found);
        
        /*	Reinitialize */
        found = false;
        
        /*	Read winning lottery number	*/
        input = new Scanner(System.in);
        do {
        	System.out.println("Winning lottery number:");
	        while(input.hasNextLine()) {
	        	tmp = input.nextLine().trim();
	        	if(tmp.matches("\\d*")){
        			winningNumber = tmp;
        			found = true;
	        		break;
	        	} else {
	        		System.out.println("Invalid number !!");
	        		System.out.println("Winning lottery number:");
	        	}
	        }
        } while (!found);
        input.close();
	}
	
	/** 
	 * Print strings only if the value of debug is true	
	 */
	public static void debugPrint(String s) {
		if(debug)
			System.out.println(s);
	}

}
