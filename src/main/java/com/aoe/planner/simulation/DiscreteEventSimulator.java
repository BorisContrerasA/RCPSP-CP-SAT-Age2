package com.aoe.planner.simulation;

import com.aoe.planner.model.Age;
import com.aoe.planner.model.*;
import java.util.*;

public class DiscreteEventSimulator {

    private GameState state;
    private List<String> eventLog;

    public DiscreteEventSimulator(GameState initialState) {
        this.state = initialState;
        this.eventLog = new ArrayList<>();
    }

    public void executePlan(List<String> actions) {
        System.out.println("=== Ejecutando Plan de Simulación ===\n");
        for (String action : actions) {
            executeAction(action);
            logEvent(action);
        }
        System.out.println("\n=== Simulación Completada ===");
        printMetrics();
    }

    private void executeAction(String action) {
        System.out.println("[t=" + state.getCurrentTime() + "s] Ejecutando: " + action);
        
        switch (action) {
            case "CREATE_VILLAGER" -> {
                if (state.getCurrentAge() == Age.DARK_AGE && state.canAdvanceAge(Age.FEUDAL_AGE)){
                    System.out.println("  ⚠ Avanzando a Edad Feudal antes de crear aldeano...");
                    break;
                }
                
                if (state.getCurrentAge() == Age.FEUDAL_AGE && state.canAdvanceAge(Age.CASTLE_AGE)){
                    System.out.println("  ⚠ Avanzando a Edad de los Castillos antes de crear aldeano...");
                    break;
                }
                
                while (state.getPopulation() >= state.getPopulationCapacity()) {
                    if(state.canBuild(BuildingType.HOUSE)){
                        System.out.println("  → Construyendo casa para aumentar población...");
                        state.buildBuilding(BuildingType.HOUSE);
                        simulateConstruction(25);
                    } else {
                        state.tick();
                    }
                }
                
                while(!state.canCreateVillager()){
                    state.tick();
                }
                
                state.createVillager();
                simulateUntilVillagerReady();
            }
            
            case "BUILD_HOUSE" -> {
                if (state.canBuild(BuildingType.HOUSE)) {
                    state.buildBuilding(BuildingType.HOUSE);
                    simulateConstruction(25);
                }
            }
            
            case "BUILD_MILL" -> {
                if (state.canBuild(BuildingType.MILL)) {
                    state.buildBuilding(BuildingType.MILL);
                    simulateConstruction(35);
                }
            }
            
            case "BUILD_LUMBER_CAMP" -> {
                if (state.canBuild(BuildingType.LUMBER_CAMP)) {
                    state.buildBuilding(BuildingType.LUMBER_CAMP);
                    simulateConstruction(35);
                }
            }
            
            case "BUILD_BARRACKS" -> {
                if (state.canBuild(BuildingType.BARRACKS)) {
                    state.buildBuilding(BuildingType.BARRACKS);
                    simulateConstruction(50);
                }
            }
            
            case "BUILD_MARKET" -> {
                if (state.canBuild(BuildingType.MARKET)) {
                    state.buildBuilding(BuildingType.MARKET);
                    simulateConstruction(60);
                }
            }
            
            case "BUILD_BLACKSMITH" -> {
                if (state.canBuild(BuildingType.BLACKSMITH)) {
                    state.buildBuilding(BuildingType.BLACKSMITH);
                    simulateConstruction(50);
                }
            }
            
          case "ADVANCE_FEUDAL" -> {
            System.out.println("\n=== ADVANCE_FEUDAL: INICIO ===");
            System.out.println("Comida: " + state.getResources().getFood() + "/500");
            System.out.println("canAdvanceAge(FEUDAL): " + state.canAdvanceAge(Age.FEUDAL_AGE));
            System.out.println("hasRequiredBuildings: " + state.hasRequiredBuildingsForFeudal());

            int loopCount = 0;

            while (!state.canAdvanceAge(Age.FEUDAL_AGE)) {
                int foodBefore = state.getResources().getFood();
                state.tick();
                int foodAfter = state.getResources().getFood();
                loopCount++;

                if (loopCount % 20 == 0) {
                    System.out.println("[Loop " + loopCount + "] t=" + state.getCurrentTime() + 
                                    " | Comida: " + foodAfter + "/500 (+" + (foodAfter - foodBefore) + ")");
                }

                if (loopCount > 1000) {
                    System.err.println("\n LOOP INFINITO EN FEUDAL");
                    System.err.println("Comida final: " + state.getResources().getFood());
                    System.err.println("canAdvanceAge: " + state.canAdvanceAge(Age.FEUDAL_AGE));
                    return;
                }
            }

            System.out.println("\n✓ Condición cumplida después de " + loopCount + " ticks");
            System.out.println("Comida final: " + state.getResources().getFood() + "/500");

            state.advanceAge(Age.FEUDAL_AGE);
            simulate(130);
            System.out.println("✓ Feudal Age alcanzada en t=" + state.getCurrentTime() + "s\n");
}
            
            case "ADVANCE_CASTLE" -> {
    System.out.println("\n=== INTENTANDO AVANZAR A CASTLE AGE ===");
    System.out.println("Recursos actuales: " + state.getResources());
    System.out.println("Distribución: F=" + state.getVillagersOnFood() + 
                     " W=" + state.getVillagersOnWood() + 
                     " G=" + state.getVillagersOnGold());
    
    int loopCount = 0;
    int maxIterations = 500;  // Límite de seguridad
    
    while (!state.canAdvanceAge(Age.CASTLE_AGE)) {
        state.tick();
        loopCount++;
        
        // Debug cada 50 ticks
        if (loopCount % 50 == 0) {
            System.out.println("[Loop " + loopCount + "] t=" + state.getCurrentTime() + 
                             " | Comida: " + state.getResources().getFood() + "/800" +
                             " | Oro: " + state.getResources().getGold() + "/200");
        }
        
        // ABORTAR SI EXCEDE EL LÍMITE
        if (loopCount > maxIterations) {
            System.err.println("\n ERROR: Loop infinito detectado en ADVANCE_CASTLE");
            System.err.println("Estado final:");
            System.err.println("  Tiempo: t=" + state.getCurrentTime() + "s");
            System.err.println("  Recursos: " + state.getResources());
            System.err.println("  Comida: " + state.getResources().getFood() + "/800");
            System.err.println("  Oro: " + state.getResources().getGold() + "/200");
            System.err.println("  Distribución: F=" + state.getVillagersOnFood() + 
                             " W=" + state.getVillagersOnWood() + 
                             " G=" + state.getVillagersOnGold());
            
            // Verificar si los aldeanos están recolectando
            long gatheringGold = state.getVillagers().stream()
                .filter(v -> v.isReady() && v.getAssignedResource() == ResourceType.GOLD)
                .count();
            long gatheringFood = state.getVillagers().stream()
                .filter(v -> v.isReady() && v.getAssignedResource() == ResourceType.FOOD)
                .count();
            
            System.err.println("  Aldeanos recolectando oro: " + gatheringGold);
            System.err.println("  Aldeanos recolectando comida: " + gatheringFood);
            System.err.println("\n⚠️ Abortando simulación para evitar crash.");
            return;  // Salir del simulador
        }
    }
    
    System.out.println("✓ Requisitos cumplidos después de " + loopCount + " ticks");
    state.advanceAge(Age.CASTLE_AGE);
    simulate(160);
    System.out.println("✓ Castle Age alcanzada en t=" + state.getCurrentTime() + "s\n");
}
            
            case "GATHER_RESOURCES" -> {
                simulate(10);
            }
            
            case "RESEARCH_ARADO" -> {
                if (state.canResearch(TechnologyType.ARADO)) {
                    state.researchTechnology(TechnologyType.ARADO);
                    simulate(40);
                    System.out.println("  ✓ Arado investigado (+15% comida)");
                }
            }
            
            case "RESEARCH_SIERRA_DOBLE" -> {
                if (state.canResearch(TechnologyType.SIERRA_DOBLE)) {
                    state.researchTechnology(TechnologyType.SIERRA_DOBLE);
                    simulate(40);
                    System.out.println("  ✓ Sierra Doble investigada (+20% madera)");
                }
            }
            
            default -> System.out.println("  ⚠ Acción desconocida: " + action);
        }  // ← CIERRA SWITCH AQUÍ
        
        // ESTAS LÍNEAS VAN FUERA DEL SWITCH:
        System.out.println("  Aldeanos: " + state.getVillagers().size() 
            + " | Distribución: F=" + state.getVillagersOnFood() 
            + " W=" + state.getVillagersOnWood() 
            + " G=" + state.getVillagersOnGold());
        
        System.out.println("  Estado: " + state);
    }

    private void simulateUntilVillagerReady() {
        for (int i = 0; i < 25; i++) {
            state.tick();
        }
    }

    private void simulateConstruction(int duration) {
        for (int i = 0; i < duration; i++) {
            state.tick();
        }
    }

    private void simulate(int seconds) {
        for (int i = 0; i < seconds; i++) {
            state.tick();
        }
    }

    private void logEvent(String action) {
        eventLog.add(String.format("[t=%ds] %s", state.getCurrentTime(), action));
    }

    private void printMetrics() {
        System.out.println("\n=== Métricas de Evaluación ===");
        System.out.println("Tiempo total: " + state.getCurrentTime() + " segundos (" +
                         (state.getCurrentTime() / 60.0) + " minutos)");
        System.out.println("Edad alcanzada: " + state.getCurrentAge().getDisplayName());
        System.out.println("Recursos finales: " + state.getResources());
        System.out.println("Población: " + state.getPopulation() + "/" + state.getPopulationCapacity());
        System.out.println("Aldeanos: " + state.getVillagers().size());
        System.out.println("Edificios: " + state.getBuildings().size());
        System.out.println("Traslados de aldeanos: " + state.getVillagerTransfers());
        System.out.println("Tiempo de ociosidad del TC: " + state.getTownCenterIdleTime() + " segundos");
        
        boolean success = state.hasReachedCastleAge() && state.getCurrentTime() <= 900;
        System.out.println("\n¿Criterio de éxito cumplido? " + (success ? "SÍ ✓" : "NO ✗"));
        System.out.println("(Objetivo: Edad de los Castillos en ≤ 15 minutos)");
    }

    public GameState getState() {
        return state;
    }

    public List<String> getEventLog() {
        return eventLog;
    }
}
