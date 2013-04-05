package uk.ac.dotrural.getthere.quality.test;

import java.util.ArrayList;

import uk.ac.dotrural.getthere.quality.quality.QualityAPI;
import uk.ac.dotrural.getthere.quality.quality.QualityScore;

public class Test {
	
	private final String observationUri = "http://dtp-24.sncs.abdn.ac.uk:8093/observation/8391fd0f-aa78-46cf-955e-e79016490d87";
	private final String observationEndpoint = "http://107.20.159.169:8080/";
	private final String ruleLocation = "http://dtp-126.sncs.abdn.ac.uk/ontologies/GetThereQuality/GetThereQualityRules.ttl";
		
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new Test();
	}
	
	public Test()
	{	
		QualityAPI qa = new QualityAPI(observationEndpoint);
		ArrayList<QualityScore> scores = qa.assess(observationUri, ruleLocation);
		
		for(int i=0;i<scores.size();i++)
		{
			QualityScore qs = (QualityScore)scores.get(i);
			System.out.println(qs.toString());
		}
	}

}
