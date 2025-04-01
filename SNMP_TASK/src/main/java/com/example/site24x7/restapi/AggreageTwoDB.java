package com.example.site24x7.restapi;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class AggreageTwoDB {

	public static JSONObject getAggregate(JSONArray data1,JSONArray data2) {
		
        Map<Integer, JSONObject> aggregatedData = new HashMap<>();
        for (int i = 0; i < data1.length(); i++) {
            JSONObject obj = data1.getJSONObject(i);
            int interfaceId = obj.getInt("interface_id");
            aggregatedData.put(interfaceId, new JSONObject(obj.toString())); 
        }

        for (int i = 0; i < data2.length(); i++) {
            JSONObject obj = data2.getJSONObject(i);
            int interfaceId = obj.getInt("interface_id");

            if (aggregatedData.containsKey(interfaceId)) {
                JSONObject existingObj = aggregatedData.get(interfaceId);
                
                existingObj.put("avg_out_error", existingObj.optDouble("avg_out_error", 0) + obj.optDouble("avg_out_error", 0)/2);
                existingObj.put("avg_in_discard", existingObj.optDouble("avg_in_discard", 0) + obj.optDouble("avg_in_discard", 0)/2);
                existingObj.put("avg_out_discard", existingObj.optDouble("avg_out_discard", 0) + obj.optDouble("avg_out_discard", 0)/2);
                existingObj.put("avg_in_error", existingObj.optDouble("avg_in_error", 0) + obj.optDouble("avg_in_error", 0)/2);

                existingObj.put("avg_in_traffic", existingObj.optDouble("avg_in_traffic", 0) + obj.optDouble("avg_in_traffic", 0)/2);
                existingObj.put("avg_out_traffic", existingObj.optDouble("avg_out_traffic", 0) + obj.optDouble("avg_out_traffic", 0)/2);
            } else {
                aggregatedData.put(interfaceId, new JSONObject(obj.toString()));
            }
        }

        JSONArray mergedArray = new JSONArray();
        for (JSONObject obj : aggregatedData.values()) {
            mergedArray.put(obj);
        }

        JSONObject result = new JSONObject();
        result.put("data", mergedArray);
        
		return result;
		
	}
}
