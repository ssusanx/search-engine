package org.search.rank;

import java.util.ArrayList;
import java.util.List;

public class RankInfo {
	
	private Integer count;
	private List<Integer> positions = new ArrayList<Integer>();
	private String urlHash;
	
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public List<Integer> getPositions() {
		return positions;
	}
	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
	public String getUrlHash() {
		return urlHash;
	}
	public void setUrlHash(String urlHash) {
		this.urlHash = urlHash;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((urlHash == null) ? 0 : urlHash.hashCode());
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
		RankInfo other = (RankInfo) obj;
		if (urlHash == null) {
			if (other.urlHash != null)
				return false;
		} else if (!urlHash.equals(other.urlHash))
			return false;
		return true;
	}
	

}
