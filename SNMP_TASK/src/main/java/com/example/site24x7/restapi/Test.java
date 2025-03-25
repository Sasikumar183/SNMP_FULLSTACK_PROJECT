package com.example.site24x7.restapi;

import java.sql.SQLException;

public class Test {

	public static void main(String args[]) throws SQLException {
		
		System.out.println(GetSpecificInterface.getInsights(3,"30d","localhost").toString(4));

	}
}

