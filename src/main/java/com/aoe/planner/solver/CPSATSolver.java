package com.aoe.planner.solver;

import com.aoe.planner.algorithm.PrecedenceGraph;
import com.aoe.planner.model.*;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.*;

public class CPSATSolver {
    
    private RCPSPModel rcpspModel;
    private CpModel model;
    private Map<String, IntVar> startTimes;
    private Map<String, IntVar> endTimes;
    private Map<String, IntervalVar> intervals;
    
    public CPSATSolver(RCPSPModel rcpspModel) {
        this.rcpspModel = rcpspModel;
        this.model = new CpModel();
        this.startTimes = new HashMap<>();
        this.endTimes = new HashMap<>();
        this.intervals = new HashMap<>();
    }
    
    public List<String> solve() {
        System.out.println("=== Inicializando CP-SAT Solver ===");
        
        // Cargar biblioteca nativa de OR-Tools
        Loader.loadNativeLibraries();
        
        PrecedenceGraph graph = rcpspModel.getGraph();
        int maxTime = rcpspModel.getMaxTime();
        
        System.out.println("Tareas totales: " + graph.size());
        System.out.println("Tiempo máximo: " + maxTime + " segundos");
        
        // 1. Crear variables de decisión
        createDecisionVariables(graph, maxTime);
        
        // 2. Agregar restricciones de precedencia
        addPrecedenceConstraints(graph);
        
        // 3. Agregar restricción de Town Center (no-overlap)
        addTownCenterConstraint(graph);
        
        // 4. Agregar restricciones de recursos
        addResourceConstraints(graph);
        
        // 5. Definir objetivo: minimizar makespan
        IntVar makespan = model.newIntVar(0, maxTime, "makespan");
        
        // El makespan debe ser mayor o igual al tiempo de fin de todas las tareas
        for (Task task : graph.getAllTasks()) {
            model.addLessOrEqual(endTimes.get(task.getId()), makespan);
        }
        
        // Minimizar makespan
        model.minimize(makespan);
        
        // 6. Resolver
        System.out.println("\n=== Resolviendo con CP-SAT ===");
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(60.0); // Timeout de 60 segundos
        solver.getParameters().setNumSearchWorkers(4); // Paralelización
        solver.getParameters().setLogSearchProgress(true);
        
        CpSolverStatus status = solver.solve(model);
        
        System.out.println("\n=== Resultado ===");
        System.out.println("Estado: " + status);
        
        if (status == CpSolverStatus.OPTIMAL) {
            System.out.println("✓ Solución ÓPTIMA encontrada");
        } else if (status == CpSolverStatus.FEASIBLE) {
            System.out.println("✓ Solución VIABLE encontrada (no necesariamente óptima)");
        } else {
            System.out.println("✗ No se encontró solución");
            return new ArrayList<>();
        }
        
        long optimalTime = solver.value(makespan);
        System.out.println("Tiempo total (makespan): " + optimalTime + " segundos (" + 
                         String.format("%.2f", optimalTime / 60.0) + " minutos)");
        
        // 7. Extraer plan de acciones
        List<String> plan = extractPlan(graph, solver);
        
        return plan;
    }
    
    private void createDecisionVariables(PrecedenceGraph graph, int maxTime) {
        System.out.println("\nCreando variables de decisión...");
        
        for (Task task : graph.getAllTasks()) {
            String id = task.getId();
            
            // Variable: tiempo de inicio
            IntVar start = model.newIntVar(0, maxTime, id + "_start");
            startTimes.put(id, start);
            
            // Variable: tiempo de fin
            IntVar end = model.newIntVar(0, maxTime, id + "_end");
            endTimes.put(id, end);
            
            // Restricción: end = start + duration
            model.addEquality(end, LinearExpr.sum(new LinearArgument[]{
                start,
                LinearExpr.constant(task.getDuration())
            }));
            
            // Crear intervalo para restricciones no-overlap
            IntervalVar interval = model.newIntervalVar(
                start,
                LinearExpr.constant(task.getDuration()),
                end,
                id + "_interval"
            );
            intervals.put(id, interval);
        }
        
        System.out.println("Variables creadas: " + (startTimes.size() * 2) + " (start + end)");
    }
    
    private void addPrecedenceConstraints(PrecedenceGraph graph) {
        System.out.println("Agregando restricciones de precedencia...");
        
        int constraintCount = 0;
        for (Task task : graph.getAllTasks()) {
            for (String predId : task.getPredecessorIds()) {
                // Restricción: start(task) >= end(predecessor)
                model.addGreaterOrEqual(
                    startTimes.get(task.getId()),
                    endTimes.get(predId)
                );
                constraintCount++;
            }
        }
        
        System.out.println("Restricciones de precedencia: " + constraintCount);
    }
    
    private void addTownCenterConstraint(PrecedenceGraph graph) {
        System.out.println("Agregando restricción de Town Center (no-overlap)...");
        
        List<Task> tcTasks = graph.getTasksRequiringTC();
        List<IntervalVar> tcIntervals = new ArrayList<>();
        
        for (Task task : tcTasks) {
            tcIntervals.add(intervals.get(task.getId()));
        }
        
        // Restricción: ninguna tarea del TC se solapa con otra
        model.addNoOverlap(tcIntervals);
        
        System.out.println("Tareas que usan TC: " + tcTasks.size());
    }
    
    private void addResourceConstraints(PrecedenceGraph graph) {
        System.out.println("Agregando restricciones de recursos...");
        
        // Simplificado: las restricciones de recursos se validan
        // durante la simulación posterior, no en el modelo CP-SAT
        // (para no complicar el modelo inicial)
        
        // En versión completa, modelarías:
        // - Acumulación de recursos en cada momento
        // - Consumo de recursos por tareas
        // - Balance: recursos_disponibles >= recursos_necesarios
        
        System.out.println("Restricciones de recursos: simplificadas (validación post-solver)");
    }
    
    private List<String> extractPlan(PrecedenceGraph graph, CpSolver solver) {
        System.out.println("\n=== Extrayendo Plan de Acciones ===");
        
        // Ordenar tareas por tiempo de inicio
        List<Task> sortedTasks = new ArrayList<>(graph.getAllTasks());
        sortedTasks.sort((a, b) -> Long.compare(
            solver.value(startTimes.get(a.getId())),
            solver.value(startTimes.get(b.getId()))
        ));
        
        List<String> plan = new ArrayList<>();
        
        System.out.println("\nSecuencia de tareas (ordenadas por tiempo):");
        for (Task task : sortedTasks) {
            long start = solver.value(startTimes.get(task.getId()));
            long end = solver.value(endTimes.get(task.getId()));
            
            System.out.println(String.format("  [%3ds - %3ds] %s", 
                                           start, end, task.getId()));
            
            String action = taskTypeToAction(task);
            if (action != null) {
                plan.add(action);
            }
        }
        
        System.out.println("\nAcciones totales en el plan: " + plan.size());
        
        return plan;
    }
    
    private String taskTypeToAction(Task task) {
        return switch (task.getType()) {
            case CREATE_VILLAGER -> "CREATE_VILLAGER";
            case BUILD_HOUSE -> "BUILD_HOUSE";
            case BUILD_MILL -> "BUILD_MILL";
            case BUILD_LUMBER_CAMP -> "BUILD_LUMBER_CAMP";
            case BUILD_MINING_CAMP -> "BUILD_MINING_CAMP";
            case BUILD_BARRACKS -> "BUILD_BARRACKS";
            case BUILD_MARKET -> "BUILD_MARKET";
            case BUILD_BLACKSMITH -> "BUILD_BLACKSMITH";
            case ADVANCE_FEUDAL -> "ADVANCE_FEUDAL";
            case ADVANCE_CASTLE -> "ADVANCE_CASTLE";
            case GATHER_FOOD, GATHER_WOOD, GATHER_GOLD -> "GATHER_RESOURCES";
            default -> null;
        };
    }
}
