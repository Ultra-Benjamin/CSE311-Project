package ub.cse.algo;

import java.util.ArrayList;
import java.util.HashMap;
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
        HashMap<Integer, List<Integer>> routes = new HashMap<Integer,List<Integer>>();
        HashMap<Integer,Integer> discovered = new HashMap<Integer, Integer>();
        /* TODO: Your solution goes here */
        PriorityQueue<Client> q = new PriorityQueue<>((x, y) -> Float.compare(y.payment, x.payment));
        q.addAll(this.info.clients);
        while(!q.isEmpty()){
            Client node = q.remove();


            for (int i = 1; i < graph.get(node).size(); i++) {

                int child = graph.get(node).get(i);
                int dist = discovered.get(node) + graph.get(child).get(0);


                if (!discovered.containsKey(child)|| dist < discovered.get(child)) {
                    discovered.put(child, dist);
                    parent.put(child, node);
                    q.add(new Client[]{child, dist});
                }
            }
            br
        }

        if(discovered.containsKey(_endNode)){
            int cur = _endNode;
            while (cur != _startNode){
                minpath.addFirst(cur);
                cur=parent.get(cur);
            }
            minpath.addFirst(cur);
            return minpath;
        }

        return sol;
    }
}
