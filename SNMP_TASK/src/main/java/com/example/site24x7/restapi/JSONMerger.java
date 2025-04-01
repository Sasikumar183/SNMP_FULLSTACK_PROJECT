package com.example.site24x7.restapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class JSONMerger{
	public static JSONArray merge(JSONArray insightArray, JSONArray cassandraArray, String interval) {
//        System.out.println("-----------------------------------------");
//        System.out.println("MYSQL DATA");
//        System.out.println("-----------------------------------------");
//        System.out.println(insightArray.toString(4));
//        System.out.println("-----------------------------------------");
//        System.out.println("C DATA");
//        System.out.println("-----------------------------------------");
//        System.out.println(cassandraArray.toString(4));


        JSONArray resultArray = new JSONArray();
        if (interval.equals("12h") || interval.equals("1d")) {
            Set<String> uniqueTimeSlots = new HashSet<>();

            for (int i = 0; i < insightArray.length(); i++) {
                JSONObject obj = insightArray.getJSONObject(i);
                String timeSlot = obj.getString("time_slot");

                if (!uniqueTimeSlots.contains(timeSlot)) {
                    uniqueTimeSlots.add(timeSlot);
                    resultArray.put(obj);
                }
            }
            for (int i = 0; i < cassandraArray.length(); i++) {
                JSONObject obj = cassandraArray.getJSONObject(i);
                String timeSlot = obj.optString("time_slot", obj.optString("hour_slot", obj.optString("date", "")));

                if (!uniqueTimeSlots.contains(timeSlot)) {
                    uniqueTimeSlots.add(timeSlot);
                    resultArray.put(obj);
                }
            }

            return resultArray;
        }

        else {
        	System.out.println(insightArray.toString(4));
        	if(cassandraArray.length()==0) {
        		for (int i = 0; i < insightArray.length(); i++) {
                    JSONObject sourceObject = insightArray.getJSONObject(i);
                    JSONObject targetObject = new JSONObject(sourceObject.toString());

                    // Extract only the date part using split(" ")[0]
                    String timeSlot = sourceObject.getString("time_slot");
                    String dateOnly = timeSlot.split(" ")[0];  // Extracts "2025-03-25"

                    targetObject.put("time_slot", dateOnly);
                    resultArray.put(targetObject);
                }
        		return resultArray;
        	}
        	else {
	        	for(int i=0;i<cassandraArray.length();i++) {
	        		JSONObject obj = cassandraArray.getJSONObject(i);
	        		String time = obj.optString("time_slot",obj.optString("date"));
	        		JSONObject matched =null;
	        		
	        		for(int j=0;j<insightArray.length();j++) {
	        			JSONObject obj2 = insightArray.getJSONObject(j);
	        			String time2 = obj2.getString("time_slot").substring(10);
	        			if(time.equals(time2)) {
	        				 matched = obj2;
	        				 break;	 
	        			}
	        		}
	        		JSONObject mergedObj = new JSONObject();
	        		mergedObj.put("date" , time);
	        		for(String key:obj.keySet()) {
	        			if(!(key.equals("date") || key.equals("time_slot"))) {
	                        if (obj.get(key) instanceof Number && matched != null && matched.has(key)) {
	                        	double val1 = obj.getDouble(key);
	                        	double val2 = matched.getDouble(key);
	                        	if (key.startsWith("avg_")) {
	                                mergedObj.put(key, (val1 + val2) / 2.0); // Average
	                            } else if (key.startsWith("max_")) {
	                                mergedObj.put(key, Math.max(val1, val2)); // Maximum
	                            } else if (key.startsWith("min_")) {
	                                mergedObj.put(key, Math.min(val1, val2)); // Minimum
	                            } else if (key.startsWith("sum_")) {
	                                mergedObj.put(key, val1 + val2); // Sum
	                            } else {
	                                mergedObj.put(key, val1); // Default: Keep original
	                            }
	                        } else {
	                            mergedObj.put(key, obj.get(key));
	                        }
	                       }
	        			}
	        			resultArray.put(mergedObj);
	        		}
        	}
        	return resultArray;
        		
        	}
        }
        
    
}
