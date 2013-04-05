<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>GetThere Quality Reasoner</title>
<link href="style.css" rel="stylesheet" />
<script src="http://www.openlayers.org/api/OpenLayers.js"></script>
<script src="js/lib/jquery.old.min.js"></script>
<script>

	var map;
	var markers;
	var buses;
	var provBuses;
	var gg = new OpenLayers.Projection("EPSG:4326");
	var sm = new OpenLayers.Projection("EPSG:900913");

	function init()
	{	
		//Init jQuery
		jQuery(document).ready(function($) {
			
	    })
	    
	    loadRoutes();
		
		//Load map
	    map = new OpenLayers.Map("map");
	    base = new OpenLayers.Layer.OSM();
	    map.addLayer(base);  
	    map.setCenter(new OpenLayers.LonLat(-2.1, 57.15).transform(gg,sm), 11);
	    
	    //Add buses layer
	    buses = new OpenLayers.Layer.Markers("Buses");
	    provBuses = new OpenLayers.Layer.Markers("ProvBuses");
	    map.addLayer(buses);
	    map.addLayer(provBuses);
	}
	
	function loadRoutes()
	{
		$("#displayBuses").click(function(){
			updateBuses();
		});
		
		var host = "http://dtp-24.sncs.abdn.ac.uk:8080/ecosystem-transport/timetable/getRoutesInAdminArea";
		var adminArea = "http://transport.data.gov.uk/id/administrative-area/111";
		
		$.ajax({
			  url: host + "?adminAreaUri=" + adminArea + "&includeDirections=true",
			  success: function(data)
			  {
				if(!(data.lines instanceof Array))
				{
					data.lines = [data.lines];;
					
				}
				$.each(data, function(i, row)
				{
					$.each(row, function(i, route)
					{
						routeNumber = route.altLabel;
						$.each(route.directions, function(i, directions)
						{
							$.each(directions, function(i, direction)
							{
								rt = routeNumber + " : " + direction.directionDescription + " (" + direction.direction + ")";
								$("<option/>").html(rt).val(route.uri + "&direction=" + direction.direction).appendTo($("#routes"));
								
							});
						});
					});
				});
			  },
			  error: function(xhr, status)
			  {
				  alert("Error: " + status);
			  },
			  dataType: "JSON"
		});
		
	}
	
	function updateBuses()
	{
		route = $("#routes").val();
		if(route != "Select route...")
		{
			//Query for newest point
			$.ajax({
			  type: "GET",
			  //url: "http://107.20.159.169:8080/ecosystem-transport/observation/getBusLocationsOnRoute?lineUri=http://107.20.159.169:8094/timetable/routes/17&direction=outbound&callback=",
			  //url: "http://dtp-24.sncs.abdn.ac.uk:8080/ecosystem-transport/observation/getBusLocationsOnRoute?lineUri=http://cops-022382.uoa.abdn.ac.uk:8086/timetable/route/X95&direction=outbound&callback=",
			  url: "http://dtp-24.sncs.abdn.ac.uk:8080/ecosystem-transport/observation/getBusLocationsOnRoute?lineUri=" + route + "&callback=",
			  dataType: "json",
			  beforeSend: function()
			  {
			  	$("#loader").css('background-image', 'url(img/spinningsheep.gif)');
			  },
			  success: function(data)
			  {
					if(data == null)
					{
						alert("No recent observations for this route");
						buses.clearMarkers();
					} else {
						buses.clearMarkers();	//Clear current markers
					    //Add a feature
					    var size = new OpenLayers.Size(33,44);
					    var offset = 0;
					    var icon = new OpenLayers.Icon("img/marker.png", size, offset);
					    bus = new OpenLayers.Marker(new OpenLayers.LonLat(data.locations.latitude, data.locations.longitude).transform(gg,sm), icon);
						bus.events.register("click", bus, function(){
							handleBusClick(data);
						});
					    buses.addMarker(bus);
					    map.setCenter(new OpenLayers.LonLat(data.locations.latitude, data.locations.longitude).transform(gg,sm), 13);
					    
					    //setTimeout(updateBuses, 1000);
					}
				},
			  error: function(jqXHR, textStatus, errorThrown)
				{
				  	if(errorThrown != "")
						alert(errorThrown);
				},
			  complete: function()
			  {
				  $("#loader").css('background-image', 'none');
			  }
			});
		}
	}
	
	function handleBusClick(observation)
	{
		provBuses.clearMarkers();
		$("#metadata").html("");
		$("#provenanceData").html("");
		
		getObservationModel(observation);				//Get Observation Details
		if($("#provenance").is(":checked"))
			getObservationProvenance(observation);		//Get Observation provenance
		assessQuality(observation);						//Assess quality
	}
	
	function assessQuality(observation)
	{
		$.ajax({
			type: "POST",
			url: "QualityAssessment?",
			data: 	{	
				observationUri: observation.locations.uri,
				semanticObservations: $("#semantic").is(":checked"),
				useProvenance: $("#provenance").is(":checked"),
				reuseScores: $("#reuse").is(":checked")
			},
			dataType: "JSON",
			beforeSend: function()
			{
				$("#quality").empty();
			},
			success: function(results)
			{
				console.log(results);
				$("<h2/>").html("Results").appendTo($("#quality"));
				$.each(results.annotations, function(){
					
					$("<h3/>").html(this.name).appendTo($("#quality"));	//Quality dimension name
					
					if(this.label == "provenance")
					{
						$("<img/>", {
							src: "img/prov.png",
							alt: "Used provenance",
							class: "assessmentClassifier"
						}).appendTo($("#quality"));
					}
					
					if(this.label == "reuse")
					{
						$("<img/>", {
							src: "img/reuse.png",
							alt: "Re-used existing score",
							class: "assessmentClassifier"
						}).appendTo($("#quality"));				
					}
					
					$("<br/>",{
						class: "break"
					}).appendTo($("#quality"));
					
					holder = $("<div/>", {
							id: this.name + "GraphHolder"
						}).appendTo($("#quality"));
					
					graph = $("<div/>", {
						id: this.name + "GraphBar"
					}).appendTo($("#" + this.name + "GraphHolder"));	
					
					score = this.plainScore;
					//$(graph).html(score);
					
					//Calculate graph width
					graphWidth = 200 * score;
					
					//Choose graph colour
					color = "#000";
					if(score >= 0.75)
						color = "#488214";
					else if(score >= 0.5)
						color = "#ffa812";
					else
						color = "#f00";
					
					$(holder).css({
						border: "solid 1px #000",
						width: "200px",
						height: "10px",
						background: "#eee"
					});
					
					$(graph).css({
						background: color,
						color: "#fff",
						width: graphWidth,
						height: "10px"
					});
					
					$(holder).attr("title", this.description);

				});
			},
			error: function(xhr, textStatus, errorThrown)
			{
				alert("QA Error: " + errorThrown);
			},
			complete: function()
			{
				//alert("Quality assessment complete");
			}
		});
	}
	
	function getObservationModel(observation)
	{
		$.ajax({
			type: "GET",
			url: "GetObservationJson?observationUri=" + observation.locations.uri,
			dataType: "json",
			beforeSend: function()
			{
				$("#metadata").css("background", "url(img/spinningsheep.gif) center no-repeat");
			},
			success: function(result)
			{
				console.log(result);
				$("<h1 />", {id: "ObservationHeader"}).html("Observation Metadata").appendTo($("#metadata"));
				$("<img/>",{
					src: "img/marker.png"
				}).appendTo($("#ObservationHeader"));
				$("<h2 />").html("Observation").appendTo($("#metadata"));
				
				//Observation info
				$("<ul />", {
					id: "observationList"
				}).appendTo($("#metadata"));
				$("<li />").html("<strong>URI: </strong>" + result.uri).appendTo($("#observationList"));
					
				$("<ul />", {
						id: "observationTimes"
					}).appendTo("#observationList");
				
					days = new Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
					months = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
					
					resultDate = new Date(parseInt(result.observationResultTime));
					sampling = new Date(parseInt(result.observationSamplingTime));
					server = new Date(parseInt(result.serverTime));
				
					$("<li />").html("<strong>Result time: </strong>" + days[resultDate.getDay()] + " " + resultDate.getDate() + " " + months[resultDate.getMonth()] + " " + resultDate.getHours() + ":" + resultDate.getMinutes()).appendTo($("#observationTimes"));
					$("<li />").html("<strong>Sampled at: </strong>" +  days[sampling.getDay()] + " " + sampling.getDate() + " " + months[sampling.getMonth()] + " " + sampling.getHours() + ":" + sampling.getMinutes()).appendTo($("#observationTimes"));
					$("<li />").html("<strong>Published: </strong>" +  days[server.getDay()] + " " + server.getDate() + " " + months[server.getMonth()] + " " + server.getHours() + ":" + server.getMinutes()).appendTo($("#observationTimes"));
					
					$("<li />").html("<strong>Latitude: </strong>" + observation.locations.longitude).appendTo($("#observationList"));
					$("<li />").html("<strong>Longitude: </strong>" + observation.locations.latitude).appendTo($("#observationList"));
					
				//Get ObservationValue
				getObservationValueModel(result.observationResult.uri);
		
			},
			error: function(XHR, textStatus, errorThrown)
			{
				if(errorThrown != "")
					alert("Observation: " + errorThrown);
			},
			complete: function()
			{
				$("#metadata").css("background-image", "none");
			}
		});		
	}
	
	function getObservationValueModel(observationValueUri)
	{
		$.ajax({
			type: "GET",
			url: "GetObservationValueJson?observationValueUri=" + observationValueUri,
			dataType: "json",
			beforeSend: function()
			{
				$("#metadata").css("background", "url(img/spinningsheep.gif) center no-repeat");
			},
			success: function(observationValue)
			{
				console.log(observationValue);
				if(observationValue.accuracy != undefined)
					$("<li />").html("<strong>Accuracy: </strong>" + observationValue.accuracy + "m").appendTo("#observationList");
			},
			error: function(XHR, textStatus, errorThrown)
			{
				if(errorThrown != "")
					alert("Observation Value:" + errorThrown);
			},
			complete: function()
			{
				$("#metadata").css("background-image", "none");
			}
		});		
	}
	
	function getObservationProvenance(observationUri)
	{
		$.ajax({
			type: "GET",
			url: "GetObservationProvenanceModel?observationUri=" + observationUri.locations.uri,
			dataType: "json",
			beforeSend: function()
			{
				$("#metadata").css("background", "url(img/spinningsheep.gif) center no-repeat");
			},
			success: function(provenance)
			{
				console.log(provenance);
				$("<h1 />",{id:"ProvenanceHeader"}).html("Observation Provenance").appendTo($("#provenanceData"));
				$("<img/>",{
					src: "img/provMarker.gif"
				}).appendTo($("#ProvenanceHeader"));
				$("<ul />", {
					id: "provenanceList"
				}).appendTo($("#provenanceData"));
				
				$("<li/>").html("<strong>Derived from:</strong> " + provenance.ObservationProvenance.uri).appendTo($("#provenanceList"));
				$("<li/>").html("<strong>Latitude:</strong> " + provenance.ObservationProvenance.latitude).appendTo($("#provenanceList"));
				$("<li/>").html("<strong>Longitude:</strong> " + provenance.ObservationProvenance.longitude).appendTo($("#provenanceList"));
				$("<li/>").html("<strong>Accuracy:</strong> " + provenance.ObservationProvenance.accuracy).appendTo($("#provenanceList"));	
				
			    //Add a feature
			    var size = new OpenLayers.Size(33,44);
			    var offset = 0;
			    var icon = new OpenLayers.Icon("img/provMarker.gif", size, offset);
			    bus = new OpenLayers.Marker(new OpenLayers.LonLat(provenance.ObservationProvenance.longitude, provenance.ObservationProvenance.latitude).transform(gg,sm), icon);
			    provBuses.addMarker(bus);
			},
			error: function(XHR, textStatus, errorThrown)
			{
				if(errorThrown != "")
					alert("Provenance: " + errorThrown);
			},
			complete: function()
			{
				$("#metadata").css("background-image", "none");
			}
		});		
	}
    
</script>
</head>
<body onload="init()">
<select id="routes">
	<option>Select route...</option>
</select>
<button id="displayBuses" style="margin-bottom: 10px">Display</button> 
<div id="map" style="height: 600px; width: 800px;"></div>
<div id="loader" style="height: 32px; width: 32px; position: absolute; left: 820px; top: 15px;"></div>
<div id="assessmentControls" style="position: absolute; left: 820px; top: 50px;">
	<h1>Quality Assessment</h1>
	<h2>Controls</h2>
	<ul style="list-style-type: none;">
		<li><input type="checkbox" name="semantic" id="semantic" value="semantic" checked="checked" />Examine observation metadata</li>
		<li><input type="checkbox" name="provenance" id="provenance" value="provenance" />Use observation provenance</li>
		<li><input type="checkbox" name="reuse" id="reuse" value="reuse" />Re-use existing scores</li>
	</ul>
</div>
<div id="quality" style="position: absolute; left; left: 820px; top: 250px;"></div>
<div id="metadata" style="width: 49%; float: left;"></div>
<div id="provenanceData" style="width: 49%; float: left;"></div>
</body>
</html>