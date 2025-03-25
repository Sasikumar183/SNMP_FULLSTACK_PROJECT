package com.example.site24x7.snmp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.example.site24x7.db.DatabaseInitializer;

import jakarta.annotation.PreDestroy;
@WebListener
public class ArchiveData implements ServletContextListener {
    private CqlSession session;
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        DatabaseInitializer.initializeDatabase();
        try {
            session = DatabaseConfig.getCassandraSession();
            if (session == null || session.isClosed()) {
                throw new IllegalStateException("Cassandra session is not initialized.");
            }
            scheduler = Executors.newScheduledThreadPool(1);

            // Schedule tasks at fixed times
            scheduler.scheduleAtFixedRate(this::insertDummyData, 
                calculateDelayUntil(10, 34), 24 * 60 * 60, TimeUnit.SECONDS);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    removeData();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    System.err.println("Unexpected error in removeData: " + e.getMessage());
                }
            }, calculateDelayUntil(15, 59), 24 * 60 * 60, TimeUnit.SECONDS);

            scheduler.scheduleAtFixedRate(StoreData::fetchData, 0, 1, TimeUnit.MINUTES);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void fetchingAll() {
    	try {
            session = DatabaseConfig.getCassandraSession();
            if (session == null || session.isClosed()) {
                throw new IllegalStateException("Cassandra session is not initialized.");
            }
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	         scheduler.scheduleAtFixedRate(() -> insertDummyData(), 
	             calculateDelayUntil(15,55), 
	             24 * 60 * 60, 
	             TimeUnit.SECONDS);
	
	         scheduler.scheduleAtFixedRate(() -> {
	             try {
	                 removeData();
	             } catch (SQLException e) {
	                 e.printStackTrace();
	             }
	         }, calculateDelayUntil(15,9), 24 * 60 * 60, TimeUnit.SECONDS);
	
	         scheduler.scheduleAtFixedRate(StoreData::fetchData, 0, 1, TimeUnit.MINUTES);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	private static long calculateDelayUntil(int targetHour, int targetMinute) {
		    LocalTime now = LocalTime.now();
		    LocalTime targetTime = LocalTime.of(targetHour, targetMinute);
		
		    if (now.isAfter(targetTime)) {
		        return 24 * 60 * 60; 
		    }
		    return java.time.Duration.between(now, targetTime).getSeconds();
	} 
    
	private void insertDummyData() {	
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
	
    
    public void removeData() throws SQLException {
        String query = "DELETE FROM inter_details WHERE collected_time >= CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 10:00:00') AND collected_time < CONCAT(CURDATE(), ' 10:00:00');";
        
        Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();
        
        int rowsAffected = stmt.executeUpdate(query);
        System.out.println(rowsAffected + " rows deleted.");
        
        stmt.close();
        conn.close();
    }


    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("ServletContextListener destroyed. Closing Cassandra session.");
        if (session != null && !session.isClosed()) {
            session.close();
        }
    }
   

}
