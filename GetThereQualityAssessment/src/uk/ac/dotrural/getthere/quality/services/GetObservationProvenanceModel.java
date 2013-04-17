package uk.ac.dotrural.getthere.quality.services;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationEntity;
import uk.ac.dotrural.getthere.quality.prov.JsonSensorObservationProvenanceCreator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Servlet implementation class GetObservationProvenanceModels
 */
@WebServlet("/GetObservationProvenanceModel")
public class GetObservationProvenanceModel extends HttpServlet {
	
	private static final long serialVersionUID = 1L;   
	private final String ENDPOINT = "http://dtp-24.sncs.abdn.ac.uk:8080/ecosystem-transport/";
	private final String OBS_VAL_SERVICE = "observation/getSensorOutput?sensorOutputUri=";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObservationProvenanceModel() {
        super();

    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String observationUri = request.getParameter("observationUri");
		JsonSensorObservationEntity observationJson = new JsonSensorObservationEntity(ENDPOINT, observationUri);
		
		log("doGet","Generating provenance for " + observationUri);
		
		JsonSensorObservationProvenanceCreator provCreator = new JsonSensorObservationProvenanceCreator();
		OntModel observationProvenanceModel = provCreator.createProvenance(ENDPOINT, OBS_VAL_SERVICE, observationJson);

		ResultSet rs = queryProvenance(observationProvenanceModel);
		if(rs != null)
		{
			while(rs.hasNext())
			{
				QuerySolution sol = rs.nextSolution();
				System.out.println(sol.toString());
				Resource obsRes = sol.getResource("provObs");
				Literal sampleTime = sol.getLiteral("sampleTime");
				Literal resultTime = sol.getLiteral("resultTime");
				Literal lat = sol.getLiteral("lat");
				Literal lon = sol.getLiteral("long");
				Literal accuracy = sol.getLiteral("accuracy");
				
				StringBuilder res = new StringBuilder();
				res.append("{ \"ObservationProvenance\" : {");
				res.append("	\"uri\" : \"" + obsRes.toString() + "\"," +
						   "	\"sampleTime\" : \"" + sampleTime.getLexicalForm() + "\"," +
						   "	\"resultTime\" : \"" + resultTime.getLexicalForm() + "\"," +
						   "	\"latitude\" : \"" + lat.getLexicalForm() + "\"," +
						   "	\"longitude\" : \"" + lon.getLexicalForm() + "\"," +
						   "	\"accuracy\" : \"" + accuracy.getLexicalForm() + "\"");
				res.append("}}");
				
				response.getWriter().write(res.toString());
			}
		}
		else
		{
			System.err.println("GetObservationProvenanceModel: Unable to query observation provenance.");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
	
	public ResultSet queryProvenance(OntModel model)
	{
		String query =  "SELECT * WHERE {" + 
		  				"	?obs <http://www.w3.org/ns/prov#wasDerivedFrom> ?provObs . " +
		  				"	?provObs <http://purl.oclc.org/NET/ssnx/ssn#observationSamplingTime> ?sampleTime . " +
		  				"	?provObs <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime> ?resultTime . " +
		  				"	?provObs <http://www.dotrural.ac.uk/irp/uploads/ontologies/sensors/serverTimestamp> ?serverTime . " +
		  				"	?provObs <http://purl.oclc.org/NET/ssnx/ssn#observationResult> ?provObsResult . " +
		  				"	?provObsResult <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat . " +
		  				"	?provObsResult <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?long . " +
		  				"	?provObsResult <http://www.dotrural.ac.uk/irp/uploads/ontologies/sensors/accuracy> ?accuracy . " + 
						"}";
		   
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		try
		{
			ResultSet rs = qe.execSelect();
			System.out.println("Query complete");
			return rs;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		System.out.println("Query failed.");
		return null;
	}
	
	private void log(String method, String msg)
	{
		System.out.println("[GetObservationProvenanceModel] " + method + " : " + msg);
	}

}
