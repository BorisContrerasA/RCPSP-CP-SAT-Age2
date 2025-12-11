package com.aoe.planner.model;

public class ResourceConstraint {
    private String taskId;
    private Resource minimumRequired;
    private int timePoint; // Momento en que se necesita
    
    public ResourceConstraint(String taskId, Resource minimumRequired, int timePoint) {
        this.taskId = taskId;
        this.minimumRequired = minimumRequired;
        this.timePoint = timePoint;
    }
    
    public String getTaskId() { return taskId; }
    public Resource getMinimumRequired() { return minimumRequired; }
    public int getTimePoint() { return timePoint; }
    
    @Override
    public String toString() {
        return String.format("ResourceConstraint{task=%s, resources=%s, time=%d}", 
                           taskId, minimumRequired, timePoint);
    }
}
