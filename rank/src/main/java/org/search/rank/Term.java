package org.search.rank;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("term")
public class Term {
	
	@Id
    private ObjectId id;
	private String term;
	private Set<Rank> docIds;
	
	public String getTerm() {
		return term;
	}
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public Set<Rank> getDocIds() {
		return docIds;
	}
	public void setDocIds(Set<Rank> docIds) {
		this.docIds = docIds;
	}
	
	
	

}