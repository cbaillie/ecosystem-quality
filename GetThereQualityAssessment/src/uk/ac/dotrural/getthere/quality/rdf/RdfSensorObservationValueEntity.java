package uk.ac.dotrural.getthere.quality.rdf;

import uk.ac.dotrural.reasoning.rdf.RdfEntity;
import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationValueEntity;
import uk.ac.dotrural.getthere.quality.entity.SensorObservationUtilities;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import uk.ac.dotrural.reasoning.entity.JsonEntity;

public class RdfSensorObservationValueEntity extends RdfEntity {
	
	private OntModel sensorObservationValueModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	
	private String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private String QUALITY = "http://dtp-126.sncs.abdn.ac.uk/Quality#";
	private String SENSORS = "http://www.dotrural.ac.uk/irp/uploads/ontologies/sensors/";
	private String GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	
	private String endpoint;
	private String featureOfInterest;
	private String JOURNEY_SERVICE = "ecosystem-transport/journey/get?journeyUri=";
	
	public RdfSensorObservationValueEntity(String e, String foi)
	{
		endpoint = e;
		featureOfInterest = foi;
	}

	public void jsonEntityToRdfEntity(JsonEntity entity) 
	{
		JsonSensorObservationValueEntity observationValue = (JsonSensorObservationValueEntity)entity;
		String observationValueUri = observationValue.getUri();
		//log("jsonEntityToRdfEntity","Observation value uri is " + observationValueUri);
		String observationValueLat = observationValue.getLatitude();
		//log("jsonEntityToRdfEntity", "Observation latitude is " + observationValueLat);
		String observationValueLon = observationValue.getLongitude();
		//log("jsonEntityRdfEntity","Observation longitude is " + observationValueLon);
		String observationValueAccuracy = observationValue.getAccuracy();
		//log("jsonEntityRdfEntity","Observation accuracy is " + observationValueAccuracy);
		
		//Build model
		Resource observationValueResource = sensorObservationValueModel.createResource(observationValueUri);
		Statement observationValueTypeStmt = sensorObservationValueModel.createStatement(observationValueResource, sensorObservationValueModel.createProperty(RDF + "type"), sensorObservationValueModel.createResource("http://www.dotrural.ac.uk/irp/uploads/ontologies/sensors/LocationDeviceValue"));
		Statement observationValueLatStmt = sensorObservationValueModel.createStatement(observationValueResource, sensorObservationValueModel.createProperty(GEO + "lat"), sensorObservationValueModel.createTypedLiteral(Double.parseDouble(observationValueLat)));
		Statement observationValueLonStmt = sensorObservationValueModel.createStatement(observationValueResource, sensorObservationValueModel.createProperty(GEO + "long"), sensorObservationValueModel.createTypedLiteral(Double.parseDouble(observationValueLon)));
		//log("jsonEntityToRdFEntity","Add accuracy statement");
		
		if(observationValueAccuracy == "null");
			observationValueAccuracy = "1000000";
			
		Statement observationValueAccuracyStmt = sensorObservationValueModel.createStatement(observationValueResource, sensorObservationValueModel.createProperty(SENSORS + "accuracy"), sensorObservationValueModel.createTypedLiteral(Double.parseDouble(observationValueAccuracy)));
		//log("jsonEntityToRdfEntity","Accuracy statement added");
		
		//log("jsonEntityToRdfEntity","Calculating distnace from the route");
		//Calculate distance from the route
		double distance = SensorObservationUtilities.getDistanceFromRoute(endpoint, observationValueLat, observationValueLon, endpoint + JOURNEY_SERVICE + featureOfInterest);
		Statement observationValueDistanceFromRouteStmt = sensorObservationValueModel.createStatement(observationValueResource, sensorObservationValueModel.createProperty(QUALITY, "distanceFromRoute"), sensorObservationValueModel.createTypedLiteral(distance));
		
		//log("jsonEntityToRdfEntity","Building model");
		
		sensorObservationValueModel.add(observationValueTypeStmt);
		sensorObservationValueModel.add(observationValueLatStmt);
		sensorObservationValueModel.add(observationValueLonStmt);
		sensorObservationValueModel.add(observationValueAccuracyStmt);
		sensorObservationValueModel.add(observationValueDistanceFromRouteStmt);
		//log("jsonEntityToRdfEntity","ObservationValue built");
	}
	
	/**
	 * Return the RDF model of this SensorObservation
	 * 
	 * @return RDF model
	 */
	public OntModel getSensorObservationValueModel()
	{
		return sensorObservationValueModel;
	}
	
	public void log(String method, String msg)
	{
		System.out.println("[RdfSensorObservationValueEntity] " + method + " : " + msg);
	}

}
