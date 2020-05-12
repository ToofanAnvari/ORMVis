/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orm2vis;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxDomUtils;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.util.png.mxPngTextDecoder;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;
import com.mxgraph.view.mxStylesheet;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Toofan Anvari
 */
public class ORM2Vis extends JFrame {

    mxGraph _graph = new mxGraph() {
        // Overrides method to disallow edge label editing
        public boolean isCellEditable(Object cell) {
            if(getModel().isEdge(cell))
            return !getModel().isEdge(cell);
            else 
               return super.isCellEditable(cell);
        }

        // Overrides method to provide a cell label in the display
        public String convertValueToString(Object cell) {
            if (cell instanceof mxCell) {
                Object value = ((mxCell) cell).getValue();

                if (value instanceof Element) {
                    Element elt = (Element) value;
                    return elt.getAttribute("name");
                }
            }
            return super.convertValueToString(cell);
        }

        // Overrides method to store a cell label in the model
        public void cellLabelChanged(Object cell, Object newValue,
                boolean autoSize) {
            if (cell instanceof mxCell && newValue != null) {
                Object value = ((mxCell) cell).getValue();

                if (value instanceof Node) {
                    String label = newValue.toString();
                    Element elt = (Element) value;
                    elt.setAttribute("name", label);
                    newValue = elt;
                }
            }
            super.cellLabelChanged(cell, newValue, autoSize);
        }
    };
    mxUndoManager _undoManager;

    protected mxEventSource.mxIEventListener _undoHandler = new mxEventSource.mxIEventListener() {
        public void invoke(Object source, mxEventObject evt) {
            _undoManager.undoableEditHappened((mxUndoableEdit) evt
                    .getProperty("edit"));
        }
    };

    Object _parent = _graph.getDefaultParent();
    mxCell _parState = (mxCell) _parent;

    mxGraphComponent graphComponent;
    Document _doc = mxDomUtils.createDocument();

    private JMenuBar _menuBar = new JMenuBar();
    ;
    private final String _fileItems[] = new String[]{"Open", "Save"};

    public ORM2Vis() {

        super("ORM2Vis");

        JMenu menuList = new JMenu("File");
        for (int i = 0; i < _fileItems.length; i++) {

            JMenuItem item = new JMenuItem(_fileItems[i]);
            item.setActionCommand(_fileItems[i].toLowerCase());
            item.addActionListener(new MenuListener());
            menuList.add(item);
        }
        _menuBar.add(menuList);
        setJMenuBar(_menuBar);
        JToolBar tb = new JToolBar();

        JButton entityType = new JButton(new ImageIcon("images/EntityType.jpg"));
        entityType.addMouseListener(new entityTypeMouseListener());
        entityType.setBorder(BorderFactory.createEmptyBorder());

        JButton valueType = new JButton(new ImageIcon("images/ValueType.jpg"));
        valueType.addMouseListener(new valueTypeMouseListener());
        valueType.setBorder(BorderFactory.createEmptyBorder());
        valueType.setOpaque(false);

        JButton unaryFact = new JButton(new ImageIcon("images/UnarnyFactType.jpg"));
        unaryFact.addMouseListener(new unaryFactMouseListener());
        unaryFact.setBorder(BorderFactory.createEmptyBorder());

        JButton binaryFact = new JButton(new ImageIcon("images/BinaryFactType.jpg"));
        binaryFact.addMouseListener(new binaryFactMouseListener());
        binaryFact.setBorder(BorderFactory.createEmptyBorder());

        JButton roleConnector = new JButton(new ImageIcon("images/RoleConnector.jpg"));
        roleConnector.addMouseListener(new roleConnectorMouseListener());
        roleConnector.setBorder(BorderFactory.createEmptyBorder());

        tb.add(entityType);
        tb.add(valueType);
        tb.add(unaryFact);
        tb.add(binaryFact);
        tb.add(roleConnector);
        tb.setBackground(Color.white);

        add(tb, "North");

        _undoManager = new mxUndoManager();
        // Adds the command history to the model and view
        _graph.getModel().addListener(mxEvent.UNDO, _undoHandler);
        _graph.getView().addListener(mxEvent.UNDO, _undoHandler);

        // Keeps the selection in sync with the command history
        mxEventSource.mxIEventListener undoHandler = new mxEventSource.mxIEventListener() {
            public void invoke(Object source, mxEventObject evt) {
                List<mxUndoableEdit.mxUndoableChange> changes = ((mxUndoableEdit) evt
                        .getProperty("edit")).getChanges();
                _graph.setSelectionCells(_graph
                        .getSelectionCellsForChanges(changes));
            }
        };

        _undoManager.addListener(mxEvent.UNDO, undoHandler);
        _undoManager.addListener(mxEvent.REDO, undoHandler);

        mxStylesheet _styleSheet = _graph.getStylesheet();
        Map<String, Object> _defaultEdgeStyle = _styleSheet.getDefaultEdgeStyle();
        Map<String, Object> _defaultVertexStyle = _styleSheet.getDefaultVertexStyle();
        _defaultEdgeStyle.put("endArrow", " ");
        _defaultEdgeStyle.put("strokeColor", "black");
        _defaultVertexStyle.put("fontSize", 12); //ORM2 default size is 7, in here it is so small
        _defaultVertexStyle.put("fontFamily", "Times New Roman");
        _defaultVertexStyle.put("fillColor", "white");
        _defaultVertexStyle.put("strokeColor", "black");
        _defaultVertexStyle.put("spacing", 10);
        

        System.out.println("EdgeDefaultStyle:");
        for (Map.Entry<String, Object> iter : _defaultEdgeStyle.entrySet()) {

            System.out.println("Key = " + iter.getKey()
                    + ", Value = " + iter.getValue());
        }
        System.out.println("\nVertexDefaultStyle:");
        for (Map.Entry<String, Object> iter : _defaultVertexStyle.entrySet()) {

            System.out.println("Key = " + iter.getKey()
                    + ", Value = " + iter.getValue());
        }

        mxMultiplicity[] multiplicities = new mxMultiplicity[5];

        //unary type only connects with entity and value
        multiplicities[0] = new mxMultiplicity(true, null, "type", "Unary", 1,
                "2", Arrays.asList(new String[]{"entity", "value"}),
                null,
                "Role Must connect with Entity or Value", true);

        // Unary type only have 0 or 1 Incoming
        multiplicities[1] = new mxMultiplicity(false, null, "type", "Unary", 0,
                "1", null, "Unary only have 0 or 1 Incoming", null, true);

        multiplicities[2] = new mxMultiplicity(true, null, "type", "Unary", 0,
                "0", null, "Unary does not have an outgoing", null, true);

        multiplicities[3] = new mxMultiplicity(true, null, "type", "EntityType", 1,
                "100", Arrays.asList(new String[]{"unary",}),
                "null",
                "Entity Type only connects with unary", true);

        multiplicities[4] = new mxMultiplicity(true, null, "type", "ValueType", 1,
                "100", Arrays.asList(new String[]{"unary",}),
                "null",
                "Value Type only connects with unary", true);

        _graph.setMultiplicities(multiplicities);
        _graph.setMultigraph(false);
        _graph.setAllowDanglingEdges(false);
        graphComponent = new mxGraphComponent(_graph) {
            public String getEditingValue(Object cell, EventObject trigger) {
                if (cell instanceof mxCell) {
                    Object value = ((mxCell) cell).getValue();

                    if (value instanceof Element) {
                        Element elt = (Element) value;
                        return elt.getAttribute("name");
                    }
                }

                return super.getEditingValue(cell, trigger);
            }
        ;
        };
        _graph.addListener(mxEvent.CELL_CONNECTED, new mxEventSource.mxIEventListener() {
            public void invoke(Object source, mxEventObject evt) {
                graphComponent.setConnectable(false);
            }
        });
        graphComponent.setGridColor(Color.green);
        graphComponent.setGridVisible(true);
        //disable the edgeDrawing
        graphComponent.setConnectable(false);
        //Enables Shortcuts
        new orm2visKeyboardHandler(graphComponent);
        // Stops editing after enter has been pressed instead
        // of adding a newline to the current editing value
        graphComponent.setEnterStopsCellEditing(true);
        // Enables rubberband selection
        new mxRubberband(graphComponent);
        add(graphComponent);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);

    }

    class entityTypeMouseListener extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {

            _graph.getModel().beginUpdate();
            try {

                Element entity = _doc.createElement("entity");
                entity.setAttribute("name", "");
                entity.setAttribute("type", "EntityType");
                Object v1 = _graph.insertVertex(_parent, null, entity, e.getX(), e.getY(), 50, 30, "verticalLabelPosition=middle;autosize=true;rounded=true;entryX=0;entryY=0;resizable=false");

            } finally {
                _graph.getModel().endUpdate();
            }
        }
    }

    class valueTypeMouseListener extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {

            _graph.getModel().beginUpdate();
            try {

                Element value = _doc.createElement("value");
                value.setAttribute("name", "");
                value.setAttribute("type", "ValueType");
                _graph.insertVertex(_parent, null, value, e.getX(), e.getY(), 50, 30, "strokeWidth=1.5;autosize=true;rounded=true;align=center;dashed=true;resizable=false");

            } finally {
                _graph.getModel().endUpdate();
            }
        }
    }

    class unaryFactMouseListener extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {

            _graph.getModel().beginUpdate();
            try {
                Element unary = _doc.createElement("unary");
                unary.setAttribute("name", "");
                unary.setAttribute("type", "Unary");
                _graph.insertVertex(_parent, null, unary, e.getX(), e.getY(), 25, 15, "verticalLabelPosition=top;resizable=false");
            } finally {
                _graph.getModel().endUpdate();
            }
        }

    }

    class binaryFactMouseListener extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {

            _graph.getModel().beginUpdate();
            try {
                Element unary = _doc.createElement("unary");
                unary.setAttribute("name", "");
                unary.setAttribute("type", "Unary");
                Object grandParent = _graph.insertVertex(_parent, null, "", e.getX(), e.getY(), 56, 21, "foldable=false;verticalLabelPosition=top;resizable=false;connectable=false");
                //_graph.getModel().setVisible(grandParent, false);
                Object parent = _graph.insertVertex(grandParent, null, "", 3, 3, 50, 15, "verticalLabelPosition=top;deletable=false;foldable=false;resizable=false;movable=false");
                Object v1 = _graph.insertVertex(parent, null, unary, 0, 0, 25, 15, "editable=0;deletable=false;verticalLabelPosition=top;movable=false;resizable=false");
                boolean result = _graph.isCellEditable(v1);
                System.out.println(result);
                Object v2 = _graph.insertVertex(parent, null, unary, 25, 0, 25, 15, "editable=0;deletable=false;verticalLabelPosition=top;movable=false;resizable=false");

            } finally {
                _graph.getModel().endUpdate();
            }
        }

    }

    class roleConnectorMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {

            graphComponent.setConnectable(true);

        }

    }

    public class MenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if ("open".equals(command)) {
                System.out.println("Open Graph");
                String file = System.getProperty("user.dir") + "\\" + "graph.xml";

                Document document;
                try {
                    document = mxXmlUtils.parseXml(mxUtils.readFile(file));
                    mxCodec codec = new mxCodec(document);
                    codec.decode(document.getDocumentElement(), _graph.getModel());
                    graphComponent.setGraph(_graph);

                } catch (IOException ex) {
                    Logger.getLogger(ORM2Vis.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            if ("save".equals(command)) {
                System.out.println("Save Graph");
                String curDir = System.getProperty("user.dir");
                String filename = curDir + "\\" + "graph.xml";
                mxCodec codec = new mxCodec();
                String xml = mxXmlUtils.getXml(codec.encode(_graph
                        .getModel()));
                try {
                    mxUtils.writeFile(xml, filename);
                } catch (IOException ex) {
                    Logger.getLogger(ORM2Vis.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

    }

    public mxUndoManager getUndoManager() {
        return _undoManager;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ORM2Vis();
            }
        });
    }
}
