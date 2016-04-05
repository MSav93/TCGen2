import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.SBCLPipe;

import other.Constants;
import other.TestCase;
import renderers.BehaviourCmbRenderer;
import renderers.CPCell;
import renderers.CPModel;
import renderers.ComponentCmbRenderer;
import renderers.NOICell;
import renderers.NOIModel;
import renderers.TestPathModel;
import renderers.TestPathSelectorCell;
import renderers.TestPathSelectorModel;
import tree.Block;
import tree.Node;
import util.BTModelReader;

public class Main extends JFrame {

  /**
   * Application
   */
  private static Main frame = null;

  /**
   * Main Panel
   */
  private JPanel contentPane;

  /**
   * Components on Main Panel
   */
  private JTabbedPane tabbedPane;
  private JButton btnGenerateTestCases;
  private JButton btnGenerateExcel;

  /**
   * Components on Nodes Of Interest tab
   */
  private JTable tblNOI;
  private final TextArea taNOI = new TextArea();


  /**
   * Components on CheckPoints tab
   */
  private JTable tblCP;
  private JComboBox<Node> cmbInitialState = new JComboBox<Node>();
  private final TextArea taCP = new TextArea();

  /**
   * Components on Observations tab
   */
  private JComboBox<Node> cmbORComponent = new JComboBox<Node>();
  private JComboBox<Node> cmbORBehaviour = new JComboBox<Node>();
  private final JTextArea taORInput = new JTextArea();
  private final TextArea taORConfigured = new TextArea();

  /**
   * Components on User Actions tab
   */
  private JComboBox<Node> cmbUAComponent = new JComboBox<Node>();
  private JComboBox<Node> cmbUABehaviour = new JComboBox<Node>();
  private final TextArea taUAConfigured = new TextArea();
  private final JTextArea taUAInput = new JTextArea();
  private final JCheckBox chckbxAppearPreAmble = new JCheckBox(
      "This condition should appear in the Pre-Amble/This is an External Input not under user's control");

  /**
   * Components on the Join Test Cases tab
   */
  private JTable tblTP;
  private JTable tblFinal;

  /**
   * Maps to store info about BT Model
   */
  private TreeMap<Integer, Block> indexToNodesMap = new TreeMap<Integer, Block>();
  private HashMap<String, ArrayList<Node>> compToBehaviourMap =
      new HashMap<String, ArrayList<Node>>();

  /**
   * Variables that must be saved
   */
  private String btXML;
  private String btFilePath;
  private Node initialNode;


  /**
   * Other
   */
  private SBCLPipe sbcl = new SBCLPipe();
  private Boolean isLoaded = false;
  private ArrayList<TestCase> testCases = new ArrayList<TestCase>();
  private Node currentNode;

  Set<Node> chosenNOIs = new TreeSet<Node>();
  TreeSet<Node> chosenCPs = new TreeSet<Node>();
  LinkedHashMap<Node, String> observableResponses = new LinkedHashMap<Node, String>();
  LinkedHashMap<Node, String[]> userActions = new LinkedHashMap<Node, String[]>();

  private static final long serialVersionUID = 1L;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e1) {
      System.err
          .println("System 'Look and Feel' could not be found. Using Windows 'Look and Feel'");
    }
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e1) {
      System.err
          .println("Windows 'Look and Feel' could not be found. Using default 'Look and Feel'");
    }
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          Runtime.getRuntime().exec("taskkill /F /IM sbcl.exe");
        } catch (IOException e) {
          printErrorMessage("error|3|");
        }
      }
    }));
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          frame = new Main();
          frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Clear all previous configurations from memory.
   */
  private void clearEverything() {
    tblNOI.setModel(new NOIModel(null));
    chosenNOIs.clear();

    tblCP.setModel(new CPModel(null));
    cmbInitialState.removeAllItems();
    taCP.setText("");
    chosenCPs = new TreeSet<Node>();

    cmbORComponent.removeAllItems();
    cmbORBehaviour.removeAllItems();
    taORInput.setText("");
    taORConfigured.setText("");
    observableResponses = new LinkedHashMap<Node, String>();

    cmbUAComponent.removeAllItems();
    cmbUABehaviour.removeAllItems();
    taUAInput.setText("");
    taUAConfigured.setText("");
    chckbxAppearPreAmble.setSelected(false);
    userActions = new LinkedHashMap<Node, String[]>();

    indexToNodesMap = new TreeMap<>();
    compToBehaviourMap = new HashMap<String, ArrayList<Node>>();

    btFilePath = "";

    updateCPTA();
    updateOTA();
    updateUATA();
  }

  private void loadBTModel(File f) {
    System.out.println("FILE: " + f.getAbsolutePath());
    clearEverything();
    btFilePath = f.getAbsolutePath();
    System.out.println("loading model");

    // load bt-file into BTAnalyser
    System.out.println("Processing bt file");
    sbcl.sendCommand("(process-bt-file \"" + (f.getPath()).replace("\\", "/") + "\")");

    // important to ensure resulting TCPs are reachable/valid
    System.out.println("Ensure TCPs are reachable");
    sbcl.sendCommand("(reachable-states)");

    System.out.println("Building BT");
    btXML = sbcl.sendCommand("(print-bt)");
    if (!btXML.equals("<result><error>No Behavior Tree loaded.</error></result>")) {
      readBTXML(btXML);
    } else {
      printErrorMessage("error|6|");
      return;
    }

    populateData();
    isLoaded = true;
    frame.setTitle("TP-Optimizer - " + f.getName());
  }

  private void loadTCC(File f) {
    clearEverything();
    String file = null;
    try {
      file = new String(Files.readAllBytes(Paths.get(f.getPath())));
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (file != null) {
      readConfig(file);
    }
  }

  private void readBTXML(String btXML) {
    BTModelReader modelReader = new BTModelReader(btXML);
    indexToNodesMap = modelReader.getIndexToNodeMap();
  }

  private void readConfig(String configXML) {
    org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();

    // XML parser has problem reading ||&
    configXML = configXML.replace("||&", "");

    org.jdom2.Document doc = null;
    try {
      doc = saxBuilder.build(new StringReader(configXML));
    } catch (JDOMException | IOException e) {
      e.printStackTrace();
    }

    Element config = doc.getRootElement();
    if (!config.getAttributeValue("version").equals(Constants.version)) {
      printErrorMessage("error|6|");
    }
    btFilePath = config.getAttributeValue("filepath");
    File f = new File(btFilePath);
    loadBTModel(f);

    // NOIS
    List<Element> nodesOfInterest = config.getChild("NOI").getChildren();
    // recording nodes that get matched to remove later to avoid ConcurrentModificationException.
    ArrayList<Element> nodesToBeRemoved = new ArrayList<Element>();
    for (int i = 0; i < tblNOI.getModel().getRowCount(); i++) {
      Node tblNode = (Node) tblNOI.getValueAt(i, 0);
      for (Element noi : nodesOfInterest) {
        if (checkElementsAttributes(noi, "noi")) {
          Node xmlNode = new Node(Integer.parseInt(noi.getAttributeValue("tag")),
              noi.getAttributeValue("component"), noi.getAttributeValue("behaviour-type"),
              noi.getAttributeValue("behaviour"), noi.getAttributeValue("flag"), null);
          if (tblNode.equals(xmlNode)) {
            // Selects this node as a NOI
            chosenNOIs.add(tblNode);
            // Remove node from remaining list of unmatched nodes
            nodesToBeRemoved.add(noi);
          }
        }
      }
    }
    for (Element node : nodesToBeRemoved) {
      nodesOfInterest.remove(node);
    }
    // CPs
    List<Element> checkpoints = config.getChild("CP").getChildren("node");
    ArrayList<Element> checkpointsToBeRemoved = new ArrayList<Element>();
    for (Element cp : checkpoints) {
      if (checkElementsAttributes(cp, "cp")) {
        String component = cp.getAttributeValue("component");
        String behaviour = cp.getAttributeValue("behaviour");
        if (compToBehaviourMap.containsKey(component)) {
          ArrayList<Node> nodes = compToBehaviourMap.get(component);
          for (Node node : nodes) {
            if (node.getBehaviour().equals(behaviour)) {
              // Selects this node as a CP
              chosenCPs.add(node);
              // Remove node from remaining list of unmatched nodes
              checkpointsToBeRemoved.add(cp);
              // break;
            }
          }
        }
      }
    }
    for (Element cp : checkpointsToBeRemoved) {
      checkpoints.remove(cp);
    }

    // Initial CP
    Element initialCp = config.getChild("CP").getChild("initial");

    if (initialCp != null) {
      if (checkElementsAttributes(initialCp, "initial")) {
        Node initial = new Node(Integer.parseInt(initialCp.getAttributeValue("tag")),
            initialCp.getAttributeValue("component"), initialCp.getAttributeValue("behaviour-type"),
            initialCp.getAttributeValue("behaviour"), initialCp.getAttributeValue("flag"), null);
        updateCPInitial();
        for (int i = 0; i < cmbInitialState.getItemCount(); i++) {
          System.out.println("Component: (" + cmbInitialState.getItemAt(i).getComponent() + "|" + initial.getComponent() + ")");
          System.out.println("getBehaviour: (" + cmbInitialState.getItemAt(i).getBehaviour() + "|" + initial.getBehaviour() + ")");
          System.out.println("getBehaviourType: (" + cmbInitialState.getItemAt(i).getBehaviourType() + "|" + initial.getBehaviourType() + ")");
          System.out.println("getFlag: (" + cmbInitialState.getItemAt(i).getFlag() + "|" + initial.getFlag() + ")");
          System.out.println("getTag: (" + cmbInitialState.getItemAt(i).getTag() + "|" + initial.getTag() + ")");
          if (initial.equals(cmbInitialState.getItemAt(i))) {
            initialCp = null;
            cmbInitialState.setSelectedItem(cmbInitialState.getItemAt(i));
            initialNode = cmbInitialState.getItemAt(i);
            break;
          }
        }
      }
    }

    // Observable Responses
    List<Element> observables = config.getChild("OR").getChildren();
    ArrayList<Element> observablesToBeRemoved = new ArrayList<Element>();
    for (Element or : observables) {
      if (checkElementsAttributes(or, "or")) {
        if (compToBehaviourMap.containsKey(or.getAttributeValue("component"))) {
          ArrayList<Node> nodes = compToBehaviourMap.get(or.getAttributeValue("component"));
          for (Node node : nodes) {
            Node xmlNode = new Node(Integer.parseInt(or.getAttributeValue("tag")),
                or.getAttributeValue("component"), or.getAttributeValue("behaviour-type"),
                or.getAttributeValue("behaviour"), or.getAttributeValue("flag"), null);
            if (node.equals(xmlNode)) {
              // Populate this nodes observables
              observableResponses.put(node, or.getAttributeValue("observation"));
              // Remove node from remaining list of unmatched nodes
              observablesToBeRemoved.add(or);
              break;
            }
          }
        }
      }
    }
    for (Element or : observablesToBeRemoved) {
      observables.remove(or);
    }

    // User Actions
    List<Element> actions = config.getChild("UA").getChildren();
    ArrayList<Element> actionsToBeRemoved = new ArrayList<Element>();
    for (Element ua : actions) {
      if (checkElementsAttributes(ua, "ua")) {
        if (compToBehaviourMap.containsKey(ua.getAttributeValue("component"))) {
          ArrayList<Node> nodes = compToBehaviourMap.get(ua.getAttributeValue("component"));
          for (Node node : nodes) {
            Node xmlNode = new Node(Integer.parseInt(ua.getAttributeValue("tag")),
                ua.getAttributeValue("component"), ua.getAttributeValue("behaviour-type"),
                ua.getAttributeValue("behaviour"), ua.getAttributeValue("flag"), null);
            if (node.equals(xmlNode)) {
              // Populate this nodes actions
              userActions.put(node,
                  new String[] {ua.getAttributeValue("action"), ua.getAttributeValue("preamble")});
              // Remove node from remaining list of unmatched nodes
              actionsToBeRemoved.add(ua);
              break;
            }
          }
        }
      }
    }
    for (Element ua : actionsToBeRemoved) {
      actions.remove(ua);
    }

    // TP
    // TODO
    updateDisplay();
    reportConfigDifferences(nodesOfInterest, checkpoints, initialCp, observables, actions);
  }

  private boolean checkElementsAttributes(Element element, String elementType) {
    boolean valid = element.hasAttributes();
    switch (elementType) {
      case "noi":
        if (valid) {
          valid = (element.getAttributeValue("tag") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("component") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour-type") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("flag") != null);
        }
        return valid;
      case "cp":
        if (valid) {
          valid = (element.getAttributeValue("component") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour") != null);
        }
        return valid;
      case "initial":
        if (valid) {
          valid = (element.getAttributeValue("tag") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("component") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour-type") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("flag") != null);
        }
        return valid;
      case "or":
        if (valid) {
          valid = (element.getAttributeValue("tag") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("component") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour-type") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("flag") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("observation") != null);
        }
        return valid;
      case "ua":
        if (valid) {
          valid = (element.getAttributeValue("tag") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("component") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour-type") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("flag") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("action") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("preamble") != null);
        }
        return valid;
      default:
        return false;
    }
  }

  /**
   * Populate configuration in TCGen-UI
   */
  private void populateData() {

    // load nodes NOI table
    populateNodeMaps();

    updateTblCP();
    updateORCmbBoxes();
    updateUACmbBoxes();

    updateCPTA();
    updateOTA();
    updateUATA();

    btnGenerateTestCases.setEnabled(true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab1Name), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab2Name), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab3Name), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab4Name), true);
  }

  private void populateNodeMaps() {
    List<Node> nodes = new ArrayList<Node>();
    for (int key : indexToNodesMap.keySet()) {
      for (Node node : indexToNodesMap.get(key).getNodes()) {
        if (Constants.acceptedUABehaviourTypes.contains(node.getBehaviourType())) {
          userActions.put(node, new String[] {"", ""});
        }
        if (!Constants.unacceptedObsBehaviourTypes.contains(node.getBehaviourType())) {
          observableResponses.put(node, "");
        }
        // tagToNodeDataMap.put(node.getTag(), node);
        if (!compToBehaviourMap.containsKey(node.getComponent())) {
          compToBehaviourMap.put(node.getComponent(), new ArrayList<Node>());
        }
        compToBehaviourMap.get(node.getComponent()).add(node);
        nodes.add(node);
      }
    }
    tblNOI.setModel(new NOIModel(nodes));
  }

  private void updateDisplay() {
    updateNOITA();
    updateCPTab();
    updateORTab();
    updateUATab();
  }

  private void reportConfigDifferences(List<Element> nodesOfInterest, List<Element> checkpoints,
      Element initialCp, List<Element> observables, List<Element> actions) {
    // TODO make this look better
    if (nodesOfInterest.size() > 0 || checkpoints.size() > 0 || initialCp != null
        || observables.size() > 0 || actions.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(
          "The following elements found within the Test Case Configuration could not be matched to the BT Model.");
      sb.append(System.getProperty("line.separator"));
      sb.append("Please check the spelling of element attribute names and values.");
      sb.append(System.getProperty("line.separator"));
      sb.append(System.getProperty("line.separator"));

      if (nodesOfInterest.size() > 0) {
        sb.append("NOIs:");
        for (Element noi : nodesOfInterest) {
          sb.append(System.getProperty("line.separator") + Constants.tabSpacing);
          List<Attribute> noiAttributes = noi.getAttributes();
          for (int i = 0; i < noiAttributes.size(); i++) {
            sb.append(noiAttributes.get(i).getName() + "[" + noiAttributes.get(i).getValue() + "]");
            if (!(i == noiAttributes.size() - 1)) {
              sb.append(", ");
            }
          }
        }
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
      }

      if (checkpoints.size() > 0) {
        sb.append("CheckPoints:");
        for (Element cp : checkpoints) {
          sb.append(System.getProperty("line.separator") + Constants.tabSpacing);
          List<Attribute> cpAttributes = cp.getAttributes();
          for (int i = 0; i < cpAttributes.size(); i++) {
            sb.append(cpAttributes.get(i).getName() + "[" + cpAttributes.get(i).getValue() + "]");
            if (!(i == cpAttributes.size() - 1)) {
              sb.append(", ");
            }
          }
        }
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
      }

      if (initialCp != null) {
        sb.append("Initial CP");
        sb.append(System.getProperty("line.separator") + Constants.tabSpacing);
        List<Attribute> initialCpAttributes = initialCp.getAttributes();
        for (int i = 0; i < initialCpAttributes.size(); i++) {
          sb.append(initialCpAttributes.get(i).getName() + "["
              + initialCpAttributes.get(i).getValue() + "]");
          if (!(i == initialCpAttributes.size() - 1)) {
            sb.append(", ");
          }
        }
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
      }

      if (observables.size() > 0) {
        sb.append("Observations:");
        for (Element or : observables) {
          sb.append(System.getProperty("line.separator") + Constants.tabSpacing);
          List<Attribute> orAttributes = or.getAttributes();
          for (int i = 0; i < orAttributes.size(); i++) {
            sb.append(orAttributes.get(i).getName() + "[" + orAttributes.get(i).getValue() + "]");
            if (!(i == orAttributes.size() - 1)) {
              sb.append(", ");
            }
          }
        }
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));
      }

      if (actions.size() > 0) {
        sb.append("User Actions:");
        for (Element ua : actions) {
          sb.append(System.getProperty("line.separator") + Constants.tabSpacing);
          List<Attribute> uaAttributes = ua.getAttributes();
          for (int i = 0; i < uaAttributes.size(); i++) {
            sb.append(uaAttributes.get(i).getName() + "[" + uaAttributes.get(i).getValue() + "]");
            if (!(i == uaAttributes.size() - 1)) {
              sb.append(", ");
            }
          }
        }
      }
      JOptionPane.showMessageDialog(null, sb.toString());
    }
  }

  /**
   * Write label that shows configured CP nodes.
   */
  private void updateNOITA() {
    taNOI.setText("");
    for (Node node : chosenNOIs) {
      if (taNOI.getText().equals("")) {
        taNOI.setText(taNOI.getText() + node);
      } else {
        taNOI.setText(taNOI.getText() + ", \n" + node);
      }
    }
    taNOI.setText(taNOI.getText().replaceAll(";", ": "));
  }

  private void updateCPTab() {
    updateTblCP();
    updateCPTA();
  }

  private void updateTblCP() {
    Set<Node> cps = new TreeSet<Node>();
    for (int key : indexToNodesMap.keySet()) {
      for (Node node : indexToNodesMap.get(key).getNodes()) {
        if (node.getBehaviourType().equals("STATE-REALISATION")) {
          cps.add(node);
        }
      }
    }
    tblCP.setModel(new CPModel(cps));
  }

  /**
   * Populate Initial CP combo box.
   */
  private void updateCPInitial() {
    TreeSet<Node> potentialCP = new TreeSet<Node>();
    for (int key : indexToNodesMap.keySet()) {
      for (Node node : indexToNodesMap.get(key).getNodes()) {
        for (Node selectedCP : chosenCPs) {
          if (node.getComponent().equals(selectedCP.getComponent())
              && node.getBehaviour().equals(selectedCP.getBehaviour())) {
            if (node.getBehaviourType().equals("STATE-REALISATION")) {
              potentialCP.add(node);
            }
          }
        }
      }
    }
    cmbInitialState.setModel(new DefaultComboBoxModel<Node>(potentialCP.toArray(new Node[] {})));
    cmbInitialState.setSelectedIndex(-1);
    initialNode = null;
  }

  /**
   * Write label that shows configured CP nodes.
   */
  private void updateCPTA() {
    taCP.setText("");
    for (Node cp : chosenCPs) {
      if (taCP.getText().equals("")) {
        taCP.setText(taCP.getText() + cp);
      } else {
        taCP.setText(taCP.getText() + ", \n" + cp);
      }
    }
    taCP.setText(taCP.getText().replaceAll(";", ": "));
  }

  private void updateORTab() {
    updateORCmbBoxes();
    updateOTA();
    updateORInput();
  }

  private void updateORCmbBoxes() {
    Set<String> obsComp = new TreeSet<String>();
    Set<Node> obsCompToDisplay = new TreeSet<Node>();
    for (Node n : observableResponses.keySet()) {
      System.out.println(n);
      if (!obsComp.contains(n.getComponent())) {
        obsComp.add(n.getComponent());
        obsCompToDisplay.add(n);
      }
    }
    cmbORComponent
        .setModel(new DefaultComboBoxModel<Node>(obsCompToDisplay.toArray(new Node[] {})));
    updateORBehaviours();
  }

  private void updateORBehaviours() {
    Set<String> obsBehaviours = new TreeSet<String>();
    Set<Node> obsBehavioursToAdd = new TreeSet<Node>();
    for (Node n : observableResponses.keySet()) {
      if (n.getComponent().equals(((Node) cmbORComponent.getSelectedItem()).getComponent())
          && !obsBehaviours.contains(n.getBehaviour())) {
        obsBehaviours.add(n.getBehaviour());
        obsBehavioursToAdd.add(n);
      }
    }
    cmbORBehaviour
        .setModel(new DefaultComboBoxModel<Node>(obsBehavioursToAdd.toArray(new Node[] {})));
  }

  /**
   * Populate configuration for Observable Responses.
   */
  private void updateOTA() {
    taORConfigured.setText("");
    for (Entry<Node, String> component : observableResponses.entrySet()) {
      Node node = component.getKey();
      String value = component.getValue();
      if (value != null && !value.equals("")) {
        if (taORConfigured.getText().equals("")) {
          taORConfigured.setText(taORConfigured.getText() + node);
        } else {
          taORConfigured.setText(taORConfigured.getText() + ", \n" + node);
        }
      }
    }
    taORConfigured.setText(taORConfigured.getText().replaceAll(";", ": "));
  }

  /**
   * Populate input area for Observable Responses.
   */
  private void updateORInput() {
    if (observableResponses.get(cmbORBehaviour.getSelectedItem()) != null) {
      taORInput.setText(observableResponses.get(cmbORBehaviour.getSelectedItem()));
    } else {
      taORInput.setText("");
    }
  }

  private void updateUATab() {
    updateUACmbBoxes();
    updateUATA();
    updateUAInput();
  }

  private void updateUACmbBoxes() {
    Set<String> uaComp = new TreeSet<String>();
    Set<Node> uaCompToDisplay = new TreeSet<Node>();
    for (Node n : userActions.keySet()) {
      if (!uaComp.contains(n.getComponent())) {
        uaComp.add(n.getComponent());
        uaCompToDisplay.add(n);
      }
    }
    cmbUAComponent.setModel(new DefaultComboBoxModel<Node>(uaCompToDisplay.toArray(new Node[] {})));
    updateUABehaviours();
  }

  private void updateUABehaviours() {
    Set<String> uaBehaviours = new TreeSet<String>();
    Set<Node> uaBehavioursToAdd = new TreeSet<Node>();
    for (Node n : userActions.keySet()) {
      if (n.getComponent().equals(((Node) cmbUAComponent.getSelectedItem()).getComponent())
          && !uaBehaviours.contains(n.getBehaviour())) {
        uaBehaviours.add(n.getBehaviour());
        uaBehavioursToAdd.add(n);
      }
    }
    cmbUABehaviour
        .setModel(new DefaultComboBoxModel<Node>(uaBehavioursToAdd.toArray(new Node[] {})));
  }

  /**
   * Populate configuration for User Actions.
   */
  private void updateUATA() {
    taUAConfigured.setText("");
    for (Entry<Node, String[]> entry : userActions.entrySet()) {
      Node node = entry.getKey();
      String[] value = entry.getValue();
      if (!value[0].equals("")) {
        if (taUAConfigured.getText().equals("")) {
          taUAConfigured.setText(taUAConfigured.getText() + node);
        } else {
          taUAConfigured.setText(taUAConfigured.getText() + ", \n" + node);
        }
      }
    }
    taUAConfigured.setText(taUAConfigured.getText().replaceAll(";", ": "));
    updateUAChkBox();
  }

  private void updateUAChkBox() {
    if (taUAInput.getText().equals("")) {
      chckbxAppearPreAmble.setEnabled(false);
      chckbxAppearPreAmble.setSelected(false);
    } else {
      chckbxAppearPreAmble.setEnabled(true);
    }
  }

  /**
   * Populate input area for User Actions.
   */
  private void updateUAInput() {
    if (userActions.get(cmbUABehaviour.getSelectedItem()) != null) {
      taUAInput.setText(userActions.get(cmbUABehaviour.getSelectedItem())[0]);
      if (userActions.get(cmbUABehaviour.getSelectedItem())[1].equals("true")) {
        chckbxAppearPreAmble.setSelected(true);
      } else {
        chckbxAppearPreAmble.setSelected(false);
      }
    }
    updateUAChkBox();
  }

  private void updateTPTbls() {
    if (currentNode == null) {
      currentNode = initialNode;
    }
    for (TestCase tc : testCases) {
      if (!(tc.getStartNode().getBlockIndex() == currentNode.getBlockIndex())) {
        ArrayList<Integer> blocks = calcTotalTestCaseSteps(currentNode, tc);
        tc.setStepsAway(blocks, getNodeList(blocks));
      }
    }
  }

  private void populateTPTab(ArrayList<ArrayList<Integer>> rawTestCases) {
    for (int i = 0; i < rawTestCases.size(); i++) {
      ArrayList<Integer> testCase = rawTestCases.get(i);


      testCases.add(constructTestCase(testCase));
    }
    tblTP.setModel(new TestPathSelectorModel(testCases));
    updateTPTbls();
  }

  /**
   * Open folder containing test cases.
   */
  private void openFolder() {
    String path =
        System.getProperty("user.dir") + System.getProperty("file.separator") + "test-cases";
    Path thePath = Paths.get(path);
    if (Files.exists(thePath)) {
      try {
        Desktop.getDesktop().open(new File(path));
      } catch (IOException e) {
        printErrorMessage("error|8");
        e.printStackTrace();
      }
    } else {
      boolean success = (new File(path)).mkdirs();
      if (!success) {
        // Directory creation failed
        printErrorMessage("error|7");
      } else {
        try {
          Desktop.getDesktop().open(new File(System.getProperty("user.dir")
              + System.getProperty("file.separator") + "test-cases"));
        } catch (IOException e1) {
          printErrorMessage("error|8");
          e1.printStackTrace();
        }
      }
    }
  }

  /**
   * Populate test case configuration in TCC file format.
   */

  private void SaveConfig() {
    JFrame parentFrame = new JFrame();

    JFileChooser fileChooser = new JFileChooser() {
      private static final long serialVersionUID = 1L;

      @Override
      public void approveSelection() {
        File f = getSelectedFile();
        if (f.exists()) {
          int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?",
              "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
          switch (result) {
            case JOptionPane.YES_OPTION:
              super.approveSelection();
              return;
            case JOptionPane.NO_OPTION:
              return;
            case JOptionPane.CLOSED_OPTION:
              return;
            case JOptionPane.CANCEL_OPTION:
              cancelSelection();
              return;
          }
        }
        super.approveSelection();
      }
    };

    javax.swing.filechooser.FileFilter filter =
        new FileNameExtensionFilter("Test Case Config file (.tcc.xml)", new String[] {"xml"});
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);

    fileChooser.setDialogTitle("Specify a file to save");

    int userSelection = fileChooser.showSaveDialog(parentFrame);

    if (userSelection == JFileChooser.APPROVE_OPTION) {

      File fileToSave = fileChooser.getSelectedFile();
      String filename = fileToSave.getAbsolutePath();
      if (fileToSave.getAbsolutePath().endsWith(".tcc.xml")) {
        filename = filename.substring(0, filename.length() - 8);
      }
      System.out.println("Save as file: " + filename + ".tcc.xml");
      XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
      FileOutputStream fos;
      try {
        fos = new FileOutputStream(filename + ".tcc.xml");
        fos.write(xmlOut.outputString(constructTCC()).getBytes());
        fos.close();
      } catch (Exception e) {

      }
    }
  }

  private Document constructTCC() {
    Document doc = new Document();
    Element config = new Element("config");
    config.setAttribute(new Attribute("version", Constants.version));
    config.setAttribute(new Attribute("filepath", btFilePath));
    Element noi = new Element("NOI");
    for (Node node : chosenNOIs) {
      Element nodeToAdd = new Element("node");
      nodeToAdd.setAttribute("tag", node.getTag().toString());
      nodeToAdd.setAttribute("component", node.getComponent());
      nodeToAdd.setAttribute("behaviour", node.getBehaviour());
      nodeToAdd.setAttribute("behaviour-type", node.getBehaviourType());
      nodeToAdd.setAttribute("flag", node.getFlag());
      noi.addContent(nodeToAdd);
    }

    Element cp = new Element("CP");
    for (Node node : chosenCPs) {
      Element nodeToAdd = new Element("node");
      nodeToAdd.setAttribute("component", node.getComponent());
      nodeToAdd.setAttribute("behaviour", node.getBehaviour());
      cp.addContent(nodeToAdd);
    }
    if (!initialNode.equals("")) {
      Element initialCP = new Element("initial");
      initialCP.setAttribute("tag", initialNode.getTag().toString());
      initialCP.setAttribute("component", initialNode.getComponent());
      initialCP.setAttribute("behaviour", initialNode.getBehaviour());
      initialCP.setAttribute("behaviour-type", initialNode.getBehaviourType());
      initialCP.setAttribute("flag", initialNode.getFlag());
      cp.addContent(initialCP);
    }

    Element or = new Element("OR");
    for (Entry<Node, String> observation : observableResponses.entrySet()) {
      if (!observation.getValue().equals("")) {
        Node node = observation.getKey();
        Element nodeToAdd = new Element("node");
        nodeToAdd.setAttribute("tag", node.getTag().toString());
        nodeToAdd.setAttribute("component", node.getComponent());
        nodeToAdd.setAttribute("behaviour", node.getBehaviour());
        nodeToAdd.setAttribute("behaviour-type", node.getBehaviourType());
        nodeToAdd.setAttribute("flag", node.getFlag());
        nodeToAdd.setAttribute("observation", observation.getValue());
        or.addContent(nodeToAdd);
      }
    }

    Element ua = new Element("UA");
    for (Entry<Node, String[]> action : userActions.entrySet()) {
      if (!action.getValue()[0].equals("")) {
        Node node = action.getKey();
        Element nodeToAdd = new Element("node");
        nodeToAdd.setAttribute("tag", node.getTag().toString());
        nodeToAdd.setAttribute("component", node.getComponent());
        nodeToAdd.setAttribute("behaviour", node.getBehaviour());
        nodeToAdd.setAttribute("behaviour-type", node.getBehaviourType());
        nodeToAdd.setAttribute("flag", node.getFlag());
        nodeToAdd.setAttribute("action", action.getValue()[0]);
        nodeToAdd.setAttribute("preamble", action.getValue()[1]);
        ua.addContent(nodeToAdd);
      }
    }

    config.addContent(noi);
    config.addContent(cp);
    config.addContent(or);
    config.addContent(ua);
    doc.addContent(config);
    return doc;
  }

  public Main() {
    setResizable(false);
    setIconImage(
        Toolkit.getDefaultToolkit().getImage("C:\\Users\\Soh Wei Yu\\Documents\\icon.png"));
    setTitle("TP-Optimizer");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(222, -51, 1158, 850);
    setLocationRelativeTo(null);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);

    JPanel panelMain = new JPanel();
    panelMain.setBounds(63, 5, 0, 900);
    panelMain.setLayout(new GridLayout(0, 1, 0, 0));
    createMainButtons(panelMain);

    contentPane.add(createMenuBar());

    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBounds(10, 131, 1131, 660);
    contentPane.add(tabbedPane);

    tabbedPane.addTab(Constants.tab1Name, null, createNOITab(), null);
    tabbedPane.addTab(Constants.tab2Name, null, createCPTab(), null);
    tabbedPane.addTab(Constants.tab3Name, null, createOATab(), null);
    tabbedPane.addTab(Constants.tab4Name, null, createUATab(), null);
    tabbedPane.addTab(Constants.tab5Name, null, createTPTab(), null);

    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      tabbedPane.setEnabledAt(i, false);
    }
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.setBounds(0, 0, 1158, 21);

    JMenu mnFile = new JMenu("File");
    menuBar.add(mnFile);
    mnFile.setMnemonic(KeyEvent.VK_F);

    /**
     * Load BT File.
     */
    JMenuItem mntmLoad = new JMenuItem("Load BTModel or Test Case Config");
    mntmLoad.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser chooser = new JFileChooser(
            System.getProperty("user.dir") + System.getProperty("file.separator") + "models");
        FileNameExtensionFilter filter =
            new FileNameExtensionFilter("BT Model", "bt", "btc", "xml");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(false);
        int retVal = chooser.showOpenDialog(frame);

        if (retVal == JFileChooser.APPROVE_OPTION) {
          File f = chooser.getSelectedFile();
          System.out.println("You chose " + f.getPath());
          btFilePath = (f.getPath()).replace("\\", "/");
          if (btFilePath.endsWith("tcc.xml")) {
            String result = connectToServer();
            if (!(result.equals("") || result.contains("error"))) {
              System.out.println("loading config");
              loadTCC(f);
            } else {
              printErrorMessage("error|4|");
            }
          } else if (btFilePath.endsWith("btc") || btFilePath.endsWith("bt")) {
            String result = connectToServer();
            if (!(result.equals("") || result.contains("error"))) {
              System.out.println("loading model");
              loadBTModel(f);
            } else {
              printErrorMessage("error|4|");
            }
          } else {
            printErrorMessage("error|9|");
          }
        } else if (retVal == JFileChooser.ERROR_OPTION) {
          printErrorMessage("error|6|");
        }
      }
    });
    mnFile.add(mntmLoad);


    JMenuItem mntmSave = new JMenuItem("Save Test Case Config");
    mntmSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (isLoaded) {
          SaveConfig();
        } else {
          printErrorMessage("error|11|");
        }
      }
    });
    mnFile.add(mntmSave);

    JMenuItem mntmExit = new JMenuItem("Exit");
    mntmExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (isLoaded == false) {
          System.exit(0);
        } else {
          int dialogButton = JOptionPane.YES_NO_OPTION;
          int dialogResult = JOptionPane.showConfirmDialog(null,
              "Would you like to save your configurations first?", "Warning", dialogButton);
          if (dialogResult == JOptionPane.YES_OPTION) {
            SaveConfig();
            System.exit(0);
          }
          System.exit(0);
        }
      }
    });
    mnFile.add(mntmExit);

    JMenu mnHelp = new JMenu("Help");
    menuBar.add(mnHelp);

    JMenuItem mntmTips = new JMenuItem("Tips");
    mntmTips.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(frame,
            "Instructions on generating test cases: \n\n1) File > Load BT > Choose BT file \n2) Fill in 'Nodes of Interest', 'CheckPoints' \n3) Click on Generate Test Paths \n4) Fill in Observable Responses and User Actions \n5) Click Generate Test Cases \n6) Save Config by File > Save Test Case Config\n\nDo note that you do not need to fill in 'Observable Responses' and 'User Actions' to generate test paths, but that is required for generating test cases.",
            "Tips", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    mnHelp.add(mntmTips);

    JMenuItem mntmAbout = new JMenuItem("About");
    mntmAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(frame,
            "TP-Optimizer\r\nBy Mitchell Savell\r\nmitchellsavell@gmail.com\r\n\r\nBased on TCGen-UI\r\nBy Soh Wei Yu",
            "About", JOptionPane.PLAIN_MESSAGE);

      }
    });
    mnHelp.add(mntmAbout);
    return menuBar;
  }

  private void createMainButtons(JPanel panelMain) {

    btnGenerateExcel = new JButton("Generate Excel Output");
    btnGenerateExcel.setEnabled(false);
    btnGenerateExcel.setBounds(425, 59, 281, 51);
    btnGenerateExcel.setEnabled(false);
    /**
     * Generate natural language test cases in Excel format.
     */
    btnGenerateExcel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // TODO Output to Excel
      }
    });
    contentPane.add(btnGenerateExcel);

    btnGenerateTestCases = new JButton("Generate Test Cases");
    btnGenerateTestCases.setEnabled(false);
    btnGenerateTestCases.setBounds(72, 59, 281, 51);
    /**
     * Call API and generate TCPs, pre-amble and post-amble.
     */
    btnGenerateTestCases.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ArrayList<String> commands = creatTestCaseGenCommands();
        ArrayList<ArrayList<Integer>> testCases = sendTestCaseGenCommands(commands);
        populateTPTab(testCases);
        tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.tab5Name), true);
      }

    });
    contentPane.setLayout(null);
    contentPane.add(panelMain);
    contentPane.add(btnGenerateTestCases);

    JButton btnOpenTestCase = new JButton("Open Test Case Folder");
    btnOpenTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openFolder();
      }
    });
    btnOpenTestCase.setBounds(778, 59, 301, 51);
    contentPane.add(btnOpenTestCase);
  }

  private JPanel createNOITab() {
    JPanel panelNOI = new JPanel();

    panelNOI.setLayout(null);
    JLabel lblSelectNodes = new JLabel("Select nodes of interest:");
    lblSelectNodes.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectNodes.setBounds(30, 20, 191, 22);
    panelNOI.add(lblSelectNodes);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(30, 45, 629, 455);
    panelNOI.add(scrollPane);

    tblNOI = new JTable(new NOIModel(null));
    tblNOI.setDefaultRenderer(Node.class, new NOICell());
    tblNOI.setDefaultEditor(Node.class, new NOICell());
    tblNOI.setRowHeight(40);
    tblNOI.setBorder(null);
    scrollPane.setViewportView(tblNOI);
    tblNOI.setFillsViewportHeight(true);
    tblNOI.setCellSelectionEnabled(true);
    tblNOI.setTableHeader(null);

    JButton btnRemoveNOI = new JButton("Remove NOI");
    btnRemoveNOI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i : tblNOI.getSelectedRows()) {
          chosenNOIs.remove((Node) tblNOI.getValueAt(i, 0));
        }
        updateNOITA();
      }
    });
    btnRemoveNOI.setBounds(161, 526, 151, 61);
    panelNOI.add(btnRemoveNOI);

    JButton btnAddNOI = new JButton("Add NOI");
    btnAddNOI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        for (int i : tblNOI.getSelectedRows()) {
          chosenNOIs.add((Node) tblNOI.getValueAt(i, 0));
        }
        updateNOITA();
      }
    });
    btnAddNOI.setBounds(381, 526, 151, 61);
    panelNOI.add(btnAddNOI);

    taNOI.setEditable(false);
    taNOI.setBounds(740, 45, 345, 580);
    panelNOI.add(taNOI);

    JLabel lblSelectedNOI = new JLabel("Selected nodes:");
    lblSelectedNOI.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectedNOI.setBounds(740, 25, 236, 22);
    panelNOI.add(lblSelectedNOI);

    return panelNOI;
  }

  private JPanel createCPTab() {
    JPanel panelCP = new JPanel();

    panelCP.setLayout(null);

    JLabel lblCPNote1 = new JLabel("Note: Selection restricted to state realisation nodes");
    lblCPNote1.setBounds(410, 30, 247, 14);
    panelCP.add(lblCPNote1);

    JLabel lblSelectedCP = new JLabel("Selected checkpoints:");
    lblSelectedCP.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectedCP.setBounds(740, 20, 236, 22);
    panelCP.add(lblSelectedCP);
    taCP.setEditable(false);

    taCP.setBounds(740, 45, 345, 580);
    panelCP.add(taCP);
    cmbInitialState.setBounds(252, 546, 376, 32);
    panelCP.add(cmbInitialState);

    JLabel lblCPNote2 =
        new JLabel("Note: Selection restricted to CheckPoints which you have chosen.");
    lblCPNote2.setBounds(158, 608, 361, 14);
    panelCP.add(lblCPNote2);

    JLabel lblInitialState = new JLabel("Select Initial State of the System:");
    lblInitialState.setBounds(81, 555, 161, 14);
    panelCP.add(lblInitialState);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(30, 45, 630, 410);
    panelCP.add(scrollPane);

    tblCP = new JTable(new CPModel(null));
    tblCP.setDefaultRenderer(Node.class, new CPCell());
    tblCP.setDefaultEditor(Node.class, new CPCell());
    tblCP.setRowHeight(40);
    tblCP.setBorder(null);
    scrollPane.setViewportView(tblCP);
    tblCP.setFillsViewportHeight(true);
    tblCP.setCellSelectionEnabled(true);
    tblCP.setTableHeader(null);

    JButton btnRemoveCP = new JButton("Remove CP");
    btnRemoveCP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i : tblCP.getSelectedRows()) {
          chosenCPs.remove((Node) tblCP.getValueAt(i, 0));
        }
        updateCPTA();
        updateCPInitial();
      }
    });
    btnRemoveCP.setBounds(158, 468, 151, 61);
    panelCP.add(btnRemoveCP);

    JButton btnAddCP = new JButton("Add CP");
    btnAddCP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        for (int i : tblCP.getSelectedRows()) {
          chosenCPs.add((Node) tblCP.getValueAt(i, 0));
        }
        updateCPTA();
        updateCPInitial();
      }
    });
    btnAddCP.setBounds(378, 468, 151, 61);
    panelCP.add(btnAddCP);

    JLabel lblSelectCheckpoints = new JLabel("Select checkpoints:");
    lblSelectCheckpoints.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectCheckpoints.setBounds(30, 20, 191, 22);
    panelCP.add(lblSelectCheckpoints);
    cmbInitialState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbInitialState.getSelectedItem() != null) {
          initialNode = (Node) cmbInitialState.getSelectedItem();
        }
      }
    });
    return panelCP;
  }

  private JPanel createOATab() {
    JPanel panelOA = new JPanel();
    tabbedPane.addTab("Observable Responses", null, panelOA, null);
    panelOA.setLayout(null);

    JLabel lblORComponent = new JLabel("Select Component Name:");
    lblORComponent.setBounds(331, 38, 121, 14);
    panelOA.add(lblORComponent);

    cmbORComponent.setBounds(460, 30, 310, 30);
     cmbORComponent.setRenderer(new ComponentCmbRenderer());
    panelOA.add(cmbORComponent);

    cmbORBehaviour.setBounds(460, 75, 310, 30);
     cmbORBehaviour.setRenderer(new BehaviourCmbRenderer());
    panelOA.add(cmbORBehaviour);

    JLabel lblORBehaviour = new JLabel("Select Behaviour:");
    lblORBehaviour.setBounds(368, 83, 84, 14);
    panelOA.add(lblORBehaviour);
    taORInput.setLineWrap(true);

    taORInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent arg0) {
        for (Node n : observableResponses.keySet()) {
          if (n.equalsSimple((Node) cmbORBehaviour.getSelectedItem())) {
            observableResponses.put(n, taORInput.getText());
          }
        }
        updateOTA();
      }
    });
    taORInput.setBounds(10, 205, 630, 410);
    panelOA.add(taORInput);

    cmbORComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbORComponent.getSelectedItem() != null) {
          updateORBehaviours();
          updateORInput();
        }
      }
    });


    cmbORBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbORComponent.getSelectedItem() != null) {
          updateORInput();
        }
      }
    });


    JLabel lblORInput = new JLabel("Observable Response:");
    lblORInput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORInput.setBounds(10, 172, 174, 22);
    panelOA.add(lblORInput);

    JLabel lblNoteSelectionRestricted_1 = new JLabel(
        "Note: Selection restricted to all components except events, external inputs, selections & guards.");
    lblNoteSelectionRestricted_1.setBounds(331, 127, 464, 14);
    panelOA.add(lblNoteSelectionRestricted_1);

    JLabel lblORConfigured = new JLabel("Observable Responses configured:");
    lblORConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORConfigured.setBounds(670, 172, 299, 22);
    panelOA.add(lblORConfigured);


    taORConfigured.setEditable(false);
    taORConfigured.setBounds(670, 205, 440, 410);

    panelOA.add(taORConfigured);
    return panelOA;
  }

  private JPanel createUATab() {
    JPanel panelUA = new JPanel();
    tabbedPane.addTab("User Actions/External Inputs", null, panelUA, null);
    panelUA.setLayout(null);

    JLabel lblUAComponent = new JLabel("Select Component Name:");
    lblUAComponent.setBounds(329, 38, 121, 14);
    panelUA.add(lblUAComponent);

    JLabel lblUABehaviour = new JLabel("Select Behaviour:");
    lblUABehaviour.setBounds(366, 83, 84, 14);
    panelUA.add(lblUABehaviour);

    cmbUABehaviour.setBounds(460, 75, 310, 30);
     cmbUABehaviour.setRenderer(new BehaviourCmbRenderer());
    panelUA.add(cmbUABehaviour);

    cmbUAComponent.setBounds(460, 30, 310, 30);
     cmbUAComponent.setRenderer(new ComponentCmbRenderer());
    panelUA.add(cmbUAComponent);


    JLabel lblUAinput = new JLabel("Action to be taken:");
    lblUAinput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUAinput.setBounds(10, 172, 151, 22);
    panelUA.add(lblUAinput);
    taUAInput.setLineWrap(true);

    taUAInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        updateUAChkBox();
        for (Node n : userActions.keySet()) {
          if (n.equalsSimple((Node) cmbUABehaviour.getSelectedItem())) {
            userActions.put(n, new String[] {taUAInput.getText(),
                Boolean.toString(chckbxAppearPreAmble.isSelected())});
          }
        }
        updateUATA();
      }
    });
    taUAInput.setBounds(10, 205, 640, 410);
    panelUA.add(taUAInput);


    cmbUAComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (cmbUAComponent.getSelectedItem() != null) {
          updateUABehaviours();
          updateUAInput();
        }
      }
    });


    cmbUABehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (cmbUAComponent.getSelectedItem() != null) {
          updateUAInput();
        }
      }
    });

    JLabel lblNoteSelectionRestricted_2 =
        new JLabel("Note: Selection restricted to events & external inputs.");
    lblNoteSelectionRestricted_2.setBounds(510, 135, 260, 14);
    panelUA.add(lblNoteSelectionRestricted_2);


    taUAConfigured.setEditable(false);
    taUAConfigured.setBounds(670, 205, 440, 410);
    panelUA.add(taUAConfigured);

    JLabel lblUserActionsConfigured = new JLabel("Actions configured:");
    lblUserActionsConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUserActionsConfigured.setBounds(670, 172, 180, 22);
    panelUA.add(lblUserActionsConfigured);
    chckbxAppearPreAmble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!taUAInput.getText().equals("")) {
          if (cmbUAComponent.getSelectedItem() != null
              && cmbUABehaviour.getSelectedItem() != null) {
            for (Node n : userActions.keySet()) {
              if (n.equalsSimple((Node) cmbUABehaviour.getSelectedItem())) {
                userActions.put(n, new String[] {taUAInput.getText(),
                    Boolean.toString(chckbxAppearPreAmble.isSelected())});
              }
            }
          }
          updateUATA();
        }
      }
    });

    chckbxAppearPreAmble.setBounds(329, 105, 481, 23);
    panelUA.add(chckbxAppearPreAmble);
    return panelUA;
  }

  private JPanel createTPTab() {
    JPanel panelTP = new JPanel();

    panelTP.setLayout(null);
    
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(10, 33, 340, 507);
    panelTP.add(scrollPane);
    
        tblTP = new JTable(new TestPathSelectorModel(null));
        scrollPane.setViewportView(tblTP);
        tblTP.setDefaultRenderer(TestCase.class, new TestPathSelectorCell());
        tblTP.setDefaultEditor(TestCase.class, new TestPathSelectorCell());
    tblTP.setRowHeight(60);
    
    JScrollPane scrollPane_1 = new JScrollPane();
    scrollPane_1.setBounds(360, 33, 756, 507);
    panelTP.add(scrollPane_1);
    
        tblFinal = new JTable();
        scrollPane_1.setViewportView(tblFinal);
        tblFinal.setModel(new TestPathModel(null));
        tblFinal.getColumnModel().getColumn(0).setResizable(false);
        tblFinal.getColumnModel().getColumn(1).setResizable(false);
        tblFinal.getColumnModel().getColumn(2).setResizable(false);

    JLabel lblGeneratedTestCases = new JLabel("Generated Test Cases:");
    lblGeneratedTestCases.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblGeneratedTestCases.setBounds(10, 11, 299, 22);
    panelTP.add(lblGeneratedTestCases);

    JLabel lblCurrentTestPath = new JLabel("Current Test Path");
    lblCurrentTestPath.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblCurrentTestPath.setBounds(360, 11, 299, 22);
    panelTP.add(lblCurrentTestPath);

    JButton btnAddTestCase = new JButton("Add Test Case ->");
    btnAddTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {}
    });
    btnAddTestCase.setBounds(168, 551, 151, 61);
    panelTP.add(btnAddTestCase);

    JButton btnUndo = new JButton("Undo");
    btnUndo.setBounds(487, 551, 151, 61);
    panelTP.add(btnUndo);

    JButton btnExportToHtml = new JButton("Export to HTML");
    btnExportToHtml.setBounds(806, 551, 151, 61);
    panelTP.add(btnExportToHtml);
    return panelTP;
  }

  private ArrayList<Integer> calcTotalTestCaseSteps(Node startNode, TestCase tc) {
    StringBuilder sb = new StringBuilder();
    sb.append("(test-path-preamble " + startNode.getBlockIndex() + " (");
    for (Node n : tc.getNodeSteps()) {
      sb.append(n.getBlockIndex() + " ");
    }
    sb.setLength(sb.length() - 1);
    sb.append("))");
    ArrayList<ArrayList<Integer>> result =
        sendTestCaseGenCommands(new ArrayList<String>(Arrays.asList(sb.toString())));
    if (result.size() > 0) {
      return result.get(0);
    } else {
      return null;
    }
  }

  private ArrayList<String> creatTestCaseGenCommands() {
    ArrayList<String> commands = new ArrayList<String>();
    ArrayList<Integer> noiBlocks = new ArrayList<Integer>();
    ArrayList<Integer> startingBlocks = new ArrayList<Integer>();
    ArrayList<Integer> endingBlocks = new ArrayList<Integer>();
    for (Node n : chosenCPs) {
      if ((n.getFlag() == "")) {
        startingBlocks.add(n.getBlockIndex());
        endingBlocks.add(n.getBlockIndex());
      } else {
        endingBlocks.add(n.getBlockIndex());
      }
    }
    for (Node n : chosenNOIs) {
      noiBlocks.add(n.getBlockIndex());
    }
    StringBuilder sb = new StringBuilder();
    for (int i : startingBlocks) {
      for (int j : endingBlocks) {
        sb.append("(find-test-paths " + i + " (");
        for (int k : noiBlocks) {
          sb.append(k + " ");
        }
        // Remove final space added
        sb.setLength(sb.length() - 1);
        sb.append(") " + j + " (");
        for (int k : endingBlocks) {
          sb.append(k + " ");
        }
        // Remove final space added
        sb.setLength(sb.length() - 1);
        sb.append("))");
        commands.add(sb.toString());
        sb.setLength(0);
      }
    }
    return commands;
  }

  private ArrayList<ArrayList<Integer>> sendTestCaseGenCommands(ArrayList<String> commands) {
    ArrayList<ArrayList<Integer>> testCases = new ArrayList<ArrayList<Integer>>();
    for (String command : commands) {
      String result = sbcl.sendCommand(command);
      System.out.println(result);
      if (result
          .matches("<result><path>(<block-index>\\d+<\\/block-index>)+<\\/path><\\/result>")) {
        org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();

        // XML parser has problem reading ||&
        result = result.replace("||&", "");

        org.jdom2.Document doc = null;
        try {
          doc = saxBuilder.build(new StringReader(result));
        } catch (JDOMException | IOException e) {
          e.printStackTrace();
        }

        Element root = doc.getRootElement();
        ArrayList<Integer> pathList = new ArrayList<Integer>();
        List<Element> pathXML = root.getChild("path").getChildren("block-index");
        for (Element block : pathXML) {
          pathList.add(Integer.parseInt(block.getValue()));
        }
        testCases.add(pathList);
      }
    }
    for (ArrayList<Integer> list : testCases) {
      System.out.println(list);
    }
    return testCases;
  }

  private static void printErrorMessage(String error) {
    int errorIndex =
        Integer.parseInt(error.substring(error.indexOf('|') + 1, error.lastIndexOf('|')));
    String errorMessage = error.substring(error.lastIndexOf('|') + 1);
    switch (errorIndex) {
      case 0:
        JOptionPane.showMessageDialog(null,
            "IP address name resolution failed.\r\n" + errorMessage);
        break;
      case 1:
        JOptionPane.showMessageDialog(null,
            "Unkown error while trying to connect to server.\r\n" + errorMessage);
        break;
      case 2:
        JOptionPane.showMessageDialog(null,
            "Message could not be sent. Connection may have been lost" + errorMessage);
        break;
      case 3:
        JOptionPane.showMessageDialog(null, "Unable to close lisp server.");
        break;
      case 4:
        JOptionPane.showMessageDialog(null, "Unable to start lisp server.");
        break;
      case 5:
        JOptionPane.showMessageDialog(null, "Communication with server timed out.");
        break;
      case 6:
        JOptionPane.showMessageDialog(null, "Unable to read Behaviour Tree Model");
        break;
      case 7:
        JOptionPane.showMessageDialog(null,
            "Unable to create folder. Please ensure write access is available in "
                + System.getProperty("user.dir"));
        break;
      case 8:
        JOptionPane.showMessageDialog(null,
            "An unknown error blocked this application from opening "
                + System.getProperty("user.dir") + System.getProperty("file.separator")
                + "test-cases");
        break;
      case 9:
        JOptionPane.showMessageDialog(null,
            "Selected file has invalid type. Application can read \"btc\" and \"tcc.xml\" files only.");
        break;
      case 10:
        JOptionPane.showMessageDialog(null,
            "Selected Test Case Configuration is using a different version to the current application.");
        break;
      case 11:
        JOptionPane.showMessageDialog(null, "There is nothing to save.");
        break;
      default:
        JOptionPane.showMessageDialog(null, "Other error.\r\n" + errorMessage);
        break;
    }
  }

  private String connectToServer() {
    String connectionResult = "";
    try {
      connectionResult = sbcl.connect("localhost", 12);
      if (!connectionResult.equals("success")) {
        printErrorMessage(connectionResult);
        return connectionResult;
      }
    } catch (InterruptedException | IOException e) {
      // nop
    }
    return connectionResult;
  }

  private TestCase constructTestCase(Collection<Integer> blocks) {
    ArrayList<Node> nodeList = getNodeList(blocks);
    return new TestCase(blocks, nodeList);
  }

  private ArrayList<Node> getNodeList(Collection<Integer> blocks) {
    ArrayList<Node> nodeList = new ArrayList<Node>();
    for (int i : blocks) {
      nodeList.addAll(indexToNodesMap.get(i).getNodes());
    }
    return nodeList;
  }
}
