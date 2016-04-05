package tree;

import java.util.List;

public class Block {

  private Integer parent;
  private List<Integer> children;
  private List<Node> nodes;
  private Integer index;
  private String branchType;


  public Block(Integer parentParam, List<Integer> childrenParam, Integer indexParam,
      String branchTypeParam, List<Node> nodesParam) {
    parent = parentParam;
    children = childrenParam;
    index = indexParam;
    branchType = branchTypeParam;
    nodes = nodesParam;
  }

  public Integer getParent() {
    return parent;
  }


  public List<Integer> getChildren() {
    return children;
  }

  public Integer getIndex() {
    return index;
  }
  
  public String getBranchType() {
    return branchType;
  }
  
  public List<Node> getNodes() {
    return nodes;
  }

  @Override
  public String toString() {
    String retVal = "(" + index + "," + nodes + ", ";
    System.out.println(index + ", " + children);
    for(Integer child : children) {
      retVal = retVal.concat(child + ",");
    }
    return retVal + ")";
  }
}
