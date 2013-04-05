package uk.ac.dotrural.getthere.quality.services;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationValueEntity;
import uk.ac.dotrural.getthere.quality.entity.SensorObservationUtilities;

/**
 * Servlet implementation class GetObservationValueDetails
 */
@WebServlet("/GetObservationValueJson")
public class GetObservationValueJson extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final String ENDPOINT = "http://dtp-24.sncs.abdn.ac.uk:8080/ecosystem-transport/";
	private final String SERVICE = "observation/getSensorOutput?sensorOutputUri=";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObservationValueJson() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String observationValueUri = request.getParameter("observationValueUri");
		log("doGet","Querying for observation value " + observationValueUri);
		String observationResultUri = SensorObservationUtilities.getObservationResultUri(ENDPOINT + SERVICE, observationValueUri);
		JsonSensorObservationValueEntity observationValue = new JsonSensorObservationValueEntity(ENDPOINT, observationResultUri);
		response.getWriter().write(observationValue.getJsonDescription());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private void log(String method, String message)
	{
		System.out.println("[GetObservationValueJson] " + method + " : " + message);
	}

}
