package table;

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
import other.PreAmble;
import other.TestCase;
import tree.Node;

public class TestCasePreAmbleCell extends AbstractCellEditor
    implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;

  PreAmble testCases;

  public TestCasePreAmbleCell() {
    text = new JLabel();
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
    panel.setBackground(Constants.notSelectedColour);
  }

  protected void updateData(PreAmble testCase, boolean isSelected, JTable table) {
    this.testCases = testCase;
    text.setText(getCellText());
    // If cell is selected give it a border
    if (isSelected) {
      panel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
    } else {
      panel.setBorder(table.getBorder());
    }
    panel.setToolTipText(getToolTipText());
    ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    PreAmble feed = (PreAmble) value;
    updateData(feed, true, table);
    return panel;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    PreAmble feed = (PreAmble) value;
    updateData(feed, isSelected, table);
    return panel;
  }

  public Object getCellEditorValue() {
    return null;
  }

  private int getNodesInvolved() {
    int nodes = 0;
    for (TestCase tc : testCases) {
      nodes += tc.getNodeLength();
    }
    return nodes;
  }

  private int getUserActionsInvolved() {
    int i = 0;
    for (TestCase tc : testCases) {
      for (Node n : tc.getNodeSteps()) {
        if (!(n.getAction().equals(""))) {
          i++;
        }
      }
    }
    return i;
  }

  public String getToolTipText() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>");
    sb.append("<b>Nodes Involved:</b> " + getNodesInvolved());
    for (TestCase tc : testCases) {
      for (Node n : tc.getNodeSteps()) {
        sb.append("<br>" + Constants.htmlTabSpacing + formatNode(n));
      }
    }
    sb.append("</html>");
    return sb.toString();
  }

  private String formatNode(Node n) {
    return "[" + n.getTag() + "] " + n.getComponent() + ": " + n.getBehaviour() + "["
        + n.getBlockIndex() + "]" + getFlagSymbol(n.getFlag());
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
    return "<html><b>Start Node:</b> " + testCases.get(0).getStartNode() + "<br><b>End Node:</b> "
        + testCases.get(testCases.size() - 1).getFirstNodeOfEndingBlock() + "<br>"
        + "<b>User Actions Involved:</b> " + getUserActionsInvolved() + "</html>";
  }
}
