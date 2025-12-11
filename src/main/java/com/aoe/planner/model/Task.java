package com.aoe.planner.model;

import java.util.*;

public class Task {
    private final String id;
    private final TaskType type;
    private final int duration; // en segundos
    private final Resource cost;
    private final Set<String> predecessorIds;
    private boolean completed;
    
    // Nuevos campos para RCPSP con recursos acumulables
    private boolean requiresTownCenter; // Si bloquea el TC
    private Resource resourcesGenerated; // Para tareas de recolección
    private double generationRatePerSecond; // Tasa de generación
    private int villagersRequired; // Aldeanos necesarios para esta tarea
    
    public Task(String id, TaskType type, int duration, Resource cost) {
        this.id = id;
        this.type = type;
        this.duration = duration;
        this.cost = cost;
        this.predecessorIds = new HashSet<>();
        this.completed = false;
        this.requiresTownCenter = false;
        this.resourcesGenerated = new Resource(0, 0, 0);
        this.generationRatePerSecond = 0.0;
        this.villagersRequired = 0;
    }
    
    public Task(String id, TaskType type, int duration, Resource cost, boolean requiresTC) {
        this(id, type, duration, cost);
        this.requiresTownCenter = requiresTC;
    }
    
    public void addPredecessor(String predecessorId) {
        predecessorIds.add(predecessorId);
    }
    
    public boolean canExecute(Set<String> completedTasks) {
        return predecessorIds.stream().allMatch(completedTasks::contains);
    }
    
    public void setResourceGeneration(Resource generated, double ratePerSecond) {
        this.resourcesGenerated = generated;
        this.generationRatePerSecond = ratePerSecond;
    }
    
    public void setVillagersRequired(int count) {
        this.villagersRequired = count;
    }
    
    // Getters
    public String getId() { return id; }
    public TaskType getType() { return type; }
    public int getDuration() { return duration; }
    public Resource getCost() { return cost; }
    public Set<String> getPredecessorIds() { return predecessorIds; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public boolean requiresTownCenter() { return requiresTownCenter; }
    public Resource getResourcesGenerated() { return resourcesGenerated; }
    public double getGenerationRatePerSecond() { return generationRatePerSecond; }
    public int getVillagersRequired() { return villagersRequired; }
    
    @Override
    public String toString() {
        return String.format("Task{id='%s', type=%s, duration=%d, cost=%s, TC=%s}", 
                           id, type, duration, cost, requiresTownCenter);
    }
}