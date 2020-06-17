package farsight.switchpipe.activation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.softwareag.util.IDataMap;
import com.wm.data.IData;

public class ServiceState {
	
	public static final ServiceState ACTIVE = new ServiceState(Type.ACTIVE, 0, null);
	public static final ServiceState OFF = new ServiceState(Type.OFF, 0, null);
	public static final ServiceState UNCONFIGURED = new ServiceState(Type.UNCONFIGURED, 0, null);
	
	public static enum Type {
		UNCONFIGURED,
		ACTIVE,
		OFF,
		TEMPORARY_COUNT,
		TEMPORARY_TIME
	}
	
	public static class ServiceStateEntry {
		public final String serviceID;
		public final ServiceState state;
		
		public ServiceStateEntry(String serviceID, ServiceState state) {
			this.serviceID = serviceID;
			this.state = state;
		}
		
		public IData getIData() {
			IDataMap data = new IDataMap();
			data.put("serviceID", serviceID);
			state.setTo(data);
			return data.getIData();
		}
	}
	
	public final Type type;
	private int counter;
	private LocalDateTime date;
	
	private ServiceState(Type type, int counter, LocalDateTime date) {
		this.type = type;
		this.counter = counter;
		this.date = date;
	}
	
	public static ServiceState get(ActivationState state) {
		switch (state) {
		case ACTIVE:
			return ACTIVE;
		case INACTIVE:
			return OFF;
		default:
			return UNCONFIGURED;
		}
	}
	
	public int getCounter() {
		return counter;
	}
	
	public LocalDateTime getDate() {
		return date;
	}
	
	private void setTo(IDataMap data) {
		data.put("mode", type.toString());
		switch(type) {
		case ACTIVE:
			data.put("enabled", "true");
			break;
		case OFF:
			data.put("enabled", "false");
			break;
		case TEMPORARY_COUNT:
			data.put("enabled", String.valueOf(counter > 0));
			data.put("counter", String.valueOf(counter));
			break;
		case TEMPORARY_TIME:
			data.put("enabled", String.valueOf(date.isBefore(LocalDateTime.now())));
			data.put("date", date);
			break;
		default:
			break;
		}
	}
	
	public static ServiceState create(Boolean desiredState) {
		if(desiredState == null)
			return UNCONFIGURED;
		return desiredState ? ACTIVE : OFF;
	}

	public static ServiceState createTemporaryCounter(int counter) {
		return new ServiceState(Type.TEMPORARY_COUNT, counter, null);
	}
	
	public static ServiceState createTemporaryDate(LocalDateTime date) {
		return new ServiceState(Type.TEMPORARY_TIME, 0, date);
	}
	
	public static ServiceState createTemporaryOffset(int seconds) {
		return createTemporaryDate(LocalDateTime.now().plusSeconds(seconds));
	}
	
	//legacy 
	public static ServiceState createTemporaryDate(Date date) {
		if(date == null)
			return UNCONFIGURED;
		return createTemporaryDate(new java.sql.Timestamp(date.getTime()).toLocalDateTime());
	}
	
	public static ServiceState createTemporaryDate(long date) {
		if(date <= 0)
			return UNCONFIGURED;
		return createTemporaryDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()));
	}
	
	
	// implementation based on ServiceState
	
	public ActivationState trigger() {
		switch(type) {
		case ACTIVE:
			return ActivationState.ACTIVE;
		case OFF:
			return ActivationState.INACTIVE;
		case TEMPORARY_COUNT:
			if(counter > 0) {
				counter--;
				return ActivationState.ACTIVE;
			} else {
				return ActivationState.INACTIVE;
			}
		case TEMPORARY_TIME:
			return date.isAfter(LocalDateTime.now()) ?
					ActivationState.ACTIVE :
					ActivationState.INACTIVE;
		default:
			return ActivationState.UNDEFINED;
		}
	}
	
	public ActivationState check() {
		switch(type) {
		case ACTIVE:
			return ActivationState.ACTIVE;
		case OFF:
			return ActivationState.INACTIVE;
		case TEMPORARY_COUNT:
			return counter > 0 ?
					ActivationState.ACTIVE :
					ActivationState.INACTIVE;	
		case TEMPORARY_TIME:
			return date.isAfter(LocalDateTime.now()) ?
					ActivationState.ACTIVE :
					ActivationState.INACTIVE;
		default:
			return ActivationState.UNDEFINED;
		}
	}



	
	
	

}
