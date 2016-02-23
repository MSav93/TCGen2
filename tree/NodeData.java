package tree;


public class NodeData {
  private String tag;
  private String component;
  private String behaviourType;
  private String behaviour;


  public NodeData(String tagParam, String componentParam, String behaviourTypeParam,
      String behaviourParam) {
    tag = tagParam;
    component = componentParam;
    behaviourType = behaviourTypeParam;
    behaviour = behaviourParam;
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

  public String toString() {
    return tag + ", " + component + ", " + behaviourType + ", " + behaviour;
  }
}
