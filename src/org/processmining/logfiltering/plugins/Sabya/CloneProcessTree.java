package org.processmining.logfiltering.plugins.Sabya;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTree.Type;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Or;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class CloneProcessTree {
	Map<UUID, UUID> nodeIdMap = new LinkedHashMap<UUID, UUID>();//Stores the Mapping of old node ids to new node ids
	
	public ProcessTree clone(ProcessTree tree) {
		ProcessTree newTree = new ProcessTreeImpl();
		newTree.setRoot(clone(tree, tree.getRoot(), newTree));
		return newTree;
	}
	
	public Node clone(ProcessTree tree, Node node, ProcessTree newTree) {
		if (tree.getType(node) == Type.AUTOTASK) {
			Node newNode = new Automatic("tau");
			newTree.addNode(newNode);
			nodeIdMap.put(node.getID(),newNode.getID());
			return newNode;
		} else if (tree.getType(node) == Type.MANTASK) {
			Node newNode = new Manual(node.getName());
			newTree.addNode(newNode);
			nodeIdMap.put(node.getID(),newNode.getID());
			return newNode;
		} else {
			Block newNode;
			if (tree.getType(node) == Type.XOR) {
				newNode = new Xor("");
				nodeIdMap.put(node.getID(),newNode.getID());
			} else if (tree.getType(node) == Type.SEQ) {
				newNode = new Seq("");
				nodeIdMap.put(node.getID(),newNode.getID());
			} else if (tree.getType(node) == Type.AND) {
				newNode = new And("");
				nodeIdMap.put(node.getID(),newNode.getID());
			}else if (tree.getType(node) == Type.LOOPXOR ) {
				newNode = new XorLoop("");
				nodeIdMap.put(node.getID(),newNode.getID());
			} else if (tree.getType(node) == Type.OR ) {
				newNode = new Or("");
				nodeIdMap.put(node.getID(),newNode.getID());
			} else {
				throw new RuntimeException("not implemented");
			}
			newTree.addNode(newNode);
			for (Node child : ((Block) node).getChildren()) {
				newNode.addChild(clone(tree, child, newTree));
			}
			return newNode;
		}
	}
	
	public Map<UUID,UUID> getNodeIdMap() {
		return nodeIdMap;
	}
}