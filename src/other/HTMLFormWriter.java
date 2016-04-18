package other;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import tree.Node;

public class HTMLFormWriter {

  private static StringBuilder html;

  public static void output(ArrayList<TestCase> selectedTCs, File loadedFile) {
    html = new StringBuilder();
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
        new FileNameExtensionFilter("HTML Form (.html)", new String[] {"html"});
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);

    fileChooser.setDialogTitle("Specify a file to save");

    int userSelection = fileChooser.showSaveDialog(parentFrame);

    if (userSelection == JFileChooser.APPROVE_OPTION) {

      File fileToSave = fileChooser.getSelectedFile();
      String filename = fileToSave.getAbsolutePath();
      if (fileToSave.getAbsolutePath().endsWith(".html")) {
        filename = filename.substring(0, filename.length() - 5);
      }
      System.out.println("Save as file: " + filename + ".html");
      FileOutputStream fos;
      try {
        fos = new FileOutputStream(filename + ".html");
        fos.write(constructHTML(selectedTCs, loadedFile));
        fos.close();
      } catch (Exception e) {

      }
    }
  }

  private static byte[] constructHTML(ArrayList<TestCase> selectedTCs, File loadedFile) {
    addHeader(loadedFile);
    startBody();
    startTable();
    addPreambleColumnHeadings(selectedTCs);
    addPreambleRows(selectedTCs);
    addTableColumnHeadings();
    addTableRows(selectedTCs);
    finishTable();
    finishBody();
    return html.toString().getBytes();
  }

  private static void addHeader(File loadedFile) {
    html.append("<html>\r\n<head>\r\n");
    setTitle(loadedFile);
    addStyle();
    html.append("</head>\r\n<body>");
  }

  private static void setTitle(File loadedFile) {
    html.append("<title>" + loadedFile.getName() + "</title>\r\n");
  }

  private static void addStyle() {
    html.append("<style type=\"text/css\">\r\n"
        + ".tg  {border-collapse:collapse;border-spacing:0;border-color:#aabcfe;margin:0px auto;}\r\n"
        + ".tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 20px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#669;background-color:#e8edff;}\r\n"
        + ".tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 20px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aabcfe;color:#039;background-color:#b9c9fe;}\r\n"
        + ".tg .tg-yw4l{vertical-align:top}\r\n"
        + ".tg .tg-pre{background-color:#ffffc7;vertical-align:top}\r\n" + "</style>\r\n");
  }

  private static void startBody() {
    html.append("<body>\r\n");
  }

  private static void startTable() {
    html.append("<table class=\"tg\">");
  }

  private static void addPreambleColumnHeadings(ArrayList<TestCase> selectedTCs) {
    html.append("  <tr>\r\n" + "    <th colspan=\"2\" class=\"tg-031e\">Associated Test Cases</th>"
        + "    <th colspan=\"5\" class=\"tg-031e\">Preamble</th>" + "</tr>\r\n");
  }

  private static void addPreambleRows(ArrayList<TestCase> selectedTCs) {
    for (Entry<String, Set<Integer>> entry : getPreambleActions(selectedTCs).entrySet()) {
      html.append("<tr><td colspan=\"2\" class=\"tg-yw4l\">");
      html.append(entry.getValue().toString());
      html.append("</td>\r\n");
      html.append("<td colspan=\"5\" class=\"tg-yw4l\">");
      html.append(entry.getKey());
      html.append("</td>\r\n</tr>\r\n");
    }
  }

  private static void addTableColumnHeadings() {
    html.append("  <tr>\r\n" + "    <th class=\"tg-031e\">Test Case ID</th>\r\n"
        + "    <th class=\"tg-yw4l\">Node List</th>\r\n"
        + "    <th class=\"tg-yw4l\">User Actions</th>\r\n"
        + "    <th class=\"tg-yw4l\">Expected Observable Response</th>\r\n"
        + "    <th class=\"tg-yw4l\">Nodes Of Interest</th>\r\n"
        + "    <th class=\"tg-yw4l\">Pass / Fail</th>\r\n"
        + "    <th class=\"tg-yw4l\">Remarks</th>\r\n" + "  </tr>");
  }

  private static void addTableRows(ArrayList<TestCase> selectedTCs) {
    for (int i = 0; i < selectedTCs.size(); i++) {
      TestCase tc = selectedTCs.get(i);
      String cellClass = "tg-yw41";
      if (tc.isPreAmble()) {
        cellClass = "tg-pre";
      }
      html.append("  <tr>\r\n    <td class=\"" + cellClass + "\">" + i + "</td>\r\n"
          + "    <td class=\"" + cellClass + "\">");
      html.append(getNodeList(tc).toString());
      html.append("</td>\r\n" + "    <td class=\"" + cellClass + "\">");
      html.append(getUserActions(tc).toString());
      html.append("</td>\r\n" + "    <td class=\"" + cellClass + "\">");
      html.append(getObservables(tc).toString());
      html.append("</td>\r\n" + "    <td class=\"" + cellClass + "\">");
      html.append(getNodesOfInterest(tc).toString());
      html.append("</td>\r\n" + "    <td class=\"" + cellClass + "\"></td>\r\n" + "    <td class=\""
          + cellClass + "\"></td>\r\n" + "  </tr>");
    }
  }

  private static void finishTable() {
    html.append("</table>\r\n");

  }

  private static void finishBody() {
    html.append("<body>\r\n");
  }

  private static Map<String, Set<Integer>> getPreambleActions(ArrayList<TestCase> selectedTCs) {
    Map<String, Set<Integer>> preamble = new TreeMap<String, Set<Integer>>();
    for (int i = 0; i < selectedTCs.size(); i++) {
      for (Node node : selectedTCs.get(i).getNodeSteps()) {
        if (!node.getAction().equals("") && node.isPreamble()) {
          if (!preamble.containsKey(node.getAction())) {
            preamble.put(node.getAction(), new TreeSet<Integer>());
          }
          preamble.get(node.getAction()).add(i);
        }
      }
    }
    return preamble;
  }

  private static StringBuilder getNodesOfInterest(TestCase tc) {
    StringBuilder nois = new StringBuilder();
    for (Node node : tc.getNodeSteps()) {
      if (node.isNoi()) {
        nois.append(node.toString() + "<br>");
      }
    }
    return nois;
  }

  private static StringBuilder getObservables(TestCase tc) {
    StringBuilder observables = new StringBuilder();
    for (Node node : tc.getNodeSteps()) {
      if (!node.getObservable().equals("")) {
        observables.append("[" + node.getTag() + "] " + node.getObservable() + "<br>");
      }
    }
    return observables;
  }

  private static StringBuilder getNodeList(TestCase tc) {
    StringBuilder nodes = new StringBuilder();
    for (Node node : tc.getNodeSteps()) {
      nodes.append(node.toString() + "<br>");
    }
    return nodes;
  }

  private static StringBuilder getUserActions(TestCase tc) {
    StringBuilder actions = new StringBuilder();
    for (Node node : tc.getNodeSteps()) {
      if (!node.getAction().equals("") && !node.isPreamble()) {
        actions.append("[" + node.getTag() + "] " + node.getAction() + "<br>");
      }
    }
    return actions;
  }
}
