package talend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//TODO Maybe in Future -> 
//    Consider git branch must be used on develop maybe I need to do a check
//  --help 
//  --list_available_context
//  --list_context_env
//  --list_sql_no_map
//  --html_output
//  --debug_mode
//  --output field_name to be parsed
//  --original //don't use context variables mapped //only original// with original context
//  --filter talendComponents

public class ParseApp {
	  
	public static void main( String[] args ) {
		System.out.println("Starting ParseSql:");
       
		//create comman line opitions 
		CmdOptions CliOpt = new talend.CmdOptions(args);
        HashMap <String, String> options = new HashMap<>();
        
		options = CliOpt.getOptions();
		String[] input_job_files = CliOpt.getFiles();
		ArrayList <String> all_latest_jobs = CliOpt.getAllLatestFiles();
		
		if (options.containsKey("all_latest") ){
			input_job_files = all_latest_jobs.toArray(new String[0]);
		}
		
        for (int cnt = 0; cnt < input_job_files.length; cnt ++) {
        	String job_file = input_job_files[cnt];
        	System.out.println("Start paring file[" + job_file + "]");
        	try {
        	   JobXml job = new talend.JobXml( job_file,  options.get("context_env"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
}	