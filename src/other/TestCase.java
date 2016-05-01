package other;

import java.util.ArrayList;
import java.util.Collection;

import tree.Node;

public class TestCase {
  private ArrayList<Integer> blockList;
  private int index;
  private ArrayList<Node> nodeList;
  ArrayList<PreAmble> possiblePreAmbles;
  private Boolean isSelected = false;
  private Boolean isReachable = true;

  public TestCase(Collection<Integer> blockListParam, Collection<Node> nodeListParam) {
    this.blockList = new ArrayList<Integer>(blockListParam);
    this.nodeList = new ArrayList<Node>(nodeListParam);
    this.possiblePreAmbles = new ArrayList<PreAmble>();
  }

  public int getIndex() {
    return index;
  }

  public ArrayList<Integer> getBlocks() {
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
    return nodeList.get(nodeList.size() - 1);
  }

  public void addPreamble(PreAmble list) {
    if (list != null) {
      isReachable = true;
      possiblePreAmbles.add(list);
    } else {
      isReachable = false;
    }
  }

  public void clearPreAmble() {
    possiblePreAmbles.clear();
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

  public Integer getStartBlock() {
    return blockList.get(0);
  }

  public Integer getLastBlock() {
    return blockList.get(blockList.size() - 1);
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

  public Node getFirstNodeOfEndingBlock() {
    for (int i = 0; i < nodeList.size(); i++) {
      if (nodeList.get(i).getBlockIndex() == blockList.get(blockList.size() - 1)) {
        return nodeList.get(i);
      }
    }
    return nodeList.get(nodeList.size() - 1);
  }

  public Node getStartNode() {
    return nodeList.get(0);
  }

  public ArrayList<Integer> getUserActionsPreamble() {
    ArrayList<Integer> retVal = new ArrayList<Integer>();
    for (PreAmble preAmble : possiblePreAmbles) {
      int i = 0;
      for (TestCase tc : preAmble) {
        for (Node n : tc.getNodeSteps()) {
          if (!(n.getAction().equals("") && n.isPreamble())) {
            i++;
          }
        }
      }
      retVal.add(i);
    }
    return retVal;
  }

  public Integer getUserActionsAmount() {
    int i = 0;
    for (Node n : nodeList) {
      if (!n.getAction().equals("")) {
        i++;
      }
    }
    return i;
  }

  public ArrayList<PreAmble> getPreAmble() {
    return possiblePreAmbles;
  }
}
