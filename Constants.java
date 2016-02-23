import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

  public static final String tab1Name = "Nodes of Interest";
  
  public static final String tab2Name = "Target System States";
  
  public static final String tab3Name = "Observable Responses";
  
  public static final String tab4Name = "User Actions/External Inputs";
  
  public static final String tab5Name = "Join Test Paths";
  
  public static final ArrayList<String> unacceptedObsBehaviourTypes = new ArrayList<String>(
      Arrays.asList("EXTERNAL-INPUT", "EVENT", "EXTERNAL-OUTPUT", "SELECTION", "GUARD"));

  public static final ArrayList<String> acceptedUABehaviourTypes = new ArrayList<String>(
      Arrays.asList("EXTERNAL-INPUT", "EVENT", "EXTERNAL-OUTPUT"));
}
