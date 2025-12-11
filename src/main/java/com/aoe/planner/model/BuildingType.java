package com.aoe.planner.model;

public enum BuildingType {
    TOWN_CENTER(0, 0, 0, "Centro Urbano"),
    HOUSE(25, 0, 0, "Casa"),
    BARRACKS(175, 0, 0, "Cuartel"),
    MARKET(100, 0, 0, "Mercado"),
    BLACKSMITH(150, 0, 0, "Herrería"),
    MILL(100, 0, 0, "Molino"),
    LUMBER_CAMP(100, 0, 0, "Campamento Maderero"),
    MINING_CAMP(100, 0, 0, "Campamento Minero");
    
    private final int woodCost;
    private final int foodCost;
    private final int goldCost;
    private final String displayName;
    
    BuildingType(int woodCost, int foodCost, int goldCost, String displayName) {
        this.woodCost = woodCost;
        this.foodCost = foodCost;
        this.goldCost = goldCost;
        this.displayName = displayName;
    }
    
    public Resource getCost() {
        return new Resource(foodCost, woodCost, goldCost);
    }
    
    public String getDisplayName() {
        return displayName;
    }
}