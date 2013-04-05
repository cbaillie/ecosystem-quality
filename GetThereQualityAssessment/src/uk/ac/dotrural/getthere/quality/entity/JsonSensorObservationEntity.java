package uk.ac.dotrural.getthere.quality.entity;

import uk.ac.dotrural.reasoning.entity.EntityUtilities;
import uk.ac.dotrural.reasoning.entity.JsonEntity;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JsonSensorObservationEntity extends JsonEntity {
	
	private String jsonDescription;
	private JSONObject sensorObservation;
	private final String SERVICE = "ecosystem-transport/observation/get?observationUri=";
	
	public JsonSensorObservationEntity(String endpoint, String uri)
	{
		super();
		setJsonDescription(endpoint + SERVICE + uri);
	}
	
	public void setJsonDescription(String uri) 
	{
		this.log("setJsonDescription", "Observation has URI " + uri);
		jsonDescription = EntityUtilities.getModelJson(uri);
		sensorObservation = (JSONObject)JSONSerializer.toJSON(jsonDescription);
	}

	public String getJsonDescription() {
		return jsonDescription;
	}
	
	public String getFeatureOfInterest()
	{
		JSONObject foi = getJsonObject("featureOfInterest");
		if(foi != null)
			return getJsonValue(foi, "uri");
		return "null";
	}
	
	public String getObservationResult()
	{
		JSONObject result = getJsonObject("observationResult");
		if(result != null)
			return getJsonValue(result, "uri");
		return "null";
	}
	
	public String getObservationResultTime()
	{
		return getJsonValue(sensorObservation, "observationResultTime");
	}
	
	public String getObservationSamplingTime()
	{
		return getJsonValue(sensorObservation, "observationSamplingTime");
	}
	
	public String getServerTime()
	{
		return getJsonValue(sensorObservation, "serverTime");
	}
	
	public String getObservedBy()
	{
		JSONObject observedBy = getJsonObject("observationBy");
		if(observedBy != null)
			return getJsonValue(observedBy, "uri");
		return "null";
	}
	
	public String getSensingMethodUsed()
	{
		JSONObject sensingMethodUsed = getJsonObject("sensingMethodUsed");
		if(sensingMethodUsed != null)
			return getJsonValue(sensingMethodUsed, "uri");
		return "null";
	}
	
	public String getObservationUri()
	{
		return getJsonValue(sensorObservation, "uri");
	}
	
	public String getWasDerivedFrom()
	{
		return getJsonValue(sensorObservation, "derivedFrom");
	}
	
	private JSONObject getJsonObject(String name)
	{
		if(sensorObservation.has(name))
		{
			return (JSONObject)sensorObservation.get(name);
		}
		//log("getJsonObject","sensorObservation has no associated " + name);
		return null;
	}
	
	private String getJsonValue(JSONObject obj, String name)
	{
		if(obj.has(name))
		{
			return (String)obj.get(name);
		}
		return "null";
	}
	
	public void log(String method, String msg)
	{
		System.out.println("[JsonSensorObservationEntity] " + method + " : " + msg);
	}

}
