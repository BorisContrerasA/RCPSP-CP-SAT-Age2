package com.aoe.planner.model;

import java.util.*;

public class GameState implements Cloneable {

    private int currentTime;
    private Resource resources;
    private Age currentAge;
    private List<Villager> villagers;
    private List<Building> buildings;
    private int population;
    private int populationCapacity;
    private boolean isTownCenterBusy;
    private int townCenterBusyUntil;
    private Set<String> completedTasks;
    private int villagerTransfers;
    private int townCenterIdleTime;
    private List<Technology> technologies;
    private Set<TechnologyType> researchedTechnologies;
    private int villagersOnFood;
    private int villagersOnWood;
    private int villagersOnGold;

    private int feudalAgeTime = -1;
    private int castleAgeTime = -1;
    private int ticksAtPopCap = 0;
    private double foodAccumulator = 0;
    private double woodAccumulator = 0;
    private double goldAccumulator = 0;

    public GameState() {
        this.currentTime = 0;
        this.resources = new Resource(200, 200, 100);
        this.currentAge = Age.DARK_AGE;
        this.villagers = new ArrayList<>();
        this.buildings = new ArrayList<>();
        this.population = 3;
        this.populationCapacity = 5;
        this.isTownCenterBusy = false;
        this.townCenterBusyUntil = 0;
        this.completedTasks = new HashSet<>();
        this.villagerTransfers = 0;
        this.townCenterIdleTime = 0;
        this.technologies = new ArrayList<>();
        this.researchedTechnologies = new HashSet<>();
        this.villagersOnFood = 3;
        this.villagersOnWood = 0;
        this.villagersOnGold = 0;

        technologies.add(new Technology(TechnologyType.ARADO, 40, new Resource(125, 75, 0)));
        technologies.add(new Technology(TechnologyType.SIERRA_DOBLE, 40, new Resource(50, 100, 0)));

        Building tc = new Building(BuildingType.TOWN_CENTER);
        buildings.add(tc);

        for (int i = 0; i < 3; i++) {
            Villager v = new Villager(i);
            v.assignTo(ResourceType.FOOD, 0);
            
            // Simular que ya llegaron (25 ticks)
            for (int j = 0; j < 25; j++) {
                v.tick();
            }
            
            villagers.add(v);
        }
    }

    @Override
    public GameState clone() {
        try {
            GameState cloned = (GameState) super.clone();
            cloned.resources = this.resources.copy();
            cloned.villagers = new ArrayList<>(this.villagers.size());
            cloned.buildings = new ArrayList<>(this.buildings.size());
            cloned.completedTasks = new HashSet<>(this.completedTasks);
            cloned.villagersOnFood = this.villagersOnFood;
            cloned.villagersOnWood = this.villagersOnWood;
            cloned.villagersOnGold = this.villagersOnGold;

            for (Villager v : this.villagers) {
                cloned.villagers.add(v);
            }

            for (Building b : this.buildings) {
                cloned.buildings.add(b);
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error clonando GameState", e);
        }
    }

    public boolean canResearch(TechnologyType tech) {
        if (researchedTechnologies.contains(tech)) return false;
        Technology t = technologies.stream().filter(x -> x.getType() == tech).findFirst().orElse(null);
        if (t == null) return false;

        switch (tech) {
            case ARADO:
                return currentAge == Age.FEUDAL_AGE && hasBuilding(BuildingType.MILL) && resources.hasEnough(t.getCost());
            case SIERRA_DOBLE:
                return currentAge == Age.FEUDAL_AGE && hasBuilding(BuildingType.LUMBER_CAMP) && resources.hasEnough(t.getCost());
            case LOOM:
                return resources.hasEnough(t.getCost()) && !isTownCenterBusy;
            default:
                return false;
        }
    }

    private boolean hasBuilding(BuildingType type) {
        return buildings.stream().anyMatch(b -> b.isComplete() && b.getType() == type);
    }

    public void researchTechnology(TechnologyType tech) {
        if (!canResearch(tech)) return;
        Technology t = technologies.stream().filter(x -> x.getType() == tech).findFirst().orElse(null);
        if (t == null) return;

        resources.subtract(t.getCost());
        t.setResearched(true);
        researchedTechnologies.add(tech);

        if (tech == TechnologyType.LOOM) {
            this.isTownCenterBusy = true;
        }
    }

    public void tick() {
        currentTime++;

        if (population >= populationCapacity) {
            ticksAtPopCap++;
        }
        /*
        if(currentTime % 50 == 0){
            int gathering = 0;
            for(Villager v : villagers){
                if(v.isGathering()){
                    gathering++;
                }
            }
            System.out.println("DEBUG: Tick " + currentTime + " | Aldeanos recolectando: " + gathering + "/" + villagers.size());
        }*/
        double foodRate = 0.35;
        double woodRate = 0.39;
        double goldRate = 0.38;

        if (researchedTechnologies.contains(TechnologyType.ARADO) && currentAge != Age.DARK_AGE) {
            foodRate *= 1.15;
        }

        if (researchedTechnologies.contains(TechnologyType.SIERRA_DOBLE)) {
            woodRate *= 1.20;
        }

        double foodGathered = 0;
        double woodGathered = 0;
        double goldGathered = 0;

        for (Villager v : villagers) {
            if (v.isGathering()) {
                switch (v.getAssignedResource()) {
                    case FOOD -> foodGathered += foodRate;
                    case WOOD -> woodGathered += woodRate;
                    case GOLD -> goldGathered += goldRate;
                }
            }
            v.tick();
        }

        foodAccumulator += foodGathered;
        woodAccumulator += woodGathered;
        goldAccumulator += goldGathered;

        int foodToAdd = (int) foodAccumulator;
        int woodToAdd = (int) woodAccumulator;
        int goldToAdd = (int) goldAccumulator;

        foodAccumulator -= foodToAdd;
        woodAccumulator -= woodToAdd;
        goldAccumulator -= goldToAdd;

        villagerTransfers += (int) (foodAccumulator / 10);
        foodAccumulator = foodAccumulator % 10;
        villagerTransfers += (int) (woodAccumulator / 10);
        woodAccumulator = woodAccumulator % 10;
        villagerTransfers += (int) (goldAccumulator / 10);
        goldAccumulator = goldAccumulator % 10;

        resources.addFood(foodToAdd);
        resources.addWood(woodToAdd);
        resources.addGold(goldToAdd);

        for (Building b : buildings) {
            b.tick();
        }

        if (isTownCenterBusy && currentTime >= townCenterBusyUntil) {
            isTownCenterBusy = false;
        }

        if (!isTownCenterBusy) {
            townCenterIdleTime++;
        }
    }

    public boolean canCreateVillager() {
        return !isTownCenterBusy &&
               population < populationCapacity &&
               resources.hasEnough(new Resource(50, 0, 0));
    }

    public void createVillager() {
        if (!canCreateVillager()) return;

        resources.subtract(new Resource(50, 0, 0));
        Villager newVillager = new Villager(villagers.size());
        villagers.add(newVillager);
        population++;

        System.out.println("DEBUG: Creando aldeano #" + villagers.size() +
                         " en edad " + currentAge +
                         " | F/W/G actual: " + villagersOnFood + "/" + villagersOnWood + "/" + villagersOnGold);

        if (currentAge == Age.DARK_AGE) {
            if (villagersOnFood < 10) {
                villagersOnFood++;
                newVillager.assignTo(ResourceType.FOOD, 5);
                villagerTransfers++;
                System.out.println("  → Asignado a COMIDA (total: " + villagersOnFood + ")");
            } else if (villagersOnWood < 5) {
                villagersOnWood++;
                newVillager.assignTo(ResourceType.WOOD, 5);
                villagerTransfers++;
                System.out.println("  → Asignado a MADERA (total: " + villagersOnWood + ")");
            } else {
                villagersOnFood++;
                newVillager.assignTo(ResourceType.FOOD, 5);
                villagerTransfers++;
                System.out.println("  → Asignado a COMIDA (excedente, total: " + villagersOnFood + ")");
            }
        }  else if (currentAge == Age.FEUDAL_AGE) {
            if (villagersOnFood < 12) {  
                villagersOnFood++;
                newVillager.assignTo(ResourceType.FOOD, 5);
                villagerTransfers++;
    
            } else if (villagersOnGold < 5) {  
                villagersOnGold++;
                newVillager.assignTo(ResourceType.GOLD, 5);
                villagerTransfers++;
            } else if (villagersOnWood < 3) {  
                villagersOnWood++;
                newVillager.assignTo(ResourceType.WOOD, 5);
                villagerTransfers++;
            } else {
                // Balancear entre comida y oro
                if (villagersOnFood >= villagersOnGold) {
                    villagersOnGold++;
                    newVillager.assignTo(ResourceType.GOLD, 5);
                    villagerTransfers++;
                } else {
                    villagersOnFood++;
                    newVillager.assignTo(ResourceType.FOOD, 5);
                    villagerTransfers++;
                }
        }
        isTownCenterBusy = true;
        townCenterBusyUntil = currentTime + 25;
    }
}
    public void redistributeVillagersForAge() {
    if (currentAge == Age.FEUDAL_AGE) {
        System.out.println("🔄 Reasignando aldeanos para Feudal Age...");
        System.out.println("   Antes: F=" + villagersOnFood + " W=" + villagersOnWood + " G=" + villagersOnGold);
        
        // DEBUG: Verificar distribución real de aldeanos
        int realFood = 0, realWood = 0, realGold = 0;
        for (Villager v : villagers) {
            if (v.getAssignedResource() != null) {
                switch (v.getAssignedResource()) {
                    case FOOD -> realFood++;
                    case WOOD -> realWood++;
                    case GOLD -> realGold++;
                }
            }
        }
        System.out.println("   Real: F=" + realFood + " W=" + realWood + " G=" + realGold);
        
        int foodTarget = 13;
        int goldTarget = 3;
        int woodTarget = 2;
        
        // 1. Asegurar el oro (prioridad alta)
        while (villagersOnGold < goldTarget) {
            boolean found = false;
            
            // Intentar desde comida si hay exceso
            if (villagersOnFood > foodTarget) {
                for (Villager v : villagers) {
                    if (v.isReady() && v.getAssignedResource() == ResourceType.FOOD) {
                        v.assignTo(ResourceType.GOLD, 5);
                        villagersOnFood--;
                        villagersOnGold++;
                        villagerTransfers++;
                        System.out.println("   → Aldeano #" + v.getId() + " reasignado: COMIDA → ORO");
                        found = true;
                        break;
                    }
                }
            }
            
            // Si no, tomar de madera si hay exceso
            if (!found && villagersOnWood > woodTarget) {
                for (Villager v : villagers) {
                    if (v.isReady() && v.getAssignedResource() == ResourceType.WOOD) {
                        v.assignTo(ResourceType.GOLD, 5);
                        villagersOnWood--;
                        villagersOnGold++;
                        villagerTransfers++;
                        System.out.println("   → Aldeano #" + v.getId() + " reasignado: MADERA → ORO");
                        found = true;
                        break;
                    }
                }
            }
            
            // Forzar desde comida aunque no tengamos el target (mantener mínimo 13)
            if (!found && villagersOnFood > 9) {
                for (Villager v : villagers) {
                    if (v.isReady() && v.getAssignedResource() == ResourceType.FOOD) {
                        v.assignTo(ResourceType.GOLD, 5);
                        villagersOnFood--;
                        villagersOnGold++;
                        villagerTransfers++;
                        System.out.println("   → Aldeano #" + v.getId() + " reasignado (forzado): COMIDA → ORO");
                        found = true;
                        break;
                    }
                }
            }
            
            if (!found) {
                System.out.println("    No hay aldeanos disponibles para reasignar a oro");
                break;
            }
        }
        
        // 2. Ajustar madera si hay exceso
        while (villagersOnWood > woodTarget) {
            boolean found = false;
            for (Villager v : villagers) {
                if (v.isReady() && v.getAssignedResource() == ResourceType.WOOD) {
                    v.assignTo(ResourceType.FOOD, 5);
                    villagersOnWood--;
                    villagersOnFood++;
                    villagerTransfers++;
                    System.out.println("   → Aldeano #" + v.getId() + " reasignado: MADERA → COMIDA");
                    found = true;
                    break;
                }
            }
            if (!found) break;
        }
        
        System.out.println("   Después: F=" + villagersOnFood + " W=" + villagersOnWood + " G=" + villagersOnGold);
        System.out.println("   (Objetivo: F=" + foodTarget + " W=" + woodTarget + " G=" + goldTarget + ")");
        
        // DEBUG: Verificar distribución real después
        realFood = 0; realWood = 0; realGold = 0;
        for (Villager v : villagers) {
            if (v.getAssignedResource() != null) {
                switch (v.getAssignedResource()) {
                    case FOOD -> realFood++;
                    case WOOD -> realWood++;
                    case GOLD -> realGold++;
                }
            }
        }
        System.out.println("   Real después: F=" + realFood + " W=" + realWood + " G=" + realGold);
    }
}


    public void advanceAge(Age targetAge) {
        if (!canAdvanceAge(targetAge)) return;

        if (targetAge == Age.FEUDAL_AGE) {
            resources.subtract(new Resource(500, 0, 0));
            currentAge = Age.FEUDAL_AGE;
            feudalAgeTime = currentTime;
            redistributeVillagersForAge();
        } else if (targetAge == Age.CASTLE_AGE) {
            resources.subtract(new Resource(800, 0, 200));
            currentAge = Age.CASTLE_AGE;
            castleAgeTime = currentTime;
        }
    }

    public boolean canAdvanceAge(Age targetAge) {
        if (targetAge == Age.FEUDAL_AGE) {
            return currentAge == Age.DARK_AGE && resources.getFood() >= 500 && hasRequiredBuildingsForFeudal();
        } else if (targetAge == Age.CASTLE_AGE) {
            return currentAge == Age.FEUDAL_AGE && resources.getFood() >= 800 && resources.getGold() >= 200 && hasRequiredBuildingsForCastle();
        }
        return false;
    }

    public boolean hasRequiredBuildingsForFeudal() {
        long darkAgeBuildings = buildings.stream()
            .filter(Building::isComplete)
            .filter(b -> b.getType() == BuildingType.MILL || b.getType() == BuildingType.LUMBER_CAMP)
            .count();
        return darkAgeBuildings >= 2;
    }

    private boolean hasRequiredBuildingsForCastle() {
        boolean hasBlacksmith = buildings.stream()
            .anyMatch(b -> b.isComplete() && b.getType() == BuildingType.BLACKSMITH);
        boolean hasMarket = buildings.stream()
            .anyMatch(b -> b.isComplete() && b.getType() == BuildingType.MARKET);
        return hasBlacksmith && hasMarket;
    }

    public boolean canBuild(BuildingType type) {
        if (type == BuildingType.MILL || type == BuildingType.LUMBER_CAMP) {
            return currentAge == Age.DARK_AGE;
        }
        if (type == BuildingType.MARKET || type == BuildingType.BLACKSMITH) {
            return currentAge == Age.FEUDAL_AGE;
        }
        if (type == BuildingType.HOUSE) {
            return true;
        }
        return true;
    }

    public void buildBuilding(BuildingType type) {
        if (!canBuild(type)) return;
        
        Building building = new Building(type);
        buildings.add(building);

        if (type == BuildingType.HOUSE) {
            populationCapacity += 5;
        }
    }

    public void assignVillagerTo(ResourceType resourceType) {
        for (Villager v : villagers) {
            if (v.isReady() && v.getAssignedResource() == null) {
                v.assignTo(resourceType, 5);
                break;
            }
        }
    }

    public void reassignVillager(int villagerId, ResourceType newResource) {
        if (villagerId < villagers.size()) {
            Villager v = villagers.get(villagerId);
            
            if (v.getAssignedResource() != null) {
                switch (v.getAssignedResource()) {
                    case FOOD -> villagersOnFood--;
                    case WOOD -> villagersOnWood--;
                    case GOLD -> villagersOnGold--;
                }
            }
            
            v.assignTo(newResource, 5);
            
            switch (newResource) {
                case FOOD -> villagersOnFood++;
                case WOOD -> villagersOnWood++;
                case GOLD -> villagersOnGold++;
            }
            
            villagerTransfers++;
        }
    }

    public boolean hasReachedCastleAge() {
        return currentAge == Age.CASTLE_AGE;
    }

    // Getters
    public int getCurrentTime() { return currentTime; }
    public Resource getResources() { return resources; }
    public Age getCurrentAge() { return currentAge; }
    public List<Villager> getVillagers() { return villagers; }
    public List<Building> getBuildings() { return buildings; }
    public int getPopulation() { return population; }
    public int getPopulationCapacity() { return populationCapacity; }
    public Set<String> getCompletedTasks() { return completedTasks; }
    public int getVillagerTransfers() { return villagerTransfers; }
    public int getTownCenterIdleTime() { return townCenterIdleTime; }
    public List<Technology> getTechnologies() { return technologies; }
    public int getVillagersOnFood() { return villagersOnFood; }
    public int getVillagersOnWood() { return villagersOnWood; }
    public int getVillagersOnGold() { return villagersOnGold; }
    public int getFeudalAgeTime() { return feudalAgeTime; }
    public int getCastleAgeTime() { return castleAgeTime; }
    public int getTicksAtPopCap() { return ticksAtPopCap; }
    public double getPopulationCapReachedPercent() {
        if (currentTime == 0) return 0.0;
        return (ticksAtPopCap * 100.0) / currentTime;
    }

    @Override
    public String toString() {
        return String.format("GameState{time=%d, age=%s, resources=%s, pop=%d/%d, villagers=%d, buildings=%d}",
                           currentTime, currentAge, resources, population, populationCapacity,
                           villagers.size(), buildings.size());
    }
}
