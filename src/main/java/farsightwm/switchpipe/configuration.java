package farsightwm.switchpipe;

import java.util.Date;

import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---
import com.wm.app.b2b.server.ServiceException;

// -----( IS Java Code Template v1.2
import com.wm.data.IData;

import farsight.switchpipe.SwitchpipeAPI;
import farsight.switchpipe.activation.ServiceState;
import farsight.switchpipe.activation.ServiceState.Type;
import farsight.switchpipe.exception.SwitchpipeException;

public final class configuration

{
	// ---( internal utility methods )---

	final static configuration _instance = new configuration();

	static configuration _newInstance() { return new configuration(); }

	static configuration _cast(Object o) { return (configuration)o; }

	// ---( server methods )---




	public static final void checkEnabled (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(checkEnabled)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional serviceID
		// [o] field:0:required enabled
		IDataMap p = new IDataMap(pipeline);
		p.put("enabled", String.valueOf(SwitchpipeAPI.defaultInstance().isSwitchpipeEnabled(p.getAsString("serviceID"))));			
		// --- <<IS-END>> ---

                
	}



	public static final void configureService (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(configureService)>> ---
		// @sigtype java 3.5
		// [i] field:0:required serviceID
		// [i] field:0:optional mode {"OFF","ACTIVE","UNCONFIGURED","TEMPORARY_COUNT","TEMPORARY_TIME"}
		// [i] field:0:optional setCounter
		// [i] object:0:optional setDate
		// [i] field:0:optional setDuration
		// [i] field:0:optional persistent {"false","true"}
		// [o] field:0:required success {"false","true"}
		IDataMap p = new IDataMap(pipeline);
		boolean persistent = p.getAsBoolean("persistent");
		String serviceID = p.getAsString("serviceID");
		String modeString = p.getAsString("mode");
		ServiceState.Type mode = modeString == null ? null : ServiceState.Type.valueOf(modeString);
		ServiceState state; 
		
		if(mode == null) {
			if(p.containsKey("setCounter"))
				mode = Type.TEMPORARY_COUNT;
			else if(p.containsKey("setDate") || p.containsKey("setDuration"))
				mode = Type.TEMPORARY_TIME;
			else
				mode = Type.UNCONFIGURED;
		}
		
		switch(mode) {
		case ACTIVE:
			state = ServiceState.ACTIVE;
			break;
		case OFF:
			state = ServiceState.OFF;
			break;
		case TEMPORARY_COUNT:
			state = ServiceState.createTemporaryCounter(p.getAsInteger("setCounter", 3));
			break;
		case TEMPORARY_TIME:
			if(p.containsKey("setDate")) {
				state = ServiceState.createTemporaryDate(((Date)p.get("setDate")).getTime());
			} else {
				state = ServiceState.createTemporaryOffset(p.getAsInteger("setDuration", 300));
			}
			break;
		default:
			state = ServiceState.UNCONFIGURED;
		}
		p.put("success", String.valueOf(SwitchpipeAPI.defaultInstance().configureService(serviceID, state, persistent)));
		
		
		
			
		// --- <<IS-END>> ---

                
	}



	public static final void listServices (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(listServices)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional packageName
		// [o] record:1:required services
		// [o] - field:0:required serviceID
		// [o] - field:0:required mode
		// [o] - field:0:required enabled
		// [o] - field:0:required counter
		// [o] - object:0:required date
		IDataMap p = new IDataMap(pipeline);
		p.put("services", SwitchpipeAPI.defaultInstance().getConfiguredServicesAsIDataList(p.getAsString("packageName")));
			
		// --- <<IS-END>> ---

                
	}



	public static final void reloadConfiguration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(reloadConfiguration)>> ---
		// @sigtype java 3.5
		try {
			SwitchpipeAPI.defaultInstance().reloadConfiguration();
		} catch(SwitchpipeException e) {
			throw new ServiceException(e);
		}
			
		// --- <<IS-END>> ---

                
	}



	public static final void showConfiguration (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(showConfiguration)>> ---
		// @sigtype java 3.5
		IDataMap p = new IDataMap(pipeline);
		SwitchpipeAPI api = SwitchpipeAPI.defaultInstance();
		p.put("initialized", api.isInitialized());
		if(api.isInitialized()) {
			p.put("source", String.valueOf(api.getConfiguration().getSource()));
			try {
				p.put("configuration", api.getConfigurationIData());
			} catch(Exception e) {}
		}
			
		// --- <<IS-END>> ---

                
	}
}

