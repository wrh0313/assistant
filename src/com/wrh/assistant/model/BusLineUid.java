package com.wrh.assistant.model;

import java.io.Serializable;
import java.util.List;

/***
 * 
 * 用于保存Uid和位置对应的公交（地铁）站集合
 * 
 */
@SuppressWarnings("serial")
public class BusLineUid implements Serializable{
	private int position;
	private String uid;
	private List<String> stationList;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<String> getStationList() {
		return stationList;
	}

	public void setStationList(List<String> stationList) {
		this.stationList = stationList;
	}

}
