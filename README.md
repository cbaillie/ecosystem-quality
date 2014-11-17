ecosystem-quality
=================

Quality assessment ecosystem for GetThere

Using ecosystem-quality programmatically:

QualityAPI qual = new QualityAPI(String endpoint);

//Perform assessment
//observationUri : the URI of the observation to assess
//ruleLocation : URL of the ontology containing SPIN rules to guide assessment
qual.assess(String observationUri, String ruleLocation)

//Using ecosystem-quality as a web service:

POST http:// … /ecosystem-quality/QualityAssessment?

Parameters:
observationUri = URI of the observation to assess
observationEndpoint =  URL of the endpoint containing description of the observation
ruleLocation = URL of the ontology containing SPIN rules to guide assessment

Licence

Licensed under the Lesser General Public Licence (LGPL) Version 2.1 (http://www.gnu.org/licenses/lgpl-2.1.html)
