package com.aoe.planner.model;

public class Technology {
    private final TechnologyType type;
    private final int duration; // segundos
    private final Resource cost;
    private boolean researched;

    public Technology(TechnologyType type, int duration, Resource cost) {
        this.type = type;
        this.duration = duration;
        this.cost = cost;
        this.researched = false;
    }

    public TechnologyType getType() { return type; }
    public int getDuration() { return duration; }
    public Resource getCost() { return cost; }
    public boolean isResearched() { return researched; }
    public void setResearched(boolean researched) { this.researched = researched; }
}