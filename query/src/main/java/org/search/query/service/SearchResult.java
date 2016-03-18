package org.search.query.service;

import java.util.Comparator;

public class SearchResult implements Comparable<SearchResult>{
	
	private String title;
	private String link;
	private double tfidf;
	private double linkAnalysis;
	private double score;
	private double similarity;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public double getTfidf() {
		return tfidf;
	}
	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}
	public double getLinkAnalysis() {
		return linkAnalysis;
	}
	public void setLinkAnalysis(double linkAnalysis) {
		this.linkAnalysis = linkAnalysis;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	@Override
	public int compareTo(SearchResult o1) {
		
		if (this.score < o1.score)
			return 1;
		else if (this.score > o1.score)
			return -1;
		else
			return 0;
	}
	public double getSimilarity() {
		return similarity;
	}
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

}
