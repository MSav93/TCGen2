import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;


/**
 * Written by Soh Wei Yu. You're welcome to contact me with any questions: sohweiyu@outlook.com
 */
public class TestPathsog extends JDialog {

  private static final long serialVersionUID = 1L;

  public TestPathsog(Main frame, HashMap<String, String[]> testPaths) {

    super(frame, "Generated Test Case Paths", true);


    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {

        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }

        DefaultTableModel model = new DefaultTableModel() {
          private static final long serialVersionUID = 1L;

          public Class<String> getColumnClass(int columnIndex) {
            return String.class;
          }

          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };

        model.addColumn("Test Case Path ID");
        model.addColumn("Pre-Amble");
        model.addColumn("Test Case Path");
        model.addColumn("Post-Amble");

        for (int row = 0; row < testPaths.size(); row++) {

          // populate rows

          Vector<String> rowData = new Vector<>(3);

          rowData.add("Test Case Path " + (row + 1));
          String[] testPathStr = testPaths.get(Integer.toString(row + 1));

          rowData.add(testPathStr[0].substring(1));
          rowData.add(testPathStr[1].substring(1));
          rowData.add(testPathStr[2].substring(1));

          model.addRow(rowData);
        }

        JTable table = new JTable(model);
        table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
        JScrollPane scroll = new JScrollPane(table);
        getContentPane().add(scroll);

        getContentPane().setLayout(null);

        DeleteRowFromTableAction deleteAction = new DeleteRowFromTableAction(table, model);

        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
        am.put("deleteRow", deleteAction);

        scroll.setBounds(0, 0, 0, 0);

        table.setDefaultRenderer(String.class, new MultiLineCellRenderer());

        try {
          for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++) {
              Component comp =
                  table.prepareRenderer(table.getCellRenderer(row, column), row, column);
              rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }

            table.setRowHeight(row, rowHeight * 2);
          }
        } catch (ClassCastException e) {
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(5, 126, 1083, 587);
        // scrollPane.setBounds(0, 176, 1083, 587);
        // scrollPane.setBounds(222, -51, 1158, 850);
        getContentPane().add(scrollPane);

        JButton btnNewButton = new JButton("Ok");
        btnNewButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
          }
        });
        btnNewButton.setBounds(425, 723, 247, 81);
        // btnNewButton.setBounds(425, 793, 247, 81);
        getContentPane().add(btnNewButton);

        JLabel lblNewLabel =
            new JLabel(
                "Delete the generated test case paths that you do not wish to be included in test cases.");
        lblNewLabel.setBounds(26, 5, 1067, 33);
        // lblNewLabel.setBounds(26, 28, 1067, 33);
        getContentPane().add(lblNewLabel);

        JButton btnNewButton_1 = new JButton(deleteAction);
        // btnNewButton_1.setBounds(26, 86, 331, 71);
        btnNewButton_1.setBounds(26, 46, 331, 71);
        getContentPane().add(btnNewButton_1);

        setSize(1125, 978);

        // setLocationRelativeTo(frame);
        setLocationRelativeTo(null);
      }

    });
  }

  public abstract class AbstractTableAction<T extends JTable, M extends TableModel> extends
      AbstractAction {

    /**
		* 
		*/
    private static final long serialVersionUID = 1L;
    private T table;
    private M model;

    public AbstractTableAction(T table, M model) {
      this.table = table;
      this.model = model;
    }

    public T getTable() {
      return table;
    }

    public M getModel() {
      return model;
    }

  }

  /**
   * Delete row from table.
   */
  public class DeleteRowFromTableAction extends AbstractTableAction<JTable, DefaultTableModel> {

    /**
		* 
		*/
    private static final long serialVersionUID = 1L;

    public DeleteRowFromTableAction(JTable table, DefaultTableModel model) {
      super(table, model);
      putValue(NAME, "Delete Selected Rows");
      putValue(SHORT_DESCRIPTION, "Delete selected rows");
      table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          setEnabled(getTable().getSelectedRowCount() > 0);
        }
      });
      setEnabled(getTable().getSelectedRowCount() > 0);


    }

    @Override
    public void actionPerformed(ActionEvent e) {

      JTable table = getTable();

      Main parent = (Main) getParent();



      if (table.getSelectedRowCount() > 0) {
        List<Vector> selectedRows = new ArrayList<>(25);
        DefaultTableModel model = getModel();
        Vector rowData = model.getDataVector();
        for (int row : table.getSelectedRows()) {


          String[] testPathID = table.getValueAt(row, 0).toString().split(" ");

          parent.listOfTestPaths.remove(testPathID[3]);
          parent.testPaths.remove(testPathID[3]);

          int modelRow = table.convertRowIndexToModel(row);
          Vector rowValue = (Vector) rowData.get(modelRow);
          selectedRows.add(rowValue);
        }

        for (Vector rowValue : selectedRows) {
          int rowIndex = rowData.indexOf(rowValue);
          model.removeRow(rowIndex);
        }
      }
    }

  }



  /**
   * @version 1.0 11/09/98
   */

  class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {

    /**
		* 
		*/
    private static final long serialVersionUID = 1L;

    public MultiLineCellRenderer() {
      setLineWrap(true);
      setWrapStyleWord(true);
      setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(table.getBackground());
      }
      setFont(table.getFont());
      if (hasFocus) {
        setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        if (table.isCellEditable(row, column)) {
          setForeground(UIManager.getColor("Table.focusCellForeground"));
          setBackground(UIManager.getColor("Table.focusCellBackground"));
        }
      } else {
        setBorder(new EmptyBorder(1, 2, 1, 2));
      }
      setText((value == null) ? "" : value.toString());
      return this;
    }
  }

  public void showCentered() {

    Main parent = (Main) getParent();
    Dimension dim = parent.getSize();
    Point loc = parent.getLocationOnScreen();

    Dimension size = getSize();

    loc.x += (dim.width - size.width) / 2;
    loc.y += (dim.height - size.height) / 2;

    if (loc.x < 0)
      loc.x = 0;
    if (loc.y < 0)
      loc.y = 0;

    Dimension screen = getToolkit().getScreenSize();

    if (size.width > screen.width)
      size.width = screen.width;
    if (size.height > screen.height)
      size.height = screen.height;

    if (loc.x + size.width > screen.width)
      loc.x = screen.width - size.width;

    if (loc.y + size.height > screen.height)
      loc.y = screen.height - size.height;

    setBounds(loc.x, loc.y, size.width, size.height);

    setVisible(true);
  }
}
