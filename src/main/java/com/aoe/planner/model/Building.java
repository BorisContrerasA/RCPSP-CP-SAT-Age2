package com.aoe.planner.model;

public class Building {
    private final BuildingType type;
    private boolean isConstructing;
    private int constructionTimeRemaining;
    private final int buildTime; // en segundos
    
    public Building(BuildingType type) {
        this.type = type;
        this.isConstructing = true;
        this.buildTime = getBuildTimeForType(type);
        this.constructionTimeRemaining = buildTime;
    }
    
    private int getBuildTimeForType(BuildingType type) {
        return switch (type) {
            case TOWN_CENTER -> 0;
            case HOUSE -> 25;
            case BARRACKS -> 50;
            case MARKET -> 60;
            case BLACKSMITH -> 50;
            case MILL -> 35;
            case LUMBER_CAMP -> 35;
            case MINING_CAMP -> 35;
        };
    }
    
    public void tick() {
        if (isConstructing && constructionTimeRemaining > 0) {
            constructionTimeRemaining--;
            if (constructionTimeRemaining == 0) {
                isConstructing = false;
            }
        }
    }
    
    public boolean isComplete() {
        return !isConstructing;
    }
    
    public BuildingType getType() {
        return type;
    }
    
    public int getConstructionTimeRemaining() {
        return constructionTimeRemaining;
    }
}