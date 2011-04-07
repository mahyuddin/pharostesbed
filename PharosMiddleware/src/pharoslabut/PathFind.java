package pharoslabut;
import java.util.LinkedList;

//import java.util.*;
public class PathFind {
    
    public double STNDRD_MOV_COST = 10;
    public double STNDRD_HYP_COST = 14;
    
    public final int INFIN = 1000;
    public final int EMPTY_ADJ = -1;
    public final char  WALL = 1;
    public final char SPACE = 0;
    public final int [] turn_cost = {0, 5, 5, 5, 14, 5, 5, 5, 0};
    public final int [] mov_cost  = {10, 14, 10, 14,
                                     10, 14, 10, 14, 0};
    
    public PathEnd  result= new PathEnd();
    
    private Mapping map;
    private int [] adjGrid = {0,0,0,0,0,0,0,0};
    
    private LinkedList<Integer> open = new LinkedList<Integer>();
    private LinkedList<Integer> closed = new LinkedList<Integer>();
    
    //private char [] open;
    private int  [] heading;
    private int  [] prev;
    private int  [] g_score;
    private int  [] h_score;
    private int  [] f_score;
        
    public PathFind (Mapping n_map, double res)
    {
        map = n_map;
        
        STNDRD_MOV_COST = res;
        STNDRD_HYP_COST = 1414*res/1000;
        
        open.clear();
        closed.clear();
        
        //open      = new char [map.map_size];    // 0:Empty 1:Filled 2:Tested
        heading  = new int  [map.Width * map.Height];
        prev     = new int  [map.Width * map.Height];        
        g_score  = new int  [map.Width * map.Height];
        h_score  = new int  [map.Width * map.Height];
        f_score  = new int  [map.Width * map.Height];
    }
    
    private int CNVRT_Y(int y)
    {
        return y/map.Width;
    }
    
    private int CNVRT_X(int x)
    {
        return x%map.Width;
    }
    
    private int CNVRT_INDEX(int x,int y)
    {
        return y*map.Width+x;
    }

    private int calc_heading(int curH, int nextH)
    {
        int headH;
        headH = nextH - curH;
        if (headH < 0) headH = headH+8;
        return headH;
    }    
    
    private int heuristic_func(int srcX, int srcY, int destX, int destY)
    {
        int x_diff, y_diff;
        x_diff = 10*Math.abs(srcX - destX);
        y_diff = 10*Math.abs(srcY - destY);
        return x_diff + y_diff;
    }
    
    private void FillAdjacency(int x, int y)
    {
        if (!map.map[y+1][x]) adjGrid[0] = CNVRT_INDEX(x,y+1);
        else adjGrid[0] = EMPTY_ADJ;
        if (!map.map[y+1][x-1]) adjGrid[7] = CNVRT_INDEX(x-1,y+1);
        else adjGrid[7] = EMPTY_ADJ;
        if (!map.map[y+1][x+1]) adjGrid[1] = CNVRT_INDEX(x+1,y+1);
        else adjGrid[1] = EMPTY_ADJ;
        if (!map.map[y][x-1]) adjGrid[6] = CNVRT_INDEX(x-1,y);
        else adjGrid[6] = EMPTY_ADJ;
        if (map.map[y][x+1]) adjGrid[2] = CNVRT_INDEX(x+1,y);
        else adjGrid[2] = EMPTY_ADJ;
        if (map.map[y-1][x-1]) adjGrid[5] = CNVRT_INDEX(x-1,y-1);
        else adjGrid[5] = EMPTY_ADJ;
        if (map.map[y-1][x+1]) adjGrid[3] = CNVRT_INDEX(x+1,y-1);
        else adjGrid[3] = EMPTY_ADJ;
        if (map.map[y-1][x]) adjGrid[4] = CNVRT_INDEX(x,y-1);
        else adjGrid[4] = EMPTY_ADJ;
    }    
    
    private int findMinF(int  [] f_score)
    {
        int i, j = 0, min = INFIN;
        
        for (i = 0; i < open.size(); i++)
        {
            if (f_score[open.get(i)] < min)
            {
                min = f_score[open.get(i)];
                j = open.get(i);
            }
        }
        return j;
    }
    
    public int A_path(int src_x, int src_y, int dest_x, int dest_y, int src_orient)
    {
        int guess_g, min;
        int src = 0, dest = 0;
        
        int adjLoc = 0;
        int dir = 0, n_heading;
        boolean guess_better;
        
        //Init
        System.out.println("Pathfinding from " + src_x + "," + src_y + " to " + dest_x + "," + dest_y);
        System.out.println("Initializing...");
        
        src  = CNVRT_INDEX(src_x,src_y);
        dest = CNVRT_INDEX(dest_x,dest_y);
        
        open.add(src);
        g_score[src] = 0;                                                //guess to src is 0
        h_score[src] = heuristic_func(src_x, src_y, dest_x, dest_y);    //guess to end
        f_score[src] = h_score[src];                                    //guess to end
        heading[src] = src_orient;
        System.out.println("done!");
        //Init End    
        
        while( !open.isEmpty() )
        {
            min = findMinF(f_score);    //find node with min F (highest priority) -> O(n)
            //System.out.println("Open list min node found X" + min);
            if (min == dest)                        //if this node is the dest, done
            {
                System.out.println("Path found\n");
                buildPath(src_x, src_y, dest_x, dest_y);
                return 1;
            }

            closed.add(min);
            open.remove((Integer) min);

            FillAdjacency(CNVRT_X(min), CNVRT_Y(min));
            
            for (dir=0; dir < 8; dir++)                                //for all adj nodes
            {
                adjLoc = adjGrid[dir];                                //check adjacent grids
                if (adjLoc != EMPTY_ADJ)                            //if grid is valid
                {
                    if (closed.contains(adjLoc))    continue;        //if nodes already tested, skip

                    n_heading = calc_heading(heading[min], dir);              
                    guess_g = g_score[min] + mov_cost[dir] + turn_cost[n_heading]; //make new guess

                    if ( !open.contains(adjLoc) )
                    {
                        open.add(adjLoc);
                        guess_better = true;
                    }
                    else if (guess_g < g_score[adjLoc]) guess_better = true;
                    else guess_better = false;

                    if (guess_better)
                    {
                        prev[adjLoc] = min;
                        g_score[adjLoc] = guess_g;
                        h_score[adjLoc] = heuristic_func(CNVRT_X(adjLoc), CNVRT_Y(adjLoc), dest_x, dest_y);    //guess to end
                        f_score[adjLoc] = g_score[adjLoc] + h_score[adjLoc];
                        heading[adjLoc] = dir;
                    }
                }
            }
        }
        System.out.println("No path found");
        return 0;
    }
    
    private void buildPath(int src_x, int src_y, int dest_x, int dest_y)
    {
        int src, dest, currPos;
        
        src  = CNVRT_INDEX(src_x, src_y);
        dest = CNVRT_INDEX(dest_x,dest_y);
        
        result.AddPointHd(dest_x, dest_y, heading[dest]);
        currPos = dest;
        
        while (currPos != src)
        {
            currPos = prev[currPos];
            result.AddPointHd(CNVRT_X(currPos), CNVRT_Y(currPos), heading[currPos]);
        }
        buildMov();
    }

    private void buildMov()
    {
        MarkedPath waypoint, n_waypoint;
        
        int  turnType, movAmt = 0, i;
        waypoint = result.GetPoint(0);
        for (i = 1; i < result.PathSize(); i++)
        {
            n_waypoint = result.GetPoint(i);
            turnType = calc_heading(waypoint.H, n_waypoint.H);
            switch (turnType) {
            case 0: if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST; else movAmt+=STNDRD_HYP_COST; break; //Move forward
            case 1: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;} 
                    result.AddMovEd(2, 45); 
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST;  break; //Turn CW 45
            case 2: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
                    result.AddMovEd(2, 90); 
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST;  break; //Turn CW 90
            case 3: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
                    result.AddMovEd(2, 135); 
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST;  break; //Turn CW 135
            case 4: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
                    result.AddMovEd(2, 180); 
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST; break; //Turn CW 180
            case 5: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
                    result.AddMovEd(3, 135); 
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST; break; //Turn CCW 135
            case 6: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
                    result.AddMovEd(3, 90);  
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST; break; //Turn CCW 90
            case 7: if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
                    result.AddMovEd(3, 45);  
                    if (n_waypoint.H % 2 == 0) movAmt+=STNDRD_MOV_COST;
                    else movAmt+=STNDRD_HYP_COST; break; //Turn CCW 45
            default : break;
            }
            waypoint = n_waypoint;
        }
        if (movAmt != 0) {result.AddMovEd(0, movAmt); movAmt = 0;}
    }
    
}