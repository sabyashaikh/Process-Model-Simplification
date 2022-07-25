package org.processmining.logfiltering.plugins.Sabya;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;

public class CloneDataStructureWithNewNodes {
	public static Map<UUID, NodeDetailStore>  clone(Map<UUID, NodeDetailStore> treeNodeFreq, Map<UUID,UUID> nodeIdMap, ProcessTree newTree) {
		Map<UUID, NodeDetailStore> clonedTreeNodeFreq =  new LinkedHashMap<UUID, NodeDetailStore>();
		for (Map.Entry<UUID, NodeDetailStore> entry : treeNodeFreq.entrySet()) {
			UUID newNodeId = nodeIdMap.get(entry.getKey());
			
			NodeDetailStore oldNodeNDS = entry.getValue();
			NodeDetailStore newNodeNDS = oldNodeNDS.clone();
			newNodeNDS.setNode(newTree.getNode(newNodeId));
			newNodeNDS.setParent(nodeIdMap.get(oldNodeNDS.getParent()));
			List<Node> newNodeChildren = new ArrayList<Node>();
			for (Node child : oldNodeNDS.getChildren()) {
				UUID newChildId = nodeIdMap.get(child.getID());
				Node newChild = newTree.getNode(newChildId);
				newNodeChildren.add(newChild);
			}
			newNodeNDS.setChildren(newNodeChildren);
			clonedTreeNodeFreq.put(newNodeId, newNodeNDS);
		}
		return clonedTreeNodeFreq;
	}
}
