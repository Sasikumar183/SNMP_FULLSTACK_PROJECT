SELECT 
        		        inter_details.id, 
        		        interface.idx,
        		        interface.interface_name,
        		        interface.IP, 
        		        AVG(in_traffic) AS avg_in_traffic, 
        		        AVG(out_traffic) AS avg_out_traffic, 
        		        AVG(in_error) AS avg_in_error, 
        		        AVG(out_error) AS avg_out_error, 
        		        AVG(in_discard) AS avg_in_discard, 
        		        AVG(out_discard) AS avg_out_discard
        		    FROM inter_details 
        		    JOIN interface ON inter_details.id = interface.id 
        		    WHERE collected_time >= NOW() - INTERVAL 1 hour
        		    GROUP BY inter_details.id;

                    