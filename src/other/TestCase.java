package other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tree.Node;

public class TestCase {
  private ArrayList<Integer> blockList;
  private int index;
  private ArrayList<Node> nodeList;
  private Node endNode;
  private List<Integer> blocksAway = new ArrayList<Integer>();
  private ArrayList<Node> nodesAway = new ArrayList<Node>();
  private Boolean isSelected = false;
  private Boolean isReachable = true;
  private boolean preAmble;

  public TestCase(Collection<Integer> blockListParam, Collection<Node> nodeListParam) {
    this.blockList = new ArrayList<Integer>(blockListParam);
    this.nodeList = new ArrayList<Node>(nodeListParam);
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

  public Node getEndNode() {
    return endNode;
  }

  public void setStepsAway(List<Integer> list, ArrayList<Node> nodesAwayParam) {
    if (list != null) {
      isReachable = true;
      blocksAway = list;
      nodesAway = nodesAwayParam;
    } else {
      isReachable = false;
    }
  }

  public List<Integer> getBlocksAway() {
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
    return blockList + "(" + getLength() + ")";
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

  public void setPreAmble(boolean preAmble) {
    this.preAmble = preAmble;
  }

  public boolean isPreAmble() {
    return this.preAmble;
  }

  public Integer getStartBlock() {
    return blockList.get(0);
  }

  public Node getLastNodeOfStartingBlock() {
    Node n = nodeList.get(0);
    for (int i = 0; i < nodeList.size(); i++) {
      if (nodeList.get(i).getBlockIndex() == blockList.get(0)) {
        n = nodeList.get(i);
      } else {
        return n;
      }
    }
    return n;
  }
}
