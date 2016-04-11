package renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import other.TestCase;
import tree.Node;

public class TestCaseCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;

  TestCase path;

  public TestCaseCell() {
    text = new JLabel();
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
    panel.setBackground(new Color(255, 77, 77));
  }

  private void updateData(TestCase path, boolean isSelected, JTable table) {
    this.path = path;

    text.setText(getCellText());

    // If cell is selected give it a border
    if (isSelected) {
      panel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
    } else {
      panel.setBorder(table.getBorder());
    }
    if (path.isSelected()) {
      // If test case has been added to test path colour it green
      panel.setBackground(Constants.cellChosenBG);
    } else if (!path.isReachable()) {
      // If test case is unreachable colour it red
      panel.setBackground(Constants.cellUnavailableBG);
    } else {
      panel.setBackground(Constants.cellNotChosenBG);
    }
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    TestCase feed = (TestCase) value;
    updateData(feed, true, table);
    return panel;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    TestCase feed = (TestCase) value;
    updateData(feed, isSelected, table);
    panel.setToolTipText(getToolTipText());
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    return panel;
  }

  public Object getCellEditorValue() {
    return null;
  }

  public String getToolTipText() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><b>Steps Away:</b> " + path.getNodesAwayLength());
    for (Node n : path.getNodesAway()) {
      sb.append("<br>" + Constants.htmlTabSpacing + formatNode(n));
    }
    sb.append("<br><b>Steps Involved:</b> " + path.getNodeLength());
    for (Node n : path.getNodeSteps()) {
      sb.append("<br>" + Constants.htmlTabSpacing + formatNode(n));
    }
    sb.append("</html>");
    return sb.toString();
  }

  private String formatNode(Node n) {
    return "[" + n.getTag() + "] " + n.getComponent() + ": " + n.getBehaviour() + "["
        + n.getBehaviourType() + "]" + getFlagSymbol(n.getFlag());
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

  private String getCellText() {
    return "<html><b>Start Node:</b> " + path.getStartNode() + "<br><b>TargetNode:</b> "
        + path.getEndNode() + "<br><b>Steps Away:</b> " + path.getNodesAwayLength()
        + Constants.htmlTabSpacing + "<b>Steps Involved:</b> " + path.getNodeLength() + "</html>";
  }
}
