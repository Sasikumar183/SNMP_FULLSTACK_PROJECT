package com.example.site24x7.snmp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.example.site24x7.db.DatabaseConfig;

public class InsertToCassandra {

	static void insertToCassandra() {
	    CqlSession session = DatabaseConfig.getCassandraSession();
        if (session == null || session.isClosed()) {
            throw new IllegalStateException("Cassandra session is not initialized.");
        }
		System.out.println("Working");
	    String sqlQuery = """
	        SELECT      
	            interface.id AS primary_id,      
	            interface.idx AS interface_idx,      
	            DATE_FORMAT(collected_time, '%Y-%m-%d %H:00:00') AS hour_slot,      
	            interface.IP AS interface_ip,       
	        
	            MAX(in_traffic) AS max_in_traffic,      
	            MIN(in_traffic) AS min_in_traffic,      
	            AVG(in_traffic) AS avg_in_traffic,       
	        
	            MAX(out_traffic) AS max_out_traffic,      
	            MIN(out_traffic) AS min_out_traffic,      
	            AVG(out_traffic) AS avg_out_traffic,       
	        
	            MAX(in_error) AS max_in_error,      
	            MIN(in_error) AS min_in_error,      
	            AVG(in_error) AS avg_in_error,      
	            SUM(in_error) AS sum_in_error,       
	        
	            MAX(out_error) AS max_out_error,      
	            MIN(out_error) AS min_out_error,      
	            AVG(out_error) AS avg_out_error,      
	            SUM(out_error) AS sum_out_error, 
	        
	            MAX(in_discard) AS max_in_discard,      
	            MIN(in_discard) AS min_in_discard,      
	            AVG(in_discard) AS avg_in_discard,      
	            SUM(in_discard) AS sum_in_discard,

	            MAX(out_discard) AS max_out_discard,      
	            MIN(out_discard) AS min_out_discard,      
	            AVG(out_discard) AS avg_out_discard,      
	            SUM(out_discard) AS sum_out_discard    
	        
	        FROM inter_details 
	        JOIN interface ON inter_details.id = interface.id  
	        GROUP BY inter_details.id, hour_slot, interface.IP  
	        ORDER BY inter_details.id, hour_slot;
	    """;

	    String insertQuery = """
	        INSERT INTO snmp_interface_traffic (
	            primary_id, interface_idx, hour_slot, interface_ip, 
	            avg_in_discard, avg_in_error, avg_in_traffic, 
	            avg_out_discard, avg_out_error, avg_out_traffic,  
	            max_in_discard, max_in_error, max_in_traffic, 
	            max_out_discard, max_out_error, max_out_traffic,  
	            min_in_discard, min_in_error, min_in_traffic, 
	            min_out_discard, min_out_error, min_out_traffic,  
	            sum_in_discard, sum_in_error, sum_out_discard, sum_out_error
	        ) 
	        VALUES (
	            ?, ?, ?, ?, ?, 
	            ?, ?, ?, 
	            ?, ?, ?,  
	            ?, ?, ?, 
	            ?, ?, ?,  
	            ?, ?, ?,  
	            ?, ?, ?,  
	            ?, ?, ?
	        );
	    """;

	    try (Connection conn = DatabaseConfig.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sqlQuery)) {

	        if (session == null || session.isClosed()) {
	            throw new IllegalStateException("Cassandra session is not initialized.");
	        }

	        com.datastax.oss.driver.api.core.cql.PreparedStatement statement = session.prepare(insertQuery);

	        while (rs.next()) {
	            BoundStatement boundStatement = statement.bind(
	                rs.getInt("primary_id"), 
	                rs.getInt("interface_idx"),
	                rs.getString("hour_slot"), 
	                rs.getString("interface_ip"),

	                rs.getDouble("avg_in_discard"), rs.getDouble("avg_in_error"), rs.getDouble("avg_in_traffic"),
	                rs.getDouble("avg_out_discard"), rs.getDouble("avg_out_error"), rs.getDouble("avg_out_traffic"),

	                rs.getDouble("max_in_discard"), rs.getDouble("max_in_error"), rs.getDouble("max_in_traffic"),
	                rs.getDouble("max_out_discard"), rs.getDouble("max_out_error"), rs.getDouble("max_out_traffic"),

	                rs.getDouble("min_in_discard"), rs.getDouble("min_in_error"), rs.getDouble("min_in_traffic"),
	                rs.getDouble("min_out_discard"), rs.getDouble("min_out_error"), rs.getDouble("min_out_traffic"),

	                rs.getDouble("sum_in_discard"), rs.getDouble("sum_in_error"), 
	                rs.getDouble("sum_out_discard"), rs.getDouble("sum_out_error")
	            );

	            session.execute(boundStatement);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
