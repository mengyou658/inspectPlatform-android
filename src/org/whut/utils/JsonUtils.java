package org.whut.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.whut.database.entity.Task;

import android.util.Log;

public class JsonUtils {

	private static final String SUCCESS = "操作成功！";
	/*
	 *
	 * 
	 *	number相当于id,可忽略
	 * 
	 * {"message":"操作成功！","data":
	 * {"number":6,"role":"机修人员","roleNum":1,
	 * "name":"赵伟","userName":"zhaowei",
	 * "id":6,"image":"/inspectManagementResource/userImage/1/zhaowei.jpg",
	 * "sex":null,"userRole":"ROLE_ADMIN"},"code":200}
	 * 
	 * */
	public static HashMap<String,Object> GetUserData(String message) throws Exception{
		HashMap<String,Object> map = new HashMap<String, Object>();
		JSONObject jsonObject = new JSONObject(message);
		if(jsonObject.getInt("code")==200){
			JSONObject jsonItem = jsonObject.getJSONObject("data");
			map.put("name", jsonItem.getString("name"));
			map.put("userName", jsonItem.getString("userName"));
			map.put("id", jsonItem.getInt("id"));
			map.put("image", jsonItem.getString("image"));
			map.put("sex", jsonItem.getString("sex"));
			map.put("userRole", jsonItem.getString("userRole"));
		}else{
			return null;
		}
		return map;
	}


	/*
	 * 
	 * {"message":"操作成功！","data":[
	 * {"id":1,"name":"机修人员点检表","description":"","createtime":"2014-05-29 00:00:00","appId":1},
	 * {"id":2,"name":"门机司机日常点检表","description":"","createtime":"2014-05-29 00:00:00","appId":1},
	 * {"id":3,"name":"门机队机械技术员点检表","description":"","createtime":"2014-06-13 00:00:00","appId":1},
	 * {"id":4,"name":"门机技术员电气日常点检表","description":"","createtime":"2014-06-21 00:00:00","appId":1},
	 * {"id":5,"name":"门机减速机专项点检卡","description":"","createtime":"2014-06-21 00:00:00","appId":1},
	 * {"id":6,"name":"门机周一定保专项点检卡片","description":"","createtime":"2014-06-21 00:00:00","appId":1},
	 * {"id":7,"name":"流动式起重机械定期检查表","description":"流动式起重机械定期检查表","createtime":"2014-06-30 00:00:00","appId":1}
	 * ],"code":200}

	 * 
	 * 
	 * */

	public static List<HashMap<String,Object>> GetFileList(String message) throws Exception{
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();


		JSONObject jsonObject = new JSONObject(message);
		if(jsonObject.get("message").equals(SUCCESS)){
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for(int i=0;i<jsonArray.length();i++){
				HashMap<String,Object> map = new HashMap<String, Object>();
				JSONObject jsonItem = jsonArray.getJSONObject(i);
				map.put("id", jsonItem.getInt("id"));
				map.put("name", jsonItem.getString("name"));
				list.add(map);
			}
		}
		return list;
	}

	//map转化为jsonString
	public static String HashToJson(HashMap<String,Object> params){
		String string = "{";  
		for (Iterator<Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext();) {  
			Entry<String,Object> e = (Entry<String,Object>) it.next();  
			string += "\"" + e.getKey() + "\":";  
			string += "\"" + e.getValue() + "\",";  
		}  
		string = string.substring(0, string.lastIndexOf(","));  
		string += "}";  
		return string;
	}


	public static List<Task> GetTaskList(String message) throws Exception{
		// TODO Auto-generated method stub
		List<Task> list = new ArrayList<Task>();
		JSONObject jsonObject = new JSONObject(message);
		if(jsonObject.getString("message").equals(SUCCESS)){
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for(int i=0;i<jsonArray.length();i++){
				JSONObject jsonItem = jsonArray.getJSONObject(i);
				//状态未完成
					Task ta = new Task();
					//服务器端任务编号：id
					ta.setId(jsonItem.getInt("id"));
					ta.setInspectPlanId(jsonItem.getInt("inspectPlanId"));
					ta.setInspectTableId(jsonItem.getInt("inspectTableId"));
					ta.setInspectTableRecordId(jsonItem.getInt("inspectTableRecordId"));
					ta.setUserId(jsonItem.getInt("userId"));
					ta.setDeviceId(jsonItem.getInt("deviceId"));
					ta.setFaultCount(jsonItem.getInt("faultCount"));
					ta.setInspectTime(jsonItem.getString("inspectTime"));
					ta.setCreatetime(jsonItem.getString("createtime"));
					ta.setStatus(jsonItem.getInt("status"));
					ta.setTaskDate(jsonItem.getString("taskDate"));
					ta.setTimeStart(jsonItem.getInt("timeStart"));
					ta.setTimeEnd(jsonItem.getInt("timeEnd"));
					ta.setAppId(jsonItem.getInt("appId"));
					ta.setTableName(jsonItem.getString("tableName"));
					ta.setPlanName(jsonItem.getString("planName"));
					ta.setDeviceName(jsonItem.getString("deviceName"));
					ta.setUserName(jsonItem.getString("userName"));
					ta.setStartDay(jsonItem.getString("startDay"));
					ta.setEndDay(jsonItem.getString("endDay"));
					list.add(ta);
				}
			return list;
		}
		return null;
	}

	//验证服务器端返回信息是否正确
	public static boolean Validate(String message) throws Exception{
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(message);
		if(jsonObject.getString("message").equals(SUCCESS)){
			return true;
		}
		return false;
	}
	
	public static boolean UploadIsSuccess(String message) throws Exception{
		JSONObject jsonObject = new JSONObject(message);
		if(Integer.parseInt(jsonObject.getString("code"))==200){
		
			Log.i("msg", jsonObject.getString("code")+jsonObject.getString("message"));
			return true;
		}
		return false;
	}


	public static int getInfo(String message) throws Exception{
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(message);
		Log.i("msg", jsonObject.getString("code"));
		return Integer.parseInt(jsonObject.getString("code"));
	}




	public static List<Map<String, String>> getReturnInfo(String message) throws Exception{
		// TODO Auto-generated method stub
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		JSONObject jsonObject = new JSONObject(message);
		JSONArray jsonArray = jsonObject.getJSONArray("data");
		for(int i=0;i<jsonArray.length();i++){
			Map<String,String> map = new HashMap<String, String>();
			map.put("itemId", jsonArray.getJSONObject(i).getString("itemId"));
			map.put("tableRecordId", jsonArray.getJSONObject(i).getString("tableRecordId"));
			map.put("itemRecordId", jsonArray.getJSONObject(i).getString("itemRecordId"));
			list.add(map);
		}
		return list;
	}


	public static int getLatestVersion(String message) throws Exception{
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(message);
		return jsonObject.getInt("versionCode");
	}


	public static List<HashMap<String, String>> GetUserRoleData(String message) throws Exception{
		// TODO Auto-generated method stub
		List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		JSONObject jsonObject = new JSONObject(message);
		JSONObject jsonItem = jsonObject.getJSONObject("data");
		JSONArray jsonArray = jsonItem.getJSONArray("employeeRoleList");
		for(int i=0;i<jsonArray.length();i++){
			JSONObject jsonArrayItem = jsonArray.getJSONObject(i);
			HashMap<String,String> map = new HashMap<String, String>();
			map.put("id", jsonArrayItem.getInt("id")+"");
			map.put("name", jsonArrayItem.getString("name"));
			list.add(map);
		}
		return list;
	}
	
	
}
