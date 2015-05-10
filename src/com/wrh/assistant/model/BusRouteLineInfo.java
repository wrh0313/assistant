package com.wrh.assistant.model;

import java.io.Serializable;
import java.util.List;

import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.route.TransitRouteLine.TransitStep;

@SuppressWarnings("serial")
public class BusRouteLineInfo implements Serializable {
	private String routeOutlineStr;
	private String stationNum;
	private String onFoot;
	private String duration;
	private List<TransitStep> steps;
	private RouteNode startNode;
	private RouteNode endNode;

	public RouteNode getStartNode() {
		return startNode;
	}

	public void setStartNode(RouteNode startNode) {
		this.startNode = startNode;
	}

	public RouteNode getEndNode() {
		return endNode;
	}

	public void setEndNode(RouteNode endNode) {
		this.endNode = endNode;
	}

	public List<TransitStep> getSteps() {
		return steps;
	}

	public void setSteps(List<TransitStep> steps) {
		this.steps = steps;
	}

	public String getRouteOutlineStr() {
		return routeOutlineStr;
	}

	public void setRouteOutlineStr(String routeOutlineStr) {
		this.routeOutlineStr = routeOutlineStr;
	}

	public String getStationNum() {
		return stationNum;
	}

	public void setStationNum(String stationNum) {
		this.stationNum = stationNum;
	}

	public String getOnFoot() {
		return onFoot;
	}

	public void setOnFoot(String onFoot) {
		this.onFoot = onFoot;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

}
