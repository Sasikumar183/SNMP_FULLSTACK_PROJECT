package com.example.action;

import com.opensymphony.xwork2.ActionSupport;
import org.json.JSONArray;

public class ItemAction extends ActionSupport {
    private static final long serialVersionUID = 1L;
	private JSONArray items;

    public String execute() {
        items = new JSONArray();
        items.put("Item 1");
        items.put("Item 2");
        items.put("Item 3");
        return SUCCESS;
    }

    public JSONArray getItems() {
        return items;
    }
}
