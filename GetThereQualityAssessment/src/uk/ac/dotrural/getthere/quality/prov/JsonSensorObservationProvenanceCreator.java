package uk.ac.dotrural.getthere.quality.prov;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationEntity;
import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationValueEntity;
import uk.ac.dotrural.getthere.quality.entity.SensorObservationUtilities;
import uk.ac.dotrural.getthere.quality.rdf.RdfSensorObservationEntity;
import uk.ac.dotrural.getthere.quality.rdf.RdfSensorObservationValueEntity;
import uk.ac.dotrural.prov.model.ProvenanceBundle;

public class JsonSensorObservationProvenanceCreator {
	
	public JsonSensorObservationEntity findObservationDerivedFrom(String endpoint, JsonSensorObservationEntity observation)
	{
		String derivedFromUri = observation.getWasDerivedFrom();
		log("findObservationDerivedFrom",observation.getObservationUri() + " prov:wasDerivedFrom " + derivedFromUri);
		if(derivedFromUri == "null")
			return null;
		JsonSensorObservationEntity derivedFrom = new JsonSensorObservationEntity(endpoint, derivedFromUri);
		return derivedFrom;
	}
	
	public OntModel createProvenance(String endpoint, String service, JsonSensorObservationEntity observationJson)
	{
		//Find observation that this obs is derived from
		JsonSensorObservationEntity derivedFromObservation = findObservationDerivedFrom(endpoint, observationJson);
		
		if(derivedFromObservation == null)
			return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		else
		{
			log("createProvenance","derivedFromObservation : " + derivedFromObservation);
			
			//Find derived from observation's value
			String derivedFromObservationResultUri = derivedFromObservation.getObservationResult();
			String derivedFromObservationValueUri = SensorObservationUtilities.getObservationResultUri(endpoint + service, derivedFromObservationResultUri);
			JsonSensorObservationValueEntity derivedFromObservationValue = new JsonSensorObservationValueEntity(endpoint, derivedFromObservationValueUri);
			
			//Convert JSON entities to RDF
			RdfSensorObservationEntity rdfObservationEntity = new RdfSensorObservationEntity(endpoint);
			rdfObservationEntity.jsonEntityToRdfEntity(observationJson);
			
			RdfSensorObservationEntity rdfDerivedFromObservationEntity = new RdfSensorObservationEntity(endpoint);
			rdfDerivedFromObservationEntity.jsonEntityToRdfEntity(derivedFromObservation);
			
			RdfSensorObservationValueEntity rdfDerivedFromObservationValueEntity = new RdfSensorObservationValueEntity(endpoint, derivedFromObservation.getFeatureOfInterest());
			rdfDerivedFromObservationValueEntity.jsonEntityToRdfEntity(derivedFromObservationValue);
			
			//Create provenance model
			ProvenanceBundle prov = new ProvenanceBundle("http://dtp-24.sncs.abdn.ac.uk/observationProvenance");
			prov.createEntity(observationJson.getObservationUri());
			prov.createEntity(derivedFromObservation.getObservationUri());
			prov.addWasDerivedFrom(observationJson.getObservationUri(), derivedFromObservation.getObservationUri());
			
			OntModel provModel = prov.getModel();
			provModel.add(rdfDerivedFromObservationEntity.getSensorObservationModel());
			provModel.add(rdfDerivedFromObservationValueEntity.getSensorObservationValueModel());
			return provModel;
		}
	}
	
	private void log(String method, String msg)
	{
		System.out.println("[JsonSensorObservationProvenanceCreator] " + method + " : " + msg);
	}

}
