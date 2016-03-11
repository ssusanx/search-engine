package org.search.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("index")
public class Term {
	
	@Id
    private ObjectId id;
	private String term;
	private double linkAnalysis;
	private Map<Integer, Rank> docIds;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public double getLinkAnalysis() {
		return linkAnalysis;
	}
	public void setLinkAnalysis(double linkAnalysis) {
		this.linkAnalysis = linkAnalysis;
	}
	public Map<Integer, Rank> getDocIds() {
		return docIds;
	}
	public void setDocIds(Map<Integer, Rank> docIds) {
		this.docIds = docIds;
	}
	
	public void addDocId(Integer urlHash)
	{
		if(docIds == null)
		{
			docIds = new HashMap<Integer, Rank>();
		}
		
		Rank r = docIds.get(urlHash);
		if(r == null)
		{
			r = new Rank();
			r.setDocId(urlHash);
			r.setTf(0);
		}
		
		r.setTf(r.getTf() + 1 );
		docIds.put(urlHash, r);
		
	}

}