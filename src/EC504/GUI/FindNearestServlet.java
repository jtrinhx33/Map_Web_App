package EC504.GUI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import LoadData.MapData;
import RTree.RTreeNode_GlobalScale;
import RTree.pqDistances;

/**
 * Servlet implementation class FindNearestServlet
 */
@WebServlet(name="FindNearestServlet", urlPatterns={"/Find"})
public class FindNearestServlet extends HttpServlet {
	private static final long serialVersionUID = 102831973239L;
      
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
			// Store form fields and convert to variable type double
			Double y = Double.parseDouble(request.getParameter("latitude"));
			Double x = Double.parseDouble(request.getParameter("longitude"));
			int k = Integer.parseInt(request.getParameter("k"));
			
			// Truncate original values to four decimal places (or will cause error in "pqDistances.java, line 38")
			x = (long) (x * 10000) / 10000.0;
			y = (long) (y * 10000) / 10000.0;
	 
	        // Load map data
			String myFile = getServletContext().getRealPath("NationalFile_StateProvinceDecimalLatLong.txt");
			HashMap mapData_States = LoadMapData(myFile);

			// Create RTree
			RTreeNode_GlobalScale Root = CreateRTree(mapData_States);
			ArrayList<RTreeNode_GlobalScale> nodesContainingPoint;
			nodesContainingPoint = Root.findNodesContainingPoint(x, y);
			String stateAbbrv = null;
			for (int ii = 0; ii < nodesContainingPoint.size(); ii++) {
				RTreeNode_GlobalScale currentStateNode = nodesContainingPoint.get(ii);			// Get current state node
				//if (stateAbbrv.equals("Root: United States")) continue;
				stateAbbrv = currentStateNode.getName();										// Get name of state (abbreviation)	
				//System.out.println(nodesContainingPoint.get(ii));
			}
			
			// Display resulting nodes that point is in or nearby
			for (int ii = 0; ii < nodesContainingPoint.size(); ii++) {
				System.out.println("From recursive function. Nodes containing test point: " + nodesContainingPoint.get(ii).getName());
			}
		
			// Try priority queue
			//pqDistances pq = new pqDistances(mapData_States.States, 100, "CO", -104.98, 39.7516667);
			pqDistances pq = new pqDistances(mapData_States, 100, stateAbbrv, x, y, k); // 
			//pq.printQueue(k);
			
	        // Get response writer
	        PrintWriter writer = response.getWriter();
	        // Build HTML code
	        String htmlResponse = "<html>";
	        htmlResponse += "<style type=\"text/css\"> body { font-size: 0.8em; font-family: sans-serif; margin-top: 0.1em; margin-left: 0; margin-right: 0;} </style>";
	        htmlResponse += "<body>Original point was: " + y + ", " + x + "<br/>";      
	        htmlResponse += "The " + k + " nearest counties are: " + "<br/>";
	        htmlResponse += pq.printQueue(k);
	        htmlResponse += "</body>";
	        htmlResponse += "</html>";
	      
	        // Return response
	        writer.println(htmlResponse);
}

	public static RTreeNode_GlobalScale CreateRTree(HashMap<String, HashMap<String, ArrayList>> mapData) {
		RTreeNode_GlobalScale rootNode = new RTreeNode_GlobalScale();			// Root of tree
		
		//This prints out all the states. Just for debugging purposes.
	    for (Object current_state : mapData.keySet()) {											// Iterate through all states
	    	
	    	// IMPORTANT: Uncomment when selecting to print out a specific state (for testing purposes only)
	    	//if (!current_state.toString().equals("MH")) continue;
	    	
	    	RTreeNode_GlobalScale stateNode = new RTreeNode_GlobalScale();						// Initialize current state's nodes
	        //System.out.println(current_state);													// Prints out state
	        stateNode.setName(current_state.toString());										// Add state's name to node
	        
	        HashMap state_value = (HashMap) mapData.get(current_state);							// Get internal state hashmap (counties and their dimensions)
	        
	        for (Object current_county : state_value.keySet()){									// Iterate through all counties in state
	        	
	            //System.out.print("   - " + current_county);										// Prints out county belonging to [this] state
	            ArrayList county_dimensions = (ArrayList) state_value.get(current_county);		// Get county's list of rectangular dimensions.
	            //System.out.print(" " + county_dimensions + "\n");								// Print out county's dimensions (points form)
	            
	            // Store coordinates. Note ArrayList points order: [x1, y1, x2, y2]
	            Double x1, x2, y1, y2;
	            
	            if (county_dimensions.size() != 4) continue;		// If ArrayList does not have 4 points, skip county
	            
	            y2 = (Double) county_dimensions.get(0);			// Get y2 = max latitude
	            x1 = (Double) county_dimensions.get(1);			// Get x1 = min longitude 
	            y1 = (Double) county_dimensions.get(2);			// Get y1 = min latitude
	            x2 = (Double) county_dimensions.get(3);			// Get x2 = max longitude
	            
	            // Create county node: name, x1, x2, y1, y2
	            RTreeNode_GlobalScale countyNode = new RTreeNode_GlobalScale(current_county.toString(), x1, x2, y1, y2);
	            
	            // Add county to state
	            stateNode.addChild(countyNode);
	        }
	        
	        // Add state to root node
	        rootNode.addChild(stateNode);
	    }
	    
	    rootNode.setName("Root: United States");
		
	    return rootNode;
	}

	public static HashMap LoadMapData(String filename) throws IOException {
		System.out.println("Loading Map Data...");
		MapData mapData = new MapData(filename);
		System.out.println("Loading Map Data completed...");
		return mapData.getStates();
	}
}
