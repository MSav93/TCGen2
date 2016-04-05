package tree;

import other.Constants;

public class Node implements Comparable<Node> {
  private Integer tag;
  private String component;
  private String behaviourType;
  private String behaviour;
  private String flag;
  private Integer blockIndex;


  public Node(Integer tagParam, String componentParam, String behaviourTypeParam,
      String behaviourParam, String flagParam, Integer blockIndexParam) {
    tag = tagParam;
    component = componentParam;
    behaviourType = behaviourTypeParam;
    behaviour = behaviourParam;
    if (flagParam == null) {
      flag = "";
    } else {
      flag = flagParam;
    }
    blockIndex = blockIndexParam;
  }

  public Integer getTag() {
    return tag;
  }

  public String getComponent() {
    return component;
  }

  public String getBehaviour() {
    return behaviour;
  }

  public String getBehaviourType() {
    return behaviourType;
  }

  public String getFlag() {
    return flag;
  }
  
  public Integer getBlockIndex() {
    return blockIndex;
  }

  public String toString() {
    return "[" + tag + "] " + component + ": " + behaviour + "[" + behaviourType + "]"
        + getFlagSymbol(flag);
  }

  private String getFlagSymbol(String flag) {
    if (!flag.equals("")) {
      if (Constants.nodeFlags.contains(flag)) {
        return " " + Constants.nodeFlagSymbols.get(Constants.nodeFlags.indexOf(flag));
      }
      return " " + flag;
    }
    return "";
  }

  @Override
  public int compareTo(Node otherNode) {
    int result = getTag().compareTo(otherNode.getTag());
    if (result == 0) {
      result = getComponent().compareTo(otherNode.getComponent());
    }
    if (result == 0) {
      result = getBehaviour().compareTo(otherNode.getBehaviour());
    }
    if (result == 0) {
      result = getBehaviourType().compareTo(otherNode.getBehaviourType());
    }
    if (result == 0) {
      result = getFlag().compareTo(otherNode.getFlag());
    }
    return result;
  }

  @Override
  public boolean equals(Object otherNode) {
    if (otherNode instanceof Node) {
      boolean result = getTag().equals(((Node) otherNode).getTag());
      if (result) {
        result = getComponent().equals(((Node) otherNode).getComponent());
      }
      if (result) {
        result = getBehaviour().equals(((Node) otherNode).getBehaviour());
      }
      if (result) {
        result = getBehaviourType().equals(((Node) otherNode).getBehaviourType());
      }
      if (result) {
        result = getFlag().equals(((Node) otherNode).getFlag());
      }
      return result;
    } else {
      return false;
    }
  }

  public boolean equalsSimple(Node otherNode) {
    boolean result = getComponent().equals(otherNode.getComponent());
    if (result) {
      result = getBehaviour().equals(otherNode.getBehaviour());
    }
    if (result) {
      result = getBehaviourType().equals(otherNode.getBehaviourType());
    }
    return result;
  }
}
