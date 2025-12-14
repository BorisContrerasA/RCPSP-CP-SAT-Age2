package com.aoe.planner.solver;

import com.aoe.planner.algorithm.PrecedenceGraph;
import com.aoe.planner.model.*;

public class RCPSPModel {

    private PrecedenceGraph graph;
    private int maxTime;
    private Resource initialResources;
    private int initialVillagers;

    public RCPSPModel(int maxTime) {
        this.graph = new PrecedenceGraph();
        this.maxTime = maxTime;
        this.initialResources = new Resource(200, 200, 100); // recursos iniciales estándar
        this.initialVillagers = 3;                           // 3 aldeanos iniciales
    }

    /**
     * Construye un modelo mínimo:
     * - Aldeanos 1–25
     * - 2 casas
     * - Molino + campamento maderero
     * - Avance a Feudal
     * - Mercado + herrería
     * - Avance a Castillos
     */
    public void buildModel() {
        // 1. Crear aldeanos 1–25 encadenados (TC exclusivo)
        createVillagerChain(13);

        // 2. Construir 2 casas
        createHouses(2);

        // 3. Construir Molino y Lumber Camp
        createDarkAgeEcoBuildings();

        // 4. Avanzar a Feudal
        Task feudalTask = createAdvanceFeudalTask();

        // 5. Construir Market + Blacksmith en Feudal
        createFeudalBuildings(feudalTask);

        // 6. Avanzar a Castle
        createAdvanceCastleTask();
    }

    /**
     * Aldeanos 1–25, cada uno depende del anterior y usa el TC
     */
    private void createVillagerChain(int count) {
        Task previous = null;
        for (int i = 1; i <= count; i++) {
            Task villager = new Task(
                    "villager_" + i,
                    TaskType.CREATE_VILLAGER,
                    25,                          // 25 s por aldeano
                    new Resource(50, 0, 0),      // 50 comida
                    true                         // requiere TC
            );

            if (previous != null) {
                villager.addPredecessor(previous.getId());
            }

            graph.addTask(villager);
            previous = villager;
        }
    }

    /**
     * Dos casas, con precedencias suaves (solo para dar orden mínimo)
     */
    private void createHouses(int count) {
        for (int i = 1; i <= count; i++) {
            Task house = new Task(
                    "house_" + i,
                    TaskType.BUILD_HOUSE,
                    15,
                    new Resource(0, 30, 0),   // 30 madera (aprox)
                    false
            );

            // Casa 1 después del aldeano 3, casa 2 después del aldeano 8
            if (i == 1) {
                house.addPredecessor("villager_3");
            } else if (i == 2) {
                house.addPredecessor("villager_8");
            }

            graph.addTask(house);
        }
    }

    /**
     * Molino y campamento maderero como edificios de Edad Oscura
     */
    private void createDarkAgeEcoBuildings() {
        Task mill = new Task(
                "mill",
                TaskType.BUILD_MILL,
                35,
                new Resource(0, 100, 0),
                false
        );
        // Construible después del aldeano 6
        mill.addPredecessor("villager_6");
        graph.addTask(mill);

        Task lumberCamp = new Task(
                "lumber_camp",
                TaskType.BUILD_LUMBER_CAMP,
                35,
                new Resource(0, 100, 0),
                false
        );
        lumberCamp.addPredecessor("villager_6");
        graph.addTask(lumberCamp);
    }

    /**
     * Avance a Feudal:
     * - Requiere Molino + Lumber Camp
     * - Requiere al menos aldeano_10 creado (no 21, para no hacerlo demasiado rígido)
     */
    private Task createAdvanceFeudalTask() {
        Task feudal = new Task(
                "advance_feudal",
                TaskType.ADVANCE_FEUDAL,
                130,                      // tiempo real de subida
                new Resource(500, 0, 0),  // 500 comida
                true                      // usa TC
        );

        feudal.addPredecessor("mill");
        feudal.addPredecessor("lumber_camp");
        feudal.addPredecessor("villager_10"); // mínimo 10 aldeanos antes de subir

        graph.addTask(feudal);
        return feudal;
    }

    /**
     * Edificios de Feudal necesarios para Castle:
     * - Market
     * - Blacksmith
     * Ambos después de llegar a Feudal
     */
    private void createFeudalBuildings(Task feudalTask) {
        Task market = new Task(
                "market",
                TaskType.BUILD_MARKET,
                60,
                new Resource(0, 100, 0),    // 100 madera
                false
        );
        market.addPredecessor(feudalTask.getId());
        graph.addTask(market);

        Task blacksmith = new Task(
                "blacksmith",
                TaskType.BUILD_BLACKSMITH,
                50,
                new Resource(0, 150, 0),    // 150 madera aprox
                false
        );
        blacksmith.addPredecessor(feudalTask.getId());
        graph.addTask(blacksmith);
    }

    private void createTechnologies(Task feudalTask) {
        // Arado: +15% eficiencia granjas
        // Requiere: Feudal Age + Molino
        Task researchArado = new Task(
            "research_arado",
            TaskType.RESEARCH_TECH,
            40,                           // 40s de investigación
            new Resource(125, 75, 0),     // 125 F, 75 W
            false                         // no usa TC
        );
        researchArado.addPredecessor(feudalTask.getId());
        researchArado.addPredecessor("mill");
        researchArado.addPredecessor("villager_12");  // tener economía mínima
        graph.addTask(researchArado);

        // Sierra Doble: +20% madera
        // Requiere: Feudal Age + Lumber Camp
        Task researchSierra = new Task(
            "research_sierra_doble",
            TaskType.RESEARCH_TECH,
            40,                           // 40s
            new Resource(50, 100, 0),     // 50 F, 100 W
            false
        );
        researchSierra.addPredecessor(feudalTask.getId());
        researchSierra.addPredecessor("lumber_camp");
        researchSierra.addPredecessor("villager_12");
        graph.addTask(researchSierra);
       
    }
    /**
     * Avance a Castle:
     * - Requiere Market + Blacksmith
     * - Requiere al menos aldeano_25 (boom razonable)
     */
    private void createAdvanceCastleTask() {
        Task castle = new Task(
                "advance_castle",
                TaskType.ADVANCE_CASTLE,
                160,                         // tiempo real de subida
                new Resource(800, 0, 200),   // 800 comida, 200 oro
                true                         // usa TC
        );

        castle.addPredecessor("market");
        castle.addPredecessor("blacksmith");
        castle.addPredecessor("villager_13");

        graph.addTask(castle);
    }

    // Getters básicos usados por CPSATSolver

    public PrecedenceGraph getGraph() {
        return graph;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public Resource getInitialResources() {
        return initialResources;
    }

    public int getInitialVillagers() {
        return initialVillagers;
    }
}
