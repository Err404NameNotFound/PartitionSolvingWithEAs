package main;

import help.ArrayHelp;

public class Solver {
    public final ISolver solver;
    public final String description;
    public final String parameter;
    public final String fullName;

    private Solver(String description, String parameter, String fullName, ISolver solver) {
        this.parameter = parameter;
        this.solver = solver;
        this.description = description;
        this.fullName = fullName;
    }

    public static Solver getRLS() {
        return new Solver("RLS", "-", "RLS", PartitionSolver::solveRLS);
    }

    public static Solver getRLSUniformNeighbour(int n) {
        return new Solver("RLS-N", "n=" + n, "RLS-N(" + n + ")",
                (a, b) -> PartitionSolver.solveRLS_UniformNeighbour(a, b, n));
    }

    public static Solver getRLSUniformRing(int r) {
        return new Solver("RLS-R", "r=" + r, "RLS-R(" + r + ")",
                (a, b) -> PartitionSolver.solveRLS_UniformRing(a, b, r));
    }

    public static Solver getEA() {
        return new Solver("EA", "-", "(1+1) EA(1/n)", PartitionSolver::solveEA);
    }

    public static Solver getEA(int k) {
        return new Solver("EA-SM", k + "/n", "(1+1) EA(" + k + "/n)",
                (a, b) -> PartitionSolver.solveEA(a, b, (double) k / a.length));
    }

    public static Solver getFmut(double p) {
        return new Solver("fmut", String.format("%.3f", p), "fmut(" + p + ")",
                (a, b) -> PartitionSolver.solveFmut(a, b, p));
    }

    public static Solver getPmut(double n) {
        return new Solver("pmut", String.format("%.2f", n), "pmut(" + n + ")",
                (a, b) -> PartitionSolver.solvePmut(a, b, n));
    }

    public static Solver[] getRLSComparison() {
        return new Solver[]{
                getRLS(),
                getRLSUniformNeighbour(2),
                getRLSUniformNeighbour(3),
                getRLSUniformNeighbour(4),
                getRLSUniformRing(2),
                getRLSUniformRing(3),
                getRLSUniformRing(4)
        };
    }

    public static Solver[] getRLSNeighbourComparison(int[] values) {
        Solver[] ret = new Solver[values.length];
        ArrayHelp.fill(ret, (i) -> getRLSUniformNeighbour(values[i]));
        return ret;
    }

    public static Solver[] getEAComparison() {
        return new Solver[]{
                getEA(),
                getEA(2),
                getEA(3),
                getEA(4),
                getEA(5),
                getEA(10),
//                getEA(50),
//                getEA(100),
        };
    }

    public static Solver[] getFmutComparison() {
        return new Solver[]{
                getFmut(0.1),
                getFmut(0.25),
                getFmut(0.5),
                getFmut(0.75),
                getFmut(0.9),
        };
    }

    public static Solver[] getPmutComparison() {
        return new Solver[]{
                getPmut(-1.25),
                getPmut(-1.5),
                getPmut(-1.75),
                getPmut(-2.0),
                getPmut(-2.25),
                getPmut(-2.5),
                getPmut(-2.75),
                getPmut(-3.0),
                getPmut(-3.25),
        };
    }

    public static Solver[] getComparison(int k_Ring, int k_Neighbour, int c_EA, double p_pmut) {
        return new Solver[]{
                getRLS(),
                getRLSUniformNeighbour(k_Neighbour),
                getRLSUniformRing(k_Ring),
                getEA(),
                getEA(c_EA),
                getPmut(p_pmut),
        };
    }

    public static Solver[] getComparison() {
        return new Solver[]{
                getRLS(),
                getRLSUniformRing(2),
                getRLSUniformNeighbour(2),
                getEA(),
                getEA(2),
                getPmut(-2.75)
        };
    }

    public Solution solve(long[] values, long maxSteps) {
        return solver.solve(values, maxSteps);
    }

    @Override
    public String toString() {
        return description + " with " + parameter;
    }

    public interface ISolver {
        Solution solve(long[] values, long maxSteps);
    }
}