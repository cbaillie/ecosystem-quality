package uk.ac.dotrural.getthere.quality.entity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import uk.ac.dotrural.reasoning.entity.EntityUtilities;

import uk.me.jstott.jcoord.LatLng;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class SensorObservationUtilities {
	
	/**
	 * Retrieve the observation value from a JSON observation
	 * @param observationJSON JSON string describing the observation
	 * @return The URI referencing the observation value
	 */
	
	public static String getObservationResultUri(String endpoint, String observationUri)
	{
		//log("getObservationValueUri", observationUri);
		JSONObject sensorOutputJsonObject = (JSONObject)JSONSerializer.toJSON(EntityUtilities.getModelJson(endpoint + observationUri));
		JSONObject sensorOutputHasValue = (JSONObject)sensorOutputJsonObject.get("hasValue");
		String observationResultUri = (String)sensorOutputHasValue.get("uri");
		//log("getObservationResultUri", "observationResultUri is " + observationResultUri);
		return observationResultUri;
	}
	
	public static String getJourneyRoute(String url)
	{
		String routeJson = EntityUtilities.getModelJson(url);
		
		JSONObject obj = (JSONObject)JSONSerializer.toJSON(routeJson);
		JSONObject line = obj.getJSONObject("line");
		String lineUri = line.getString("uri");
		
		log("getJourneyRoute","Line URI is " + lineUri);

		return lineUri;
	}
	
	public static double getDistanceFromRoute(String endpoint, String observationValueLat, String observationValueLon, String route)
	{
		LatLng ll = new LatLng(Double.parseDouble(observationValueLat), Double.parseDouble(observationValueLon));
		return getClosestStop(endpoint, ll, getJourneyRoute(route));
	}
	
	private static double getClosestStop(String endpoint, LatLng obs, String route)
	{
		double distance = 10000000.0;
		String url = endpoint + "ecosystem-transport/timetable/getBusStopsOnRoute?lineUri=" + route;
		
		try
		{
			String in;
			StringBuilder sb = new StringBuilder();
			InputStreamReader input = new InputStreamReader(new URL(url).openStream(), "UTF-8");
			BufferedReader br = new BufferedReader(input);
			while((in = br.readLine()) != null)
			{
				sb.append(in);
			}
			
			input.close();
			br.close();
			JSONObject js = (JSONObject) JSONSerializer.toJSON(sb.toString());
			JSONArray arr = js.getJSONArray("busStops");
			
			for(int i=0;i<arr.size();i++)
			{
				JSONObject obj = (JSONObject)arr.get(i);
				LatLng stop = new LatLng(Double.parseDouble(obj.getString("latitude")), Double.parseDouble(obj.getString("longitude")));
				double thisDistance = (obs.distance(stop)*1000.0);
				if(thisDistance < distance)
					distance = thisDistance;
			}
			
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		log("getClosestStop","Observation was (at best) " + distance + " from the bus route");
		return distance;
	}
	
	public static void log(String method, String msg)
	{
		System.out.println("[SensorObservationUtilities] " + method + " : " + msg);
	}
	

}
