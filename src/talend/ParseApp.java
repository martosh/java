package talend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//TODO Future -> 
//  --add HashMap as Options to JobXml Constructor to be optional ORIGINAL CONTENT
//    Consider git branch must be used on develop maybe I need to do a check
//  --list_available_context
//  --list_context_env
//  --html_output
//  --debug_mode
//  --find_regex  test
//  --show and --hide options -show sql,java,
//  --list_keys
//  show the reasons for not parsed files.

// FROM CMD CLASS
// make --jobs and Latest --tdir logic // maybe -tdir will presume all if jobs then will search in latest and --all_versions will search in older versions
// FIX -jobs bug when option after -jobs, solution migrate to  JCommander there is a bug with UNLIMITED_VALUES
// create --filter/find option I need know for all jobs list of jobs, list of components, show info 
// maybe tdir must be mandatory and may use UNLIMITED_VALUES
// Perform a check --jobs are they in the list of all
// Inline::Java::Callback for best regex take a look at this lib

public class ParseApp {
	
	//##############################
	// MAIN
	//##############################
	public static void main( String[] args ) {
		System.out.println("Starting ParseAPP:");
       
		//create command line opitions 
		CmdOptions CliOpt = new talend.CmdOptions(args);
        HashMap <String, String> options = new HashMap<>();
	    ArrayList<String> FailedToParse = new ArrayList<>();
       
	    //get all external command line args 
		options = CliOpt.getOptions();
		ArrayList <String> input_job_files = CliOpt.getFiles();
		ArrayList <String> all_latest_jobs = CliOpt.getAllLatestFiles();

		//ls option
		if (options.get("list").equals("true") ){
			input_job_files = CliOpt.getFiles();
			input_job_files.forEach( System.out::println);
			System.exit(1);
		}
		
        for (int cnt = 0; cnt < input_job_files.size(); cnt ++) {
        	String job_file = input_job_files.get(cnt);
        	//System.out.println("Start paring file[" + job_file + "]");
        	try {
        	   JobXml job = new talend.JobXml( job_file,  options, CliOpt.getFindForIn());
			} catch (Exception e) {
				// TODO fix exception
				e.printStackTrace();
				FailedToParse.add(job_file);
			}
        }
       
        System.out.println( "\nFiles Failed to be parsed:");
        System.out.println(FailedToParse.toString());
        
	}
	
}	