package com.aoe.planner.algorithm;

import com.aoe.planner.model.Task;
import com.aoe.planner.model.ResourceConstraint;
import java.util.*;

public class PrecedenceGraph {
    private Map<String, Task> tasks;
    private Map<String, List<String>> adjacencyList;
    private List<ResourceConstraint> resourceConstraints;
    
    public PrecedenceGraph() {
        this.tasks = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.resourceConstraints = new ArrayList<>();
    }
    
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        adjacencyList.putIfAbsent(task.getId(), new ArrayList<>());
        
        for (String predId : task.getPredecessorIds()) {
            adjacencyList.putIfAbsent(predId, new ArrayList<>());
            adjacencyList.get(predId).add(task.getId());
        }
    }
    
    public void addResourceConstraint(ResourceConstraint constraint) {
        resourceConstraints.add(constraint);
    }
    
    public List<Task> getAvailableTasks(Set<String> completedTasks) {
        List<Task> available = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (!task.isCompleted() && task.canExecute(completedTasks)) {
                available.add(task);
            }
        }
        return available;
    }
    
    public List<Task> getTasksRequiringTC() {
        List<Task> tcTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (task.requiresTownCenter()) {
                tcTasks.add(task);
            }
        }
        return tcTasks;
    }
    
    public List<Task> getTopologicalOrder() {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String taskId : tasks.keySet()) {
            inDegree.put(taskId, 0);
        }
        
        for (Task task : tasks.values()) {
            for (String predId : task.getPredecessorIds()) {
                inDegree.put(task.getId(), inDegree.get(task.getId()) + 1);
            }
        }
        
        Queue<String> queue = new LinkedList<>();
        for (String taskId : inDegree.keySet()) {
            if (inDegree.get(taskId) == 0) {
                queue.add(taskId);
            }
        }
        
        List<Task> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String taskId = queue.poll();
            sorted.add(tasks.get(taskId));
            
            for (String successor : adjacencyList.getOrDefault(taskId, new ArrayList<>())) {
                inDegree.put(successor, inDegree.get(successor) - 1);
                if (inDegree.get(successor) == 0) {
                    queue.add(successor);
                }
            }
        }
        
        return sorted;
    }
    
    public Task getTask(String taskId) {
        return tasks.get(taskId);
    }
    
    public Collection<Task> getAllTasks() {
        return tasks.values();
    }
    
    public Map<String, List<String>> getAdjacencyList() {
        return adjacencyList;
    }
    
    public List<ResourceConstraint> getResourceConstraints() {
        return resourceConstraints;
    }
    
    public int size() {
        return tasks.size();
    }
}