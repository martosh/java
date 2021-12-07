package talend;
//TODO create log output/debug, remove stdout
//output data should be implemented in objects 

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

//import java.io.Serializable;
//import org.apache.commons.lang.builder.ToStringBuilder;
//import org.apache.commons.lang.builder.ToStringStyle;
//TODO: fix wrong Items files in --all option
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobXml
{

	private String xml_path;
	private String context_env;
	private HashMap<String, String> xmlContext = new HashMap<>();
	//result_hash maybe it's better to be a array of objects;
	private HashMap<String, HashMap<String,String>> xmlComponents = new HashMap<>();
	private ArrayList<String> Parents = new ArrayList <>();

	private HashMap <String, ArrayList<String>> parentsChildren = new HashMap<>();

	private ArrayList<String> Children = new ArrayList <>();
	//Ordered Tree is array with 

	private ArrayList<String> OrderedTree = new ArrayList <>();
	private ArrayList<String> Roots = new ArrayList <>();
	private Integer level = 0;
	private Integer saved_node_level = 0;

	//#######################################
	//  Constructor 
	//#######################################
	public JobXml(String xml_path, String context_env) {
		File xml = new File(xml_path); 

		this.xml_path = xml_path;
		this.context_env = context_env;

		if(xml.exists() && !xml.isDirectory() && xml.length() > 0) {

			this.createContextEnv();
			System.out.print(this.getContextEnv().toString());
			this.createComponents();
			//System.out.print(this.getComponents().toString());
			this.createOrderedTree();
			//System.out.println( this.getOrderedTree.toString());

			//for ( int cnt = 0; cnt < OrderedTree.size() ; cnt++) {
			//System.out.println(OrderedTree.get(cnt) );
			//}
			//TODO: this will create useful data for some components for example create table statement;
			//this.filter();

			this.doOutput();

		} else {
			System.out.println( "Error: the given xml file[" + xml_path + "] do not exist or it's too small");
			System.exit(1);
		}
	}

	//#######################################
	// 
	//#######################################
	private void doOutput() {

		System.out.println("\n\n" + "#####".repeat(20));
		System.out.println("\t#####->" + xml_path + "<-#####");
		System.out.println("#####".repeat(20) + "\n" );

		for (int cnt = 0; cnt < OrderedTree.size(); cnt++ ) {
			//level;node;name
			String[] params = OrderedTree.get(cnt).split(";");  
			System.out.println("");
			System.out.println("####".repeat(10) );
			System.out.println(params[0] +";" + params[1] + ";" + params[2]);
			System.out.println("####".repeat(10) );
			System.out.println("");
			System.out.println( xmlComponents.get(params[2]));

		}
	}

	//#######################################
	//  read context environment from XML
	//#######################################
	private void createContextEnv() {
		//takes String->Array

		HashMap<String, String> context_result = new HashMap<String, String> ();

		Document doc = this.getXmlParser();
		NodeList nList = doc.getElementsByTagName("context");

		boolean found_cont_env = false;
		StringBuilder contextListBuilder = new StringBuilder();

		for (int cnt = 0; cnt < nList.getLength(); cnt++) {
			Node nNode = nList.item(cnt);

			Element eElement = (Element) nNode;
			//System.out.println("\nCurrent Element :" + nNode.getNodeName() + "attr name [" + eElement.getAttribute("name") + "]");


			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				contextListBuilder.append(eElement.getAttribute("name") + ";" );

				if (context_env.equalsIgnoreCase(eElement.getAttribute("name") ) ) {

					System.out.println("Parsing context_env for " + this.context_env + "!");
					found_cont_env = true;

					NodeList nList_lower = eElement.getElementsByTagName("contextParameter");
					for (int p_cnt = 0; p_cnt < nList_lower.getLength(); p_cnt++) {
						Node cparNode = nList_lower.item(p_cnt);
						Element cparElement = (Element) cparNode;
						//System.out.println( cparElement.getAttribute("name") +  " -> " + cparElement.getAttribute("value"));

						context_result.put(cparElement.getAttribute("name"), cparElement.getAttribute("value").replaceAll("\"", ""));
					}
				} 

			}

		}

		if (found_cont_env == false ) {
			System.out.println( "Error: no data found for context environment[" + context_env + "]");
			System.out.println( "\tAvailable context environment: " + contextListBuilder.toString() );
			//System.exit(0);
		}

		this.xmlContext = context_result; 
		//return context_result;
	}

	//###################################
	// getter for xmlContext Environment 
	//###################################
	public HashMap<String, String> getContextEnv(){

		return this.xmlContext; 
	}

	//###################################
	//  read Talend components from XML
	//###################################
	private void createComponents (){

		HashMap<String, HashMap<String,String>> result = new HashMap<>();

		//Looks like this lib is vary bad choice for parsing xml
		Document doc = this.getXmlParser();
		NodeList nList = doc.getElementsByTagName("node");

		//This is mapping of names taken from xml and names show in the output 
		HashMap<String, String> elementsToTakeOutputNameXmlName = new HashMap<String,String>(){{
			put("file_action", "file_action" );
			put("code" , "java_code");
			put("query", "\nSQL" );
			put("host", "host" );
			put("port", "port" );
			put("user", "user" );
			put("table", "table" );
			put("tableaction", "table_action" );
			put("storage_format", "file_type" );
			put("label", "custom_name" );

		}};

		for (int cnt = 0; cnt < nList.getLength(); cnt++) {
			Node nNode = nList.item(cnt);

			Element eElement = (Element) nNode;
			//System.out.println("\nCurrent Element :" + nNode.getNodeName() + "attr name [" + eElement.getAttribute("name") + "]");

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				NodeList nList_lower = eElement.getElementsByTagName("elementParameter");

				String uniq_name_key = null; //result hash key

				HashMap <String,String> inner_elements = new HashMap <>();

				inner_elements.put("component_name", eElement.getAttribute("componentName"));

				for (int p_cnt = 0; p_cnt < nList_lower.getLength(); p_cnt++) {

					Node jobNode = nList_lower.item(p_cnt);
					Element jobElement = (Element) jobNode;
					//System.out.println( "field: " + jobElement.getAttribute("field") + " name: " + jobElement.getAttribute("name") + " value:" + jobElement.getAttribute("value") );
					//Take <node><elementParameter value= > from xml

					if (jobElement.getAttribute("name").equalsIgnoreCase("unique_name")) {
						uniq_name_key = jobElement.getAttribute("value");
					}

					for (String element_to_take : elementsToTakeOutputNameXmlName.keySet() ) 
					{

						if (jobElement.getAttribute("name").equalsIgnoreCase(element_to_take) ) {
							String element_data = "";


							element_data = fixContextValues(jobElement.getAttribute("value") , this.getContextEnv());
							//add new line for readability remove it in html output 
							if (element_to_take.equalsIgnoreCase("query") ) {
								element_data = "\n" + element_data + "\n\n";
							}

							if (element_data != null ) {
								inner_elements.put( elementsToTakeOutputNameXmlName.get(element_to_take) , element_data );
							}
						}
					}

					//lowest level 
					//TODO: if to many field are important from elementValue tag do it with for HashMap	
					NodeList nList_bottom = jobElement.getElementsByTagName("elementValue");

					for (int bot_c = 0; bot_c < nList_bottom.getLength(); bot_c++) {
						Node lastNode = nList_bottom.item(bot_c);
						Element lastElement = (Element) lastNode;

						if (lastElement.getAttribute("elementRef").equalsIgnoreCase("command")) {
							inner_elements.put( "command", fixContextValues(lastElement.getAttribute("value"), this.getContextEnv() ));
						}
					}

					result.put(uniq_name_key, inner_elements);
				}

				//Take metadata tag columns 
				NodeList nList_metadata = eElement.getElementsByTagName("metadata");

				String[] columnAttributes = { "usefulColumn", "length", "name", "sourceType", "nullable", "precision", "pattern" };

				StringBuilder schema = new StringBuilder();

				//full table schema   
				for (int m_cnt = 0; m_cnt < nList_metadata.getLength(); m_cnt++) {

					Node jobMetaNode = nList_metadata.item(m_cnt);
					Element jobMetaElement = (Element) jobMetaNode;

					//Take only metadata->connector=FLOW 
					if (jobMetaElement.getAttribute("connector").equals("FLOW")) {
						//schema.append( jobMetaElement.getAttribute("connector") + "\n" );
						schema.append("\n");

						NodeList nList_bottom = jobMetaElement.getElementsByTagName("column");
						// cycle column tags
						for (int bot_c = 0; bot_c < nList_bottom.getLength(); bot_c++) {
							Node lastNode = nList_bottom.item(bot_c);
							Element lastElement = (Element) lastNode;

							//header of csv fields
							if (bot_c == 0) {
								for (int attr_cnt = 0; attr_cnt < columnAttributes.length; attr_cnt++) {
									schema.append(columnAttributes[attr_cnt] + ";");
								}
								schema.append("\n");
							}

							for (int attr_cnt = 0; attr_cnt < columnAttributes.length; attr_cnt++) {
								schema.append( lastElement.getAttribute( columnAttributes[attr_cnt] ) + ";");
							}

							schema.append("\n");
						}

						schema.append("\n");


						inner_elements.put( "schema", schema.toString());

						//tHiveCreateTable
						if ( inner_elements.get("component_name" ).contains("Create")) {
							inner_elements.put(  "create_table" , this.genCreateTableStatement( inner_elements ));
						}
					}
				}
			}
		}

		this.xmlComponents = result;
	}

	//###################################
	// getter for Talend Components 
	//###################################
	public HashMap<String, HashMap<String, String>> getComponents( ){

		return this.xmlComponents; 
	}
	//################################################################################
	//  read non ordered connection between Talend components from xml connection tag
	//  and create directory style tree by calling buildTree()
	//################################################################################
	private void createOrderedTree(){

		Document doc = this.getXmlParser();
		//parse xml connection tag
		NodeList nList = doc.getElementsByTagName("connection");

		for (int cnt = 0; cnt < nList.getLength(); cnt++) {
			Node nNode = nList.item(cnt);

			Element eElement = (Element) nNode;

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				this.Parents.add( eElement.getAttribute("source") );
				this.Children.add( eElement.getAttribute("target") );

				//System.out.println( eElement.getAttribute("source") + " -> " + eElement.getAttribute("target"));
			}
		}

		for (int cnt = 0; cnt < Parents.size(); cnt++) {
			ArrayList<String> memberChildren = new ArrayList<>();
			memberChildren = this.isThisParent( Parents.get(cnt) );
			parentsChildren.put( Parents.get(cnt), memberChildren );
		}

		//System.out.println( parentsChildren.toString() );

		ArrayList<String> rootMembers = findRootParent();
		Roots = rootMembers;
		//System.out.println( rootMembers.toString() );

		this.buildTree(rootMembers);
	}

	//###############################################################
	// callBack method for building Parent->Child tree array (Directory tree) 
	// Puts the result in this.OrderTree
	//###############################################################
	private void buildTree (ArrayList<String> members ) {

		//for all given members check who is parent of who via pareteChildren hash 
		for (int cnt_member = 0; cnt_member < members.size(); cnt_member++) {	
			String member = members.get(cnt_member);

			//if member is root set zero the level variable
			for ( int rc = 0; rc < Roots.size(); rc++ ) {
				if (member.equals( Roots.get(rc))) {
					level = 0;
				}
			}

			if (parentsChildren.containsKey(member)) {
				//System.out.println( "Parent[" + member + "] has children [" + foundChildren.toString() + "]");
				//add member, first start is with root members
				this.OrderedTree.add( level + ";+;" + member);


				level++;
				//check if there are more than one node save level
				if (parentsChildren.get(member).size() > 1 ) {
					saved_node_level = level;	
				}

				//add in Tree childless children first
				for (int child_cnt = 0; child_cnt < parentsChildren.get(member).size(); child_cnt++) {
					String child = parentsChildren.get(member).get(child_cnt);

					//if notContainsKey in parentsChildren hash or member is child that does not have children
					if (parentsChildren.get(child) == null) {
						OrderedTree.add( level + ";;" + child);
						if (saved_node_level != 0) {
							//load level back to node
							level = saved_node_level;   						
						}
						parentsChildren.get(member).remove(child_cnt);
					}
				}

				//put all children that are parent last
				//call back for children that are parents
				this.buildTree(parentsChildren.get(member));

			} else {
				//member is not a parent
				this.OrderedTree.add( level + ";;" + member);
				//End of tree
			}

		}
	}

	//###################################
	// getter for orderedTree
	//###################################
	public ArrayList<String> getOrderTree(){

		return this.OrderedTree; 
	}

	//###############################################################

	//###############################################################
	private ArrayList<String> isThisParent( String member) {
		ArrayList<String> foundChildren = new ArrayList<String >();

		//System.out.println( "Check is this memebre has chieldren " + member); 
		for (int c = 0; c < Parents.size(); c++) {
			//System.out.println(Parents.get(c) + " => " + Children.get(c));
			if (member.equals( Parents.get(c)) ) {
				foundChildren.add(Children.get(c));
			}
		}

		return foundChildren;
	}

	//###############################################################
	// Find the first elements i.e the roots(may be more than one)
	//###############################################################
	private ArrayList<String> findRootParent () {

		ArrayList<String> root =  new ArrayList<>();

		parents: for (int counter = 0; counter < Parents.size(); counter++) {
			//if parent does not appear as a child it is root
			for ( int cnt = 0; cnt < Children.size(); cnt++) { 		      
				if (Parents.get(counter).equals( Children.get(cnt))) {
					continue parents;
				}
			}
			root.add( Parents.get(counter) );
		}   		

		return root;
	}

	//####################################################
	// Prepare xml document reader for parsing data   
	//####################################################
	private Document getXmlParser(){

		File inputFile = new File(this.xml_path);

		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			return doc;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	//####################################################
	//  map sql query with context.env to executable sql 
	//####################################################
	private static String fixContextValues (String raw_string, HashMap<String, String> contextEnv  )
	{

		Pattern r = Pattern.compile("\\\"?\\s*\\+?\\s*context\\.(\\w+)\\s*\\+?\\s*\\\"?", Pattern.MULTILINE );
		Matcher m = r.matcher(raw_string);

		while (m.find()){
			//replace "+content.$catch_regex_group+" with $catch_regex_group and map the real value with contextEnv-Hash
			String context_env_key = m.group(1);
			String replace_with_value = null;

			if (contextEnv.containsKey(context_env_key)) {
				replace_with_value = contextEnv.get(context_env_key);
			} else {
				replace_with_value = "MISSING_CONTEXT_KEY[context." + context_env_key + "]"; 
			}

			raw_string = raw_string.replaceAll("\"?\\s*\\+?\\s*context\\." + context_env_key + "\\s*\\+?\\s*\"?", replace_with_value );

		} 

		return raw_string;
	}

	//##############################################################
	// Generates Create Table statement from csv formatted string 
	//##############################################################
	private String genCreateTableStatement ( HashMap<String, String> elements  ) {

		String[] rows = elements.get("schema").split("\n");
		String action = "";

		if (elements.get( "table_action") != null) {
			if ( elements.get("table_action").contains( "NOT") ) {
				action = "CREATE TABLE IF NOT EXISTS";
			} else {
				action = "CREATE TABLE";
			}
		}

		//Header  ->usefulColumn;length;name;sourceType;nullable;Precision;pattern
		//Example line->true;15;anzpe1_best;DECIMAL;true;0;;
		//sourceType is DECIMAL or STRING

		StringBuilder result = new StringBuilder();

		result.append( "\n " + action + " " + elements.get("table") + " ( ");

		for ( int rcnt = 0; rcnt < rows.length; rcnt++ ) {

			if (rows[rcnt].isEmpty() ) {
				continue;
			}

			String[] columns  = rows[rcnt].split(";");
			String  usable = columns[0];
			String  length = columns[1];
			String  name   = columns[2];
			String  type = columns[3].toLowerCase();
			String  precision = columns[5];
			//String  nullable = columns[4];

			if (usable.equals("true") ) { //take active fields

				result.append(   name + " " );
				if ( type.equals("decimal") ) {
					result.append(  type + "(" + length + ","+ precision + ")" );
				} else if ( type.equals("string") ) {
					result.append( type );
				}

				result.append(", ");
			}

		}
		//remove last coma	
		result.setLength(result.length() - 2);	
		result.append( " ) stored as PARQUET; \n");

		return result.toString();
	}
	
}
// TalendEntity EntityCollector 