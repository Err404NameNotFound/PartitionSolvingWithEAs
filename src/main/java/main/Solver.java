package main;

import help.ArrayHelp;

public class Solver {
    public final ISolver solver;
    public final String description;
    public final String parameter;
    public final String fullName;
    public final String shortName;

    private Solver(String description, String parameter, String fullName, String shortName, ISolver solver) {
        this.parameter = parameter;
        this.solver = solver;
        this.description = description;
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public static Solver getRLS() {
        return new Solver("RLS", "-", "RLS", "RLS", PartitionSolver::solveRLS);
    }

    public static Solver getRLSUniformNeighbour(int n) {
        if (n == 1) {
            return getRLS();
        }
        return new Solver("RLS-N", "n=" + n, "RLS-N (" + n + ")", "RLS-N" + n,
                (a, b) -> PartitionSolver.solveRLS_UniformNeighbour(a, b, n));
    }

    public static Solver getRLSUniformRing(int r) {
        if (r == 1) {
            return getRLS();
        }
        return new Solver("RLS-R", "r=" + r, "RLS-R (" + r + ")", "RLS-R" + r,
                (a, b) -> PartitionSolver.solveRLS_UniformRing(a, b, r, false));
    }

    public static Solver getEA() {
        return new Solver("EA", "-", "(1+1) EA (1/n)", "EA", PartitionSolver::solveEA);
    }

    public static Solver getEA(int k) {
        if (k == 1) {
            return getEA();
        }
        return new Solver("EA-SM", k + "/n", "(1+1) EA (" + k + "/n)", "EA" + k,
                (a, b) -> PartitionSolver.solveEA(a, b, (double) k / a.length));
    }

    public static Solver getFmut(double p) {
        return new Solver("fmut", String.format("%.3f", p), "fmut (" + p + ")", "fmut" + p,
                (a, b) -> PartitionSolver.solveFmut(a, b, p));
    }

    public static Solver getPmut(double n) {
        return new Solver("pmut", String.format("%.2f", n), "pmut (" + n + ")", "pmut" + n,
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

    public static Solver[] getRLSNeighbourComparison(int... values) {
        Solver[] ret = new Solver[values.length];
        ArrayHelp.fill(ret, (i) -> getRLSUniformNeighbour(values[i]));
        return ret;
    }

    public static Solver[] getRLSRingComparison(int ... values) {
        Solver[] ret = new Solver[values.length];
        ArrayHelp.fill(ret, (i) -> getRLSUniformRing(values[i]));
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
                getEA(50),
                getEA(100),
        };
    }

    public static Solver[] getEAComparison(int... args) {
        Solver[] solvers = new Solver[args.length];
        for (int i = 0; i < args.length; ++i) {
            solvers[i] = getEA(args[i]);
        }
        return solvers;
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
                getPmut(1.25),
                getPmut(1.5),
                getPmut(1.75),
                getPmut(2.0),
                getPmut(2.25),
                getPmut(2.5),
                getPmut(2.75),
                getPmut(3.0),
                getPmut(3.25),
        };
    }

    public static Solver[] getPmutComparison(double... args) {
        Solver[] solvers = new Solver[args.length];
        for (int i = 0; i < args.length; ++i) {
            solvers[i] = getPmut(args[i]);
        }
        return solvers;
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
                getPmut(2.75)
        };
    }

    public Solution solve(long[] values, long maxSteps) {
        return solver.solve(values, maxSteps);
    }

    @Override
    public String toString() {
        return fullName;
    }

    public interface ISolver {
        Solution solve(long[] values, long maxSteps);
    }
}