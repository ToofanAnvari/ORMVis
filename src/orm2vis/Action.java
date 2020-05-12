/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orm2vis;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author Toofan Anvari
 */
public class Action {
    
    public static final ORM2Vis getORM2Vis(ActionEvent e){
        
        if (e.getSource() instanceof Component)
		{
			Component component = (Component) e.getSource();

			while (component != null
					&& !(component instanceof ORM2Vis))
			{
				component = component.getParent();
			}

			return (ORM2Vis) component;
		}
        
		return null;
    }
    
     public static class HistoryAction extends AbstractAction {

        /**
         *
         */
        protected boolean undo;

        /**
         *
         */
        public HistoryAction(boolean undo) {
            this.undo = undo;
        }

        /**
         *
         */
        public void actionPerformed(ActionEvent e) {
            
            ORM2Vis vis = getORM2Vis(e);
            

            if (undo) {
                vis.getUndoManager().undo();
            } else {
                vis.getUndoManager().redo();
            }

        }
    }
}
