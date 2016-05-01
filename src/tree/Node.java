package tree;

import other.Constants;

public class Node implements Comparable<Node> {
  private Integer tag;
  private String component;
  private String behaviourType;
  private String behaviour;
  private String flag;
  private Integer blockIndex;
  private Boolean noi = false;
  private Boolean cp = false;
  private String action = "";
  private Boolean preamble = false;
  private String observable = "";


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

  public Boolean isNoi() {
    return noi;
  }

  public void setNoi(Boolean noi) {
    this.noi = noi;
  }

  public Boolean isCp() {
    return cp;
  }

  public void setCp(Boolean cp) {
    this.cp = cp;
  }

  public String toString() {
    String flag = "";
    if (getFlag().equals("REVERSION")) {
      flag = " ^";
    } else if (getFlag().equals("REFERENCE")) {
      flag = " =>";
    } else if (getFlag().equals("KILL")) {
      flag = " --";
    } else if (getFlag().equals("SYNCHRONISE")) {
      flag = " =";
    }
    return getTag() + " | " + simpleToString() + flag;
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

  public String simpleToString() {
    String openParen = "";
    String closeParen = "";
    if (getBehaviourType().equals("STATE-REALISATION")) {
      openParen = "[";
      closeParen = "]";
    } else if (getBehaviourType().equals("SELECTION")) {
      openParen = "?";
      closeParen = "?";
    } else if (getBehaviourType().equals("GUARD")) {
      openParen = "???";
      closeParen = "???";
    } else if (getBehaviourType().equals("EVENT")) {
      openParen = "??";
      closeParen = "??";
    } else if (getBehaviourType().equals("INTERNAL-INPUT")) {
      openParen = ">";
      closeParen = "<";
    } else if (getBehaviourType().equals("INTERNAL-OUTPUT")) {
      openParen = "<";
      closeParen = ">";
    } else if (getBehaviourType().equals("EXTERNAL-INPUT")) {
      openParen = ">>";
      closeParen = "<<";
    } else if (getBehaviourType().equals("EXTERNAL-OUTPUT")) {
      openParen = "<<";
      closeParen = ">>";
    }
    return getComponent() + " " + openParen + getBehaviour() + closeParen;
  }

  public String otherToString() {
    return "[" + tag + "] " + component + ": " + behaviour + "[" + behaviourType + "]"
        + getFlagSymbol(flag);
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
    if (result == 0) {
      result = isCp().compareTo(otherNode.isCp());
    }
    if (result == 0) {
      result = isNoi().compareTo(otherNode.isNoi());
    }
    if (result == 0) {
      result = getAction().compareTo(otherNode.getAction());
    }
    if (result == 0) {
      result = isPreamble().compareTo(otherNode.isPreamble());
    }
    if (result == 0) {
      result = getObservable().compareTo(otherNode.getObservable());
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

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getObservable() {
    return observable;
  }

  public void setObservable(String observable) {
    this.observable = observable;
  }

  public Boolean isPreamble() {
    return preamble;
  }

  public void setPreamble(Boolean preamble) {
    this.preamble = preamble;
  }
}
