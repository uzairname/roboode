package upackage;

public class QuadraticEquationSolver {

	private double [] solve (double a, double b, double c) {
	
		double [] roots = new double [2];
		
		double temp1 = Math.sqrt(b*b - 4*a*c);
		roots[0] = (-b - temp1)/(2*a);
		roots[1] = (-b + temp1)/(2*a);
		return roots;
		
	}	
	
	
	public static void main(String args []) {
		QuadraticEquationSolver quadraticEquationSolver = new QuadraticEquationSolver();
		
		double[] roots = quadraticEquationSolver.solve(1, 2, 1);
		System.out.println(roots[0] + " and " + roots[1]); 
		
	}
}
