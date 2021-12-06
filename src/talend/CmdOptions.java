package talend;

import java.util.HashMap;
import org.apache.commons.cli.*;

public class CmdOptions {

		private HashMap<String, String> Options = new HashMap<>();
		private String[] Files;

	public CmdOptions(String[] args) {
		    this.parseCliArgs(args); 	
	}

	//##############################################
	// Object with args
	//##############################################
	private void parseCliArgs( String[] args ) {

		Options options = new Options();

		Option input =  new Option ( "jobs", "talend_job", true, "Talend job file to parse eg: path/NMPstatistik_0.29.item");
		Option output = new Option ("o", "output", true, "Output result file for sql");
		Option context_env = new Option ("ce", "context_env", true, "Take wanted context environment" );

		input.setArgs(Option.UNLIMITED_VALUES); 
		input.setRequired(true);


		options.addOption(input);
		options.addOption(output);
		options.addOption(context_env);

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
		this.Options = result;
		this.Files = cmd.getOptionValues("jobs");

	}

	//getters
	public String[] getFiles() {
		return this.Files;
	}

	public HashMap<String,String> getOptions() {
		return this.Options;
	}

}


