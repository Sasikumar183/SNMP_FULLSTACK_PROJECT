package com.example.site24x7.snmp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.example.site24x7.db.DatabaseConfig;

public class CheckDeletion {
	
	public static Map<String, Integer> isDeleted() {
		String query = "SELECT IP, idx, isDeleted FROM interface;";
		Map<String,Integer> deleteCheck = new HashMap<String, Integer>();
		
		try (Connection con = DatabaseConfig.getConnection();
			    PreparedStatement ps = con.prepareStatement(query)) {
				
				
				try (ResultSet rs = ps.executeQuery()) {
					while(rs.next()) {
						deleteCheck.put(rs.getString("IP")+"-"+rs.getInt("idx"), rs.getInt("isDeleted"));
					}
				}
				con.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return deleteCheck;
		
	}
	
	public static void changeStatus(String IP, int idx) {
		String query = "UPDATE interface set isDeleted = 1 WHERE IP = ? AND idx = ?";
		try {
			Connection con = DatabaseConfig.getConnection();
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, IP);
			ps.setInt(2, idx);
			ps.executeUpdate();
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
	}
}
	
}
