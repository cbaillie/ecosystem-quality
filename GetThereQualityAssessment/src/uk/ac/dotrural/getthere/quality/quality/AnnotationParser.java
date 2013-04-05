package uk.ac.dotrural.getthere.quality.quality;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.dotrural.reasoning.sparql.SPARQLUpdate;
import uk.ac.dotrural.getthere.quality.quality.QualityScore;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class AnnotationParser {
	
	private final String QS_ENDPOINT = "http://localhost:3030/QualityScores/update";
	
	public List<QualityScore> getScores(Model results)
	{
		ArrayList<QualityScore> scores = new ArrayList<QualityScore>();
		
		String select = "SELECT * WHERE {" +
						"	?result a <http://abdn.ac.uk/~r01ccb9/Qual-O/Result> . " +
						"	?result <http://www.w3.org/2000/01/rdf-schema#label> ?label . " +
						"	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/metricDescription> ?desc . " +
						"	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/hasScore> ?score . " +
						"	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/basedOn> ?metric . " +
						"	?result <http://abdn.ac.uk/~r01ccb9/Qual-O/annotates> ?observation . " +
						"	?metric <http://abdn.ac.uk/~r01ccb9/Qual-O/measures> ?dimension . " +
						"}";
		
		QueryExecution qe = QueryExecutionFactory.create(select, results);
		
		try
		{
			ResultSet rs = qe.execSelect();
			while(rs.hasNext())
			{
				QuerySolution qs = rs.next();
				String label = "";
				
				try
				{
					label = qs.getLiteral("label").getLexicalForm();
				}
				catch(Exception ex)
				{}
								
				Literal description = qs.getLiteral("desc");
				Literal qScore = qs.getLiteral("score");
				Resource observation = qs.getResource("observation");
				Resource metric = qs.getResource("metric");
				Resource dimension = qs.getResource("dimension");
				
				QualityScore score = new QualityScore(
										dimension.getLocalName(),
										description.getLexicalForm(),
										qScore.getLexicalForm(),
										observation.getURI(),
										metric.getURI(),
										label
									);
				
				score.setNamespaces(observation.getNameSpace(), dimension.getNameSpace());
				scores.add(score);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return scores;
	}
	
	public void sendScoresAsSparqlUpdates(Model results)
	{
		ArrayList<QualityScore> scores = (ArrayList<QualityScore>)getScores(results);
		SPARQLUpdate update = new SPARQLUpdate(QS_ENDPOINT);
		try
		{
			for(int i=0;i<scores.size();i++)
			{
				QualityScore score = (QualityScore)scores.get(i);
				
				String scoreUri = "quality:" + UUID.randomUUID();
				String req = "quality:" + UUID.randomUUID();
				String viol = "quality:" + UUID.randomUUID();
				
				String updateQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
								"PREFIX dqm: <http://purl.org/dqm-vocabulary/v1/dqm#>" +
								"PREFIX quality: <http://dtp-126.sncs.abdn.ac.uk/quality/scores/>" +
								"PREFIX obs: <" + score.getNamespace() + ">" +
								"PREFIX ssnQual: <" + score.getTypeNamespace() + ">" + 
								"INSERT DATA {" +
									scoreUri + " a ssnQual:" + score.getDimension() + " . " +
									scoreUri + " dqm:plainScore " + score.getScore() + " . " +
									scoreUri +	" dqm:basedOn " +  score.getReqName() + " . " +
									req + " a quality:AccuracyRequirement . " +
									req + " dqm:ruleViolation " + viol + " . " +
									req + " dqm:reqName \"" +  score.getReqName() + "\" . " + 
									viol + " a dqm:DataRequirementViolation . " +
									viol + " dqm:affectedInstance obs:" + score.getAffectedInstance() + " . " +
									viol + " dqm:ruleOfIdentification " + req + " . " + 
								"}";
				
				log("sendScoresAsSparqlUpdates","Sending quality score to triple store");
				update.doSPARQLUpdate(updateQuery);		
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void log(String method, String msg)
	{
		System.out.println("[AnnotationParser] " + method + " : " + msg);
	}
}
