package org.whut.entity;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;


public class Tag implements Listable{
	
	private final String cardType = "0x02";
	private String deviceType;
	private String deviceTypeNum;
	private String deviceNum;
	private String tagArea;
	private String tagAreaNum;
	private String number;
	
	public final int PROPERTY_COUNT = 4;
	
	public Tag(){}
	
	public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() { 
        public Tag createFromParcel(Parcel p) { 
            return new Tag(p); 
        } 
 
        public Tag[] newArray(int size) { 
            return new Tag[size]; 
        } 
    }; 
	
	public Tag(String data){
		if(data!=null){
			String[] d = data.split(",");
			tagArea = d[1];
			deviceNum = d[3];
			tagAreaNum = d[4];
		}else{
		}
	}

	public Tag(Parcel p){
		this.tagArea = p.readString();
		this.deviceNum = p.readString();
		this.tagAreaNum = p.readString();
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(tagArea);
		dest.writeString(deviceNum);
		dest.writeString(tagAreaNum);
	}
	
	public void setByList(List<String> params){
		if(params.size()==PROPERTY_COUNT){
			this.tagArea = params.get(1);
			this.deviceNum = params.get(2);
			this.tagAreaNum = params.get(3);
		}else{
		}
	}
	
	public int getPropertyCount(){
		return PROPERTY_COUNT;
	}
	
	public List<String> getParams(){
		
		ArrayList<String> params = new ArrayList<String>();
		params.add(cardType);
		params.add(tagArea);
		params.add(deviceNum); 
		params.add(tagAreaNum);
		
		return params;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	public String getCardType() {
		return cardType;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceTypeNum() {
		return deviceTypeNum;
	}
	public void setDeviceTypeNum(String deviceTypeNum) {
		this.deviceTypeNum = deviceTypeNum;
	}
	public String getDeviceNum() {
		return deviceNum;
	}
	public void setDeviceNum(String deviceNum) {
		this.deviceNum = deviceNum;
	}
	public String getTagArea() {
		return tagArea;
	}
	public void setTagArea(String tagArea) {
		this.tagArea = tagArea;
	}
	public String getTagAreaNum() {
		return tagAreaNum;
	}
	public void setTagAreaNum(String tagAreaNum) {
		this.tagAreaNum = tagAreaNum;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	
}
