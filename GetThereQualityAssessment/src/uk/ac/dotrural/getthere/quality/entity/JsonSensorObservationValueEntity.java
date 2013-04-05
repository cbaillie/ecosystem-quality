package uk.ac.dotrural.getthere.quality.entity;

import uk.ac.dotrural.reasoning.entity.EntityUtilities;
import uk.ac.dotrural.reasoning.entity.JsonEntity;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JsonSensorObservationValueEntity extends JsonEntity {
	
	private String jsonDescription;
	private JSONObject sensorObservationValue;
	private final String SERVICE = "ecosystem-transport/observation/getObservationValue?observationValueUri=";

	public JsonSensorObservationValueEntity(String endpoint, String uri)
	{
		super();	
		setJsonDescription(endpoint + SERVICE + uri);
	}
	
	public void setJsonDescription(String uri) 
	{
		this.log("setJsonDescription", "Observation Value has URI " + uri);
		jsonDescription = EntityUtilities.getModelJson(uri);
		sensorObservationValue = (JSONObject)JSONSerializer.toJSON(jsonDescription);
	}

	public String getJsonDescription()
	{
		return jsonDescription;
	}
	
	public String getUri()
	{
		return getJsonValue("uri");
	}
	
	public String getLatitude()
	{
		return getJsonValue("latitude");
	}
	
	public String getLongitude()
	{
		return getJsonValue("longitude");
	}
	
	public String getAccuracy()
	{
		return getJsonValue("accuracy");
	}
	
	private String getJsonValue(String name)
	{
		if(sensorObservationValue.has(name))
		{
			return (String)sensorObservationValue.get(name);
		}
		//log("getJsonValue","sensorObservationValue has no associated " + name);
		return "null";
	}
	
	public void log(String method, String msg)
	{
		System.out.println("[JsonSensorObservationValueEntity] " + method + " : " + msg);
	}

}
