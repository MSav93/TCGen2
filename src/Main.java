import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
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
import java.util.List;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringEscapeUtils;
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
import table.CPCell;
import table.CPFilter;
import table.NOICell;
import table.NOIFilter;
import table.NodeTableModel;
import table.ORCell;
import table.ORFilter;
import table.TestCaseCell;
import table.TestCasesModel;
import table.TestPathCell;
import table.TestPathModel;
import table.UACell;
import table.UAFilter;
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
  private JButton btnSpare;

  /**
   * Components on CheckPoints tab
   */
  private JTable tblCP;
  private JComboBox<Node> cmbInitialState = new JComboBox<Node>();
  private final JTextArea taORInput = new JTextArea();
  private final JTextArea taUAInput = new JTextArea();
  private final JCheckBox chckbxAppearPreAmble =
      new JCheckBox("This condition should appear in the Pre-Amble for the entire test path");

  /**
   * Components on User Actions tab
   */
  private JTable tblUA;

  /**
   * Components on Observables tab
   */
  private JTable tblOR;

  /**
   * Components on Nodes Of Interest tab
   */
  private JTable tblNOI;

  /**
   * Components on the Join Test Cases tab
   */
  private JTable tblTCs;
  private JTable tblTP;

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
  private Node currentNode;

  Set<Node> chosenNOIs = new TreeSet<Node>();
  Set<Node> chosenCPs = new TreeSet<Node>();
  Set<Node> observableResponses = new TreeSet<Node>();
  Set<Node> userActions = new TreeSet<Node>();
  ArrayList<TestCase> selectedTCs = new ArrayList<TestCase>();

  private Set<Node> allNodes = new TreeSet<Node>();

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
          SBCLPipe.killServer();
        } catch (IOException e1) {
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
    chosenNOIs.clear();
    chosenCPs.clear();
    observableResponses.clear();
    userActions.clear();

    indexToNodesMap.clear();
    compToBehaviourMap.clear();

    cmbInitialState.removeAllItems();
    taUAInput.setText("");
    taORInput.setText("");
    chckbxAppearPreAmble.setSelected(false);

    btFilePath = "";
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

    // CPs
    List<Element> checkpoints = config.getChild("CP").getChildren("node");
    ArrayList<Element> checkpointsToBeRemoved = new ArrayList<Element>();
    for (Element cp : checkpoints) {
      for (Node btNode : chosenCPs) {
        if (checkElementsAttributes(cp, "cp")) {
          Node configNode = new Node(null, cp.getAttributeValue("component"),
              cp.getAttributeValue("behaviour-type"), cp.getAttributeValue("behaviour"), null,
              null);
          if (configNode.equalsSimple(btNode)) {
            // Selects this node as a CP
            btNode.setCp(true);
            // Remove node from remaining list of unmatched nodes
            checkpointsToBeRemoved.add(cp);
            // break;
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
      for (Node btNode : observableResponses) {
        if (checkElementsAttributes(or, "or")) {
          Node xmlNode = new Node(Integer.parseInt(or.getAttributeValue("tag")),
              or.getAttributeValue("component"), or.getAttributeValue("behaviour-type"),
              or.getAttributeValue("behaviour"), or.getAttributeValue("flag"), null);
          if (btNode.equals(xmlNode)) {
            // Populate this nodes observables
            btNode
                .setObservable(StringEscapeUtils.unescapeXml(or.getAttributeValue("observation")));
            // Remove node from remaining list of unmatched nodes
            observablesToBeRemoved.add(or);
            break;
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
      for (Node btNode : userActions) {
        if (checkElementsAttributes(ua, "ua")) {
          Node xmlNode = new Node(Integer.parseInt(ua.getAttributeValue("tag")),
              ua.getAttributeValue("component"), ua.getAttributeValue("behaviour-type"),
              ua.getAttributeValue("behaviour"), ua.getAttributeValue("flag"), null);
          if (btNode.equals(xmlNode)) {
            // Populate this nodes actions
            btNode.setAction(StringEscapeUtils.unescapeXml(ua.getAttributeValue("action")));
            btNode.setPreamble(ua.getAttributeValue("preamble").equals("true"));
            // Remove node from remaining list of unmatched nodes
            actionsToBeRemoved.add(ua);
            break;
          }
        }
      }
    }
    for (Element ua : actionsToBeRemoved) {
      actions.remove(ua);
    }

    // NOIS
    List<Element> nodesOfInterest = config.getChild("NOI").getChildren();
    // recording nodes that get matched to remove later to avoid ConcurrentModificationException.
    ArrayList<Element> nodesToBeRemoved = new ArrayList<Element>();
    for (Node btNode : chosenNOIs) {
      for (Element noi : nodesOfInterest) {
        if (checkElementsAttributes(noi, "noi")) {
          Node configNode = new Node(Integer.parseInt(noi.getAttributeValue("tag")),
              noi.getAttributeValue("component"), noi.getAttributeValue("behaviour-type"),
              noi.getAttributeValue("behaviour"), noi.getAttributeValue("flag"), null);
          if (btNode.equals(configNode)) {
            // Selects this node as a NOI
            btNode.setNoi(true);
            // Remove node from remaining list of unmatched nodes
            nodesToBeRemoved.add(noi);
          }
        }
      }
    }
    for (Element node : nodesToBeRemoved) {
      nodesOfInterest.remove(node);
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
        if (valid) {
          valid = (element.getAttributeValue("behaviour-type") != null);
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

    populateTblCP();
    populateTblUA();
    populateTblOR();
    populateTblNOI();
    updateDisplay();

    btnGenerateTestCases.setEnabled(true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.noiTabName), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.cpTabName), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.observablesTabName), true);
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.userActionsTabName), true);
  }

  private void populateNodeMaps() {
    for (int key : indexToNodesMap.keySet()) {
      for (Node node : indexToNodesMap.get(key).getNodes()) {
        allNodes.add(node);
        // if (Constants.acceptedCPFlags.contains(node.getFlag())) {
        // chosenCPs.add(node);
        // }
        // if (Constants.acceptedNOIFlags.contains(node.getFlag())) {
        // chosenNOIs.add(node);
        // }
        // if (Constants.acceptedUABehaviourTypes.contains(node.getBehaviourType())) {
        // userActions.add(node);
        // }
        // if (Constants.acceptedORBehaviourTypes.contains(node.getBehaviourType())) {
        // observableResponses.add(node);
        // }
        if (!compToBehaviourMap.containsKey(node.getComponent())) {
          compToBehaviourMap.put(node.getComponent(), new ArrayList<Node>());
        }
        compToBehaviourMap.get(node.getComponent()).add(node);
      }
    }
  }

  private void updateDisplay() {
    updateCPInitial();
    updateORInput();
    updateUAChkBox();
    updateUAInput();
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

  @SuppressWarnings("unchecked")
  private void populateTblCP() {
    ((NodeTableModel) tblCP.getModel()).addData(allNodes);
    ((TableRowSorter<NodeTableModel>) tblCP.getRowSorter()).setRowFilter(new CPFilter());
  }

  /**
   * Populate Initial CP combo box.
   */
  private void updateCPInitial() {
    TreeSet<Node> potentialInitialCP = new TreeSet<Node>();
    for (Node node : chosenCPs) {
      if (node.isCp()) {
        potentialInitialCP.add(node);
      }
    }
    cmbInitialState
        .setModel(new DefaultComboBoxModel<Node>(potentialInitialCP.toArray(new Node[] {})));
    // Make cmbBox appear blank as no initialNode is selected
    cmbInitialState.setSelectedIndex(-1);
    // If the changes made to CP did not relate to currently selected initial node then set it back
    // to that.
    cmbInitialState.setSelectedItem(initialNode);
    // If initialNode is not a selectable option set it to null
    if (cmbInitialState.getSelectedIndex() == -1) {
      initialNode = null;
    }
  }

  @SuppressWarnings("unchecked")
  private void populateTblOR() {
    ((NodeTableModel) tblOR.getModel()).addData(allNodes);
    ((TableRowSorter<NodeTableModel>) tblOR.getRowSorter()).setRowFilter(new ORFilter());
  }

  /**
   * Populate input area for Observable Responses.
   */
  private void updateORInput() {
    if (tblOR.getSelectedRow() != -1) {
      Node selectedNode = (Node) tblOR.getValueAt(tblOR.getSelectedRow(), 0);
      taORInput.setText(selectedNode.getObservable());
    }
  }

  @SuppressWarnings("unchecked")
  private void populateTblUA() {
    ((NodeTableModel) tblUA.getModel()).addData(allNodes);
    ((TableRowSorter<NodeTableModel>) tblUA.getRowSorter()).setRowFilter(new UAFilter());
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
    if (tblUA.getSelectedRow() != -1) {
      Node selectedNode = (Node) tblUA.getValueAt(tblUA.getSelectedRow(), 0);
      taUAInput.setText(selectedNode.getAction());
      if (selectedNode.isPreamble()) {
        chckbxAppearPreAmble.setSelected(true);
      } else {
        chckbxAppearPreAmble.setSelected(false);
      }
    }
    updateUAChkBox();
  }

  private void populateTblNOI() {
    ((NodeTableModel) tblNOI.getModel()).addData(allNodes);
    refreshTblNOIFilter();
  }

  @SuppressWarnings("unchecked")
  private void refreshTblNOIFilter() {
    ((TableRowSorter<NodeTableModel>) tblNOI.getRowSorter()).setRowFilter(new NOIFilter());
  }

  private void populateTPTab(ArrayList<ArrayList<Integer>> rawTestCases) {
    ArrayList<TestCase> testCases = new ArrayList<TestCase>();
    for (int i = 0; i < rawTestCases.size(); i++) {
      ArrayList<Integer> testCase = rawTestCases.get(i);
      testCases.add(constructTestCase(testCase));
    }
    tblTCs.setModel(new TestCasesModel(testCases));
    updateTblTCs();
  }

  private void updateTblTCs() {
    if (currentNode == null) {
      currentNode = initialNode;
    }
    for (int i = 0; i < tblTCs.getRowCount(); i++) {
      TestCase tc = (TestCase) tblTCs.getValueAt(i, 0);
      if (!(tc.getStartNode().getBlockIndex() == currentNode.getBlockIndex())) {
        ArrayList<Integer> blocks = calcTotalTestCaseSteps(currentNode, tc);
        if (blocks != null) {
          if (blocks.size() == 1 && blocks.get(0) == tc.getStartNode().getBlockIndex()) {
            blocks.add(tc.getStartNode().getBlockIndex());
            tc.setStepsAway(blocks, getNodeList(blocks));
          } else {
            tc.setStepsAway(blocks, getNodeList(blocks));
          }
        } else {
          tc.setReachable(false);
        }
      } else {
        // Test Case is 0 steps away
        tc.setStepsAway(new ArrayList<Integer>(), new ArrayList<Node>());
      }
    }
    tblTCs.repaint();
  }

  private void updateTblTp() {
    tblTP.setModel(new TestCasesModel(selectedTCs));
    tblTP.repaint();
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
      if (node.isNoi()) {
        Element nodeToAdd = new Element("node");
        nodeToAdd.setAttribute("tag", node.getTag().toString());
        nodeToAdd.setAttribute("component", node.getComponent());
        nodeToAdd.setAttribute("behaviour", node.getBehaviour());
        nodeToAdd.setAttribute("behaviour-type", node.getBehaviourType());
        nodeToAdd.setAttribute("flag", node.getFlag());
        noi.addContent(nodeToAdd);
      }
    }

    Element cp = new Element("CP");
    for (Node node : chosenCPs) {
      if (node.isCp()) {
        Element nodeToAdd = new Element("node");
        nodeToAdd.setAttribute("component", node.getComponent());
        nodeToAdd.setAttribute("behaviour", node.getBehaviour());
        nodeToAdd.setAttribute("behaviour-type", node.getBehaviourType());
        cp.addContent(nodeToAdd);
      }
    }
    if (initialNode != null) {
      Element initialCP = new Element("initial");
      initialCP.setAttribute("tag", initialNode.getTag().toString());
      initialCP.setAttribute("component", initialNode.getComponent());
      initialCP.setAttribute("behaviour", initialNode.getBehaviour());
      initialCP.setAttribute("behaviour-type", initialNode.getBehaviourType());
      initialCP.setAttribute("flag", initialNode.getFlag());
      cp.addContent(initialCP);
    }

    Element or = new Element("OR");
    for (Node orNode : observableResponses) {
      if (!orNode.getObservable().equals("")) {
        Element nodeToAdd = new Element("node");
        nodeToAdd.setAttribute("tag", orNode.getTag().toString());
        nodeToAdd.setAttribute("component", orNode.getComponent());
        nodeToAdd.setAttribute("behaviour", orNode.getBehaviour());
        nodeToAdd.setAttribute("behaviour-type", orNode.getBehaviourType());
        nodeToAdd.setAttribute("flag", orNode.getFlag());
        nodeToAdd.setAttribute("observation",
            StringEscapeUtils.escapeXml10(orNode.getObservable()));
        or.addContent(nodeToAdd);
      }
    }

    Element ua = new Element("UA");
    for (Node uaNode : userActions) {
      if (!uaNode.getAction().equals("")) {
        Element nodeToAdd = new Element("node");
        nodeToAdd.setAttribute("tag", uaNode.getTag().toString());
        nodeToAdd.setAttribute("component", uaNode.getComponent());
        nodeToAdd.setAttribute("behaviour", uaNode.getBehaviour());
        nodeToAdd.setAttribute("behaviour-type", uaNode.getBehaviourType());
        nodeToAdd.setAttribute("flag", uaNode.getFlag());
        nodeToAdd.setAttribute("action", StringEscapeUtils.escapeXml10(uaNode.getAction()));
        nodeToAdd.setAttribute("preamble", uaNode.isPreamble().toString());
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
    contentPane = new JPanel(null);
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);

    createMainButtons();

    contentPane.add(createMenuBar());

    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBounds(10, 131, 1131, 660);
    contentPane.add(tabbedPane);

    tabbedPane.addTab(Constants.cpTabName, null, createCPTab(), null);
    tabbedPane.addTab(Constants.observablesTabName, null, createOATab(), null);
    tabbedPane.addTab(Constants.userActionsTabName, null, createUATab(), null);
    tabbedPane.addTab(Constants.noiTabName, null, createNOITab(), null);
    tabbedPane.addTab(Constants.joiningTabName, null, createTPTab(), null);

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

  private void createMainButtons() {
    /**
     * Call API and generate TCPs
     */
    btnGenerateTestCases = new JButton("Generate Test Cases");
    btnGenerateTestCases.setEnabled(false);
    btnGenerateTestCases.setBounds(72, 59, 281, 51);
    btnGenerateTestCases.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        selectedTCs.clear();
        currentNode = null;
        ArrayList<String> commands = creatTestCaseGenCommands();
        if (commands.size() > 0) {
          ArrayList<ArrayList<Integer>> testCases = sendTestCaseGenCommands(commands);
          if (testCases.size() > 0) {
            populateTPTab(testCases);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab(Constants.joiningTabName), true);
          } else {
            JOptionPane.showMessageDialog(null, "No test cases generated.");
          }
        } else {
          JOptionPane.showMessageDialog(null,
              "Invalid config. Make sure you have selected some checkpoints.");
        }
      }
    });
    contentPane.add(btnGenerateTestCases);

    /**
     * Generate natural language test cases in Excel format.
     */
    btnSpare = new JButton("Spare Button (Does Nothing)");
    btnSpare.setEnabled(false);
    btnSpare.setBounds(425, 59, 281, 51);
    btnSpare.setEnabled(false);
    btnSpare.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // TODO Put stuff here
      }
    });
    contentPane.add(btnSpare);

    JButton btnOpenTestCase = new JButton("Open Test Case Folder");
    btnOpenTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        openFolder();
      }
    });
    btnOpenTestCase.setBounds(778, 59, 301, 51);
    contentPane.add(btnOpenTestCase);
  }

  private JPanel createCPTab() {
    JPanel panelCP = new JPanel(null);

    /* Select Checkpoints Label */
    JLabel lblSelectCP = new JLabel("Select checkpoints:");
    lblSelectCP.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectCP.setBounds(10, 11, 393, 22);
    panelCP.add(lblSelectCP);

    /* Select Checkpoints Note */
    JLabel lblCPNote1 = new JLabel("Note: Selection restricted to state realisation nodes");
    lblCPNote1.setHorizontalAlignment(SwingConstants.RIGHT);
    lblCPNote1.setBounds(555, 18, 555, 14);
    panelCP.add(lblCPNote1);

    /* Checkpoints Table */
    tblCP = new JTable(new NodeTableModel());
    TableRowSorter<NodeTableModel> sorter =
        new TableRowSorter<NodeTableModel>((NodeTableModel) tblCP.getModel());
    tblCP.setRowSorter(sorter);
    tblCP.setDefaultRenderer(Node.class, new CPCell());
    tblCP.setDefaultEditor(Node.class, new CPCell());
    tblCP.setRowHeight(30);
    tblCP.setBorder(null);
    tblCP.setFillsViewportHeight(true);
    tblCP.setCellSelectionEnabled(true);
    tblCP.setTableHeader(null);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(10, 33, 1100, 424);
    scrollPane.setViewportView(tblCP);
    panelCP.add(scrollPane);

    /* Remove Checkpoint Button */
    JButton btnRemoveCP = new JButton("Remove CP");
    btnRemoveCP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i : tblCP.getSelectedRows()) {
          for (Node n : allNodes) {
            Node tblNode = (Node) tblCP.getValueAt(i, 0);
            if (n.equalsSimple(tblNode)) {
              n.setCp(false);
              chosenCPs.remove(n);
            }
          }
          tblCP.repaint();
        }
        updateCPInitial();
        refreshTblNOIFilter();
      }
    });
    btnRemoveCP.setBounds(242, 468, 200, 60);
    panelCP.add(btnRemoveCP);

    /* Add Checkpoint Button */
    JButton btnAddCP = new JButton("Add CP");
    btnAddCP.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        for (int i : tblCP.getSelectedRows()) {
          Node tblNode = (Node) tblCP.getValueAt(i, 0);
          for (Node n : allNodes) {
            if (n.equalsSimple(tblNode)) {
              n.setCp(true);
              chosenCPs.add(n);
            }
          }
          tblCP.repaint();
        }
        updateCPInitial();
        refreshTblNOIFilter();
      }
    });
    btnAddCP.setBounds(684, 468, 200, 60);
    panelCP.add(btnAddCP);

    /* Select Initial CheckPoint Label */
    JLabel lblInitialState = new JLabel("Select Initial State of the System:");
    lblInitialState.setHorizontalAlignment(SwingConstants.RIGHT);
    lblInitialState.setBounds(0, 565, 477, 14);
    panelCP.add(lblInitialState);

    /* Initial Checkpoint Note */
    JLabel lblCPNote2 =
        new JLabel("Note: Selection restricted to CheckPoints which you have chosen.");
    lblCPNote2.setHorizontalAlignment(SwingConstants.CENTER);
    lblCPNote2.setBounds(0, 608, 1126, 14);
    panelCP.add(lblCPNote2);

    /* Initial Checkpoint ComboBox */
    cmbInitialState.setBounds(497, 556, 376, 32);
    cmbInitialState.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (cmbInitialState.getSelectedItem() != null) {
          initialNode = (Node) cmbInitialState.getSelectedItem();
        }
      }
    });
    panelCP.add(cmbInitialState);
    return panelCP;
  }

  private JPanel createOATab() {
    JPanel panelOA = new JPanel(null);

    /* Select Node Label */
    JLabel lblSelectOR = new JLabel("Select Node:");
    lblSelectOR.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectOR.setBounds(10, 21, 521, 22);
    panelOA.add(lblSelectOR);

    /* Select Node Note */
    JLabel lblORNote = new JLabel(
        "Note: Selection restricted to State-Realisation, External-Output and Internal-Output nodes. Also Reference and Reversion nodes share observables with original node.");
    lblORNote.setBounds(10, 42, 1106, 14);
    panelOA.add(lblORNote);

    /* Observable Response Label */
    JLabel lblORInput = new JLabel("Observable Response:");
    lblORInput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblORInput.setBounds(665, 21, 174, 22);
    panelOA.add(lblORInput);

    /* Observable Response Table */
    tblOR = new JTable(new NodeTableModel());
    TableRowSorter<NodeTableModel> sorter =
        new TableRowSorter<NodeTableModel>((NodeTableModel) tblOR.getModel());
    tblOR.setRowSorter(sorter);
    tblOR.setDefaultRenderer(Node.class, new ORCell());
    tblOR.setDefaultEditor(Node.class, new ORCell());
    tblOR.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tblOR.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        updateORInput();
        if (tblOR.getSelectedRow() == -1) {
          taORInput.setEnabled(false);
        } else {
          taORInput.setEnabled(true);
        }
      }
    });
    tblOR.setRowHeight(30);
    tblOR.setTableHeader(null);
    tblOR.setFillsViewportHeight(true);
    tblOR.setCellSelectionEnabled(true);
    tblOR.setBorder(null);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(10, 64, 643, 555);
    scrollPane.setViewportView(tblOR);
    panelOA.add(scrollPane);

    /* Observable Response Text Input */
    taORInput.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
    taORInput.setWrapStyleWord(true);
    taORInput.setLineWrap(true);
    taORInput.setEnabled(false);
    taORInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent arg0) {
        Node tblNode = (Node) tblOR.getValueAt(tblOR.getSelectedRow(), 0);
        if (observableResponses.contains(tblNode)) {
          for (Node n : observableResponses) {
            if (n.equalsSimple(tblNode)) {
              n.setObservable(taORInput.getText());
              if (taORInput.getText().equals("")) {
                observableResponses.remove(n);
              }
            }
          }
        } else {
          for (Node n : allNodes) {
            if (n.equalsSimple(tblNode)) {
              n.setObservable(taORInput.getText());
              observableResponses.add(n);
            }
          }
        }
        tblOR.repaint();
      }
    });
    taORInput.setBounds(665, 64, 451, 557);
    panelOA.add(taORInput);

    return panelOA;
  }

  private JPanel createUATab() {
    JPanel panelUA = new JPanel(null);

    /* Select Node Label */
    JLabel lblSelectUA = new JLabel("Select Node:");
    lblSelectUA.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectUA.setBounds(10, 11, 521, 22);
    panelUA.add(lblSelectUA);

    /* Select Node Note */
    JLabel lblUANote = new JLabel(
        "Note: Selection restricted to Event & External-Input nodes. Also Reference and Reversion nodes share actions with original node.");
    lblUANote.setBounds(10, 38, 649, 14);
    panelUA.add(lblUANote);

    /* Action Label */
    JLabel lblUAinput = new JLabel("Action to be taken:");
    lblUAinput.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblUAinput.setBounds(665, 11, 451, 22);
    panelUA.add(lblUAinput);

    /* Action Checkbox */
    chckbxAppearPreAmble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        ((Node) tblUA.getValueAt(tblUA.getSelectedRow(), 0)).setAction(taUAInput.getText());
        ((Node) tblUA.getValueAt(tblUA.getSelectedRow(), 0)).setPreamble(false);
        tblUA.repaint();
        updateUAChkBox();
      }
    });
    chckbxAppearPreAmble.setBounds(665, 35, 451, 22);
    panelUA.add(chckbxAppearPreAmble);

    /* Action Table */
    tblUA = new JTable(new NodeTableModel());
    TableRowSorter<NodeTableModel> sorter =
        new TableRowSorter<NodeTableModel>((NodeTableModel) tblUA.getModel());
    tblUA.setRowSorter(sorter);
    tblUA.setDefaultRenderer(Node.class, new UACell());
    tblUA.setDefaultEditor(Node.class, new UACell());
    tblUA.setRowHeight(30);
    tblUA.setTableHeader(null);
    tblUA.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tblUA.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        updateUAInput();
        if (tblUA.getSelectedRow() == -1) {
          taUAInput.setEnabled(false);
        } else {
          taUAInput.setEnabled(true);
        }
      }
    });
    tblUA.setFillsViewportHeight(true);
    tblUA.setCellSelectionEnabled(true);
    tblUA.setBorder(null);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(10, 64, 645, 557);
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setViewportView(tblUA);
    panelUA.add(scrollPane);

    /* Action Text Input */
    taUAInput.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
    taUAInput.setWrapStyleWord(true);
    taUAInput.setLineWrap(true);
    taUAInput.setEnabled(false);
    taUAInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        updateUAChkBox();
        Node tblNode = (Node) tblUA.getValueAt(tblUA.getSelectedRow(), 0);
        if (userActions.contains(tblNode)) {
          for (Node n : userActions) {
            if (n.equalsSimple(tblNode)) {
              n.setAction(taUAInput.getText());
              n.setPreamble(chckbxAppearPreAmble.isSelected());
              if (taUAInput.getText().equals("")) {
                userActions.remove(n);
              }
            }
          }
        } else {
          for (Node n : allNodes) {
            if (n.equalsSimple(tblNode)) {
              n.setAction(taUAInput.getText());
              n.setPreamble(chckbxAppearPreAmble.isSelected());
              userActions.add(n);
            }
          }
        }
        tblUA.repaint();
        updateUAChkBox();
      }
    });
    taUAInput.setBounds(665, 64, 451, 557);
    panelUA.add(taUAInput);

    return panelUA;
  }

  private JPanel createNOITab() {
    JPanel panelNOI = new JPanel(null);

    /* Select NOI Label */
    JLabel lblSelectNOI = new JLabel("Select nodes of interest:");
    lblSelectNOI.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblSelectNOI.setBounds(10, 11, 191, 22);
    panelNOI.add(lblSelectNOI);

    tblNOI = new JTable(new NodeTableModel());
    TableRowSorter<NodeTableModel> sorter =
        new TableRowSorter<NodeTableModel>((NodeTableModel) tblNOI.getModel());
    tblNOI.setRowSorter(sorter);
    tblNOI.setDefaultRenderer(Node.class, new NOICell());
    tblNOI.setDefaultEditor(Node.class, new NOICell());
    tblNOI.setRowHeight(30);
    tblNOI.setBorder(null);
    tblNOI.setFillsViewportHeight(true);
    tblNOI.setCellSelectionEnabled(true);
    tblNOI.setTableHeader(null);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBounds(10, 32, 1106, 499);
    scrollPane.setViewportView(tblNOI);
    panelNOI.add(scrollPane);

    /* Remove NOI Button */
    JButton btnRemoveNOI = new JButton("Remove NOI");
    btnRemoveNOI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i : tblNOI.getSelectedRows()) {
          ((Node) tblNOI.getValueAt(i, 0)).setNoi(false);
          tblNOI.repaint();
        }
      }
    });
    btnRemoveNOI.setBounds(242, 542, 200, 60);
    panelNOI.add(btnRemoveNOI);

    /* Add NOI Button */
    JButton btnAddNOI = new JButton("Add NOI");
    btnAddNOI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        for (int i : tblNOI.getSelectedRows()) {
          for (Node n : allNodes) {
            Node tblNode = ((Node) tblNOI.getValueAt(i, 0));
            if (n.equalsSimple(tblNode)) {
              n.setNoi(true);
              n.setCp(false);
            }
          }
        }
        tblNOI.repaint();
      }
    });
    btnAddNOI.setBounds(684, 542, 200, 60);
    panelNOI.add(btnAddNOI);

    return panelNOI;
  }

  private JPanel createTPTab() {
    JPanel panelTP = new JPanel(null);

    /* Generated Test Cases Label */
    JLabel lblGeneratedTestCases = new JLabel("Generated Test Cases:");
    lblGeneratedTestCases.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblGeneratedTestCases.setBounds(10, 11, 299, 22);
    panelTP.add(lblGeneratedTestCases);

    /* Generated Test Cases Table */
    tblTCs = new JTable(new TestCasesModel(null));
    tblTCs.setDefaultRenderer(TestCase.class, new TestCaseCell());
    tblTCs.setDefaultEditor(TestCase.class, new TestCaseCell());
    tblTCs.setRowHeight(60);
    tblTCs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tblTCs.setTableHeader(null);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(10, 33, 340, 507);
    scrollPane.setViewportView(tblTCs);
    panelTP.add(scrollPane);

    /* Current Test Path Label */
    JLabel lblCurrentTestPath = new JLabel("Current Test Path");
    lblCurrentTestPath.setFont(new Font("Tahoma", Font.PLAIN, 18));
    lblCurrentTestPath.setBounds(360, 11, 299, 22);
    panelTP.add(lblCurrentTestPath);

    /* Current Test Path Table */
    tblTP = new JTable();
    tblTP.setModel(new TestPathModel(null));
    tblTP.setDefaultRenderer(TestCase.class, new TestPathCell());
    tblTP.setDefaultEditor(TestCase.class, new TestPathCell());
    tblTP.setRowHeight(60);
    tblTP.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tblTP.setTableHeader(null);
    JScrollPane scrollPane2 = new JScrollPane();
    scrollPane2.setBounds(360, 33, 756, 507);
    scrollPane2.setViewportView(tblTP);
    panelTP.add(scrollPane2);

    /* Help Button */
    JButton btnHelp = new JButton("Help");
    btnHelp.setBounds(104, 551, 151, 61);
    btnHelp.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(frame,
            "<html>Cells are coloured to represent to show their availability:<br>"
                + "<ul><li>A Red cell indicates that the test case is unreachable."
                + "<li>A Green cell indicates that the test case has been selected to be part of the test path."
                + "<li>A Yellow cell indicates that the test case is part of a preamble to reach a test and not an actual test itself."
                + "</ul>",
            "Help", JOptionPane.INFORMATION_MESSAGE);
      }
    });
    panelTP.add(btnHelp);

    /* Add Test Case Button */
    JButton btnAddTestCase = new JButton("Add Test Case ->");
    btnAddTestCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TestCase tc = (TestCase) tblTCs.getValueAt(tblTCs.getSelectedRow(), 0);
        System.out.println("Test Case: " + tc);
        if (tc.getStartNode().equals(currentNode)) {
          System.out.println("No Preamble needed");
        } else {
          if (tc.getBlocksAway().size() > 0) {
            System.out.println("Preamble generated");
            List<Integer> preAmbleBlocks = tc.getBlocksAway();
            preAmbleBlocks.add(tc.getStartNode().getBlockIndex());
            TestCase preAmble = new TestCase(preAmbleBlocks, getNodeList(preAmbleBlocks));
            preAmble.setPreAmble(true);
            System.out.println("PREAMBLE: " + preAmble);
            selectedTCs.add(preAmble);
          }
        }
        selectedTCs.add(tc);
        tc.setSelected(true);
        currentNode = tc.getEndNode();
        updateTblTCs();
        updateTblTp();
      }
    });
    btnAddTestCase.setBounds(359, 551, 151, 61);
    panelTP.add(btnAddTestCase);

    /* Undo Button */
    JButton btnUndo = new JButton("Undo");
    btnUndo.setBounds(614, 551, 151, 61);
    btnUndo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!selectedTCs.isEmpty()) {
          // Remove most recent Test Case added
          selectedTCs.get(selectedTCs.size() - 1).setSelected(false);
          selectedTCs.remove(selectedTCs.size() - 1);
          // Remove that test cases preAmble if it has one
          if (selectedTCs.isEmpty()) {
            // If the removed test case was the only one reset initial node
            currentNode = initialNode;
          } else {
            if (selectedTCs.get(selectedTCs.size() - 1).isPreAmble()) {
              selectedTCs.get(selectedTCs.size() - 1).setSelected(false);
              selectedTCs.remove(selectedTCs.size() - 1);
            }
            if (selectedTCs.isEmpty()) {
              // If the removed test case was the only one reset initial node
              currentNode = initialNode;
            } else {
              currentNode = selectedTCs.get(selectedTCs.size() - 1).getEndNode();
            }
          }
          updateTblTCs();
          updateTblTp();
        }
      }
    });
    panelTP.add(btnUndo);

    /* Export to HTML Button */
    JButton btnExportToHtml = new JButton("Export to HTML");
    btnExportToHtml.setBounds(869, 551, 151, 61);
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
    System.out.println("STARTING BLOCKS: " + startingBlocks);
    System.out.println("ENDING BLOCKS: " + endingBlocks);
    for (Node n : chosenNOIs) {
      if (n.isNoi()) {
        noiBlocks.add(n.getBlockIndex());
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i : startingBlocks) {
      for (int j : endingBlocks) {
        sb.append("(find-test-paths " + i + " (");
        for (int k : noiBlocks) {
          sb.append(k + " ");
        }
        if (noiBlocks.size() > 0) {
          // Remove final space added
          sb.setLength(sb.length() - 1);
        }
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
      if (result
          .matches("<result><path>(<block-index>\\d+<\\/block-index>)+<\\/path><\\/result>")) {
        org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();

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
      } else if (result.matches("error\\|\\d+\\|")) {
        printErrorMessage(result);
        break;
      }
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
