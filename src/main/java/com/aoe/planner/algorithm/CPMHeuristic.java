package com.aoe.planner.algorithm;

import com.aoe.planner.model.*;
import java.util.*;

public class CPMHeuristic {
    
    /**
     * Calcula la heurística h(s) usando el método de Critical Path Method (CPM)
     * Estima el tiempo mínimo restante para alcanzar la Edad de los Castillos
     */
    public double calculate(GameState state, PrecedenceGraph graph) {
        if (state.hasReachedCastleAge()) {
            return 0.0;
        }
        
        // Identificar tareas restantes necesarias
        Set<String> completed = state.getCompletedTasks();
        List<Task> remainingTasks = new ArrayList<>();
        
        for (Task task : graph.getAllTasks()) {
            if (!completed.contains(task.getId())) {
                remainingTasks.add(task);
            }
        }
        
        if (remainingTasks.isEmpty()) {
            return 0.0;
        }
        
        // Construir grafo parcial de precedencias
        Map<String, Integer> earliestStart = new HashMap<>();
        Map<String, Integer> latestStart = new HashMap<>();
        
        // Calcular tiempo más temprano de inicio (forward pass)
        for (Task task : remainingTasks) {
            int maxPredFinish = 0;
            for (String predId : task.getPredecessorIds()) {
                if (!completed.contains(predId)) {
                    Integer predStart = earliestStart.get(predId);
                    if (predStart != null) {
                        Task predTask = graph.getTask(predId);
                        maxPredFinish = Math.max(maxPredFinish, 
                                                predStart + predTask.getDuration());
                    }
                }
            }
            earliestStart.put(task.getId(), maxPredFinish);
        }
        
        // Estimar duración del camino crítico
        int criticalPathLength = 0;
        for (Task task : remainingTasks) {
            int taskFinish = earliestStart.getOrDefault(task.getId(), 0) + task.getDuration();
            criticalPathLength = Math.max(criticalPathLength, taskFinish);
        }
        
        // Agregar estimación de tiempo para recolectar recursos faltantes
        double resourceGatheringTime = estimateResourceGatheringTime(state, remainingTasks);
        
        return criticalPathLength + resourceGatheringTime;
    }
    
    private double estimateResourceGatheringTime(GameState state, List<Task> tasks) {
        // Calcular recursos totales necesarios
        Resource totalNeeded = new Resource();
        for (Task task : tasks) {
            totalNeeded.add(task.getCost());
        }
        
        // Ajustar por recursos ya disponibles
        Resource deficit = new Resource(
            Math.max(0, totalNeeded.getFood() - state.getResources().getFood()),
            Math.max(0, totalNeeded.getWood() - state.getResources().getWood()),
            Math.max(0, totalNeeded.getGold() - state.getResources().getGold())
        );
        
        // Estimar tiempo basado en tasa de recolección y aldeanos disponibles
        int availableVillagers = (int) state.getVillagers().stream()
            .filter(Villager::isReady)
            .count();
        
        if (availableVillagers == 0) {
            return 0.0;
        }
        
        double foodTime = deficit.getFood() / (availableVillagers * ResourceType.FOOD.getGatherRate() / 3.0);
        double woodTime = deficit.getWood() / (availableVillagers * ResourceType.WOOD.getGatherRate() / 3.0);
        double goldTime = deficit.getGold() / (availableVillagers * ResourceType.GOLD.getGatherRate() / 3.0);
        
        return Math.max(foodTime, Math.max(woodTime, goldTime));
    }
}