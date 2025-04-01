package com.example.site24x7.restapi;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.site24x7.db.DatabaseConfig;
import java.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;

public class CassandraSpecificInterface {
	
	public static JSONArray getCassandraData(int id,String interval,String IP,int index) {
//		System.out.println(index +"  "+IP);
		Instant now = Instant.now();
		Instant inter;
		if (interval.equals("12h")) {
		    inter = now.minusSeconds(12 * 3600);  
		} 
		else if (interval.equals("1d")) {
		    inter = now.minusSeconds(24 * 3600);  
		} 
		else if (interval.equals("1w")) {
		    inter = now.minusSeconds(7 * 24 * 3600); 
		} 
		else if (interval.equals("30d")) {
		    inter = now.minusSeconds(30 * 24 * 3600);
		}
		else {
			return new JSONArray();
		}
		
		String query = "SELECT primary_id, hour_slot, avg_in_discard, avg_in_error, avg_in_traffic, " +
                "avg_out_discard, avg_out_error, avg_out_traffic,"+
                "max_in_discard, max_in_error, " +
                "max_in_traffic, max_out_discard, max_out_error, max_out_traffic, min_in_discard, " +
                "min_in_error, min_in_traffic, min_out_discard, min_out_error, min_out_traffic, " +
                "sum_in_discard, sum_in_error, sum_out_discard, sum_out_error " +
                "FROM snmp_interface_traffic WHERE interface_ip = ? AND interface_idx = ? AND hour_slot>= ?;";

        // Execute query
        String timestampStr = inter.toString();
        
        CqlSession session = DatabaseConfig.getCassandraSession();
        
        ResultSet resultSet = session.execute(session.prepare(query).bind(IP,index,timestampStr));

        JSONArray jsonArray = new JSONArray();
        for (Row row : resultSet) {
            JSONObject jsonObject = new JSONObject();
            
            jsonObject.put("interface_id", row.getInt("primary_id"));
            jsonObject.put("hour_slot", row.getString("hour_slot"));
            jsonObject.put("avg_in_discard", row.getDouble("avg_in_discard"));
            jsonObject.put("avg_in_error", row.getDouble("avg_in_error"));
            jsonObject.put("avg_in_traffic", row.getDouble("avg_in_traffic"));

            jsonObject.put("avg_out_discard", row.getDouble("avg_out_discard"));
            jsonObject.put("avg_out_error", row.getDouble("avg_out_error"));
            jsonObject.put("avg_out_traffic", row.getDouble("avg_out_traffic"));

            
            jsonObject.put("max_in_discard", row.getDouble("max_in_discard"));
            jsonObject.put("max_in_error", row.getDouble("max_in_error"));
            jsonObject.put("max_in_traffic", row.getDouble("max_in_traffic"));

            jsonObject.put("max_out_discard", row.getDouble("max_out_discard"));
            jsonObject.put("max_out_error", row.getDouble("max_out_error"));
            jsonObject.put("max_out_traffic", row.getDouble("max_out_traffic"));

            jsonObject.put("min_in_discard", row.getDouble("min_in_discard"));
            jsonObject.put("min_in_error", row.getDouble("min_in_error"));
            jsonObject.put("min_in_traffic", row.getDouble("min_in_traffic"));

            jsonObject.put("min_out_discard", row.getDouble("min_out_discard"));
            jsonObject.put("min_out_error", row.getDouble("min_out_error"));
            jsonObject.put("min_out_traffic", row.getDouble("min_out_traffic"));

            jsonObject.put("count_in_discard", row.getDouble("sum_in_discard"));
            jsonObject.put("count_in_error", row.getDouble("sum_in_error"));

            jsonObject.put("count_out_discard", row.getDouble("sum_out_discard"));
            jsonObject.put("count_out_error", row.getDouble("sum_out_error"));

            jsonArray.put(jsonObject);
        }
//        System.out.println("------------------------------------------------------");
//        System.out.println(jsonArray);
//        System.out.println("------------------------------------------------------");

        if(interval.equals("30d") || interval.equals("1w")) {
        	jsonArray =  CassandraDataAggregator.getAggregated(jsonArray, interval);
        }
		return jsonArray;
	}
	
	public static void main(String args[]) {
		//JSONArray res = getCassandraData(3,"12h");
		//System.out.println(res.toString(4));
	}
	
	
   
}
