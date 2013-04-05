<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>QA Test JSP</title>
<script src="js/lib/jquery.old.min.js"></script>
</head>
<script type="text/javascript">

	//Init jQuery
	jQuery(document).ready(function($) {
		
		$.ajax({
			  type: "POST",
			  url: "QualityAssessment",
			  data: {
				  observationUri: "http://dtp-24.sncs.abdn.ac.uk:8093/observation/8391fd0f-aa78-46cf-955e-e79016490d87",
				  observationEndpoint: "http://107.20.159.169:8080/"
			  },
			  dataType: "JSON",
			  success: function(data)
			  {
				console.log(data);
				$("#results").empty();
				
				annotations = data.annotations;
				
				$.each(annotations, function(index, elem)
				{
					console.log(elem);
					$("<h2/>").html(elem.name).appendTo($("#results"));
					$("<p/>").html(elem.description).appendTo($("#results"));
					$("<p/>").html("<strong>Score:</strong> " + elem.plainScore).appendTo($("#results"));
				});
				
			  },
			  error: function(xhr, status, errorThrown)
			  {
				  alert("Error: " + errorThrown);
			  },
		});
		
	})

</script>
<body>
	<h1>Results</h1>
	<div id="results">
		Loading...
	</div>
</body>
</html>