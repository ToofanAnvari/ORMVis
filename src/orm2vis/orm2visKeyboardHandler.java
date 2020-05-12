/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orm2vis;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import com.mxgraph.swing.util.mxGraphActions;


/**
 *
 * @author Toofan Anvari
 */
public class orm2visKeyboardHandler extends mxKeyboardHandler {

    public orm2visKeyboardHandler(mxGraphComponent graphComponent) {
        super(graphComponent);
    }

    protected InputMap getInputMap(int condition) {
        InputMap map = super.getInputMap(condition);

        if (condition == JComponent.WHEN_FOCUSED && map != null) {
            map.put(KeyStroke.getKeyStroke("control Z"), "undo");
            map.put(KeyStroke.getKeyStroke("control Y"), "redo");

        }

        return map;
    }

    protected ActionMap createActionMap() {
        ActionMap map = super.createActionMap();
        
        map.put("undo", new Action.HistoryAction(true));
        map.put("redo", new Action.HistoryAction(false));

        return map;
    }

}
