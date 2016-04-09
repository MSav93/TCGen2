package renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import other.TestCase;

public class TestPathCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;
  // JButton showButton;

  TestCase path;

  public TestPathCell() {
    text = new JLabel();
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
    panel.setBackground(new Color(255, 77, 77));
  }

  private void updateData(TestCase path, boolean isSelected, JTable table) {
    this.path = path;

    text.setText("<html><b>Start Node:</b> " + path.getStartNode() + "<br><b>TargetNode:</b> "
        + path.getEndNode() + "<br><b>Steps Involved:</b> " + path.getNodeLength() + "</html>");

    if (isSelected) {
      panel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
    } else {
      panel.setBorder(table.getBorder());
    }
    if (path.isSelected()) {
      panel.setBackground(Constants.cellChosenBG);
    } else if (!path.isReachable()) {
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

  public Object getCellEditorValue() {
    return null;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    TestCase feed = (TestCase) value;
    updateData(feed, isSelected, table);
    return panel;
  }
}
