package uk.ac.dotrural.getthere.quality.quality;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationEntity;
import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationValueEntity;
import uk.ac.dotrural.getthere.quality.entity.SensorObservationUtilities;
import uk.ac.dotrural.getthere.quality.prov.JsonSensorObservationProvenanceCreator;
import uk.ac.dotrural.getthere.quality.rdf.RdfSensorObservationEntity;
import uk.ac.dotrural.getthere.quality.rdf.RdfSensorObservationValueEntity;
import uk.ac.dotrural.reasoning.reasoner.Reasoner;
import uk.ac.dotrural.reasoning.reasoner.ReasonerResult;
import uk.ac.dotrural.reasoning.sparql.SPARQLQuery;

public class QualityAPI {
	
	private final boolean observationMetadata = false; 
	private final boolean observationProvenance = true; 
	private final boolean qualityProvenance = false; 
	private final boolean doLogging = true;

	private String ENDPOINT = "";
	private final String OBS_VAL_SERVICE = "ecosystem-transport/observation/getSensorOutput?sensorOutputUri=";
	private final String RESULT_TYPE = "json";
	
	private final String NOTATION = "TTL";
	
	private long applicationTime, inferredTriples, assessedTriples, assessedDimensions;
	
	/**
	 * Constructor for QualityAPI
	 * @param endpoint The URI of the endpoint
	 */
	public QualityAPI(String endpoint)
	{
		ENDPOINT = endpoint;
	}
	
	/**
	 * Assess an observation with a given a URI
	 * 
	 * @param observationUri The URI of the observation to asses
	 * @param ruleLocation The URL of the assessment rule ontology file
	 * 
	 * @return ArrayList of QualityScores
	 */
	public ArrayList<QualityScore> assess(String observationUri, String ruleLocation)
	{
		ArrayList<QualityScore> scores = new ArrayList<QualityScore>();
		long start = 0, finish = 0;
		JsonSensorObservationEntity observationJson = new JsonSensorObservationEntity(ENDPOINT, observationUri);
		JsonSensorObservationValueEntity observationValueJson = new JsonSensorObservationValueEntity(ENDPOINT, SensorObservationUtilities.getObservationResultUri(ENDPOINT + OBS_VAL_SERVICE, observationJson.getObservationResult()));
		
		OntModel observation = getObservationModel(observationJson);
		OntModel observationValue = getObservationValueModel(observationValueJson, observationJson.getFeatureOfInterest());
		
		OntModel assessment = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		assessment.add(observation);
		assessment.add(observationValue);
		
		if(observationProvenance)
			assessment.add(getObservationProvenance(observationJson));

		if(qualityProvenance)
			assessment.add(getExistingScores(observationUri));
	
		//LOG ASSESSED TRIPLES
		assessedTriples = assessment.size();
		start = System.currentTimeMillis();
		
		Reasoner reasoner = new Reasoner(ruleLocation, NOTATION, false);
		
		if(reasoner != null)
		{
			ReasonerResult results = reasoner.performReasoning(assessment);
			
			assessment.add(results.ntriples);
			finish = System.currentTimeMillis();
			
			//LOG INFERRED TRIPLES
			inferredTriples = results.ntriples.size();
			
			AnnotationParser parser = new AnnotationParser();			
			
			if(inferredTriples > 0)
			{
				scores = (ArrayList<QualityScore>)parser.getScores(results.ntriples);
				assessedDimensions = scores.size();
				//parser.sendScoresAsSparqlUpdates(results); 
			}
			
			//LOG REASONING TIME
			applicationTime = (finish - start);
			log("assess", "Entire query took " + applicationTime + "ms");
			
			log("assess",assessedDimensions + " dimensions assessed");
			
			if(doLogging)
			{
				log("doPost","Logging results");
				logToDatabase(results, (observationMetadata ? 1 : 0), (observationProvenance ? 1 : 0), (qualityProvenance ? 1: 0), observationUri);
			}
		}
		return scores;
	}
	
	private OntModel getObservationProvenance(JsonSensorObservationEntity observationJson)
	{
		JsonSensorObservationProvenanceCreator provCreator = new JsonSensorObservationProvenanceCreator();
		return provCreator.createProvenance(ENDPOINT, OBS_VAL_SERVICE, observationJson);
	}
	
	private OntModel getExistingScores(String observationUri)
	{
		OntModel scores = ModelFactory.createOntologyModel();
		
		//log("doPost","Querying for existing quality scores");
		String existingScores = queryForExistingScores(observationUri);
		JSONObject qualJson = (JSONObject) JSONSerializer.toJSON(existingScores);
		JSONObject results = (JSONObject) qualJson.get("results");
		JSONArray bindings = (JSONArray) results.get("bindings");
		
		for(int i=0;i<bindings.size();i++)
		{
			/*JSONObject binding = (JSONObject) bindings.get(i);
			QualityScore score = new QualityScore(
					binding.getJSONObject("type").getString("value"),
					binding.getJSONObject("desc").getString("value"),
					binding.getJSONObject("score").getString("value"),
					binding.getJSONObject("obs").getString("value"),
					"reuse",
					binding.getJSONObject("source").getString("value")
			);
			scores.add(score.getRdfModel());*/
			//log("doPost","Existing quality score added to model.");
		}
		
		return scores;
	}
	
	private String queryForExistingScores(String observationUri)
	{
		SPARQLQuery spQuery = new SPARQLQuery(ENDPOINT);
		String query = "PREFIX dqm: <http://purl.org/dqm-vocabulary/v1/dqm#>" + 
					   "PREFIX qual: <http://dtp-126.sncs.abdn.ac.uk/quality/scores/>" +
					   "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + 
					   "SELECT * WHERE { " +
					   "	?viol dqm:affectedInstance <" + observationUri  + "> . " +
					   "	?viol dqm:affectedInstance ?obs . " +  
					   "	?viol dqm:ruleOfIdentification ?rule . " +  
					   		"?qual dqm:basedOn ?rule . " +  
					   		"?qual rdf:type ?type . " +  
					   		"?qual dqm:plainScore ?score ." +  
					   "	?rule dqm:reqName ?name . " +  
					   "	?rule dqm:reqDescription ?desc . " +  
					   "	?rule dqm:reqSource ?source . " + 
					   "}";
		
		System.out.println("SPARQL QUERY: " + query);
		
		return spQuery.queryEndpoint(query, RESULT_TYPE);
	}
	
	private OntModel getObservationModel(JsonSensorObservationEntity observationJson)
	{
		RdfSensorObservationEntity observationRdf = new RdfSensorObservationEntity(ENDPOINT + "ecosystem-transport/");
		observationRdf.jsonEntityToRdfEntity(observationJson);
		return observationRdf.getSensorObservationModel();
	}
	
	private OntModel getObservationValueModel(JsonSensorObservationValueEntity observationValueJson, String featureOfInterest)
	{
		RdfSensorObservationValueEntity observationValueRdf = new RdfSensorObservationValueEntity(ENDPOINT, featureOfInterest);
		observationValueRdf.jsonEntityToRdfEntity(observationValueJson);
		return observationValueRdf.getSensorObservationValueModel();
	}
	
	private void logToDatabase(ReasonerResult results, int used_obs_metadata, int used_obs_prov, int used_qual_prov, String observationUri)
	{
		//String url = 	"http://dtp-126.sncs.abdn.ac.uk/MySQLBridge.php?" +
		String url = "http://dtp-126.sncs.abdn.ac.uk:8080/MySQLBridge/MySQLBridge?" + 
						"at=" + applicationTime +
						"&rt=" + results.duration +
						"&ms=" + assessedTriples +
						"&it=" + inferredTriples +
						"&ad=" + assessedDimensions +
						"&uom=" + used_obs_metadata +
						"&uop=" + used_obs_prov + 
						"&uqp=" + used_qual_prov +
						"&ou=" + observationUri;
		
		StringBuilder in = new StringBuilder();
		String tmp = "";
		try
		{
			InputStreamReader input = new InputStreamReader(new URL(url).openStream());
			BufferedReader br = new BufferedReader(input);
			while((tmp = br.readLine()) != null)
			{
				in.append(tmp);
			}	
			input.close();
			br.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		//Parse response
		JSONObject response = (JSONObject)JSONSerializer.toJSON(in.toString());
		String result = response.getString("result");
		String reason = response.getString("reason");
		
		if(result.equalsIgnoreCase("success"))
			log("logToDatabase", "Log entry created successfully!");
		else
			log("logToDatabase", "Logging failed: " + reason);
		
	}
	
	private void log(String method, String msg)
	{
		System.out.println("[QualityAPI] " + method + " : " + msg);
	}

}
