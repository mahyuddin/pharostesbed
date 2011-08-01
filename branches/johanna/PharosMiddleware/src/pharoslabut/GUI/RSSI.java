package pharoslabut.GUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/** Test Wireless Signal Strength
 * @author Johanna Rivera Santos 
 * Summer 2011
 *
 */
public class RSSI{
/*	String IP;
	Process ls=null;
    BufferedReader input=null; 
    String line=null; 
    String signal;
	
	public RSSI(String IP){
		this.IP = IP;
		
	}
	
	
	public String run() throws IOException{
		ls= Runtime.getRuntime().exec("tcpdump -I -s 256 -i en1"); 
        input = new BufferedReader(new InputStreamReader(ls.getInputStream()));
        while( (line=input.readLine())!=null) {
            
            int index = line.indexOf(IP+" >");
           if(index != -1){
        	   signal = line.substring(77, 81);
           }
           else{ signal = "Not Found";}
        }
            
     
            
        
		return signal; 
	}
		
	public String getIP(){
		return IP;
	} */
	
	

	
	public static void main(String[] args){ 
		Process process = null;
	        BufferedReader input = null; 
	        PrintWriter out = null;
	        String line = null; 
	       
	        
        
      {
       
            try { 
            	
                   process= Runtime.getRuntime().exec("tcpdump -I -s 256 -i en1"); 
                   input = new BufferedReader(new InputStreamReader(process.getInputStream())); 
                   out = new PrintWriter(new FileWriter("output2.txt"));
                   
                  
                
            	
                } catch (IOException e1) { 
                    e1.printStackTrace();   
                    System.exit(1); 
                } 
                 
                
               try { 
                       while( (line=input.readLine())!=null) {
    
                        int index = line.indexOf("10.11.12.33 >");
                       
                        
                        
                        if(index != -1){
                        	
                        System.out.println(index);
                        System.out.println(line);
                        System.out.println(line.substring(77, 81));
                        out.println(line);
                        
                        }
                       }
                      out.close();

                } catch (IOException e1) { 
                    e1.printStackTrace();   
                    System.exit(0); 
                } 
               
               
              
    } 
      
		
	} 
}

