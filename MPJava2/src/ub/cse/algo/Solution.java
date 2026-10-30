package ub.cse.algo;

import java.util.ArrayList;

import static ub.cse.algo.Traversals.bandwidthBFS;

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
        sol.paths = bandwidthBFS(this.clients, this.graph, this.bandwidths);
        sol.bandwidths = this.bandwidths;
        sol.priorities = null;
        return sol;
    }
}
