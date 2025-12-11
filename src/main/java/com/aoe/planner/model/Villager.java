package com.aoe.planner.model;

public class Villager {
    private int id;
    private int creationTime;
    private boolean isReady;
    private ResourceType assignedResource;
    private int travelTimeRemaining;
    
    public Villager(int id) {
        this.id = id;
        this.creationTime = 0;
        this.isReady = false;
        this.assignedResource = null;
        this.travelTimeRemaining = 0;
    }
    
    public void assignTo(ResourceType resource, int travelTime) {
        this.assignedResource = resource;
        this.travelTimeRemaining = travelTime;
    }
    
    public void tick() {
        if (!isReady) {
            creationTime++;
            if (creationTime >= 25) {
                isReady = true;
            }
        }
        
        // Decrementar tiempo de viaje si está caminando
        if (travelTimeRemaining > 0) {
            travelTimeRemaining--;
        }
    }
    
    public boolean isGathering() {
        return isReady && assignedResource != null && travelTimeRemaining == 0;
    }
    
    // Getters
    public int getId() { return id; }
    public boolean isReady() { return isReady; }
    public ResourceType getAssignedResource() { return assignedResource; }
    public int getTravelTimeRemaining() { return travelTimeRemaining; }
}
