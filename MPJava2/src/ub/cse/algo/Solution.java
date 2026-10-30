package ub.cse.algo;

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
    public HashMap<Integer, Integer> BFSDistances(int contentProvider) {
        //identify the source node i from the graph.
        int source = contentProvider;

        // Initialize the Queue to run BFS and a map to track parents for the paths
        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> distances = new HashMap<>();

        // Since the source node is the root, it has no parents: parent = null
        queue.add(contentProvider);
        distances.put(contentProvider, 0);


        while (!queue.isEmpty()) {
            int current = queue.poll();
            int distance = distances.get(current);

            // Get all adjacent neighbors, from adj list, of the node polled from Queue:
            ArrayList<Integer> neighbors = this.graph.getOrDefault(current, new ArrayList<>());
            if (neighbors != null) {
                for (int neighbor: neighbors){
                    // If neighbor is not yet discovered = found the shortest path
                    if(!distances.containsKey(neighbor)){
                        distances.put(neighbor, distance + 1);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return distances;

    }

    private ArrayList<Integer> findValidPath(int start, int target, double maxDelay, ArrayList<Integer> remainingCapacities){
        Queue<Integer> queue = new LinkedList<>();
        HashMap<Integer, Integer> parents = new HashMap<>();
        HashMap<Integer, Integer> delay = new HashMap<>();

        queue.add(start);
        delay.put(start, 0);

        while (!queue.isEmpty()){
            int current = queue.poll();
            int curDelay = delay.get(current);

            if (current == target) {
                if (curDelay <= maxDelay){
                    LinkedList<Integer> path = new LinkedList<>();
                    int temp = target;
                    while (temp != start){
                        path.addFirst(temp);
                        temp = parents.get(temp);
                    }
                    path.addFirst(start);
                    return new ArrayList<>(path);
                }
                return null;
            }

            ArrayList<Integer> neighbors = this.graph.getOrDefault(current, new ArrayList<>());
            if(neighbors != null){
                for (int neighbor: neighbors){
                    if (remainingCapacities.get(neighbor) > 0 && !delay.containsKey(neighbor)) {
                        delay.put(neighbor, curDelay + 1);
                        parents.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();
        /* TODO: Your solution goes here */

        // Compute base distances, d(c), using BFS to calculate max allowed delay for clients
        HashMap<Integer, Integer> baseDistances = BFSDistances(this.graph.contentProvider);
        HashMap<Integer, ArrayList<Integer>> L = new HashMap<>();

        // Sort clients based on payments: highest to lowest, prioritizing high payment clients
        List<Client> sortedClients = new ArrayList<>(this.info.clients);
        sortedClients.sort((c1, c2)-> Float.compare(c2.payment, c1.payment));

        // Copy list of bandwiths into a list to manipulate
        ArrayList<Integer> remianingNodeCapacities = new ArrayList<>(this.bandwidths);

        // Run BFS for each client filtering out any nodes whose remaining capacity less than or equal to zero
        // If a path is found and the delay is less than or equal to the maximum allowed delay before incurring
        // a penalty:
        //      1. Add a path for that solution found
        //      2. Decrement node capacities along the path by 1
        for (Client client: sortedClients){
            int d_c = baseDistances.getOrDefault(client.id, Integer.MAX_VALUE);
            double maxDelay = client.alpha * d_c;
            sol.priorities.put(client.id, client.priority);
            ArrayList<Integer> path = findValidPath(this.graph.contentProvider, client.id, maxDelay, remianingNodeCapacities);

            if (path != null && !path.isEmpty()){
                L.put(client.id, path);
                for (int node: path) {
                    int currentBand = remianingNodeCapacities.get(node);
                    remianingNodeCapacities.set(node, currentBand - 1);
                }
            }
        }

        sol.paths = L;

        HashMap<Integer, ArrayList<ArrayList<Integer>>> trafficTimeline = ub.cse.algo.Traversals.routerTraffic(L);

        boolean valid = true;
        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> entry : trafficTimeline.entrySet()){
            int router = entry.getKey();
            ArrayList<ArrayList<Integer>> timeline = entry.getValue();
            int maxBandwith = this.bandwidths.get(router);

            for (int t = 0; t < timeline.size(); t++){
                int packetCount = timeline.get(t).size();
                if (packetCount > maxBandwith) {
                    System.err.println("Bandwith violation at the Router: " + router + "at tick: " + t + ": " + packetCount + "packets (Max: " + maxBandwith + ")");
                    valid = false;
                }
            }
        }

        sol.bandwidths = this.bandwidths;

        return sol;
    }
}
