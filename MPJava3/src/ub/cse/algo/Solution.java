
package ub.cse.algo;
import java.lang.reflect.Array;
import java.util.*;
public class Solution {
    private Info info;
    private Graph graph;
    private ArrayList<Client> clients;
    private ArrayList<Integer> bandwidths;
    public HashMap<Integer, Integer> parent = new HashMap<>();
    public HashMap<Integer, Integer> discovered = new HashMap<>();
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

    private static class NodeDist implements Comparable<NodeDist>{
        final int node;
        final int cost;

        NodeDist(int node, int cost){
            this.node = node;
            this.cost = cost;
        }

        @Override
        public int compareTo(NodeDist other){
            return Integer.compare(this.cost, other.cost);
        }
    }

    private ArrayList<Integer> findValidPath(int _startNode, int _endNode, double maxDelay, HashMap<Integer, Integer> usage){
        int num_of_nodes = this.graph.size();
        int[] dist = new int[num_of_nodes];
        int[] parents = new int[num_of_nodes];
        int[] packets = new int[num_of_nodes];
        boolean[] visited = new boolean[num_of_nodes];

        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[_startNode] = 0;
        packets[_startNode] = 0;
        PriorityQueue<NodeDist> q = new PriorityQueue<>();
        q.add(new NodeDist(_startNode, 0));
        while(!q.isEmpty()){
            NodeDist current = q.remove();
            int node = current.node;
            int cost = current.cost;
            if(node == _endNode){
                q.clear();
                break;
            }
            if (visited[node] || cost > dist[node]){
                continue;
            }
            visited[node] = true;
            ArrayList<Integer> neighbors = this.graph.get(node);
            if (neighbors == null) {
                continue;
            }

            int tpackets = packets[node] + 1;
            if (tpackets > maxDelay) {
                continue;
            }

            for (int i = 0, size = neighbors.size(); i < size; i++) {
                int child = neighbors.get(i);
                if(visited[child]){
                    continue;
                }
                int bandwidth = bandwidths.get(child);
                int used = usage.getOrDefault(child, 0);

                float congestion = 0f;

                if (child != _endNode && child != _startNode && bandwidth > 0) {
                    congestion = (float) used / bandwidth;
                }

                int newDist = cost + 1 + (int) Math.round(congestion);

                if(newDist < dist[child]){
                    dist[child] = newDist;
                    parents[child] = node;
                    packets[child] = tpackets;
                    q.add(new NodeDist(child, newDist));
                }
            }
        }
        if (dist[_endNode] != Integer.MAX_VALUE) {
            ArrayList<Integer> minPath = new ArrayList<>();
            int current = _endNode;
            while(current != _startNode){
                minPath.add(0, current);
                current = parents[current];
            }
            minPath.add(0,  _startNode);
            return minPath;
        }
        return null;
    }
    public SolutionObject outputPaths() {
        SolutionObject sol = new SolutionObject();
        /* TODO: Your solution goes here */
        // Compute base distances, d(c), using BFS to calculate delay limits for clients
        HashMap<Integer, Integer> baseDistances = BFSDistances(this.graph.contentProvider);
        HashMap<Integer, ArrayList<Integer>> L = new HashMap<>();
        // Sort clients based on payments: highest to lowest, prioritizing high payment clients
        List<Client> sortedClients = new ArrayList<>(this.info.clients);
        sortedClients.sort((c1, c2)-> Float.compare(c2.payment, c1.payment));
        // Copy list of bandwiths into a list to manipulate
        HashMap<Integer, Integer> usage = new HashMap<>();
        // Run Dijkstra's for each client filtering out any nodes whose remaining capacity less than or equal to zero
        // If a path is found and the delay is less than or equal to the maximum allowed delay before incurring
        // a penalty:
        //      1. Add a path for that solution found
        //      2. Decrement node capacities along the path by 1
        for (Client client: sortedClients){
            Integer d_c = baseDistances.get(client.id);
            if (d_c == null) continue;
            double maxDelay = client.alpha * d_c;
            sol.priorities.put(client.id, client.priority);
            ArrayList<Integer> path = findValidPath(this.graph.contentProvider, client.id, maxDelay, usage);
            if (path != null && !path.isEmpty()){
                L.put(client.id, path);
                for (int i = 1; i < path.size() - 1; i++) {
                    int router =  path.get(i);
                    usage.put(router, usage.getOrDefault(router, 0) + 1);
                }
            }
        }
        //Problem 1 output:
        //sol.paths = Traversals.bfsPaths(this.graph, this.clients);
        sol.paths = L;
        sol.bandwidths = this.bandwidths;
        return sol;
    }
}