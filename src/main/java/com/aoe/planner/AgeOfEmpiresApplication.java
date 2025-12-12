package com.aoe.planner;

import com.aoe.planner.evaluation.*;
import com.aoe.planner.model.*;
import com.aoe.planner.simulation.DiscreteEventSimulator;
import com.aoe.planner.solver.*;
import com.aoe.planner.util.Metrics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.*;

@SpringBootApplication
public class AgeOfEmpiresApplication implements CommandLineRunner {
    
    public static void main(String[] args) {
        SpringApplication.run(AgeOfEmpiresApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==============================================");
        System.out.println("  Age of Empires - RCPSP Optimizer (CP-SAT)");
        System.out.println("  Objetivo: Alcanzar Castle Age en ≤15 min");
        System.out.println("==============================================\n");
        
        // FASE 1: Resolver con CP-SAT
        System.out.println(">>> FASE 1: Generando solución óptima con CP-SAT");
        RCPSPModel rcpspModel = new RCPSPModel(1200); // 15 minutos máximo
        rcpspModel.buildModel();
        
        CPSATSolver solver = new CPSATSolver(rcpspModel);
        List<String> optimalPlan = solver.solve();
        
        if (optimalPlan.isEmpty()) {
            System.out.println("\n✗ No se pudo encontrar plan óptimo.");
            System.out.println("Abortando ejecución.");
            return;
        }
        
        // FASE 2: Simular el plan óptimo
        System.out.println("\n\n>>> FASE 2: Simulando plan óptimo");
        GameState simState = new GameState();
        DiscreteEventSimulator simulator = new DiscreteEventSimulator(simState);
        simulator.executePlan(optimalPlan);
        
        GameState optimalState = simulator.getState();
        boolean success = optimalState.hasReachedCastleAge() && 
                         optimalState.getCurrentTime() <= 900;

        Metrics optimalMetrics = new Metrics(optimalState);

        System.out.println("\n--- RESULTADO PLAN ÓPTIMO ---");
        System.out.println("Estado: " + (success ? "✓ ÉXITO" : "✗ FALLÓ"));
        System.out.println("Tiempo: " + optimalState.getCurrentTime() + "s (" + optimalMetrics.formatTime(optimalState.getCurrentTime())  + ")");
        System.out.println("Edad: " + optimalState.getCurrentAge().getDisplayName());
        System.out.println("Recursos finales: " + optimalState.getResources());

        optimalMetrics.print();
        /*
        // FASE 3: Comparar con builds profesionales
        System.out.println("\n\n>>> FASE 3: Comparando con builds profesionales");
        
        compareBuildOrder("22 Aldeanos a Feudal", 
                         BuildOrderLibrary.getBuildOrder_22Feudal(),
                         optimalState.getCurrentTime());
        
        compareBuildOrder("2 Campamentos Madereros", 
                         BuildOrderLibrary.getBuildOrder_2Lumber(),
                         optimalState.getCurrentTime());
        
        compareBuildOrder("25 Aldeanos", 
                         BuildOrderLibrary.getBuildOrder_25Population(),
                         optimalState.getCurrentTime());
        */
        // RESUMEN FINAL
        System.out.println("\n==============================================");
        System.out.println("  RESUMEN FINAL");
        System.out.println("==============================================");
        System.out.println("Plan CP-SAT (óptimo): " + optimalMetrics.formatTime(optimalState.getCurrentTime()) + "s");
        System.out.println("Objetivo cumplido: " + (success ? "✓ SÍ" : "✗ NO"));
        System.out.println("==============================================");
    }
    /*
    private void compareBuildOrder(String name, List<BuildAction> buildActions, long optimalTime) {
        System.out.println("\n--- Evaluando: " + name + " ---");
        
        List<String> plan = convertToExecutableActions(buildActions);
        
        GameState simState = new GameState();
        DiscreteEventSimulator simulator = new DiscreteEventSimulator(simState);
        simulator.executePlan(plan);
        
        GameState finalState = simulator.getState();
        long buildTime = finalState.getCurrentTime();
        
        String comparison = buildTime <= optimalTime ? "✓ Mejor o igual" : 
                           "✗ Peor (" + (buildTime - optimalTime) + "s más lento)";
        
        System.out.println("Tiempo: " + buildTime + "s (" + 
                         String.format("%.2f", buildTime/60.0) + " min)");
        System.out.println("Comparación con óptimo: " + comparison);
        System.out.println("Edad alcanzada: " + finalState.getCurrentAge().getDisplayName());
    } */
    
    private List<String> convertToExecutableActions(List<BuildAction> buildActions) {
        List<String> executableActions = new ArrayList<>();
        
        for (BuildAction ba : buildActions) {
            String action = ba.getAction();
            
            switch (action) {
                case "CREATE_VILLAGER" -> executableActions.add("CREATE_VILLAGER");
                case "BUILD_HOUSE" -> executableActions.add("BUILD_HOUSE");
                case "BUILD_MILL" -> executableActions.add("BUILD_MILL");
                case "BUILD_LUMBER_CAMP" -> executableActions.add("BUILD_LUMBER_CAMP");
                case "BUILD_MINING_CAMP" -> executableActions.add("BUILD_MINING_CAMP");
                case "BUILD_BARRACKS" -> executableActions.add("BUILD_BARRACKS");
                case "BUILD_MARKET" -> executableActions.add("BUILD_MARKET");
                case "BUILD_BLACKSMITH" -> executableActions.add("BUILD_BLACKSMITH");
                case "ADVANCE_FEUDAL" -> executableActions.add("ADVANCE_FEUDAL");
                case "ADVANCE_CASTLE" -> executableActions.add("ADVANCE_CASTLE");
                case "GATHER_RESOURCES" -> executableActions.add("GATHER_RESOURCES");
            }
        }
        
        return executableActions;
    }
}
