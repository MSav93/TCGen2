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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.table.DefaultTableModel;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.SBCLPipe;

import other.AlphanumComparator;
import other.TableCellListener;
import other.TestPath;
import other.TestPathCell;
import other.TestPathModel;
import tree.BTNode;
import tree.NodeData;
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
   * Components on first tab
   */
  private final JLabel lblSelectNodes = new JLabel("Select nodes of interest:");
  private JTable tblNOI;

  /**
   * Components on second tab
   */
  private JComboBox<String> cmbCPComponent = new JComboBox<String>();
  private JComboBox<String> cmbCPBehaviour = new JComboBox<String>();
  private final JCheckBox chkCP = new JCheckBox("This is a CheckPoint");
  private JComboBox<String> cmbInitialState = new JComboBox<String>();
  private final TextArea taCP = new TextArea();

  /**
   * Components on third tab
   */
  private JComboBox<String> cmbORComponent = new JComboBox<String>();
  private JComboBox<String> cmbORBehaviour = new JComboBox<String>();
  private final JTextArea taORInput = new JTextArea();
  private final TextArea taORConfigured = new TextArea();

  /**
   * Components on fourth tab
   */
  private JComboBox<String> cmbUAComponent = new JComboBox<String>();
  private JComboBox<String> cmbUABehaviour = new JComboBox<String>();
  private final TextArea taUAConfigured = new TextArea();
  private final JTextArea taUAInput = new JTextArea();
  private final JCheckBox chckbxAppearPreAmble = new JCheckBox(
      "This condition should appear in the Pre-Amble/This is an External Input not under user's control");

  /**
   * Maps to store info about BT Model
   */
  private TreeMap<Integer, BTNode> indexToNodeMap = new TreeMap<Integer, BTNode>();
  HashMap<String, Integer> tagToIndexMap = new HashMap<>(); // map tag to block index
  private TreeMap<String, NodeData> tagToNodeDataMap = new TreeMap<String, NodeData>();
  private HashMap<String, ArrayList<NodeData>> compToBehaviourMap =
      new HashMap<String, ArrayList<NodeData>>();

  /**
   * Variables that must be saved
   */
  private String btXML;
  private String btFilePath;
  private String initialNode = "";


  /**
   * Other
   */
  private SBCLPipe sbcl = new SBCLPipe();
  private Boolean isLoaded = false;

  ArrayList<String> chosenNOIs = new ArrayList<String>();
  ArrayList<String> chosenCPs = new ArrayList<String>();
  HashMap<String, String> observableResponses = new HashMap<>();
  HashMap<String, String[]> userActions = new HashMap<>();

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
    tblNOI.setModel(new DefaultTableModel(new Object[][] {},
        new String[] {"Tag", "Component", "Behaviour", "Behaviour Type", "Select"}) {
      private static final long serialVersionUID = -8673519275754805408L;
      @SuppressWarnings("rawtypes")
      Class[] columnTypes =
          new Class[] {String.class, String.class, String.class, String.class, Boolean.class};

      @SuppressWarnings({"unchecked", "rawtypes"})
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    tblNOI.getColumnModel().getColumn(1).setResizable(false);
    tblNOI.getColumnModel().getColumn(2).setResizable(false);
    tblNOI.getColumnModel().getColumn(3).setResizable(false);
    tblNOI.getColumnModel().getColumn(4).setResizable(false);

    chosenCPs = new ArrayList<String>();
    observableResponses = new HashMap<>();
    userActions = new HashMap<>();
    tagToIndexMap = new HashMap<>();
    indexToNodeMap = new TreeMap<>();
    tagToNodeDataMap = new TreeMap<String, NodeData>();
    compToBehaviourMap = new HashMap<String, ArrayList<NodeData>>();

    cmbInitialState.removeAllItems();
    cmbCPComponent.removeAllItems();
    cmbCPBehaviour.removeAllItems();
    cmbORComponent.removeAllItems();
    cmbORBehaviour.removeAllItems();
    cmbUAComponent.removeAllItems();
    cmbUABehaviour.removeAllItems();

    chkCP.setSelected(false);

    chckbxAppearPreAmble.setSelected(false);
    taORInput.setText("");
    taUAInput.setText("");

    taCP.setText("");

    taORConfigured.setText("");
    taUAConfigured.setText("");

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
    indexToNodeMap = modelReader.getIndexToNodeMap();
    tagToIndexMap = modelReader.getTagToIndexMap();
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
      String tag = (String) tblNOI.getValueAt(i, 0);
      String component = (String) tblNOI.getValueAt(i, 1);
      String behaviour = (String) tblNOI.getValueAt(i, 2);
      String behaviourType = (String) tblNOI.getValueAt(i, 3);
      for (Element node : nodesOfInterest) {
        if (checkElementsAttributes(node, "noi")) {
          if (tag.equals(node.getAttributeValue("tag"))
              && component.equals(node.getAttributeValue("component"))
              && behaviour.equals(node.getAttributeValue("behaviour"))
              && behaviourType.equals(node.getAttributeValue("behaviour-type"))) {
            // Selects this node as a NOI
            chosenNOIs.add(new String(tblNOI.getValueAt(i, 0) + ";" + tblNOI.getValueAt(i, 1) + ";"
                + tblNOI.getValueAt(i, 2) + ";" + tblNOI.getValueAt(i, 3)));
            tblNOI.setValueAt(true, i, 4);
            // Remove node from remaining list of unmatched nodes
            nodesToBeRemoved.add(node);
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
          ArrayList<NodeData> nodes = compToBehaviourMap.get(component);
          for (NodeData node : nodes) {
            if (node.getBehaviour().equals(behaviour)) {
              // Selects this node as a CP
              chosenCPs.add(component + ";" + behaviour);
              // Remove node from remaining list of unmatched nodes
              checkpointsToBeRemoved.add(cp);
              break;
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
      String initial = "";
      if (checkElementsAttributes(initialCp, "initial")) {
        String cpComponent = initialCp.getAttributeValue("component");
        String cpBehaviour = initialCp.getAttributeValue("behaviour");
        boolean found = false;
        if (compToBehaviourMap.containsKey(cpComponent)) {
          ArrayList<NodeData> nodes = compToBehaviourMap.get(cpComponent);
          for (NodeData node : nodes) {
            if (node.getBehaviour().equals(cpBehaviour)) {
              found = true;
              break;
            }
          }
        }
        if (found) {
          initial = "Node " + initialCp.getAttributeValue("tag") + ": "
              + initialCp.getAttributeValue("component") + " - "
              + initialCp.getAttributeValue("behaviour") + " ["
              + initialCp.getAttributeValue("behaviour-type") + "]";
          initialCp = null;
        }
      }
      if (initial != "") {
        updateCPInitial();
        for (int i = 0; i < cmbInitialState.getItemCount(); i++) {
          if (initial.equals(cmbInitialState.getItemAt(i))) {
            cmbInitialState.setSelectedItem(cmbInitialState.getItemAt(i));
            initialNode = cmbInitialState.getItemAt(i);
          }
        }
      }
    }

    // Observable Responses
    List<Element> observables = config.getChild("OR").getChildren();
    ArrayList<Element> observablesToBeRemoved = new ArrayList<Element>();
    for (Element or : observables) {
      if (checkElementsAttributes(or, "or")) {
        String orComponent = or.getAttributeValue("component");
        String orBehaviour = or.getAttributeValue("behaviour");
        String orBehaviourType = or.getAttributeValue("behaviour-type");
        if (compToBehaviourMap.containsKey(orComponent)) {
          ArrayList<NodeData> nodes = compToBehaviourMap.get(orComponent);
          for (NodeData node : nodes) {
            if (node.getBehaviour().equals(orBehaviour)
                && node.getBehaviourType().equals(orBehaviourType)) {
              // Populate this nodes observables
              observableResponses.put(
                  orComponent + ";" + orBehaviour + " [" + orBehaviourType + "]",
                  or.getAttributeValue("observation"));
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
        String uaComponent = ua.getAttributeValue("component");
        String uaBehaviour = ua.getAttributeValue("behaviour");
        String uaBehaviourType = ua.getAttributeValue("behaviour-type");
        if (compToBehaviourMap.containsKey(uaComponent)) {
          ArrayList<NodeData> nodes = compToBehaviourMap.get(uaComponent);
          for (NodeData node : nodes) {
            if (node.getBehaviour().equals(uaBehaviour)
                && node.getBehaviourType().equals(uaBehaviourType)) {
              // Populate this nodes actions
              userActions.put(uaComponent + ";" + uaBehaviour + " [" + uaBehaviourType + "]",
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
    boolean valid = true;
    switch (elementType) {
      case "noi":
        valid = element.hasAttributes();
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
          valid = (element.getAttributeValue("component") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour") != null);
        }
        if (valid) {
          valid = (element.getAttributeValue("behaviour-type") != null);
        }
        return valid;
      case "or":
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
          valid = (element.getAttributeValue("observation") != null);
        }
        return valid;
      case "ua":
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

    updateCPCmbBoxes();
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
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        tagToNodeDataMap.put(node.getTag(), node);
        if (!compToBehaviourMap.containsKey(node.getComponent())) {
          compToBehaviourMap.put(node.getComponent(), new ArrayList<NodeData>());
        }
        compToBehaviourMap.get(node.getComponent()).add(node);
        Object[] rowData = {node.getTag(), node.getComponent(), node.getBehaviour(),
            node.getBehaviourType(), false};
        ((DefaultTableModel) tblNOI.getModel()).addRow(rowData);
      }
    }
  }

  private void updateDisplay() {
    updateCPTab();
    updateORTab();
    updateUATab();
  }

  private void reportConfigDifferences(List<Element> nodesOfInterest, List<Element> checkpoints,
      Element initialCp, List<Element> observables, List<Element> actions) {
    //TODO make this look better
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

  private void updateCPTab() {
    updateCPCmbBoxes();
    updateCPTA();
    updateCPCheckBox();
  }

  private void updateCPCmbBoxes() {
    Map<String, ArrayList<String>> cpComponents = new HashMap<String, ArrayList<String>>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getBehaviourType().equals("STATE-REALISATION")) {
          if (!cpComponents.containsKey(node.getComponent())) {
            cpComponents.put(node.getComponent(), new ArrayList<String>());
          }
          cpComponents.get(node.getComponent()).add(node.getBehaviour());
        }
      }
    }

    String[] cmbData = cpComponents.keySet().toArray(new String[0]);
    cmbCPComponent.setModel(new DefaultComboBoxModel<String>(clean(cmbData)));
    updateCPBehaviours();
    updateCPCheckBox();
  }

  private void updateCPBehaviours() {
    Set<String> cmbBehaviourData = new HashSet<String>();
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        if (node.getComponent().equals(cmbCPComponent.getItemAt(cmbCPComponent.getSelectedIndex()))
            && node.getBehaviourType().equals("STATE-REALISATION")) {
          cmbBehaviourData.add(node.getBehaviour());
        }
      }
    }
    cmbCPBehaviour
        .setModel(new DefaultComboBoxModel<String>(clean(cmbBehaviourData.toArray(new String[0]))));
    updateCPCheckBox();
  }

  /**
   * Populate Initial CP combo box.
   */
  private void updateCPInitial() {
    String[] potentialCP = new String[indexToNodeMap.size() + 1];
    int i = 0;
    potentialCP[i++] = "";
    for (int key : indexToNodeMap.keySet()) {
      for (NodeData node : indexToNodeMap.get(key).getData()) {
        for (String selectedCP : chosenCPs) {
          String[] cpParts = selectedCP.split(";");
          if (node.getComponent().equals(cpParts[0]) && node.getBehaviour().equals(cpParts[1])) {
            if (node.getBehaviourType().equals("STATE-REALISATION")) {
              potentialCP[i] = "Node " + node.getTag() + ": " + node.getComponent() + " - "
                  + node.getBehaviour() + " [" + node.getBehaviourType() + "]";
              i++;
            }
          }
        }
      }
    }
    potentialCP = clean(potentialCP);
    Arrays.sort(potentialCP, new AlphanumComparator());
    cmbInitialState.setModel(new DefaultComboBoxModel<String>(potentialCP));
    if (cmbInitialState.getSelectedItem() != null) {
      initialNode = cmbInitialState.getSelectedItem().toString();
    } else {
      initialNode = "";
    }
  }

  /**
   * Write label that shows configured CP nodes.
   */
  private void updateCPTA() {
    taCP.setText("");
    for (String node : chosenCPs) {
      if (taCP.getText().equals("")) {
        taCP.setText(taCP.getText() + node);
      } else {
        taCP.setText(taCP.getText() + ", \n" + node);
      }
    }
    taCP.setText(taCP.getText().replaceAll(";", ": "));
  }

  /**
   * Populate configuration for CP.
   */
  private void updateCPCheckBox() {
    String selectedCP = cmbCPComponent.getSelectedItem().toString() + ";"
        + cmbCPBehaviour.getSelectedItem().toString();
    if (chosenCPs.contains(selectedCP)) {
      chkCP.setSelected(true);
    } else {
      chkCP.setSelected(false);
    }
  }

  private void updateORTab() {
    updateORCmbBoxes();
    updateOTA();
    updateOR();
  }

  private void updateORCmbBoxes() {

    String[] obsComps = new String[compToBehaviourMap.size()];
    int i = 0;
    for (String key : compToBehaviourMap.keySet()) {
      boolean accepted = false;
      for (NodeData node : compToBehaviourMap.get(key)) {
        if (!Constants.unacceptedObsBehaviourTypes.contains(node.getBehaviourType())
            && !Constants.unnaceptedObsFlags.contains(node.getFlag())) {
          accepted = true;
          break;
        }
      }
      if (accepted) {
        obsComps[i++] = key;
      }
    }

    cmbORComponent.setModel(new DefaultComboBoxModel<String>(clean(obsComps)));
    updateObsBehaviours();
  }

  private void updateObsBehaviours() {
    String[] obsBehaviours =
        new String[compToBehaviourMap.get(cmbORComponent.getSelectedItem()).size()];
    int i = 0;
    if (cmbORComponent.getModel().getSize() > 0) {
      for (NodeData node : compToBehaviourMap.get(cmbORComponent.getSelectedItem())) {
        if (!Constants.unacceptedObsBehaviourTypes.contains(node.getBehaviourType())
            && !Constants.unnaceptedObsFlags.contains(node.getFlag())) {
          obsBehaviours[i++] = node.getBehaviour() + " [" + node.getBehaviourType() + "]";
        }
      }
    }
    cmbORBehaviour.setModel(new DefaultComboBoxModel<String>(clean(obsBehaviours)));
  }

  /**
   * Load the observable response for selected component.
   */
  private void updateOTA() {
    taORConfigured.setText("");
    for (Entry<String, String> component : observableResponses.entrySet()) {
      String key = component.getKey();
      String value = component.getValue();

      if (value != null && !value.equals("")) {
        if (taORConfigured.getText().equals("")) {
          taORConfigured.setText(taORConfigured.getText() + key);
        } else {
          taORConfigured.setText(taORConfigured.getText() + ", \n" + key);
        }
      }
    }
    taORConfigured.setText(taORConfigured.getText().replaceAll(";", ": "));
  }

  /**
   * Populate configuration for Observable Responses.
   */
  private void updateOR() {
    try {
      String selectedOR = cmbORComponent.getSelectedItem().toString() + ";"
          + cmbORBehaviour.getSelectedItem().toString();
      if (observableResponses.get(selectedOR) != null) {
        taORInput.setText(observableResponses.get(selectedOR));
      } else {
        taORInput.setText("");
      }
    } catch (Exception e) {

    }
  }

  private void updateUATab() {
    updateUACmbBoxes();
    updateUATA();
    updateUA();
  }

  private void updateUACmbBoxes() {
    String[] uaComp = new String[compToBehaviourMap.size()];
    int i = 0;
    for (String key : compToBehaviourMap.keySet()) {
      boolean accepted = false;
      for (NodeData node : compToBehaviourMap.get(key)) {
        if (Constants.acceptedUABehaviourTypes.contains(node.getBehaviourType())) {
          accepted = true;
          break;
        }
      }
      if (accepted) {
        uaComp[i++] = key;
      }
    }

    cmbUAComponent.setModel(new DefaultComboBoxModel<String>(clean(uaComp)));
    updateUABehaviours();
  }

  private void updateUABehaviours() {
    String[] uaBehaviours =
        new String[compToBehaviourMap.get(cmbUAComponent.getSelectedItem()).size()];
    int i = 0;
    if (cmbUAComponent.getModel().getSize() > 0) {
      for (NodeData node : compToBehaviourMap.get(cmbUAComponent.getSelectedItem())) {
        if (Constants.acceptedUABehaviourTypes.contains(node.getBehaviourType())) {
          uaBehaviours[i++] = node.getBehaviour() + " [" + node.getBehaviourType() + "]";
        }
      }
    }
    cmbUABehaviour.setModel(new DefaultComboBoxModel<String>(clean(uaBehaviours)));
  }

  /**
   * Load the user action for selected component.
   */
  private void updateUATA() {
    taUAConfigured.setText("");
    for (Entry<String, String[]> component : userActions.entrySet()) {
      String key = component.getKey();
      String[] value = component.getValue();

      if (!value[0].equals("")) {
        if (taUAConfigured.getText().equals("")) {
          taUAConfigured.setText(taUAConfigured.getText() + key);
        } else {
          taUAConfigured.setText(taUAConfigured.getText() + ", \n" + key);
        }
      }
    }
    taUAConfigured.setText(taUAConfigured.getText().replaceAll(";", ": "));
  }

  /**
   * Populate configuration for User Actions.
   */

  private void updateUA() {
    try {
      String selectedUA = cmbUAComponent.getSelectedItem().toString() + ";"
          + cmbUABehaviour.getSelectedItem().toString();
      if (userActions.get(selectedUA) != null) {
        String[] userAction = userActions.get(selectedUA);
        taUAInput.setText(userAction[0]);

        if (userAction[1].equals("true")) {
          chckbxAppearPreAmble.setSelected(true);
        } else {
          chckbxAppearPreAmble.setSelected(false);
        }
      } else {
        taUAInput.setText("");
        chckbxAppearPreAmble.setSelected(false);
      }
    } catch (Exception e) {

    }
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
    for (String node : chosenNOIs) {
      String[] nodeData = node.split(";");
      String tag = nodeData[0];
      String component = nodeData[1];
      String behaviour = nodeData[2];
      String behaviourType = nodeData[3];
      Element nodeToAdd = new Element("node");
      nodeToAdd.setAttribute("tag", tag);
      nodeToAdd.setAttribute("component", component);
      nodeToAdd.setAttribute("behaviour", behaviour);
      nodeToAdd.setAttribute("behaviour-type", behaviourType);
      noi.addContent(nodeToAdd);
    }

    Element cp = new Element("CP");
    for (String node : chosenCPs) {
      String[] nodeData = node.split(";");
      String component = nodeData[0];
      String behaviour = nodeData[1];
      Element nodeToAdd = new Element("node");
      nodeToAdd.setAttribute("component", component);
      nodeToAdd.setAttribute("behaviour", behaviour);
      cp.addContent(nodeToAdd);
    }
    if (!initialNode.equals("")) {
      Element initialCP = new Element("initial");
      String initialTag = initialNode.split(":")[0];
      String initialData = initialNode.split(":")[1];
      String initialComponent = initialData.substring(1, initialData.indexOf("-") - 1);
      String initialBehaviour =
          initialData.substring(initialData.indexOf("-") + 2, initialData.indexOf("[") - 1);
      String initialBehaviourType =
          initialData.substring(initialData.indexOf("[") + 1, initialData.indexOf("]"));
      initialCP.setAttribute("tag", initialTag);
      initialCP.setAttribute("component", initialComponent);
      initialCP.setAttribute("behaviour", initialBehaviour);
      initialCP.setAttribute("behaviour-type", initialBehaviourType);
      cp.addContent(initialCP);
    }

    Element or = new Element("OR");
    for (Entry<String, String> observation : observableResponses.entrySet()) {
      String[] nodeData = observation.getKey().split(";");
      String component = nodeData[0];
      String behaviour = nodeData[1].substring(0, nodeData[1].indexOf("["));
      String behaviourType =
          nodeData[1].substring(nodeData[1].indexOf("[") + 1, nodeData[1].indexOf("]"));
      Element nodeToAdd = new Element("node");
      nodeToAdd.setAttribute("component", component);
      nodeToAdd.setAttribute("behaviour", behaviour);
      nodeToAdd.setAttribute("behaviour-type", behaviourType);
      nodeToAdd.setAttribute("observation", observation.getValue());
      or.addContent(nodeToAdd);
    }

    Element ua = new Element("UA");
    for (Entry<String, String[]> action : userActions.entrySet()) {
      String[] nodeData = action.getKey().split(";");
      String component = nodeData[0];
      String behaviour = nodeData[1].substring(0, nodeData[1].indexOf("["));
      String behaviourType =
          nodeData[1].substring(nodeData[1].indexOf("[") + 1, nodeData[1].indexOf("]"));
      Element nodeToAdd = new Element("node");
      nodeToAdd.setAttribute("component", component);
      nodeToAdd.setAttribute("behaviour", behaviour);
      nodeToAdd.setAttribute("behaviour-type", behaviourType);
      nodeToAdd.setAttribute("action", action.getValue()[0]);
      nodeToAdd.setAttribute("preamble", action.getValue()[1]);
      ua.addContent(nodeToAdd);
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

    // TODO
    // btnGenerateTestCases = new JButton("Generate Test Cases");
    btnGenerateTestCases = new JButton("DON'T ClICK THIS");
    btnGenerateTestCases.setEnabled(false);
    btnGenerateTestCases.setBounds(72, 59, 281, 51);
    /**
     * Call API and generate TCPs, pre-amble and post-amble.
     */
    btnGenerateTestCases.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
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
    lblSelectNodes.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectNodes.setBounds(53, 47, 191, 22);
    panelNOI.add(lblSelectNodes);

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(53, 80, 1031, 523);
    panelNOI.add(scrollPane);

    tblNOI = new JTable();
    tblNOI.setBorder(null);
    scrollPane.setViewportView(tblNOI);
    tblNOI.setFillsViewportHeight(true);
    tblNOI.setCellSelectionEnabled(false);
    tblNOI.setRowSelectionAllowed(false);
    tblNOI.setModel(new DefaultTableModel(new Object[][] {},
        new String[] {"Tag", "Component", "Behaviour", "Behaviour Type", "Select"}) {
      private static final long serialVersionUID = -8673519275754805408L;
      @SuppressWarnings("rawtypes")
      Class[] columnTypes =
          new Class[] {String.class, String.class, String.class, String.class, Boolean.class};

      @SuppressWarnings({"unchecked", "rawtypes"})
      public Class getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }
    });
    tblNOI.getColumnModel().getColumn(1).setResizable(false);
    tblNOI.getColumnModel().getColumn(2).setResizable(false);
    tblNOI.getColumnModel().getColumn(3).setResizable(false);
    tblNOI.getColumnModel().getColumn(4).setResizable(false);

    Action cellChangeAction = new AbstractAction() {
      private static final long serialVersionUID = -7226814055304660509L;

      public void actionPerformed(ActionEvent e) {
        TableCellListener tcl = (TableCellListener) e.getSource();
        if (tcl.getColumn() == 4) {
          int row = tcl.getRow();
          String node = tblNOI.getValueAt(row, 0) + ";" + tblNOI.getValueAt(row, 1) + ";"
              + tblNOI.getValueAt(row, 2) + ";" + tblNOI.getValueAt(row, 3);
          if ((boolean) tcl.getNewValue()) {
            if (!chosenNOIs.contains(node)) {
              chosenNOIs.add(node);
            }
          } else {
            chosenNOIs.remove(node);
          }
        }
      }
    };
    new TableCellListener(tblNOI, cellChangeAction);

    return panelNOI;
  }

  private JPanel createCPTab() {
    JPanel panelCP = new JPanel();

    panelCP.setLayout(null);

    JLabel lblCPComponent = new JLabel("Select Component Name:");
    lblCPComponent.setBounds(125, 136, 121, 14);
    panelCP.add(lblCPComponent);

    JLabel lblCPBehaviour = new JLabel("Select Behaviour:");
    lblCPBehaviour.setBounds(162, 183, 84, 14);
    panelCP.add(lblCPBehaviour);

    cmbCPComponent.setBounds(256, 127, 313, 32);
    panelCP.add(cmbCPComponent);

    cmbCPBehaviour.setBounds(256, 174, 313, 32);
    panelCP.add(cmbCPBehaviour);

    chkCP.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        if (cmbCPComponent.getSelectedItem() != null && cmbCPBehaviour.getSelectedItem() != null) {
          String selectedCP = cmbCPComponent.getSelectedItem().toString() + ";"
              + cmbCPBehaviour.getSelectedItem().toString();
          if (chkCP.isSelected()) {
            chosenCPs.add(selectedCP);
          } else {
            chosenCPs.remove(selectedCP);
          }
          updateCPInitial();
        }
        updateCPTA();
      }
    });
    chkCP.setBounds(256, 224, 165, 23);
    panelCP.add(chkCP);

    cmbCPComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent action) {
        if (cmbCPComponent.getSelectedItem() != null) {
          updateCPBehaviours();
        }
      }
    });

    cmbCPBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbCPComponent.getSelectedItem() != null) {
          updateCPCheckBox();
        }
      }
    });


    JLabel lblCPNote1 = new JLabel("Note: Selection restricted to state realisation nodes");
    lblCPNote1.setBounds(194, 258, 247, 14);
    panelCP.add(lblCPNote1);

    JLabel lblSelectedCP = new JLabel("Selected checkpoints:");
    lblSelectedCP.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectedCP.setBounds(743, 14, 236, 22);
    panelCP.add(lblSelectedCP);
    taCP.setEditable(false);

    taCP.setBounds(743, 42, 345, 580);
    panelCP.add(taCP);
    cmbInitialState.setBounds(256, 333, 376, 32);
    panelCP.add(cmbInitialState);

    JLabel lblCPNote2 =
        new JLabel("Note: Selection restricted to CheckPoints which you have chosen.");
    lblCPNote2.setBounds(162, 395, 361, 14);
    panelCP.add(lblCPNote2);

    JLabel lblInitialState = new JLabel("Select Initial State of the System:");
    lblInitialState.setBounds(85, 342, 161, 14);
    panelCP.add(lblInitialState);
    cmbInitialState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbInitialState.getSelectedItem() != null) {
          initialNode = cmbInitialState.getSelectedItem().toString();
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
    lblORComponent.setBounds(331, 41, 121, 14);
    panelOA.add(lblORComponent);

    cmbORComponent.setBounds(462, 30, 313, 32);
    panelOA.add(cmbORComponent);

    cmbORBehaviour.setBounds(462, 73, 313, 32);
    panelOA.add(cmbORBehaviour);

    JLabel lblORBehaviour = new JLabel("Select Behaviour:");
    lblORBehaviour.setBounds(368, 88, 84, 14);
    panelOA.add(lblORBehaviour);
    taORInput.setLineWrap(true);

    taORInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent arg0) {
        if (cmbORComponent.getSelectedItem() != null && cmbORBehaviour.getSelectedItem() != null) {
          observableResponses.put(cmbORComponent.getSelectedItem().toString() + ";"
              + cmbORBehaviour.getSelectedItem().toString(), taORInput.getText());
        }
        updateOTA();
      }
    });
    taORInput.setBounds(10, 238, 637, 383);
    panelOA.add(taORInput);


    cmbORComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbORComponent.getSelectedItem() != null) {
          updateObsBehaviours();
          updateOR();
        }
      }
    });


    cmbORBehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbORComponent.getSelectedItem() != null) {
          updateOR();
        }
      }
    });


    JLabel lblORInput = new JLabel("Observable Response:");
    lblORInput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORInput.setBounds(10, 205, 174, 22);
    panelOA.add(lblORInput);

    JLabel lblNoteSelectionRestricted_1 = new JLabel(
        "Note: Selection restricted to all components except events, external inputs, selections & guards.");
    lblNoteSelectionRestricted_1.setBounds(331, 127, 464, 14);
    panelOA.add(lblNoteSelectionRestricted_1);

    JLabel lblORConfigured = new JLabel("Observable Responses configured for:");
    lblORConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORConfigured.setBounds(676, 205, 299, 22);
    panelOA.add(lblORConfigured);


    taORConfigured.setEditable(false);
    taORConfigured.setBounds(676, 238, 440, 383);

    panelOA.add(taORConfigured);
    return panelOA;
  }

  private JPanel createUATab() {
    JPanel panelUA = new JPanel();
    tabbedPane.addTab("User Actions/External Inputs", null, panelUA, null);
    panelUA.setLayout(null);

    JLabel lblUAComponent = new JLabel("Select Component Name:");
    lblUAComponent.setBounds(303, 31, 121, 14);
    panelUA.add(lblUAComponent);

    JLabel lblUABehaviour = new JLabel("Select Behaviour:");
    lblUABehaviour.setBounds(340, 75, 84, 14);
    panelUA.add(lblUABehaviour);

    cmbUAComponent.setBounds(434, 22, 313, 32);
    panelUA.add(cmbUABehaviour);

    cmbUABehaviour.setBounds(434, 66, 313, 32);
    panelUA.add(cmbUAComponent);


    JLabel lblUAinput = new JLabel("Action to be taken:");
    lblUAinput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUAinput.setBounds(10, 185, 151, 22);
    panelUA.add(lblUAinput);
    taUAInput.setLineWrap(true);

    taUAInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (cmbUAComponent.getSelectedItem() != null && cmbUABehaviour.getSelectedItem() != null
            && chckbxAppearPreAmble.isSelected() == true) {
          String[] userAction = new String[2];
          userAction[0] = taUAInput.getText();
          userAction[1] = "true";
          userActions.put(cmbUAComponent.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString(), userAction);
        } else if (cmbUAComponent.getSelectedItem() != null
            && cmbUABehaviour.getSelectedItem() != null
            && chckbxAppearPreAmble.isSelected() == false) {

          String[] userAction = new String[2];
          userAction[0] = taUAInput.getText();
          userAction[1] = "false";
          userActions.put(cmbUAComponent.getSelectedItem().toString() + ";"
              + cmbUABehaviour.getSelectedItem().toString(), userAction);
        }
        updateUATA();
      }
    });
    taUAInput.setBounds(10, 218, 637, 404);
    panelUA.add(taUAInput);


    cmbUAComponent.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (cmbUAComponent.getSelectedItem() != null) {
          updateUABehaviours();
          updateUA();
        }
      }
    });


    cmbUABehaviour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (cmbUAComponent.getSelectedItem() != null) {
          updateUA();
        }
      }
    });

    JLabel lblNoteSelectionRestricted_2 =
        new JLabel("Note: Selection restricted to events & external inputs.");
    lblNoteSelectionRestricted_2.setBounds(340, 135, 260, 14);
    panelUA.add(lblNoteSelectionRestricted_2);


    taUAConfigured.setEditable(false);
    taUAConfigured.setBounds(676, 218, 440, 404);
    panelUA.add(taUAConfigured);

    JLabel lblUserActionsConfigured = new JLabel("Actions configured for:");
    lblUserActionsConfigured.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUserActionsConfigured.setBounds(676, 185, 180, 22);
    panelUA.add(lblUserActionsConfigured);
    chckbxAppearPreAmble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!taUAInput.getText().equals("")) {
          if (chckbxAppearPreAmble.isSelected() == true) {
            String[] userAction = new String[2];
            userAction[0] = taUAInput.getText();
            userAction[1] = "true";
            userActions.put(cmbUAComponent.getSelectedItem().toString() + ";"
                + cmbUABehaviour.getSelectedItem().toString(), userAction);
          } else {
            String[] userAction = new String[2];
            userAction[0] = taUAInput.getText();
            userAction[1] = "false";
            userActions.put(cmbUAComponent.getSelectedItem().toString() + ";"
                + cmbUABehaviour.getSelectedItem().toString(), userAction);
          }
        }
      }
    });

    chckbxAppearPreAmble.setBounds(434, 105, 481, 23);
    panelUA.add(chckbxAppearPreAmble);
    return panelUA;
  }

  private JPanel createTPTab() {
    JPanel panelTP = new JPanel();

    List<TestPath> feeds = new ArrayList<TestPath>();
    feeds.add(new TestPath("ATM [READY]", "something", 5));
    feeds.add(new TestPath("Display [Transaction]", "something", 15));
    panelTP.setLayout(null);

    JTable table = new JTable(new TestPathModel(feeds));
    table.setDefaultRenderer(TestPath.class, new TestPathCell());
    table.setDefaultEditor(TestPath.class, new TestPathCell());
    table.setRowHeight(60);
    table.setBounds(731, 11, 500, 610);
    panelTP.add(table);
    return panelTP;
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


  /**
   * Remove null elements.
   */
  private static String[] clean(final String[] v) {
    List<String> list = new ArrayList<String>(Arrays.asList(v));
    list.removeAll(Collections.singleton(null));
    return list.toArray(new String[list.size()]);
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
}
