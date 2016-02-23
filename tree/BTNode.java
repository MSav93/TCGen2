package tree;

import java.util.List;

public class BTNode {

  private Integer parent;
  private List<Integer> children;
  private List<NodeData> data;
  private Integer index;
  private String branchType;


  public BTNode(Integer parentParam, List<Integer> childrenParam, Integer indexParam,
      String branchTypeParam, List<NodeData> dataParam) {
    parent = parentParam;
    children = childrenParam;
    index = indexParam;
    branchType = branchTypeParam;
    data = dataParam;
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
  
  public List<NodeData> getData() {
    return data;
  }

  @Override
  public String toString() {
    String retVal = "(" + index + "," + data + ", ";
    System.out.println(index + ", " + children);
    for(Integer child : children) {
      retVal = retVal.concat(child + ",");
    }
    return retVal + ")";
  }
}
