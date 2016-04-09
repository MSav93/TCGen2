package renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import tree.Node;

public class CPCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;

  Node cp;

  public CPCell() {
    text = new JLabel();
    text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
  }

  private void updateData(Node cp, boolean isSelected, JTable table) {
    this.cp = cp;
    if (cp != null) {

      text.setText(cp.toString());
      if (isSelected) {
        panel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE));
      } else {
        panel.setBorder(table.getBorder());
      }
      if (cp.isCp()) {
        panel.setBackground(Constants.cellChosenBG);
      } else {
        panel.setBackground(Constants.cellNotChosenBG);
      }
    }
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    Node cp = (Node) value;
    updateData(cp, isSelected, table);
    return panel;
  }

  public Object getCellEditorValue() {
    return null;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    Node cp = (Node) value;
    updateData(cp, isSelected, table);
    return panel;
  }
}
