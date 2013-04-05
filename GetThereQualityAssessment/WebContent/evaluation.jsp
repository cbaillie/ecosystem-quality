<%@page import="uk.ac.dotrural.reasoning.logging.MySQLLogger"%>
<%@page import="uk.ac.dotrural.getthere.quality.logging.ResultViewer" %>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Connection" %>
<%@page import="java.sql.DriverManager" %>
<%@page import="java.sql.Statement" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Calendar" %>
<%@page import="java.util.GregorianCalendar;" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>GetThere Quality Assessment Evaluation</title>
<script type="text/javascript" src="js/lib/jquery.old.min.js"></script>
<script type="text/javascript" src="js/lib/jquery.jqplot.min.js"></script>
<script type="text/javascript" src="js/lib/plugins/jqplot.categoryAxisRenderer.min.js"></script>
<script type="text/javascript" src="js/lib/plugins/jqplot.barRenderer.min.js"></script>
<script type="text/javascript" src="js/lib/plugins/jqplot.pointLabels.min.js"></script>
<link href="jquery.jqplot.css" rel="stylesheet" type="text/css" />
</head>
<style type="text/css">
div.graph
{
	float: left;
	position: relative;
	margin-bottom: 50px;
}
</style>
<body>
	<h1>GetThere Quality Assessment Evaluation</h1>
<%!

	private double executeQueryForInt(String query)
	{
		MySQLLogger mysql = new MySQLLogger("jdbc:mysql://dtp-126.sncs.abdn.ac.uk:3306/GetThere", "saruman", "uruk-h4i");
		ResultSet rs = mysql.doQuery(query);
		try
		{
			rs.next();
			return rs.getDouble(1);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			mysql.closeConnection();
		}
		return -1;
	}

	private double calculateMedian(String query, String field)
	{
		MySQLLogger mysql = new MySQLLogger("jdbc:mysql://dtp-126.sncs.abdn.ac.uk:3306/GetThere", "saruman", "uruk-h4i");
		ResultSet rs = mysql.doQuery(query);
		try
		{
			ArrayList<Integer> values = new ArrayList<Integer>();
			while(rs.next())
			{
				int value = rs.getInt(field);
				values.add(value);
			}
			
			if(values.size() == 0)
				return 0;
			
			//Identify median
			int middle = (values.size() / 2);
			if(middle % 2 == 0)
			{
				double one = values.get(middle);
				double two = values.get(middle+1);
				double result = ((one + two) / 2);
				System.out.println("Calculated median: " + result);
				return result;
			}
			double result = (values.get(middle) > 0 ? values.get(middle) : 0);
			System.out.println("Median is: " + result);
			return result;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return -1;
		}
		finally
		{
			mysql.closeConnection();
		}
	}
	
	private double calculateVariance(String query, String field)
	{
		MySQLLogger mysql = new MySQLLogger("jdbc:mysql://dtp-126.sncs.abdn.ac.uk:3306/GetThere", "saruman", "uruk-h4i");
		ResultSet rs = mysql.doQuery(query);
		try
		{
			ArrayList<Integer>values = new ArrayList<Integer>();
			while(rs.next())
			{
				values.add(rs.getInt(field));
			}
			
			if(values.size() == 0)
				return 0;
			
			int mean = calculateAverage(values);
			ArrayList<Integer>differences = new ArrayList<Integer>();
			for(int i=0;i<values.size();i++)
			{
				int difference = (values.get(i) - mean);
				differences.add((difference * difference));
			}
			double result = Math.sqrt(calculateAverage(differences));
			return result;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return 0;
		}
		finally
		{
			mysql.closeConnection();
		}
	}
	
	private int calculateAverage(ArrayList<Integer> ints)
	{
		int total = 0;
		for(int i=0;i<ints.size();i++)
			total += ints.get(i);
		
		if(ints.size() == 0)
			return 0;
		return (total / ints.size());
	}
	
%>
	<!--  REASONING TIME -->
	<div id="reasoningTime" style="height:300px; width:500px; clear: left;" class="graph"></div>
	<script type="text/javascript">
	
	<% String query = "SELECT reasoning_time FROM quality_logs WHERE "; %>
	
	varSsn = <%= calculateVariance(query + "used_obs_metadata = 1", "reasoning_time") %>;
	varProv = <%= calculateVariance(query + "used_obs_prov = 1","reasoning_time") %>;
	varReuse = <%= calculateVariance(query + "used_qual_prov = 1","reasoning_time") %>
	
	medSsn = <%= calculateMedian(query + "used_obs_metadata = 1 ORDER BY reasoning_time","reasoning_time") %>;
	medProv = <%= calculateMedian(query + "used_obs_prov = 1 ORDER BY reasoning_time","reasoning_time") %>;
	medReuse = <%= calculateMedian(query + "used_qual_prov = 1 ORDER BY reasoning_time","reasoning_time") %>;
	
	avgSsn = <%= executeQueryForInt("SELECT avg(reasoning_time) FROM quality_logs WHERE used_obs_metadata=1") %>;
	avgProv = <%= executeQueryForInt("SELECT avg(reasoning_time) FROM quality_logs WHERE used_obs_prov=1") %>;
	avgReuse = <%= executeQueryForInt("SELECT avg(reasoning_time) FROM quality_logs WHERE used_qual_prov=1") %>;
	
	var s1 = [varSsn, varProv, varReuse];
	var s2 = [medSsn, medProv, medReuse];
	var s3 = [avgSsn, avgProv, avgReuse];
	
	var ticks = ["SSN","Provenance","Reuse"];
	
	var plot1 = $.jqplot("reasoningTime", [s1,s2,s3,], {
		title: "Reasoning Time",
		seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            rendererOptions: {fillToZero: true},
			pointLabels: {show: true}
		},
		series: [
			{label : "S.D."},
			{label : "Median"},
			{label : "Mean"},
		],
		legend: {
			show: true,
			placement: "outside"
		},
        axes: {
            // Use a category axis on the x axis and use our custom ticks.
            xaxis: {
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
                label: "Assessment Type",
                showTicks: true,
                showTickMarks: true
            }
        }
	});

	</script>
	
		<!--  ASSESSMENT TIME -->
	<div id="assessmentTime" style="height:300px; width:500px; left: 100px;" class="graph"></div>
	<script type="text/javascript">
	
	<% query = "SELECT assessment_time FROM quality_logs WHERE "; %>
	
	varSsn = <%= calculateVariance(query + "used_obs_metadata = 1", "assessment_time") %>;
	varProv = <%= calculateVariance(query + "used_obs_prov = 1","assessment_time") %>;
	varReuse = <%= calculateVariance(query + "used_qual_prov = 1","assessment_time") %>
	
	medSsn = <%= calculateMedian(query + "used_obs_metadata = 1 ORDER BY assessment_time","assessment_time") %>;
	medProv = <%= calculateMedian(query + "used_obs_prov = 1 ORDER BY assessment_time","assessment_time") %>;
	medReuse = <%= calculateMedian(query + "used_qual_prov = 1 ORDER BY assessment_time","assessment_time") %>;
	
	avgSsn = <%= executeQueryForInt("SELECT avg(assessment_time) FROM quality_logs WHERE used_obs_metadata=1") %>;
	avgProv = <%= executeQueryForInt("SELECT avg(assessment_time) FROM quality_logs WHERE used_obs_prov=1") %>;
	avgReuse = <%= executeQueryForInt("SELECT avg(assessment_time) FROM quality_logs WHERE used_qual_prov=1") %>;
	
	var s1 = [varSsn, varProv, varReuse];
	var s2 = [medSsn, medProv, medReuse];
	var s3 = [avgSsn, avgProv, avgReuse];
	
	var ticks = ["SSN","Provenance","Reuse"];
	
	var plot1 = $.jqplot("assessmentTime", [s1,s2,s3,], {
		title: "Assessment Time",
		seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            rendererOptions: {fillToZero: true},
			pointLabels: {show: true}
		},
		series: [
			{label : "S.D."},
			{label : "Median"},
			{label : "Mean"},
		],
		legend: {
			show: true,
			placement: "outside"
		},
        axes: {
            // Use a category axis on the x axis and use our custom ticks.
            xaxis: {
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
                label: "Assessment Type",
                showTicks: true,
                showTickMarks: true
            }
        }
	});
	</script>
	
	<!-- MODEL SIZE -->
	<div id="modelSize" style="height:300px; width:500px; clear: left;" class="graph"></div>
	<script type="text/javascript">
	
	<% query = "SELECT model_size FROM quality_logs WHERE "; %>
	
	varSsn = <%= calculateVariance(query + "used_obs_metadata = 1", "model_size") %>;
	varProv = <%= calculateVariance(query + "used_obs_prov = 1","model_size") %>;
	varReuse = <%= calculateVariance(query + "used_qual_prov = 1","model_size") %>
	
	medSsn = <%= calculateMedian(query + "used_obs_metadata = 1 ORDER BY model_size","model_size") %>;
	medProv = <%= calculateMedian(query + "used_obs_prov = 1 ORDER BY model_size","model_size") %>;
	medReuse = <%= calculateMedian(query + "used_qual_prov = 1 ORDER BY model_size","model_size") %>;
	
	avgSsn = <%= executeQueryForInt("SELECT avg(model_size) FROM quality_logs WHERE used_obs_metadata=1") %>;
	avgProv = <%= executeQueryForInt("SELECT avg(model_size) FROM quality_logs WHERE used_obs_prov=1") %>;
	avgReuse = <%= executeQueryForInt("SELECT avg(model_size) FROM quality_logs WHERE used_qual_prov=1") %>;
	
	var s1 = [varSsn, varProv, varReuse];
	var s2 = [medSsn, medProv, medReuse];
	var s3 = [avgSsn, avgProv, avgReuse];
	
	var ticks = ["SSN","Provenance","Reuse"];
	
	var plot1 = $.jqplot("modelSize", [s1,s2,s3,], {
		title: "Model Size",
		seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            rendererOptions: {fillToZero: true},
			pointLabels: {show: true}
		},
		series: [
			{label : "S.D."},
			{label : "Median"},
			{label : "Mean"},
		],
		legend: {
			show: true,
			placement: "outside"
		},
        axes: {
            // Use a category axis on the x axis and use our custom ticks.
            xaxis: {
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
                label: "Assessment Type",
                showTicks: true,
                showTickMarks: true
            }
        }
	});
	</script>
	
	<!-- INFERRED TRIPLES -->
	<div id="inferredTriples" style="height:300px; width:500px; left: 100px;" class="graph"></div>
	<script type="text/javascript">
	
	<% query = "SELECT inferred_triples FROM quality_logs WHERE "; %>
	
	varSsn = <%= calculateVariance(query + "used_obs_metadata = 1", "inferred_triples") %>;
	varProv = <%= calculateVariance(query + "used_obs_prov = 1","inferred_triples") %>;
	varReuse = <%= calculateVariance(query + "used_qual_prov = 1","inferred_triples") %>
	
	medSsn = <%= calculateMedian(query + "used_obs_metadata = 1 ORDER BY inferred_triples","inferred_triples") %>;
	medProv = <%= calculateMedian(query + "used_obs_prov = 1 ORDER BY inferred_triples","inferred_triples") %>;
	medReuse = <%= calculateMedian(query + "used_qual_prov = 1 ORDER BY inferred_triples","inferred_triples") %>;
	
	avgSsn = <%= executeQueryForInt("SELECT avg(inferred_triples) FROM quality_logs WHERE used_obs_metadata=1") %>;
	avgProv = <%= executeQueryForInt("SELECT avg(inferred_triples) FROM quality_logs WHERE used_obs_prov=1") %>;
	avgReuse = <%= executeQueryForInt("SELECT avg(inferred_triples) FROM quality_logs WHERE used_qual_prov=1") %>;
	
	var s1 = [varSsn, varProv, varReuse];
	var s2 = [medSsn, medProv, medReuse];
	var s3 = [avgSsn, avgProv, avgReuse];
	
	var ticks = ["SSN","Provenance","Reuse"];
	
	var plot1 = $.jqplot("inferredTriples", [s1,s2,s3,], {
		title: "Inferred Triples",
		seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            rendererOptions: {fillToZero: true},
			pointLabels: {show: true}
		},
		series: [
			{label : "S.D."},
			{label : "Median"},
			{label : "Mean"},
		],
		legend: {
			show: true,
			placement: "outside"
		},
        axes: {
            // Use a category axis on the x axis and use our custom ticks.
            xaxis: {
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
                label: "Assessment Type",
                showTicks: true,
                showTickMarks: true
            }
        }
	});
	</script>
	
	<!-- ASSESSED DIMENSIONS -->
	<div id="assessedDimensions" style="height:300px; width:500px; clear: left;" class="graph"></div>
	<script type="text/javascript">
	
	<% query = "SELECT assessed_dimensions FROM quality_logs WHERE "; %>
	
	varSsn = <%= calculateVariance(query + "used_obs_metadata = 1", "assessed_dimensions") %>;
	varProv = <%= calculateVariance(query + "used_obs_prov = 1","assessed_dimensions") %>;
	varReuse = <%= calculateVariance(query + "used_qual_prov = 1","assessed_dimensions") %>
	
	medSsn = <%= calculateMedian(query + "used_obs_metadata = 1 ORDER BY assessed_dimensions","assessed_dimensions") %>;
	medProv = <%= calculateMedian(query + "used_obs_prov = 1 ORDER BY assessed_dimensions","assessed_dimensions") %>;
	medReuse = <%= calculateMedian(query + "used_qual_prov = 1 ORDER BY assessed_dimensions","assessed_dimensions") %>;
	
	avgSsn = <%= executeQueryForInt("SELECT avg(assessed_dimensions) FROM quality_logs WHERE used_obs_metadata=1") %>;
	avgProv = <%= executeQueryForInt("SELECT avg(assessed_dimensions) FROM quality_logs WHERE used_obs_prov=1") %>;
	avgReuse = <%= executeQueryForInt("SELECT avg(assessed_dimensions) FROM quality_logs WHERE used_qual_prov=1") %>;
	
	var s1 = [varSsn, varProv, varReuse];
	var s2 = [medSsn, medProv, medReuse];
	var s3 = [avgSsn, avgProv, avgReuse];
	
	var ticks = ["SSN","Provenance","Reuse"];
	
	var plot1 = $.jqplot("assessedDimensions", [s1,s2,s3,], {
		title: "Assessed Dimensions",
		seriesDefaults: {
            renderer:$.jqplot.BarRenderer,
            rendererOptions: {fillToZero: true},
			pointLabels: {show: true}
		},
		series: [
			{label : "S.D."},
			{label : "Median"},
			{label : "Mean"},
		],
		legend: {
			show: true,
			placement: "outside"
		},
        axes: {
            // Use a category axis on the x axis and use our custom ticks.
            xaxis: {
                renderer: $.jqplot.CategoryAxisRenderer,
                ticks: ticks,
                label: "Assessment Type",
                showTicks: true,
                showTickMarks: true
            }
        }
	});
	</script>
	
</body>
</html>