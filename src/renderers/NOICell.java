package renderers;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import tree.Node;

public class NOICell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = -5535773127849322173L;
  JPanel panel;
  JLabel text;

  Node node;

  public NOICell() {
    text = new JLabel();
    text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));

    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(text);
    panel.setBackground(new Color(255, 77, 77));
  }

  private void updateData(Node node, boolean isSelected, JTable table) {
    this.node = node;

    text.setText(node.toString());
    Color bg = table.getSelectionBackground();

    if (isSelected) {
      panel.setBackground(
          new Color(Math.min(255, bg.getRed() + 100), bg.getGreen(), bg.getBlue(), bg.getAlpha()));
    } else {
      panel.setBackground(table.getBackground());
    }
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
      int row, int column) {
    Node node = (Node) value;
    updateData(node, isSelected, table);
    return panel;
  }

  public Object getCellEditorValue() {
    return null;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    Node node = (Node) value;
    updateData(node, isSelected, table);
    return panel;
  }
}
