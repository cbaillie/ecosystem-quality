package uk.ac.dotrural.getthere.quality.quality;

import com.hp.hpl.jena.ontology.OntModel;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QualityScore implements Comparable<QualityScore> {
	
	private final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private final String DQM = "http://purl.org/dqm-vocabulary/v1/dqm#";
	private final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	
	private String dimension;
	private String description;
	private String score;
	private String affectedInstance;
	private String reqName;
	private String label;
	private String source = "unknown";
	
	private String namespace = "";
	private String typeNamespace = "";
	
	@XmlElement(name="dimension")
	public String getDimension() {
		return dimension;
	}

	@XmlElement(name="description")
	public String getDescription() {
		return description;
	}

	@XmlElement(name="score")
	public String getScore() {
		return score;
	}

	@XmlElement(name="affectedInstance")
	public String getAffectedInstance() {
		return affectedInstance;
	}

	@XmlElement(name="reqName")
	public String getReqName() {
		return reqName;
	}

	@XmlElement(name="label")
	public String getLabel() {
		return label;
	}

	@XmlElement(name="source")
	public String getSource() {
		return source;
	}

	@XmlElement(name="namespace")
	public String getNamespace() {
		return namespace;
	}

	@XmlElement(name="typeNamespace")
	public String getTypeNamespace() {
		return typeNamespace;
	}

	public QualityScore(String d, String desc, String s, String a, String rn, String l)
	{
		dimension = d;
		description = desc;
		score = s;
		affectedInstance = a;
		reqName = rn;
		label = l;
	}
	
	public QualityScore(String d, String desc, String s, String a, String rn, String l, String so)
	{
		dimension = d;
		description = desc;
		score = s;
		affectedInstance = a;
		reqName = rn;
		label = l;
		source = so;
	}
	
	public void setNamespaces(String n, String t)
	{
		namespace = n;
		typeNamespace = t;
	}

	public int compareTo(QualityScore qs) 
	{
		return dimension.compareTo(qs.dimension);
	}
	
	public OntModel getRdfModel()
	{
		OntModel scoreModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		
		Resource obs = scoreModel.createResource(affectedInstance);
		Resource scoreRes = scoreModel.createResource();
		Resource rule = scoreModel.createResource();
		Resource viol = scoreModel.createResource();
		
		Statement scoreTypeStmt = scoreModel.createStatement(scoreRes, scoreModel.createProperty(RDF, "type"), scoreModel.createResource(dimension));
		Statement scoreScoreStmt = scoreModel.createStatement(scoreRes, scoreModel.createProperty(DQM, "plainScore"), scoreModel.createTypedLiteral(score));
		Statement scoreRuleStmt = scoreModel.createStatement(scoreRes, scoreModel.createProperty(DQM, "basedOn"), rule);
		Statement scoreLabelStmt = null;
		if(label.length() > 0)
			scoreLabelStmt = scoreModel.createStatement(scoreRes, scoreModel.createProperty(RDFS, "label"), label);
		
		Statement ruleTypeStmt = scoreModel.createStatement(rule, scoreModel.createProperty(RDF, "type"), scoreModel.createResource(DQM + "DataRequirement"));
		Statement ruleViolStmt = scoreModel.createStatement(rule, scoreModel.createProperty(DQM, "ruleViolation"), viol);
		Statement ruleNameStmt = scoreModel.createStatement(rule, scoreModel.createProperty(DQM, "reqName"), scoreModel.createTypedLiteral(dimension.substring((dimension.lastIndexOf('#')+1))));
		Statement ruleDescStmt = scoreModel.createStatement(rule, scoreModel.createProperty(DQM, "reqDescription"), scoreModel.createTypedLiteral(description));
		Statement ruleSourceStmt = scoreModel.createStatement(rule, scoreModel.createProperty(DQM, "reqSource"), scoreModel.createTypedLiteral(source));
		
		Statement violTypeStmt = scoreModel.createStatement(viol, scoreModel.createProperty(RDF, "type"), scoreModel.createResource(DQM + "DataRequirementViolation"));
		Statement violObsStmt = scoreModel.createStatement(viol, scoreModel.createProperty(DQM, "affectedInstance"), obs);
		Statement violRuleStmt = scoreModel.createStatement(viol, scoreModel.createProperty(DQM, "ruleOfIdentification"), rule);
		
		scoreModel.add(scoreTypeStmt);
		scoreModel.add(scoreScoreStmt);
		scoreModel.add(scoreRuleStmt);
		
		if(scoreLabelStmt != null)
			scoreModel.add(scoreLabelStmt);
		
		scoreModel.add(ruleTypeStmt);
		scoreModel.add(ruleViolStmt);
		scoreModel.add(ruleNameStmt);
		scoreModel.add(ruleDescStmt);
		scoreModel.add(ruleSourceStmt);
		scoreModel.add(violTypeStmt);
		scoreModel.add(violObsStmt);
		scoreModel.add(violRuleStmt);

		return scoreModel;
	}
	
	public String toString()
	{
		return "[QualityScore] toString : " + dimension + " (" + score + ") " + description;
	}

}
