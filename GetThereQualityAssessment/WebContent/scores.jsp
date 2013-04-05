<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@ page import="uk.ac.dotrural.reasoning.sparql.*" %>
<%@ page import="net.sf.json.JSONObject" %>
<%@ page import="net.sf.json.JSONArray" %>
<%@ page import="net.sf.json.JSONSerializer" %>
    
<%!

public void getScores(JspWriter out)
{
	String endpoint = "http://localhost:3030/QualityScores/query?query=";
	String query = "PREFIX dqm: <http://purl.org/dqm-vocabulary/v1/dqm#>" + 
					"SELECT ?score WHERE { " + 
					"	?viol dqm:affectedInstance ?obs . " +  
					"	?viol dqm:ruleOfIdentification ?rule . " +  
					"	?score dqm:basedOn ?rule . " +  
					"}";
	String type = "json";
	
	SPARQLQuery spQuery = new SPARQLQuery(endpoint);
	String results = spQuery.queryEndpoint(query, type);
	try
	{
		JSONObject score = (JSONObject)JSONSerializer.toJSON(results);
		JSONObject scoreResults = (JSONObject)score.getJSONObject("results");
		JSONArray bindings = (JSONArray)scoreResults.get("bindings");
		for(int i=0;i<bindings.size();i++)
		{
			JSONObject binding = (JSONObject)bindings.get(i);
			JSONObject qualScore = (JSONObject)binding.get("score");
			String value = (String)qualScore.get("value");
			
			String url = "score.jsp?obs=" + value;
			String obj = "<a href=" + url + " target=\"scorePane\">" + value + "</a>";
			
			out.write(obj + "<br />");
			
		}
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
	
	<% getScores(out); %>
	
</body>
</html>