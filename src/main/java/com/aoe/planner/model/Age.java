package com.aoe.planner.model;

public enum Age {
    DARK_AGE(0, "Edad Oscura"),
    FEUDAL_AGE(500, "Edad Feudal"),  // 500 comida
    CASTLE_AGE(800, "Edad de los Castillos");  // 800 comida + 200 oro
    
    private final int foodCost;
    private final String displayName;
    
    Age(int foodCost, String displayName) {
        this.foodCost = foodCost;
        this.displayName = displayName;
    }
    
    public int getFoodCost() {
        return foodCost;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}