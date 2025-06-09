import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class maintenanceplanner {
	public static void main(String[] args) throws IOException {
		//Variable declaration
		File log = new File("Maintenance Log.csv");
		JFrame main = new JFrame("Allen's Maintenance Maximizer");

		//Frame setup
		main.setSize(800, 700);
		main.setLocation(400, 50);
		main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		main.setLayout(new GridLayout(4, 1));
		JTextArea maintenanceListArea = new JTextArea();
		maintenanceListArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
		main.add(maintenanceListArea);

		//System startup
		StartSystem(log);
		//Save the sorted log to file
		SaveLogToFile(SortLog(log, ReadLog(log)), log);
		//Display the schedule
		DisplaySchedule(ReadLog(log), maintenanceListArea);

		//Adding a maintenance item
		JButton addMaintenance = new JButton("Add a new maintenance item!");
		addMaintenance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Frame setup
				JFrame add = new JFrame("Add Maintenance");
				add.setSize(600, 400);
				add.setLocation(500, 100);
				add.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				add.setLayout(new GridLayout(6, 2));

				JLabel nameLabel = new JLabel("Enter the name of the maintenance item:");
				JTextField nameIn = new JTextField(25);

				add.add(nameLabel);
				add.add(nameIn);

				JLabel dayLabel = new JLabel("Enter the day this action was last performed as an integer:");
				JTextField dayIn = new JTextField(2);
				JLabel monthLabel = new JLabel("Enter the month this action was last performed as an integer:");
				JTextField monthIn = new JTextField(2);
				JLabel yearLabel = new JLabel("Enter the year this action was last performed as an integer (2025):");
				JTextField yearIn = new JTextField(2);
				JLabel intervalLabel = new JLabel("Enter the interval in days between maintenance sessions:");
				JTextField intervalIn = new JTextField(3);

				add.add(dayLabel);
				add.add(dayIn);
				add.add(monthLabel);
				add.add(monthIn);
				add.add(yearLabel);
				add.add(yearIn);
				add.add(intervalLabel);
				add.add(intervalIn);

				JButton addMaintenance = new JButton("Add item");
				addMaintenance.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String entry = "";

						//If any field is empty, do nothing.
						if(nameIn.getText().isEmpty() || dayIn.getText().isEmpty() || monthIn.getText().isEmpty() || yearIn.getText().isEmpty() || intervalIn.getText().isEmpty()) {
							return;
						}

						String name = nameIn.getText();
						if(name.contains(" ")) {//Replace spaces in the name with underscores
							System.out.println("UPDATE ENTRY: Removing a space in entry " + name);
							name = name.replaceAll(" ", "_");
						}

						//Collect the entry data when all fields are filled.
						entry = name+","+dayIn.getText()+","+monthIn.getText()+","+yearIn.getText()+","+intervalIn.getText();

						//Add the entry to the log.
						try {
							AddEntry(log, entry);
							DisplaySchedule(ReadLog(log), maintenanceListArea);
						} catch (FileNotFoundException e1) {
							System.out.println("File not found!");
							e1.printStackTrace();
						} catch (IOException e1) {
							System.out.println("Input error!");
							e1.printStackTrace();
						}
						add.dispatchEvent(new WindowEvent(add, WindowEvent.WINDOW_CLOSING));
						return;
					}// end actionPerformed
				});
				add.add(addMaintenance);

				add.setVisible(true);
				return;
			}// end actionPerformed
		});
		main.add(addMaintenance);

		//Removing an item
		JButton removeMaintenance = new JButton("Remove a maintenance item!");
		removeMaintenance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RemoveMaintenance(log, maintenanceListArea);
				return;
			}// end actionPerformed
		});
		main.add(removeMaintenance);

		//Printing the maintenance schedule
		JButton printMaintenance = new JButton("Print the maintenance schedule!");
		printMaintenance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					PrintSchedule(ReadLog(log));
				} catch (FileNotFoundException e1) {
					System.out.println("File not found!");
					e1.printStackTrace();
				} catch (IOException e1) {
					System.out.println("Input error!");
					e1.printStackTrace();
				}
				return;
			}// end actionPerformed
		});
		main.add(printMaintenance);

		// Load the main frame
		main.setVisible(true);
	}//end main

	//Validates the file and its contents.
	//Uses SaveLogToFile.
	public static void StartSystem(File log) throws IOException {
		//If no file exists, create one.
		if(!log.exists()) {
			System.out.println("START SYSTEM: No file exists. Creating a new file.");
			log.createNewFile();
		}

		//Variable declaration
		Scanner in = new Scanner(log);
		LinkedList<String> origList = new LinkedList<String>();
		LinkedList<String> newList = new LinkedList<String>();
		String header = "Name,Day,Month,Year,Interval";
		String disclaimer = "No maintenance items have been added.";
		Boolean fileChanged = false;

		//If the list is empty, add the header and disclaimer lines. Return as the list is empty.
		if(!in.hasNext()) {
			System.out.println("START SYSTEM: The list is empty. Formatting a fresh list.");
			newList.add(header);
			newList.add(disclaimer);
			in.close();
			SaveLogToFile(newList, log);
			return;
		}

		//Save the contents of the file to a LinkedList.
		while(in.hasNext()) {
			origList.add(in.nextLine());
		}
		in.close();

		//Ensure the header is accurate.
		if(!origList.peek().equals(header)) {
			System.out.println("START SYSTEM: Header inaccurate; replacing the header.");
			origList.pop();
			newList.add(header);
			fileChanged = true;
		}
		else {
			newList.add(origList.pop());//If accurate, add the header to the new list.
		}

		//Ensure if a header exists without entries, a disclaimer is added, then return as the list is empty.
		if(origList.peek() == null) {
			System.out.println("START SYSTEM: A header exists without entries. Adding a no entries disclaimer.");
			newList.add(disclaimer);
			SaveLogToFile(newList, log);
			return;
		}

		//If the only entry in the list is the disclaimer, mark the list as empty.
		Boolean notEmpty = true;
		if(origList.peek().equals(disclaimer)) {
			System.out.println("START SYSTEM: Detected a disclaimer on line #2! This list is empty.");
			notEmpty = false;
		}

		//Add remaining contents to the new list.
		while(origList.size() > 0) {
			newList.add(origList.pop());
		}

		//Save the corrected list to file if changes were made.
		if(fileChanged)
			SaveLogToFile(newList, log);

		//If there are entries in the list, check every entry in the list for updates.
		if(notEmpty) {
			LinkedList<String> uncheckedList = ReadLog(log);
			LinkedList<String> checkedList = new LinkedList<String>();
			uncheckedList.pop();//Add the header
			while(uncheckedList.size() > 0) {
				checkedList.add(UpdateEntry(uncheckedList.pop()));
			}
			checkedList.addFirst(header);//Add the header again
			SaveLogToFile(checkedList, log);//Save the file
			return;
		}

		//Otherwise, return.
		return;
	}//end StartSystem

	//Resets each entry's interval if the item exceeds the current date
	//Uses ReadLog
	public static String UpdateEntry(String entry) throws IOException {
		//Variable declaration
		Calendar curCal = Calendar.getInstance();
		Boolean adjusted = false;

		//Get the entry's data
		Scanner in = new Scanner(entry);
		in.useDelimiter(",");
		String name = in.next();//Collect the entry's name
		int date = Integer.parseInt(in.next());
		int month = Integer.parseInt(in.next()) - 1; //Adjusting for calendar silliness
		int year = Integer.parseInt(in.next());
		int interval = Integer.parseInt(in.next().trim());
		in.close();
		Calendar entryCal = Calendar.getInstance();
		entryCal.set(year, month, date);

		//If the entry is in the past (before the current date), add the entry's interval until it is current or future.
		/*
		 * the value 0 if the time represented by the argument is equal to the time represented 
		 * by this Calendar; a value less than 0 if the time of this Calendar is before the time 
		 * represented by the argument; and a value greater than 0 if the time of this Calendar 
		 * is after the time represented by the argument.
		 */
		while (entryCal.compareTo(curCal) < 0) {
			System.out.println("UPDATE ENTRY: Updating entry " + entryCal.getTime());
			entryCal.add(Calendar.DAY_OF_MONTH, interval);
			adjusted = true;
		}

		//If not adjusted, return the current entry.
		if(!adjusted) {
			System.out.println("UPDATE ENTRY: Entry " + entry + " is up to date.");
			return entry;
		}

		//Reconstruct the entry.
		int entryMonth = entryCal.get(Calendar.MONTH)+1;//Adjusting for calendar silliness
		String newEntry = name + "," +  entryCal.get(Calendar.DATE) + "," +  entryMonth + "," +  entryCal.get(Calendar.YEAR) + "," + interval;
		System.out.println("UPDATE ENTRY: Adding updated entry " + newEntry);
		return newEntry;
	}//end UpdateEntries

	//Removes a maintenance item based on its name.
	//Uses DisplaySchedule, ReadLog, SaveLogToFile.
	public static void RemoveMaintenance(File log, JTextArea maintenanceListArea) {
		JFrame remove = new JFrame("Remove Maintenance");
		remove.setSize(600, 400);
		remove.setLocation(500, 100);
		remove.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		remove.setLayout(new GridLayout(6, 2));

		JLabel nameLabel = new JLabel("Enter the name of the item you want to remove:");
		JTextField nameIn = new JTextField(20);
		remove.add(nameLabel);
		remove.add(nameIn);

		JButton removeMaintenance = new JButton("Remove item");
		removeMaintenance.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Do nothing if the user did not provide data
				if(nameIn.getText().isEmpty())
					return;
				
				Boolean notFound = true;

				//Gather the user's input and the entries from the log
				String entry = nameIn.getText();
				LinkedList<String> origList = null;
				try {
					origList = ReadLog(log);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				//Search for the entry in question
				LinkedList<String> newList = new LinkedList<String>();
				while(origList.size() > 0) {
					String curLine = origList.pop();
					Scanner lineScan = new Scanner(curLine);
					lineScan.useDelimiter(",");
					String temp = lineScan.next();
					if(!temp.equals(entry)) {
						newList.add(curLine);				
					}
					else {//When the entry is found, skip it and save the remainder of the list.
						notFound = false;
						while(origList.size() > 0) {
							newList.add(origList.pop());
						}
						//Overwrite the old file and create a new one with the new information.
						try {
							SaveLogToFile(newList, log);
							DisplaySchedule(ReadLog(log), maintenanceListArea);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						remove.dispatchEvent(new WindowEvent(remove, WindowEvent.WINDOW_CLOSING));
						break;
					}
					lineScan.close();
				}//end while loop
				
				//If no matching entry is found, inform the user.
				if(notFound) {
					JLabel noMatchesLabel = new JLabel("No matches found for entry: " + entry);
					remove.add(noMatchesLabel);
					remove.setVisible(true);
				}
				
				return;
			}// end actionPerformed
		});
		remove.add(removeMaintenance);
		remove.setVisible(true);
		return;
	}//end RemoveMaintenance

	//Displays the schedule on the program's main JFrame.
	public static void DisplaySchedule(LinkedList<String> log, JTextArea maintenanceListArea) throws FileNotFoundException {
		maintenanceListArea.selectAll();
		maintenanceListArea.replaceSelection("");

		maintenanceListArea.append("UPCOMING MAINTENANCE\n");

		//Add the header
		Scanner headerScan = new Scanner(log.pop());
		headerScan.useDelimiter(",");
		String header = "";
		String nameScannedValue = headerScan.next();
		while(nameScannedValue.length() < 25) {//Adding dots
			nameScannedValue = nameScannedValue + ".";
		}
		header = header + nameScannedValue;
		for(int i = 0; i < 2; i++) {//Gathering entries
			String curScannedValue = headerScan.next();
			while(curScannedValue.length() < 7) {//Adding dots
				curScannedValue = curScannedValue + ".";
			}
			header = header + curScannedValue;
		}
		String lastScannedValue = headerScan.next();
		header = header + lastScannedValue;
		headerScan.close();
		maintenanceListArea.append(header + "\n");

		//If only the disclaimer exists after the header, print that and return.
		if(log.peek().equals("No maintenance items have been added.")) {
			System.out.println("DISPLAY SCHEDULE: No items in the schedule. Displaying the disclaimer.");
			maintenanceListArea.append(log.pop());
			return;
		}

		//Scan through every entry in the log
		int logSize = log.size();
		for(int i = 0; i < logSize; i++) {
			System.out.println("DISPLAY SCHEDULE: Displaying item " + i + ".");

			Scanner lineScan = new Scanner(log.pop());
			lineScan.useDelimiter(",");
			String entry = "";
			String curNameScannedValue = lineScan.next();
			while(curNameScannedValue.length() < 25) {//Adding dots
				curNameScannedValue = curNameScannedValue + ".";
			}
			entry = entry + curNameScannedValue;
			for(int j = 0; j < 2; j++) {//Gathering entries
				String curScannedValue = lineScan.next();
				while(curScannedValue.length() < 7) {//Adding dots
					curScannedValue = curScannedValue + ".";
				}
				entry = entry + curScannedValue;
			}
			String curLastScannedValue = lineScan.next();
			entry = entry + curLastScannedValue;
			lineScan.close();
			maintenanceListArea.append(entry + "\n");
		}

		return;
	}//end DisplaySchedule

	//Returns the contexts of the log file as a LinkedList. Validates and corrects any defects in the file.
	//Uses SaveLogToFile
	public static LinkedList<String> ReadLog(File log) throws IOException {
		//Variable declaration
		Scanner in = new Scanner(log);
		LinkedList<String> origList = new LinkedList<String>();
		LinkedList<String> newList = new LinkedList<String>();

		String header = in.nextLine();//Save the header
		String firstLine = in.nextLine(); //Gather the first line of actual data

		//If no items have been added, return the original list with that disclaimer.
		if(firstLine.equals("No maintenance items have been added.") && !in.hasNext()) {
			System.out.println("READ LOG: No maintenance items have been added. Returning original empty list.");
			origList.add(header);
			origList.add(firstLine);
			in.close();
			return origList;
		}

		//If the disclaimer remains, remove it and return the list.
		if(firstLine.equals("No maintenance items have been added.") && in.hasNext()) {
			System.out.println("READ LOG: Removing erroneous disclaimer. Returning a new list with data.");
			newList.add(header);
			while(in.hasNextLine()) {
				newList.add(in.nextLine());
			}
			in.close();
			return newList;
		}

		//Otherwise, add the first line and read the remaining list
		origList.add(header);
		origList.add(firstLine);
		while(in.hasNext()) {
			origList.add(in.nextLine());
		}
		in.close();

		//Construct the new list
		while(origList.size() > 0) {
			String curLine = origList.pop();
			System.out.println("READ LOG: Scanning line " + curLine);
			//If the line is blank, skip the line.
			if(curLine != null && (curLine.trim().equals("") || curLine.trim().equals("\n") || curLine.trim().equals(",,,,"))) {
				System.out.println("READ LOG: Removing a blank line.");
				continue;
			}
			newList.add(curLine);
		}//end while loop
		return newList;
	}//end ReadLog

	//Overwrites the log file with the new provided list.
	public static void SaveLogToFile(LinkedList<String> logList, File log) throws IOException {
		log.delete();
		log.createNewFile();
		try {
			FileWriter out = new FileWriter(log,true);
			System.out.println("SAVE LOG TO FILE: Saving a new log file of size " + logList.size());
			while (logList.size() > 0) {
				String curLine = logList.pop();
				System.out.println("SAVE LOG TO FILE: Saving line: " + curLine);
				out.write(curLine + "\n");
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("SAVE LOG TO FILE: Unable to access file! Is it in use?");
			System.exit(0);
		}
	}//end SaveLogToFile

	//Adds an entry to the file.
	//Uses ReadLog, SaveLogToFile, SortLog
	public static void AddEntry(File log, String entry) throws IOException {
		//Variable declaration
		LinkedList<String> origList = ReadLog(log);
		LinkedList<String> newList = new LinkedList<String>();

		//Move the header from the original list to the new list.
		newList.add(origList.pop());

		//If there is a disclaimer, this is the first entry. Remove the disclaimer.
		if(origList.peek().equals("No maintenance items have been added.")) {
			System.out.println("ADD ENTRY: Removing the no items disclaimer.");
			origList.pop();
		}

		//Add the old lists' items to the new list.
		while(origList.size() > 0) {
			String curEntry = origList.pop();
			System.out.println("ADD ENTRY: Adding old entry " + curEntry);
			newList.add(curEntry);
		}

		//Update the entry and add the new entry to the new list and save the list.		
		entry = UpdateEntry(entry);
		newList.add(entry);
		System.out.println("ADD ENTRY: Adding new entry " + entry);

		//Sort and save the list to file.
		SaveLogToFile(SortLog(log, newList), log);
		return;
	}//end AddEntry

	//Sorts the provided list into the provided log file by putting the closest maintenance item on the top.
	public static LinkedList<String> SortLog(File log, LinkedList<String> inList) throws IOException {
		if(inList.size() <= 2) {//Check if there is only the header and one element. If there is, no sorting is required.
			System.out.println("SORT LOG: There is only one entry to sort. Saving the file and returing.");
			return inList;
		}


		String header = inList.pop();//Remove and save the header

		inList.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {				
				System.out.println("SORT LOG: Comparing entries " + o1 + "  and " + o2);

				//Get the date of the first entry
				String firstEntry = o1;
				Scanner firstIn = new Scanner(firstEntry);
				firstIn.useDelimiter(",");
				firstIn.next();//Skip the entry's name
				int date1 = Integer.parseInt(firstIn.next());
				int month1 = Integer.parseInt(firstIn.next())-1; //Adjusting for calendar silliness
				int year1 = Integer.parseInt(firstIn.next());
				firstIn.close();
				Calendar firstEntryCal = Calendar.getInstance();
				firstEntryCal.set(year1, month1, date1);
				Date firstRealDate = firstEntryCal.getTime();

				//Get the date of the second entry
				String secondEntry = o2;
				Scanner secondIn = new Scanner(secondEntry);
				secondIn.useDelimiter(",");
				secondIn.next();//Skip the entry's name
				int date2 = Integer.parseInt(secondIn.next());
				int month2 = Integer.parseInt(secondIn.next())-1; //Adjusting for calendar silliness
				int year2 = Integer.parseInt(secondIn.next());
				secondIn.close();
				Calendar secondCal = Calendar.getInstance();
				secondCal.set(year2, month2, date2);
				Date secondRealDate = secondCal.getTime();

				// Compare both dates
				System.out.println("SORT LOG: Comparing real entries " + firstEntryCal.getTime() + " and " + secondCal.getTime());
				return firstRealDate.compareTo(secondRealDate);//Sort such that the closest item is on the top
			}
		});

		//Add the header and the sorted contents to a new list.
		LinkedList<String> newList = new LinkedList<String>();
		newList.add(header);
		while(inList.size() > 0) {
			newList.add(inList.pop());
		}

		//Overwrite the old file with the new sorted file
		return newList;
	}//end SortLog

	//Creates and formats a printable document of the schedule and prompts printing.
	public static void PrintSchedule(LinkedList<String> log) throws FileNotFoundException {
		//Variable declaration
		JTextPane jtp = new JTextPane();
		String list = "UPCOMING MAINTENANCE\n";

		//Add and format each list item to the list string
		//Add the header
		Scanner headerScan = new Scanner(log.pop());
		headerScan.useDelimiter(",");
		String header = "";
		String nameScannedValue = headerScan.next();
		while(nameScannedValue.length() < 25) {//Adding dots
			nameScannedValue = nameScannedValue + ".";
		}
		header = header + nameScannedValue;
		for(int i = 0; i < 2; i++) {//Gathering entries
			String curScannedValue = headerScan.next();
			while(curScannedValue.length() < 7) {//Adding dots
				curScannedValue = curScannedValue + ".";
			}
			header = header + curScannedValue;
		}
		String lastScannedValue = headerScan.next();
		header = header + lastScannedValue;
		headerScan.close();
		list = list + header + "\n";

		//If only the disclaimer exists after the header, print that and return.
		if(log.peek().equals("No maintenance items have been added.")) {
			System.out.println("DISPLAY SCHEDULE: No items in the schedule. Displaying the disclaimer.");
			list = list + log.pop();
			return;
		}

		//Scan through every entry in the log
		int logSize = log.size();
		for(int i = 0; i < logSize; i++) {
			System.out.println("DISPLAY SCHEDULE: Displaying item " + i + ".");

			Scanner lineScan = new Scanner(log.pop());
			lineScan.useDelimiter(",");
			String entry = "";
			String curNameScannedValue = lineScan.next();
			while(curNameScannedValue.length() < 25) {//Adding dots
				curNameScannedValue = curNameScannedValue + ".";
			}
			entry = entry + curNameScannedValue;
			for(int j = 0; j < 2; j++) {//Gathering entries
				String curScannedValue = lineScan.next();
				while(curScannedValue.length() < 7) {//Adding dots
					curScannedValue = curScannedValue + ".";
				}
				entry = entry + curScannedValue;
			}
			String curLastScannedValue = lineScan.next();
			entry = entry + curLastScannedValue;
			lineScan.close();
			list = list + entry + "\n";
		}

		//Send the list to the printer
		jtp.setBackground(Color.white);
		jtp.setFont(new Font("Monospaced", Font.PLAIN, 15));
		jtp.setText(list);
		boolean show = true;
		try {
			jtp.print(null, null, show, null, null, show);
		} catch (java.awt.print.PrinterException ex) {
			ex.printStackTrace();
		}
	}//end PrintSchedule
}//end class