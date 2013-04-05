<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%@ page import="uk.ac.dotrural.reasoning.sparql.SPARQLQuery" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@ page import="net.sf.json.JSONArray" %>
<%@ page import="net.sf.json.JSONSerializer" %>

<%!

public void getScoreInfo(JspWriter out, String observationUri)
{
	String endpoint = "http://localhost:3030/QualityScores/query?query=";
	String query = "PREFIX dqm: <http://purl.org/dqm-vocabulary/v1/dqm#>" + 
				   "PREFIX qual: <http://dtp-126.sncs.abdn.ac.uk/quality/scores/>" +
				   "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + 
				   "SELECT * WHERE { " +
				   "	?viol dqm:affectedInstance ?obs . " +  
				   "	?viol dqm:ruleOfIdentification ?rule . " +  
				   		"<" + observationUri + "> dqm:basedOn ?rule . " +  
				   		"<" + observationUri + "> rdf:type ?type . " +  
				   		"<" + observationUri + "> dqm:plainScore ?score ." +  
				   "	?rule dqm:reqName ?name . " +  
				   "	?rule dqm:reqDescription ?desc . " +  
				   "	?rule dqm:reqSource ?source . " + 
				   "}";
	String type = "json";
	
	SPARQLQuery spQuery = new SPARQLQuery(endpoint);
	String json = spQuery.queryEndpoint(query, type);
	
	try
	{
		JSONObject jsonObj = (JSONObject)JSONSerializer.toJSON(json);
		JSONObject results = (JSONObject)jsonObj.get("results");
		JSONArray bindings = (JSONArray)results.get("bindings");		
		JSONObject binding = (JSONObject)bindings.get(0);
		JSONObject name = (JSONObject)binding.get("name");
		JSONObject desc = (JSONObject)binding.get("desc");
		JSONObject source = (JSONObject)binding.get("source");
		JSONObject score = (JSONObject)binding.get("score");
		JSONObject obs = (JSONObject)binding.get("obs");
		
		out.write("<p><strong>Measure of</strong>: " + name.get("value") + "</p>");
		out.write("<p><strong>Constraint</strong>: " + desc.get("value") + "</p>");
		out.write("<p><strong>Source</strong>: " + source.get("value") + "</p>");
		out.write("<p><strong>Score</strong>: " + score.get("value") + "</p>");
		out.write("<p><strong>Affects</strong>: " + obs.get("value") + "</p>");
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
	}
}

%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title></title>
</head>
<body>

<% getScoreInfo(out, request.getParameter("obs")); %>

</body>
</html>