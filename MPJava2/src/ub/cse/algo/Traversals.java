package ub.cse.algo;


import java.util.*;
import ub.cse.algo.Revenue;

import static ub.cse.algo.Revenue.pathRevenues;

class Traversals {

    /**
     * Finds the length of the shortest path to all client nodes
     * using BFS. The result is a HashMap with the keys being client
     * IDs and the values being the path length
     *
     * @param graph:   Graph object representing the network
     * @param clients: List of Clients
     * @return Map of Client IDs to the length of the shortest path
     */
    static HashMap<Integer, Integer> bfs(Graph graph, List<Client> clients) {
        // Initialize the distances array for all nodes in the graph
        int[] distances = new int[graph.size()];
        Arrays.fill(distances, -1);
        distances[graph.contentProvider] = 0;

        // Run BFS
        Queue<Integer> queue = new LinkedList<>();
        queue.add(graph.contentProvider);
        while (!queue.isEmpty()) {
            int node = queue.poll();

            for (int neighbor : graph.get(node)) {
                if (distances[neighbor] == -1) {
                    distances[neighbor] = distances[node] + 1;
                    queue.add(neighbor);
                }
            }
        }

        // Get all the distances to the clients only
        HashMap<Integer, Integer> clientDistances = new HashMap<>(clients.size());
        for (Client client : clients) {
            clientDistances.put(client.id, distances[client.id]);
        }
        return clientDistances;
    }

    /**
     * Finds the shortest path to all the clients using BFS
     *
     * @param graph:   Graph object representing the network
     * @param clients: List of the Clients
     * @return Map of Client IDs to an ArrayList representing
     * the path from ISP to Client
     */
    static HashMap<Integer, ArrayList<Integer>> bfsPaths(Graph graph, ArrayList<Client> clients) {
        /*
            Initialize the prior array with -1 for storing the node
            that is before the current one in the shortest paths
         */

        int maxIndex = Collections.max(graph.keySet());
        int[] priors = new int[maxIndex+1];
        Arrays.fill(priors, -1);

        // Run BFS, finding the nodes parent in the shortest path
        Queue<Integer> searchQueue = new LinkedList<>();
        searchQueue.add(graph.contentProvider);
        while (!searchQueue.isEmpty()) {
            int node = searchQueue.poll();
            for (int neighbor : graph.get(node)) {
                if (priors[neighbor] == -1 && neighbor != graph.contentProvider) {
                    priors[neighbor] = node;
                    searchQueue.add(neighbor);
                }
            }
        }

        // Get the final shortest paths
        return pathsFromPriors(clients, priors);
    }

    /**
     * Helper function to turn prior array to a Map of Paths
     *
     * @param clients: List of Clients
     * @param priors:  Array of prior IDs
     * @return Map of Client IDs to an ArrayList representing
     * the path created from the priors
     */
    private static HashMap<Integer, ArrayList<Integer>> pathsFromPriors(ArrayList<Client> clients, int[] priors) {
        HashMap<Integer, ArrayList<Integer>> paths = new HashMap<>(clients.size());
        // For every client, traverse the prior array, creating the path
        for (Client client : clients) {
            ArrayList<Integer> path = new ArrayList<>();
            int currentNode = client.id;
            while (currentNode != -1) {
                /*
                    Add this ID to the beginning of the
                    path so the path ends with the client
                 */
                path.add(0, currentNode);
                currentNode = priors[currentNode];
            }
            paths.put(client.id, path);
        }
        return paths;
    }

    //helper function that removes clients based on ID
    static ArrayList<Client> removeClients(ArrayList<Integer> IDS, ArrayList<Client> clients) {
        ArrayList<Client> out = new ArrayList<>();
        for (Client client : clients) {
            if (!IDS.contains(client.id)) {
                out.add(client);
            }
        }
        return out;
    }

    static Graph removeRouterFromGraph(Graph graph, Integer router){

        HashMap<Integer, ArrayList<Integer>> out = new HashMap<>();

        for(Integer node : graph.keySet()){
            if(node == router){ continue;}
            ArrayList<Integer> edges = graph.get(node);

            edges.remove(router);
            out.put(node, edges);
        }
        return new Graph(graph.contentProvider, out);
    }

    //keys correspond to a router,
    //values are a list of packets at the router given a certain index/time
    static HashMap<Integer, ArrayList<ArrayList<Integer>>> routerTraffic (HashMap<Integer, ArrayList<Integer>> bestPaths){
        HashMap<Integer, ArrayList<ArrayList<Integer>>> out = new HashMap<>();

        for(Integer client : bestPaths.keySet()){
            ArrayList<Integer> path = bestPaths.get(client);
            for(Integer time=0; time < path.size()-1; time++){
                Integer router = path.get(time);
                out.putIfAbsent(router, new ArrayList<>());

                ArrayList<ArrayList<Integer>> timeAndPackets = out.get(router);
                while(timeAndPackets.size() <= time){
                    timeAndPackets.add(new ArrayList<>());
                }
                timeAndPackets.get(time).add(client);
            }
        }
        return out;
    }

    static Client getClient(ArrayList<Client> clients, Integer clientID){
        for(Client client : clients){
            if(client.id == clientID){
                return client;
            }
        }
        return null;
    }

    /**
     * finds the optimal paths to each node based on maximum revenue
     *
     * @param clients:           List of Clients
     * @param graph:             Graph representing the current iteration of the network (will change after recursion)
     * @param initialBandwidths: list representing bandwidths of each router
     * @return Optimal path to each node
     */


    static HashMap<Integer, ArrayList<Integer>> bandwidthBFS(
            ArrayList<Client> clients, Graph graph, ArrayList<Integer> initialBandwidths) {

        HashMap<Integer, ArrayList<Integer>> bestPaths = bfsPaths(graph, clients);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> packetsAtRouters = routerTraffic(bestPaths);


        //check all routers for a conflict
        for (Integer router : graph.keySet()) {

            int bandwidth = initialBandwidths.get(router);
            ArrayList<ArrayList<Integer>> packetsAtRouter = packetsAtRouters.get(router);

            if(packetsAtRouter == null){ continue;}

            //check each the packets at a router for every clock cycle
            for(Integer time=0; time < packetsAtRouter.size(); time++){

                //this will be the packets at a router for a given time
                ArrayList<Integer> packets = packetsAtRouter.get(time);

                //if there is a conflict
                if(packets.size() > bandwidth){
                    ArrayList<Client> clientsWhoHaveUsedRouter = new ArrayList<>();
                    ArrayList<ArrayList<Integer>> pathsOfClientsWhoHaveUsedRouter = new ArrayList<>();

                    //check all packets at the time of conflict
                    for(Integer packet : packets){
                        clientsWhoHaveUsedRouter.add(getClient(clients, packet));
                        pathsOfClientsWhoHaveUsedRouter.add(bestPaths.get(packet));
                    }

                    TreeSet<ArrayList<Integer>> profitablePaths =
                            pathRevenues(clientsWhoHaveUsedRouter, pathsOfClientsWhoHaveUsedRouter, bestPaths);

                    // will be a hashmap of validated paths
                    HashMap<Integer, ArrayList<Integer>> toMerge = new HashMap<>();
                    //will be a list of packets that should be sent throught the router
                    ArrayList<Integer> priorityPackets = new ArrayList<>();

                    //send through as many profitable packets as the bandwidth allows
                    for(int i=0; i < initialBandwidths.get(router); i++){
                        ArrayList<Integer> path = profitablePaths.removeFirst();
                        int clientsID = path.get(path.size()-1);

                        toMerge.put(clientsID, path);
                        priorityPackets.add(clientsID);
                    }
                    //remove the congested router after conflicting packets have been rerouted
                    Graph remainingNetwork = removeRouterFromGraph(graph, router);

                    //add the resolved paths to the bandwidthBFS result of the rest of the graph
                    HashMap<Integer, ArrayList<Integer>> output = bandwidthBFS(clients,
                            remainingNetwork,
                            initialBandwidths);

                    output.putAll(toMerge);

                    return output;
                }
            }
        }
        return bestPaths;

    }
}