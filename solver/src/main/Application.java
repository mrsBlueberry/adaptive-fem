package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import main.generator.BreakType;
import main.generator.Direction;
import main.generator.Edge;
import main.generator.Generator;
import main.generator.Neighborhood;
import main.generator.Part;
import main.grid.TestGridGenerator;
import main.productions.productionFactory.L2ProjectionFactory;
import main.productions.productionFactory.RandomStuffFactory;
import main.scheduler.GraphScheduler;
import main.scheduler.Node;
import main.scheduler.NotSoDummyNode;
import main.scheduler.ProductionGraphBuilder;
import main.tree.DOF;
import main.tree.Element2D;
import main.tree.TestTreeBuilder;
import main.tree.TreeInitializer;
import main.tree.Vertex;
import main.utils.MatrixUtil;
import main.utils.Rectangle;


public class Application {

	public static void main(String[] args) {

//		TestTreeBuilder builder = new TestTreeBuilder();
//		Vertex root = builder.buildTree(4);
//		TreeInitializer.visit(root);
//		builder.printTree("",root);
		Vertex root = generateMesh();
		
		

		ProductionGraphBuilder graphBuilder = new ProductionGraphBuilder(new L2ProjectionFactory());
		
		
		GraphScheduler scheduler = new GraphScheduler();
		Set<? extends Node> graph = graphBuilder.makeGraph(root);
		List<List<Node>> scheduledNodes = scheduler.schedule(graph);
		Executor executor = new Executor();
		int idx=0;
		Map<DOF,Double> gausianElimResult = null;
		for(List<Node> nodes : scheduledNodes){
			System.out.println();
			executor.beginStage(nodes.size());
			for(Node n : nodes){
				System.out.print(n.getName() + "  ");
				executor.submitProduction(((NotSoDummyNode)n).getProduction());
				
			}
			executor.waitForEnd();
			if(idx==1){
				gausianElimResult = gatherMatrix(root);
			}
			++idx;
		}
		
		executor.shutdown();
		Map<DOF, Double> result = new TreeMap<>();
		
		Set<Vertex> leaves = new HashSet<>();
		getLeaves(leaves, root);
		for(Vertex v : leaves){
			for(int i=0;i<v.rowDofs.size();++i){
				result.put(v.rowDofs.get(i), v.x[i]);
			}
		}
		
		System.out.println();
		for(Entry<DOF, Double> e : result.entrySet()){
			System.out.println("ID: "+e.getKey().ID+" = "+ e.getValue() + "     " + gausianElimResult.get(e.getKey()));
		}
		
		for(Entry<DOF, Double> e : gausianElimResult.entrySet()){
			System.out.println("ID: "+e.getKey().ID+" = "+ e.getValue());
		}
		
		ResultPrinter.printResult(gatherElements(leaves), result);
		
		
//
//		TestGridGenerator.makeTestGrid();
		
		
		
	}
	
	private static Vertex generate2(){
		Generator gen = new Generator(0, 1, 0, 1);
		Part [] parts1 = gen.breakPart(new ArrayList<>(), BreakType.CROSS);
		Part [] parts2 = gen.breakPart(Arrays.asList(1), BreakType.CROSS);
		for(Part p : parts1){
			print(p);
		}
		for(Part p : parts2){
			print(p);
		}
		Vertex root = gen.buildEliminationTree();
		TreeInitializer.visit(root);
		TestTreeBuilder.printTree("", root);
		return root;
		
		
	}
	
	
	private static Vertex generateMesh(){
		Generator gen = new Generator(0, 1, 0, 1);
		generateMesh1(10, new ArrayList<Integer>(), gen);
		Vertex root = gen.buildEliminationTree();
		TreeInitializer.visit(root);
		TestTreeBuilder.printTree("", root);
		System.out.println("Generator leaves count: " + gen.getLeaves().size());
		return root;
		
	}
	
	//generate mesh 1 
	/*
	 * -----------------
	 * |+|+|+|+|
	 * ---------
	 * | | | | |
	 * ---------
	 * |   |   |
	 * ---------
	 * 
	 * 
	 */
	private static void generateMesh1(int level, List<Integer> path, Generator gen) {
		Queue<List<Integer>> q = new LinkedList<List<Integer>>();
		q.add(path);
		for (int i = 0; i < level; ++i) {
			List<List<Integer>> tmp = new ArrayList<>(q);
			q.clear();
			for (List<Integer> l : tmp) {
				gen.breakPart(l, BreakType.CROSS);
				l.add(0);
				q.add(new ArrayList<>(l));
				l.remove(l.size() - 1);
				l.add(1);
				q.add(new ArrayList<>(l));

			}
		}

	}


	//generate mesh 1 
	/*
	 * -----------------
	 * | | | |+|
	 * ---------
	 * |   |   |
	 * ---------
	 * 
	 * 
	 */
	private static void generateMesh2(int level, List<Integer> path, Generator gen) {
		if(level < 30){
			gen.breakPart(path, BreakType.CROSS);
			path.add(1);
			generateMesh2(level + 1, path, gen);
		}

	}

	private static List<Element2D> gatherElements(Collection<Vertex> leaves){
		List<Element2D> elements = new ArrayList<>();
		for(Vertex v : leaves){
			elements.add((Element2D)v.element);
		}
		return elements;
	}
	
	private static void generate() {
		Generator gen = new Generator(1,1,1,1);
		Rectangle r = new Rectangle(0, 1, 0, 1);
		Rectangle r2 = new Rectangle(1, 2, 0, 1);
		Part p = new Part(7);
		Part p2 = new Part(11);
		p.rectangle = r;
		p2.rectangle = r2;
		Neighborhood n = new Neighborhood.SinglePart(p);
		Neighborhood n2 = new Neighborhood.SinglePart(p2);
		Neighborhood empty = new Neighborhood.Empty();

		Edge common = new Edge(n2, n);
		p.setEdge(Direction.LEFT, new Edge(n, empty));
		p.setEdge(Direction.TOP, new Edge(empty, n));
		p.setEdge(Direction.RIGHT, common);
		p.setEdge(Direction.BOTTOM, new Edge(n, empty));

		p2.setEdge(Direction.LEFT, common);
		p2.setEdge(Direction.TOP, new Edge(empty, n2));
		p2.setEdge(Direction.BOTTOM, new Edge(n2, empty));
		p2.setEdge(Direction.RIGHT, new Edge(empty, n2));

//		Part[] parts = gen.breakHorizontal(p);
//		Part[] parts2 = gen.breakHorizontal(p2);
//		for (Part pr : parts) {
//			print(pr);
//			System.out.println();
//		}
//		for (Part pr : parts2) {
//			print(pr);
//			System.out.println();
//		}
		// print(p2);
	}

	public static void print(Part p) {
		System.out.println(p);
		for (Direction d : Direction.values()) {
			System.out.printf("%s -> %s\n", d, p.getEdge(d));
		}
	}
	
	private static void getLeaves(Set<Vertex> leaves, Vertex root){
		if(root.children.isEmpty()){
			leaves.add(root);
		}else{
			for(Vertex v : root.children){
				getLeaves(leaves, v);
			}
			
		}
	}
	
	private static Map<DOF, Double> gatherMatrix(Vertex root) {
		Map<DOF, Double> result = new TreeMap<>();
		Set<Vertex> leaves = new HashSet<>();
		getLeaves(leaves, root);
		Set<DOF> dofs = new HashSet<>();
		for (Vertex v : leaves) {
			dofs.addAll(v.rowDofs);
		}
		List<DOF> dofList = new ArrayList<>(dofs);
		double[][] matrixA = new double[dofList.size()][dofList.size()];
		double[] matrixB = new double[dofList.size()];
		combineMatrices(leaves, dofList, matrixA, matrixB);
		double[] solution = MatrixUtil.gaussianElimination(matrixA, matrixB);
		for (int i = 0; i < dofList.size(); ++i) {
			result.put(dofList.get(i), solution[i]);
		}
		return result;
	}

	private static void combineMatrices(Set<Vertex> leaves, List<DOF> dofList,
			double[][] matrixA, double[] matrixB) {
		for(Vertex v : leaves){
			for (DOF d : v.rowDofs) {
				int parentI = dofList.indexOf(d);
				int childI = v.rowDofs.indexOf(d);
				for (DOF d2 : v.rowDofs) {

					int parentJ = dofList.indexOf(d2);

					int childJ = v.rowDofs.indexOf(d2);
					matrixA[parentI][parentJ] += v.A[childI][childJ];

				}
				matrixB[parentI] += v.b[childI];
			}
		}
	}

}
