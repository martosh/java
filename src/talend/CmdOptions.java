package talend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.commons.cli.*;

//TODO: check --jobs are they in the list of all
       // make --jobs and Latest --tdir logic // maybe -tdir will presume all if jobs then will search in latest and --all_versions will search in older versions
       // FIX -jobs bug when option after -jobs
       // create --filter/find option I need know for all jobs list of jobs, list of components, show info 

public class CmdOptions {

	private static String[] CmdArgJobsFiles;
	private HashMap<String, String> Options = new HashMap<>();
	private ArrayList<String> AllLatestItems =  new ArrayList<>();

	//##############################################
	// Object with args
	//##############################################
	private void parseCliArgs( String[] args ) {

		Options options = new Options();

		Option input =  new Option ( "jobs", "talend_job", true, "Talend job file to parse eg: path/NMPstatistik_0.29.item");
		Option output = new Option ("o", "output", true, "Output result file for sql");
		Option context_env = new Option ("ce", "context_env", true, "Take wanted context environment" );
		Option all_latest = new Option ("alatest", "all_latest", false, "Execute the program for all latest versions" );
		Option talend_git_dir = new Option ("tdir", "talend_git_dir", true, "Talend_git_dir" );

		input.setArgs(Option.UNLIMITED_VALUES); 
		input.setRequired(true);

		options.addOption(input);
		options.addOption(output);
		options.addOption(context_env);
		options.addOption(all_latest);
		options.addOption(talend_git_dir);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);
			System.exit(1);
		}

		HashMap<String, String> result = new HashMap<String, String> ();
		// default context_env is PROD

		String context_final = cmd.getOptionValue("context_env");		
		if ( context_final == null ) {
			context_final = "PROD";
		}

		result.put("output", cmd.getOptionValue("output"));
		result.put("context_env", context_final);
		result.put("all_latest", cmd.getOptionValue("all_latest"));
		result.put("talend_git_dir", cmd.getOptionValue("talend_git_dir"));
		
		if( cmd.getOptionValue("talend_git_dir") != null ) {
			try {
				 getItemFiles(result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.Options = result;
		this.CmdArgJobsFiles = cmd.getOptionValues("jobs");

	}
	
	//####################################################
    //#
    //####################################################
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
							double currentVersion = Double.parseDouble(jobVersion);
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
	public String[] getFiles() {
		return this.CmdArgJobsFiles;
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
	//
	//##########################
	public CmdOptions(String[] args) {
		this.parseCliArgs(args); 	
	}

}


