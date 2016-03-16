package tree;


public class NodeData {
  private String tag;
  private String component;
  private String behaviourType;
  private String behaviour;
  private String flag;


  public NodeData(String tagParam, String componentParam, String behaviourTypeParam,
      String behaviourParam, String flagParam) {
    tag = tagParam;
    component = componentParam;
    behaviourType = behaviourTypeParam;
    behaviour = behaviourParam;
    if (flagParam == null) {
      flag = "";
    } else {
      flag = flagParam;
    }
  }

  public String getBehaviour() {
    return behaviour;
  }

  public String getBehaviourType() {
    return behaviourType;
  }

  public String getComponent() {
    return component;
  }

  public String getTag() {
    return tag;
  }
  
  public String getFlag() {
    return flag;
  }

  public String toString() {
    return tag + ", " + component + ", " + behaviourType + ", " + behaviour;
  }
}
