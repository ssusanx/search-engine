package org.search.rank;

import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

public class Rank {
	
	@Id
    private ObjectId id;
	private double tfIdf;
	private int tf;
	private double linkAnalysis;
	private Integer docId;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
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
	public Integer getDocId() {
		return docId;
	}
	public void setDocId(Integer docId) {
		this.docId = docId;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}

}
