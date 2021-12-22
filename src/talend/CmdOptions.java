package talend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.cli.*;

public class CmdOptions {

	private HashMap<String, String> Options = new HashMap<>();
	private ArrayList<String> AllLatestItems =  new ArrayList<>();
	private ArrayList<String> CmdArgJobsFiles = new ArrayList<>();
	private ArrayList<FindForIn> findObjects = new ArrayList<>();
	
	//##############################################
	// Object with args
	//##############################################
	private void parseCliArgs( String[] args ) {

		Options options = new Options();

		Option input =  new Option ( "j", "talend_jobs", true, "Talend job file to parse eg: path/NMPstatistik_0.29.item");
		Option output = new Option ("o", "output", true, "Output result in file, currently useless, and does't work");
		Option context_env = new Option ("ce", "context_env", true, "Take wanted context environment" );
		Option talend_git_dir = new Option ("dir", "talend_git_dir", true, "Give directory where the parser will look recursively for Item files" );
		Option list = new Option ("ls", "list_found_items", false, "List found Item jobs for a dir" );
		Option show_orig = new Option ("org", "show_original", false, "Show original not mapped with context env data" );
		Option find = new Option ("f", "find_for_in", true, "Find for some words in job elements example -f string1,string2 all" );
		Option help = new Option ("h", "help", false, "Prints help" );
		//Option all_latest = new Option ("latest", "all_latest", false, "Execute the program for all latest versions" );

		find.setArgs(2);
		
		input.setArgs(Option.UNLIMITED_VALUES); //BUGGY 
		show_orig.setArgs(0);
		//input.setRequired(true);

		options.addOption(input);
		options.addOption(output);
		options.addOption(context_env);
		options.addOption(talend_git_dir);
		options.addOption(list);
		options.addOption(show_orig);
		options.addOption(find);
		options.addOption(help);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println( "\n" + e.getMessage() + "\n");
			formatter.printHelp("utility-name", options);
			System.exit(1);
		}
		
		if ( cmd.hasOption("help")) {
			formatter.printHelp("utility-name", options);
			System.exit(1);
		}
	
		HashMap<String, String> result = new HashMap<String, String> ();
		//Create -find options FindForIn objects
		if (cmd.hasOption("find_for_in")) {
	        createFindOptions(cmd);
	        result.put("find", "true");
		} else {
			result.put("find", "false");
		}
	    
		// default context_env is PROD

		String context_final = cmd.getOptionValue("context_env");		
		if ( context_final == null ) {
			context_final = "PROD";
		}

		result.put("output", cmd.getOptionValue("output"));
		result.put("context_env", context_final);
		result.put("all_latest", cmd.getOptionValue("all_latest"));
		result.put("talend_git_dir", cmd.getOptionValue("talend_git_dir"));
		String list_opt_str = String.valueOf(cmd.hasOption("list_found_items"));
		String show_orig_str = String.valueOf(cmd.hasOption("show_original"));
		result.put("list", list_opt_str );
		result.put("show_orig", show_orig_str );
	
		//get list of all Item files with latest versions if tdir option
		if( cmd.getOptionValue("talend_git_dir") != null ) {
			
			try {
				 getItemFiles(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.Options = result;
		
		//if no jobs args use all files 
		if ( cmd.getOptionValues("talend_jobs") != null && cmd.getOptionValues("talend_jobs").length > 0 ) {
		    Collections.addAll(this.CmdArgJobsFiles , cmd.getOptionValues("talend_jobs"));
		} else {
			if (cmd.getOptionValue("talend_git_dir") == null) {
				System.out.println("\nError: -j or -dir option needed. You must use -j, -dir or both!");
				System.exit(0);
			}
			this.CmdArgJobsFiles =  this.AllLatestItems;
		}
		
	}


	//#######################################################################################
    //# 
    //#######################################################################################
	
	private void createFindOptions( CommandLine cmd ) {
		
		for ( int c = 0 ; c < cmd.getOptionValues( "find_for_in").length ; c++ ) {
		//	System.out.println( "SHOW " + cmd.getOptionValues("find_for_in")[c] );
			//split first, split second.
			String[] findIn = null;
			String[] findFor = null;
			
			if ((c % 2) == 0) {
			    findFor =  cmd.getOptionValues("find_for_in")[c].split(",");
			    findIn =  cmd.getOptionValues("find_for_in")[c + 1 ].split(",");
			} else {
				continue;
			}
			
			FindForIn ffi = new FindForIn();
			ArrayList<String> arrayFindFor = new ArrayList<String>(Arrays.asList(findFor));
			ArrayList<String> arrayFindIn  = new ArrayList<String>(Arrays.asList(findIn));
			
			//System.out.println( arrayFindFor.toString());
			//System.out.println( arrayFindIn.toString());
			
			ffi.setFindFor(arrayFindFor);
			ffi.setFindIn(arrayFindIn);
			this.findObjects.add(ffi);
		}
	}
	//#######################################################################################
    //# fill up this.AllLatestItems ArrayList with latest Talend item files from tdir option 
    //#######################################################################################
	private void getItemFiles (HashMap <String, String> input ) throws IOException {

		//Talend job path -> version
		///example key:value {~/work/outrvstatistik_731/OUTRVSTATISTIK/process/KIS/KIS_Datentrager_report_st_": "0.9" }
		HashMap <String, String> latest = new HashMap<>();
		ArrayList <String> result = new ArrayList<>();
		
		try (Stream<Path> walkStream = Files.walk(Paths.get( input.get("talend_git_dir") ) ) ) {

			walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {

				if (f.toString().endsWith(".item")) {

					Pattern pattern = Pattern.compile("(.*?)_(\\d+?\\.\\d+?)\\.item$", Pattern.CASE_INSENSITIVE);
					Matcher m = pattern.matcher(f.toString());

					boolean matchFound = m.find();

					if(matchFound) {
						String jobKeyPath =  m.group(1);
						String jobVersion =  m.group(2);

						if (latest.containsKey(jobKeyPath)) {
							//check higher version;
							int comp_result = Double.compare( Double.parseDouble( latest.get(jobKeyPath) ), Double.parseDouble(jobVersion));
							//System.out.println( comp_result);

							//0.1 vs 0.10 and 0.2 vs 0.20 etc workaround
							if( comp_result == 0 && jobVersion.length() > latest.get(jobKeyPath).length()  ) {
								latest.put(jobKeyPath, jobVersion);
							}

							if (comp_result < 0 ) {
								latest.put(jobKeyPath, jobVersion);
							}

						} else {
							latest.put( jobKeyPath, jobVersion);
						}

					}
				} 
			});
		} 

	    
	    for (String key : latest.keySet()) {
	    	result.add(key + "_" + latest.get(key) + ".item");
	    }
	    
	    //System.out.println( result.toString());
		this.AllLatestItems =  result;
	}	
	
    //#########################
	//getters
    //#########################
	public ArrayList<String> getFiles() {
		return this.CmdArgJobsFiles;
	}

	//#########################
	public ArrayList<FindForIn> getFindForIn() {
		return this.findObjects;
	}
    //#########################
    //
    //#########################
	public ArrayList<String> getAllLatestFiles() {
		return this.AllLatestItems;
	}
	
    //#########################
    //
    //#########################
	public HashMap<String,String> getOptions() {
		return this.Options;
	}
	
	//##########################
	// getter
	//##########################
	public CmdOptions(String[] args) {
		this.parseCliArgs(args); 	
	}

}
