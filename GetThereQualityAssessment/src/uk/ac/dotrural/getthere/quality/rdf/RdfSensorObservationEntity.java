package uk.ac.dotrural.getthere.quality.rdf;

import uk.ac.dotrural.reasoning.rdf.RdfEntity;
import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationEntity;
import uk.ac.dotrural.getthere.quality.entity.SensorObservationUtilities;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import uk.ac.dotrural.reasoning.entity.JsonEntity;

public class RdfSensorObservationEntity extends RdfEntity {
	
	private OntModel sensorObservationModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	
	private final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final String SSN = "http://purl.oclc.org/NET/ssnx/ssn#";
	private final String QUALITY = "http://dtp-126.sncs.abdn.ac.uk/Quality#";
	private final String SENSORS = "http://www.dotrural.ac.uk/irp/uploads/ontologies/sensors/";
	
	private String endpoint;
	private final String sensorOutputService = "observation/getSensorOutput?sensorOutputUri="; 
	
	public RdfSensorObservationEntity(String e)
	{
		endpoint = e;
	}

	public void jsonEntityToRdfEntity(JsonEntity entity) 
	{
		JsonSensorObservationEntity observation = (JsonSensorObservationEntity)entity;
		//log("jsonEntityToRdf", "Converting JsonEntity to RdfEntity (" + entity.getJsonDescription() + ")");
		
		//Feature of Interest
		String featureOfInterestUri = observation.getFeatureOfInterest();
		//log("jsonEntityToRdf", "featureOfInterest is " + featureOfInterestUri);
		
		//Observation Result
		String observationResultUri = observation.getObservationResult();
		//log("jsonEntityToRdf", "observationResult is " + observationResultUri);
		
		//Observation times
		String observationResultTime = observation.getObservationResultTime();
		String observationSamplingTime = observation.getObservationSamplingTime();
		String serverTime = observation.getServerTime();
		//log("jsonEntityToRdf", "observationResultTime is " + observationResultTime);
		//log("jsonEntityToRdf", "observationSamplingTime is " + observationSamplingTime);
		//log("jsonEntityToRdf", "serverTime is " + serverTime);
		
		//Sensing device
		String observedByUri = observation.getObservedBy();		
		//log("jsonEntityToRdf", "observedByUri is " + observedByUri);
		
		//Sensing process
		String sensingMethodUsedUri = observation.getSensingMethodUsed();
		//log("jsonEntityToRdf", "sensingMethodUsedUri is " + sensingMethodUsedUri);
		
		//Observation uri
		String observationUri = observation.getObservationUri();
		
		Resource observationResource = sensorObservationModel.createResource(observationUri);
		Statement observationTypeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(RDF, "type"), sensorObservationModel.createResource(SENSORS + "LocationDeviceObservation"));
		Statement observationResultStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResult"), sensorObservationModel.createResource(SensorObservationUtilities.getObservationResultUri(endpoint + sensorOutputService, observationResultUri)));
		Statement resultTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observationResultTime"), sensorObservationModel.createTypedLiteral(Long.parseLong(observationResultTime)));
		Statement samplingTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN,"observationSamplingTime"), sensorObservationModel.createTypedLiteral(Long.parseLong(observationSamplingTime)));
		Statement serverTimeStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SENSORS,"serverTimestamp"), sensorObservationModel.createTypedLiteral(Long.parseLong(serverTime)));
		
		//Data age
		Statement observationAge = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(QUALITY, "dataAge"), sensorObservationModel.createTypedLiteral(calculateDataAge(observationSamplingTime)));

		/******************/
		/** NEEDS WORK ! **/
		Statement featureOfInterestStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN,"featureOfInterest"), sensorObservationModel.createTypedLiteral(featureOfInterestUri));
		
		Statement observedByStmt = null;
		if(observedByUri != null)
			observedByStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "observedBy"), sensorObservationModel.createTypedLiteral(observedByUri));
		
		Statement sensingMethodUsedStmt = null;
		if(sensingMethodUsedUri.length() > 0)
			sensingMethodUsedStmt = sensorObservationModel.createStatement(observationResource, sensorObservationModel.createProperty(SSN, "sensingMethodUsed"), sensorObservationModel.createTypedLiteral(sensingMethodUsedUri));
		/**              **/
		/******************/
		
		//observation.add(observationBaseTypeStmt);
		sensorObservationModel.add(observationTypeStmt);
		sensorObservationModel.add(observationResultStmt);
		sensorObservationModel.add(resultTimeStmt);
		sensorObservationModel.add(samplingTimeStmt);
		sensorObservationModel.add(serverTimeStmt);
		sensorObservationModel.add(observationAge);
		
		sensorObservationModel.add(featureOfInterestStmt);
		
		if(observedByStmt != null)
			sensorObservationModel.add(observedByStmt);
		
		if(sensingMethodUsedStmt != null)
			sensorObservationModel.add(sensingMethodUsedStmt);
	}
	
	/**
	 * Calculate the age of this observation
	 * @param observationSamplingTime The time the observation was created
	 * @return The number of seconds between now and when the observation was created
	 */
	private long calculateDataAge(String observationSamplingTime)
	{
		long age = (System.currentTimeMillis() - Long.parseLong(observationSamplingTime)) / 1000;
		//log("calculateDataAge","Observation age is " + age + " seconds");
		return age;
	}
	
	/**
	 * Return the RDF model of this SensorObservation
	 * 
	 * @return RDF model
	 */
	public OntModel getSensorObservationModel()
	{
		return sensorObservationModel;
	}
	
	public void log(String method, String msg)
	{
		System.out.println("[RdfSensorObservationEntity] " + method + " : " + msg);
	}

}
