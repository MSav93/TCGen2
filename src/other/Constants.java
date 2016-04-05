package other;
import java.util.ArrayList;
import java.util.Arrays;

public final class Constants {

  public static final String version = "0.1";

  public static final String tab1Name = "Nodes of Interest";

  public static final String tab2Name = "CheckPoints";

  public static final String tab3Name = "Observable Responses";

  public static final String tab4Name = "User Actions/External Inputs";

  public static final String tab5Name = "Join Test Cases";
  
  public static final String htmlTabSpacing = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
  
  // 8 spaces
  public static final String tabSpacing = "        ";

  public static final ArrayList<String> unacceptedObsBehaviourTypes = new ArrayList<String>(
      Arrays.asList("EXTERNAL-INPUT", "EVENT", "EXTERNAL-OUTPUT", "SELECTION", "GUARD"));

  public static final ArrayList<String> unnaceptedObsFlags =
      new ArrayList<String>(Arrays.asList("REVERSION", "REFERENCE"));
  
  public static final ArrayList<String> acceptedUABehaviourTypes =
      new ArrayList<String>(Arrays.asList("EXTERNAL-INPUT", "EVENT"));
  
  public static final ArrayList<String> nodeFlags = new ArrayList<String>(
      Arrays.asList("REVERSION", "REFERENCE", "KILL", "SYNCHRONISATION"));
  
  public static final ArrayList<String> nodeFlagSymbols = new ArrayList<String>(
      Arrays.asList("^", "=>", "--", "="));
}
