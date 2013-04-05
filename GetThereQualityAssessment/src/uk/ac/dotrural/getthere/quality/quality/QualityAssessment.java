package uk.ac.dotrural.getthere.quality.quality;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class QualityAssessment
 */
@WebServlet("/QualityAssessment")
public class QualityAssessment extends HttpServlet {

	private static final long serialVersionUID = -7243768642115211630L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String test = request.getParameter("test");
		
		if(Boolean.parseBoolean(test))
			doPost(request, response);
		else
			System.out.println("Not a test...");
		
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("Received request");
		String observationUri = request.getParameter("observationUri");
		String observationEndpoint = request.getParameter("observationEndpoint");
		String ruleLocation = request.getParameter("ruleLocation");
		
		System.out.println("Servlet received URI: " + observationUri);
		System.out.println("Servlet received URL: " + observationEndpoint);
		
		log("doPost","Assessing the quality of " + observationUri);	
		
		QualityAPI qa = new QualityAPI(observationEndpoint);
		
		ArrayList<QualityScore> scores = qa.assess(observationUri, ruleLocation);
		writeResults(scores, response.getWriter());
	}
	
	private void writeResults(ArrayList<QualityScore> scores, PrintWriter out)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"annotations\" : { ");
		for(int i=0;i<scores.size();i++)
		{
			QualityScore qs = (QualityScore)scores.get(i);
			if(i > 0)
				sb.append(",");
			sb.append("\"" + qs.getDimension() + "Annotation\": { \"name\": \"" + qs.getDimension() + "\", \"description\" : \"" + qs.getDescription() + "\", \"plainScore\" : \"" + roundValue(checkResultBounds(Double.parseDouble(qs.getScore()))) + "\", \"affectedInstance\" : \"" + qs.getAffectedInstance() + "\", \"label\" : \"" + ((qs.getLabel() != null) ? qs.getLabel() : "null") + "\" }");
		}
		sb.append("}}");
		
		//log("writeResults","Results returned to the user");
		out.write(sb.toString());
		out.flush();
		out.close();
	}
	
	/**
	 * Check that the quality score is not < 0 and > 1
	 * @param result The result to check
	 * @return The sanitised value
	 */
	private double checkResultBounds(double result)
	{
		if(result < 0)
			return 0.0;
		else if(result > 1)
			return 1.0;
		return result;
	}
	
	/**
	 * Round the value of a double
	 * @param val The value to round
	 * @return The rounded value
	 */
	private String roundValue(double val)
	{
		if(val > 0)
		{
			String str = "" + val;
			String[] arr = str.split("\\.");
			return arr[0] + '.' + (arr[1].length() >= 2 ? arr[1].substring(0,2) : arr[1]);
		}
		return "" + val;
	}
	
	private void log(String method, String msg)
	{
		System.out.println("[QualityAssessment] " + method + " : " + msg);
	}

}
