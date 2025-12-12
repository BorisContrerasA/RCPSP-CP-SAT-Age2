package com.aoe.planner.util;

import com.aoe.planner.model.GameState;

public class Metrics {
    private int timeToFeudal;
    private int timeToCastle;
    private int townCenterIdleTime;
    private int villagerTransfers;
    private double populationCapReachedPercent;
    private int totalTime;
    
    public Metrics(GameState finalState) {
        this.totalTime = finalState.getCurrentTime();
        this.timeToFeudal = finalState.getFeudalAgeTime();
        this.timeToCastle = finalState.getCastleAgeTime();
        this.townCenterIdleTime = finalState.getTownCenterIdleTime();
        this.villagerTransfers = finalState.getVillagerTransfers();
        this.populationCapReachedPercent = finalState.getPopulationCapReachedPercent();
        // Otras métricas podrían calcularse durante la simulación
    }

    public String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d minutos con %02d segundos", minutes, seconds);
    }
    public void print() {
        System.out.println("\n=== Métricas Detalladas ===");
        System.out.println("Tiempo total: " + totalTime + "s (" + formatTime(totalTime) + ")");

        System.out.println("Tiempo a Edad Feudal: " + timeToFeudal + "s (" + formatTime(timeToFeudal) + ")");

        System.out.println("Tiempo a Edad de los Castillos: " + timeToCastle + "s (" + formatTime(timeToCastle) + ")");

        System.out.println("Ociosidad del TC: " + townCenterIdleTime + "s (" + formatTime(townCenterIdleTime) + ")");

        System.out.println("Traslados de aldeanos: " + villagerTransfers);

        System.out.println("% tiempo en límite de población: " + 
                         String.format("%.2f%%", populationCapReachedPercent));
    }
    
    // Getters
    public int getTimeToFeudal() { return timeToFeudal; }
    public int getTimeToCastle() { return timeToCastle; }
    public int getTownCenterIdleTime() { return townCenterIdleTime; }
    public int getVillagerTransfers() { return villagerTransfers; }
    public double getPopulationCapReachedPercent() { return populationCapReachedPercent; }
    public int getTotalTime() { return totalTime; }
    
    public void setTimeToFeudal(int time) { this.timeToFeudal = time; }
    public void setTimeToCastle(int time) { this.timeToCastle = time; }
}
