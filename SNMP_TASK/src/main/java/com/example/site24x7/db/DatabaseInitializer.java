package com.example.site24x7.db;

import java.sql.Connection;
import java.sql.Statement;

import com.datastax.oss.driver.api.core.CqlSession;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Creating tables if they do not exist
            String interfaceTable = """
                CREATE TABLE IF NOT EXISTS interface (
			    id INT NOT NULL AUTO_INCREMENT,
			    idx INT,
			    IP VARCHAR(20),
			    interface_name VARCHAR(30),
			    isDeleted int default 0,
			    PRIMARY KEY(id),
			    UNIQUE KEY unique_interface (idx, interface_name, IP)
            	);
            """;

            String interDetailsTable = """
                CREATE TABLE IF NOT EXISTS inter_details (
                    id INT,
                    collected_time TIMESTAMP,
                    in_traffic BIGINT,
                    out_traffic BIGINT,
                    in_error INT,
                    out_error INT,
                    in_discard INT,
                    out_discard INT,
                    admin_status INT,
                    oper_status INT,
                    PRIMARY KEY (id, collected_time)
                );
            """;

           
            String snmpInterfaceTrafficTable= """
                    CREATE TABLE IF NOT EXISTS snmp_interface_traffic (
                        primary_id INT,
                        hour_slot TEXT,
                        avg_in_discard DOUBLE,
                        avg_in_error DOUBLE,
                        avg_in_traffic DOUBLE,
                        avg_out_discard DOUBLE,
                        avg_out_error DOUBLE,
                        avg_out_traffic DOUBLE,
                        interface_idx INT,
                        interface_ip TEXT,
                        max_in_discard DOUBLE,
                        max_in_error DOUBLE,
                        max_in_traffic DOUBLE,
                        max_out_discard DOUBLE,
                        max_out_error DOUBLE,
                        max_out_traffic DOUBLE,
                        min_in_discard DOUBLE,
                        min_in_error DOUBLE,
                        min_in_traffic DOUBLE,
                        min_out_discard DOUBLE,
                        min_out_error DOUBLE,
                        min_out_traffic DOUBLE,
                        sum_in_discard DOUBLE,
                        sum_in_error DOUBLE,
                        sum_out_discard DOUBLE,
                        sum_out_error DOUBLE,
                        PRIMARY KEY (primary_id, hour_slot)
                    ) WITH CLUSTERING ORDER BY (hour_slot DESC);
                """;

            // Execute table creation statements
            stmt.executeUpdate(interfaceTable);
            stmt.executeUpdate(interDetailsTable);
            System.out.println("SQL Table Created");
            CqlSession session = DatabaseConfig.getCassandraSession();
            session.execute(snmpInterfaceTrafficTable);
            System.out.println("Cassandra Table Created");
            System.out.println("Database tables created successfully (if not exist).");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
