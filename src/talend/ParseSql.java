package talend;



//import java.util.ArrayList;
import java.util.HashMap;

//TODO Maybe in Future -> 
//    Consider git branch
//  --help 
//  --list_available_context
//  --list_context_env
//  --list_sql_no_map
//  --html_output
//  --debug_mode
//  --output field_name to be parsed

public class ParseSql {
	  
	public static void main( String[] args ) {
		System.out.println("Starting ParseSql:");
        
		CmdOptions CliOpt = new talend.CmdOptions(args);
        HashMap <String, String> options = new HashMap<>();
        
		options = CliOpt.getOptions();
		String[] input_job_files = CliOpt.getFiles();
		
        for (int cnt = 0; cnt < input_job_files.length; cnt ++) {
        	String job_file = input_job_files[cnt];
        	System.out.println("Start paring file[" + job_file + "]");
        	JobXml job = new talend.JobXml( job_file,  options.get("context_env"));
        }
	}
}	