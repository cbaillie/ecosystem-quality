package uk.ac.dotrural.getthere.quality.services;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.dotrural.getthere.quality.entity.JsonSensorObservationEntity;

/**
 * Servlet implementation class GetObservationDetails
 */
@WebServlet("/GetObservationJson")
public class GetObservationJson extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final String ENDPOINT = "http://dtp-24.sncs.abdn.ac.uk:8080/ecosystem-transport/";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetObservationJson() {
        super();

    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String observationUri = request.getParameter("observationUri");
		log("doGet","Querying for observation " + observationUri);
		JsonSensorObservationEntity observation = new JsonSensorObservationEntity(ENDPOINT, observationUri);
		response.getWriter().write(observation.getJsonDescription());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}
	
	private void log(String method, String message)
	{
		System.out.println("[GetObservationJson] " + method + " : " + message);
	}

}
