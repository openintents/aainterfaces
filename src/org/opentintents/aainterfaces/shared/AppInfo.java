package org.opentintents.aainterfaces.shared;

import java.io.Serializable;
import java.util.ArrayList;

public class AppInfo implements Serializable {

	public String packageName;
	public ArrayList<IntentFilter> filters;
	public String titel;
	
	public AppInfo(){
		
	}
}
