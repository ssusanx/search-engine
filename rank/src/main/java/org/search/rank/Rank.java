package org.search.rank;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

public class Rank {
	
	
	@Id
    private ObjectId id;
	private Integer docId;
	private double tfIdf;
	private int tf;
	private int totalTermCount;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId i) {
		this.id = i;
	}
	public double getTfIdf() {
		return tfIdf;
	}
	public void setTfIdf(double tfIdf) {
		this.tfIdf = tfIdf;
	}

	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
	public int getTotalTermCount() {
		return totalTermCount;
	}
	public void setTotalTermCount(int totalTermCount) {
		this.totalTermCount = totalTermCount;
	}
	public Integer getDocId() {
		return docId;
	}
	public void setDocId(Integer docId) {
		this.docId = docId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docId == null) ? 0 : docId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rank other = (Rank) obj;
		if (docId == null) {
			if (other.docId != null)
				return false;
		} else if (!docId.equals(other.docId))
			return false;
		return true;
	}

}
