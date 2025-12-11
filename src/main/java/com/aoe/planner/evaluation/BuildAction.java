
package com.aoe.planner.evaluation;

public class BuildAction {
    private String action;
    private int populationTarget;
    private String description;

    public BuildAction(String action, int populationTarget, String description) {
        this.action = action;
        this.populationTarget = populationTarget;
        this.description = description;
    }

    public String getAction() { return action; }
    public int getPopulationTarget() { return populationTarget; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[Pop %d] %s - %s", populationTarget, action, description);
    }
}
