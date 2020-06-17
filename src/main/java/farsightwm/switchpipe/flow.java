package farsightwm.switchpipe;

// --- <<IS-END-IMPORTS>> ---
import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.ServiceException;

// -----( IS Java Code Template v1.2
import com.wm.data.IData;

import farsight.switchpipe.SwitchpipeAPI;

public final class flow

{
	// ---( internal utility methods )---

	final static flow _instance = new flow();

	static flow _newInstance() { return new flow(); }

	static flow _cast(Object o) { return (flow)o; }

	// ---( server methods )---




	public static final void loadPipelineDocument (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(loadPipelineDocument)>> ---
		// @sigtype java 3.5
		// [i] field:0:required debugID
		// [i] field:0:required serviceID
		// [i] field:0:required storeID
		// [o] record:0:required document
		IDataMap p = new IDataMap(pipeline);
		p.put("document", SwitchpipeAPI.defaultInstance().loadPipeline(
				p.getAsString("debugID"),
				p.getAsString("serviceID"),
				p.getAsString("storeID")));
			
		// --- <<IS-END>> ---

                
	}



	public static final void restorePipeline (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(restorePipeline)>> ---
		// @sigtype java 3.5
		// [i] field:0:required debugID
		// [i] field:0:required serviceID
		// [i] field:0:required storeID
		IDataMap p = new IDataMap(pipeline);
		SwitchpipeAPI.defaultInstance().restorePipeline(
				pipeline,
				p.getAsString("debugID"),
				p.getAsString("serviceID"),
				p.getAsString("storeID"));
			
		// --- <<IS-END>> ---

                
	}



	public static final void storePipeline (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(storePipeline)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional debugID
		// [i] field:0:optional serviceID
		// [i] field:0:optional storeID
		// [o] field:0:optional debugID
		IDataMap p = new IDataMap(pipeline);
		String debugID = p.getAsString("debugID");
		p.remove("debugID"); //prevent debugIDs from getting stored
		String generatedDebugID = SwitchpipeAPI.defaultInstance().storePipeline(
				pipeline,
				debugID,
				p.getAsString("serviceID"),
				p.getAsString("storeID"));
		if(generatedDebugID != null) {
			p.put("debugID", generatedDebugID);
		} else if(debugID != null) {
			p.put("debugID", debugID); //restore debugID if set
		}
		// --- <<IS-END>> ---

                
	}



	public static final void storePipelineDocument (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(storePipelineDocument)>> ---
		// @sigtype java 3.5
		// [i] record:0:required document
		// [i] field:0:optional debugID
		// [i] field:0:optional serviceID
		// [i] field:0:optional storeID
		// [o] field:0:required debugID
		IDataMap p = new IDataMap(pipeline);
		IData document = p.getAsIData("document");
		if (document == null)
			return;
		SwitchpipeAPI.defaultInstance().storePipeline(
				document,
				p.getAsString("debugID"),
				p.getAsString("serviceID"),
				p.getAsString("storeID"));		
		// --- <<IS-END>> ---

                
	}



	public static final void switchpipe (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(switchpipe)>> ---
		// @sigtype java 3.5
		// [i] field:0:optional debugID
		// [i] field:0:optional debugServiceID
		// [i] field:0:optional debugStoreID
		// [o] field:0:optional debugID
		IDataMap p = new IDataMap(pipeline);
		String debugID = p.getAsString("debugID");
		p.remove("debugID"); //prevent debugIDs from getting stored
		String generatedDebugID = SwitchpipeAPI.defaultInstance().switchPipeline(
				pipeline,
				debugID,
				p.getAsString("debugServiceID"),
				p.getAsString("debugStoreID"));
		if(generatedDebugID != null) {
			p.put("debugID", generatedDebugID);
		} else if(debugID != null) {
			p.put("debugID", debugID); //restore debugID if set
		}
		// --- <<IS-END>> ---

                
	}
}

