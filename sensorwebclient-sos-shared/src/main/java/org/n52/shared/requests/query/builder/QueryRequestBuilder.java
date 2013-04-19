package org.n52.shared.requests.query.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.n52.shared.requests.query.QueryRequest;
import org.n52.shared.serializable.pojos.BoundingBox;


public abstract class QueryRequestBuilder {
	
	private String serviceUrl;
	
	private Collection<String> offeringFilter = new ArrayList<String>();
	
	private Collection<String> procedureFilter = new ArrayList<String>();
	
	private Collection<String> phenomenonFilter = new ArrayList<String>();
	
	private Collection<String> featureOfInterestFilter = new ArrayList<String>();

    private BoundingBox spatialFilter;
    
	private int offset;
	
	private int size;

	public abstract QueryRequest build();
	
	public QueryRequestBuilder addServiceUrl(String serviceUrl){
		this.serviceUrl = serviceUrl;
		return this;
	}

	public QueryRequestBuilder addOfferingFilter(String offeringFilter) {
		this.offeringFilter.add(offeringFilter);
		return this;
	}
	
	public QueryRequestBuilder addProcedureFilter(String procedureFilter) {
		this.procedureFilter.add(procedureFilter);
		return this;
	}

	public QueryRequestBuilder addPhenomenonFilter(String phenomenonFilter) {
		this.phenomenonFilter.add(phenomenonFilter);
		return this;
	}

	public QueryRequestBuilder addFeatureOfInterestFilter(String featureOfInterestFilter) {
		this.featureOfInterestFilter.add(featureOfInterestFilter);
		return this;
	}
	
	public QueryRequestBuilder addOfferingFilter(Collection<String> offeringFilter) {
		this.offeringFilter.addAll(offeringFilter);
		return this;
	}
	
	public QueryRequestBuilder addProcedureFilter(Collection<String> procedureFilter) {
		this.procedureFilter.addAll(procedureFilter);
		return this;
	}

	public QueryRequestBuilder addPhenomenonFilter(Collection<String> phenomenonFilter) {
		this.phenomenonFilter.addAll(phenomenonFilter);
		return this;
	}

	public QueryRequestBuilder addFeatureOfInterestFilter(Collection<String> featureOfInterestFilter) {
		this.featureOfInterestFilter.addAll(featureOfInterestFilter);
		return this;
	}
	public QueryRequestBuilder setOffset(int offset) {
		this.offset = offset;
		return this;
	}
    
    public QueryRequestBuilder addSpatialFilter(BoundingBox boundingBox) {
        this.spatialFilter = boundingBox;
        return this;
    }
    
	public QueryRequestBuilder setSize(int size) {
		this.size = size;
		return this;
	}
	
	public String getServiceUrl() {
		return serviceUrl;
	}

	public Collection<String> getOfferingFilter(){
		return offeringFilter;
	}
	
	public Collection<String> getProcedureFilter(){
		return procedureFilter;
	}

	public Collection<String> getPhenomenonFilter() {
		return phenomenonFilter;
	}

	public Collection<String> getFeatureOfInterestFilter() {
		return featureOfInterestFilter;
	}

    public BoundingBox getSpatialFilter() {
        return spatialFilter;
    }
    
	public int getOffset() {
		return offset;
	}
	
	public int getSize() {
		return size;
	}

}
