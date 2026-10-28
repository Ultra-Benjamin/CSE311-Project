package ub.cse.algo;

import java.util.ArrayList;
import java.util.*;

public class Solution {

    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;

    /**
     * Basic Constructor
     *
     * @param info: data parsed from input file
     */
    public Solution(Info info) {
        this.info = info;
        this.graph = info.graph;
        this.clients = info.clients;
        this.bandwidths = info.bandwidths;
    }

    /**
     * Method that returns the calculated 
     * SolutionObject as found by your algorithm
     *
     * @return SolutionObject containing the paths, priorities and bandwidths
     */
    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();
        /* TODO: Your solution goes here */

        //identify the source node i from the graph.
        int source = this.graph.contentProvider;

        // Initialize the Queue to run BFS and a map to track parents for the paths
        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> parents = new HashMap<>();

        // Since the source node is the root, it has no parents: parent = null
        queue.add(source);
        parents.put(source, null);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            // Get all adjacent neighbors, from adj list, of the node polled from Queue:
            ArrayList<Integer> neighbors = this.graph.getOrDefault(current, new ArrayList<>());
            if (neighbors != null) {
                for (int neighbor: neighbors){
                    // If neighbor is not yet discovered = found the shortest path
                    if(!parents.containsKey(neighbor)){
                        parents.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Reconstruct paths found from source to client
        HashMap<Integer, ArrayList<Integer>> paths = new HashMap();

        for (Client client : this.clients) {
            int clientID = client.id;
            ArrayList<Integer> path = new ArrayList<>();

            if (parents.containsKey(clientID)) {
                Integer current = clientID;
                while (current != null) {
                    path.add(0, current);
                    current = parents.get(current);
                }
            }
            paths.put(clientID, path);
            sol.priorities.put(client.id, client.priority);

        }

        sol.paths = paths;
        sol.bandwidths = this.bandwidths;

        // Print paths to debug
        /*System.out.println("\n---- Shortest Paths ----");
        for (Map.Entry<Integer, ArrayList<Integer>> entry : paths.entrySet()) {
            System.out.println("Client " + entry.getKey() + " Path: " + entry.getValue());
        }
        System.out.println("--------End--------");
        */

        return sol;
    }
}
