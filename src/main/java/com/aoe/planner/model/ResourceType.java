package com.aoe.planner.model;

public enum ResourceType {
    FOOD(0.5),    // 0.5 comida por segundo por aldeano
    WOOD(0.4),    // 0.4 madera por segundo por aldeano
    GOLD(0.35);   // 0.35 oro por segundo por aldeano
    
    private final double gatherRate;
    
    ResourceType(double gatherRate) {
        this.gatherRate = gatherRate;
    }
    
    public double getGatherRate() {
        return gatherRate;
    }
}