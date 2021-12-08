package talend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//TODO Future -> 
//  --add HashMap as Options to JobXml Constructor to be optional ORIGINAL CONTENT
//    Consider git branch must be used on develop maybe I need to do a check
//  --help 
//  --list_available_context
//  --list_context_env
//  --list_sql_no_map
//  --html_output
//  --debug_mode
//  --output field_name to be parsed
//  --original //don't use context variables mapped //only original// with original context
//  --find sql,java,original_sql, original_java, context, all "asdasdasd"
//  --find_regex same

// FROM CMD CLASS
// make --jobs and Latest --tdir logic // maybe -tdir will presume all if jobs then will search in latest and --all_versions will search in older versions
// FIX -jobs bug when option after -jobs
// create --filter/find option I need know for all jobs list of jobs, list of components, show info 
// migrate to  JCommander there is a bug with UNLIMITED_VALUES
// maybe tdir must be mandatory and may use UNLIMITED_VALUES
// check --jobs are they in the list of all
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
	
		if (options.get("list").equals("true") ){
			input_job_files = CliOpt.getFiles();
			input_job_files.forEach( System.out::println);
			System.exit(1);
		}
		
        for (int cnt = 0; cnt < input_job_files.size(); cnt ++) {
        	String job_file = input_job_files.get(cnt);
        	System.out.println("Start paring file[" + job_file + "]");
        	try {
        	   JobXml job = new talend.JobXml( job_file,  options.get("context_env"));
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