package org.search.rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("index")
public class RankInfo {
	
	@Id
    private ObjectId id;
	private String term;
	private double tfIdf;
	private Integer count;
	private double linkAnalysis;
	private Set<Integer> docIds;
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public double getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;
	}
	public double getLinkAnalysis() {
		return linkAnalysis;
	}
	public void setLinkAnalysis(double linkAnalysis) {
		this.linkAnalysis = linkAnalysis;
	}
	public Set<Integer> getDocIds() {
		return docIds;
	}
	public void setDocIds(Set<Integer> docIds) {
		this.docIds = docIds;
	}
	
	public void addDocId(Integer urlHash)
	{
		if(docIds == null)
		{
			docIds = new HashSet<Integer>();
		}
		
		docIds.add(urlHash);
		
	}

}