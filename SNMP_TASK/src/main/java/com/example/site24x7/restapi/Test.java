package com.example.site24x7.restapi;

import java.sql.SQLException;

import com.example.site24x7.snmp.CheckDeletion;

public class Test {

	public static void main(String args[]) throws SQLException {
		
		System.out.println(CheckDeletion.isDeleted());

	}
}

