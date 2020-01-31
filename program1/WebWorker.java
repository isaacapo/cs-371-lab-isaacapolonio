/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

//Helper variables
private Socket socket;
boolean wasRead = false;
boolean isEmpty = false;
boolean fileExists;

//global file reader
BufferedReader fileReader;

//Define tag strings
String datetag = "{{cs371date}}";
String servertag = "{{cs371server}}";

//Define correct tag text
LocalDate cs371Date = LocalDate.now();
String cs371Server = "Hoya Server";
/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
   String fileIn;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      readHTTPRequest(is);
      writeHTTPHeader(os,"text/html");
      writeContent(os);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private void readHTTPRequest(InputStream is)
{
   String line;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
         if(line.contains("GET") && wasRead == false)	//read the file request but not the icon request
         {
        	 String[] tokens = line.split("[ ]+");
        	 //System.err.println("REQUESTED FILE : " + tokens[1]);	//DEBUG
        	 String trueFile = tokens[1].substring(1, tokens[1].length());	//This is the final parsed file name
        	 
        	 if(tokens[1] == "/")	//Check if page asked for no file
        	 {
        		 isEmpty = true;
        	 }
        	 
        	 
        	 File file = new File(trueFile);	//make a new file object using requested file
        	 fileReader = new BufferedReader(new FileReader(file));	//make fileReader use that file
        	 
        	 fileExists = true;
        	 wasRead = true;
         }
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         fileExists = false;
         break;
      }
   }
   return;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
	
	//In firefox, the page request recieves 404 error in inspect element>network
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   
   if(fileExists == true)	//Send 404 Error if fileExists == false.
   {
	   os.write("HTTP/1.1 200 OK\n".getBytes());
   }
   else	os.write("HTTP/1.1 404 Not Found\n".getBytes());
   
   
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
//   os.write("Content-Type: ".getBytes());
//   os.write(contentType.getBytes());
   os.write("\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os) throws Exception
{
	if(fileExists == true)	//If file of same name is found within directory file or not		
	{	
		//os.write("<head></head>\n".getBytes());
		while(fileReader.ready())	//while there are still lines to read in the file
		{
			String readingLine = fileReader.readLine();	//Read the current line of the file
			
	       	 String[] words = readingLine.split(" ", 20);	//split file's current line into words.
	       	 String finalLine = "";	//final line to be built with correct words
	       	 
	       	 for (int i = 0; i < words.length; i++)
	       	 {
	       		//System.out.println(">" + words[i] + "<");			//DEBUG
	       		 
	       		if(words[i].equals(datetag))	//if this word is the same as the date tag
	       		 {	 
	       			//System.out.println("! ! [datetag found] ! !"); 	//DEBUG
	       			finalLine = finalLine + cs371Date.toString() + " ";
	       		 }
	       		 else if(words[i].equals(servertag)) //if this word is the same as the server tag
	       		 {
	       			 //System.out.println("! ! [servertag found] ! !");		//DEBUG
	       			finalLine = finalLine + cs371Server + " ";
	       		 }
	       		 else	//if the word matches no tag
	   			 {
	       			 finalLine = finalLine + words[i] + " ";
	   			 }	 
	       	 }
			os.write(finalLine.getBytes());	//output final line with tags replaced
		}
	}
}

} // end class
