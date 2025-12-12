package com.aoe.planner.model;

public class Resource {
    private int food;
    private int wood;
    private int gold;
    
    public Resource() {
        this(0, 0, 0);
    }
    
    public Resource(int food, int wood, int gold) {
        this.food = food;
        this.wood = wood;
        this.gold = gold;
    }
    
    public Resource copy() {
        return new Resource(food, wood, gold);
    }
    
    public boolean hasEnough(Resource cost) {
        return this.food >= cost.food && 
               this.wood >= cost.wood && 
               this.gold >= cost.gold;
    }
    
    public void subtract(Resource cost) {
        this.food -= cost.food;
        this.wood -= cost.wood;
        this.gold -= cost.gold;
    }
    
    public void add(Resource amount) {
        this.food += amount.food;
        this.wood += amount.wood;
        this.gold += amount.gold;
    }
    
    public void addFood(int amount) { this.food += amount; }
    public void addWood(int amount) { this.wood += amount; }
    public void addGold(int amount) { this.gold += amount; }
    
    public int getFood() { return food; }
    public int getWood() { return wood; }
    public int getGold() { return gold; }
    
    @Override
    public String toString() {
        return String.format("Resource{Food: %d, Wood: %d, Gold: %d}", food, wood, gold);
    }
}
