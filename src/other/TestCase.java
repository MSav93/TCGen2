package other;

import java.util.ArrayList;
import java.util.Collection;

import tree.Node;

public class TestCase {
  private ArrayList<Integer> blockList;
  private int index;
  private ArrayList<Node> nodeList;
  private Node startNode;
  private Node endNode;
  private ArrayList<Integer> blocksAway = new ArrayList<Integer>();
  private ArrayList<Node> nodesAway = new ArrayList<Node>();
  private Boolean isSelected = false;
  private Boolean isReachable = true;

  public TestCase(Collection<Integer> blockListParam, Collection<Node> nodeListParam) {
    this.blockList = new ArrayList<Integer>(blockListParam);
    this.nodeList = new ArrayList<Node>(nodeListParam);
    this.startNode = nodeList.get(0);
    this.endNode = nodeList.get(nodeList.size() - 1);

  }

  public int getIndex() {
    return index;
  }

  public ArrayList<Integer> getSteps() {
    return blockList;
  }

  public ArrayList<Node> getNodeSteps() {
    return nodeList;
  }

  public Integer getLength() {
    return blockList.size();
  }

  public Integer getNodeLength() {
    return nodeList.size();
  }

  public Node getStartNode() {
    return startNode;
  }

  public Node getEndNode() {
    return endNode;
  }

  public void setStepsAway(ArrayList<Integer> blocksAwayParam, ArrayList<Node> nodesAwayParam) {
    if (blocksAwayParam != null) {
      blocksAway = blocksAwayParam;
      nodesAway = nodesAwayParam;
    }
  }

  public ArrayList<Integer> getBlocksAway() {
    return blocksAway;
  }

  public int getBlocksAwayLength() {
    return blocksAway.size();
  }

  public ArrayList<Node> getNodesAway() {
    return nodesAway;
  }

  public int getNodesAwayLength() {
    return nodesAway.size();
  }

  public String toString() {
    return endNode + "" + blockList + "(" + getLength() + ")";
  }

  public Boolean isSelected() {
    return isSelected;
  }

  public void setSelected(Boolean isSelected) {
    this.isSelected = isSelected;
  }

  public Boolean isReachable() {
    return isReachable;
  }

  public void setReachable(Boolean isReachable) {
    this.isReachable = isReachable;
  }


}
