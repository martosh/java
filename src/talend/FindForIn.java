package talend;

import java.util.ArrayList;

public class FindForIn {
         private ArrayList<String> findFor = new ArrayList<String>();
         private ArrayList<String> findIn = new ArrayList<String>();
        
         //#########################################################
         public void setFindFor ( ArrayList<String> findForArg) {
        	 
        	this.findFor = findForArg; 
        	 
         }
         
         //#########################################################
         public void setFindIn ( ArrayList<String> findInArg) {
        	 
        	this.findIn = findInArg; 
        	 
         }
         
         //#########################################################
         public ArrayList<String> getFindFor ( ){
        	 return this.findFor;
        	 
         }
         
         //#########################################################
         public ArrayList<String> getFindIn ( ){
        	 return this.findIn;
        	 
         }
         
         
}
