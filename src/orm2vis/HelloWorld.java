package orm2vis;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.awt.Color;

public class HelloWorld extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	public HelloWorld()
	{
		super("Hello, World!");

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
                Object v1;

		graph.getModel().beginUpdate();
		try
		{
			 v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
					30, "shape=swimlane;spacingBottom=25;autosize=true");
			Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
					80, 30, "autosize=false");
			graph.insertEdge(parent, null, "edge", v1, v2,"strokeWidth=2;endArrow;entryX=0;entryY=0.5;exitX=0.5;exitY=1");
		}
		finally
		{
			graph.getModel().endUpdate();
		}

                
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
                //graphComponent.getViewport().setBackground(Color.red);
		getContentPane().add(graphComponent);
	}

	public static void main(String[] args)
	{
		HelloWorld frame = new HelloWorld();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 320);
		frame.setVisible(true);
	}

}
