package parameters;


import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
/**
 * Validator.java
 * parses an .xml file and reports any errors it detects
 */
public class Validator {
	String filetoread;
	public Validator(String filename)
	{
		filetoread=filename;
	}
	public String validate()
	{
		StringBuffer sb=new StringBuffer();
		 try {
	      	 File x = new File(filetoread);
	         DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
	         f.setValidating(true); // Default is false
	         DocumentBuilder b = f.newDocumentBuilder();
	         // ErrorHandler h = new DefaultHandler();
	         ErrorHandler h = new MyErrorHandler();
	         b.setErrorHandler(h);
	         Document d = b.parse(x);
	      } catch (ParserConfigurationException e) {
	         sb.append(e.toString());
	      } catch (SAXException e) {
	        sb.append(e.toString());
	      } catch (IOException e) {
	         sb.append(e.toString());
	      }
		return sb.toString();
	}
   public static void main(String[] args) {
      
      try {
      	 File x = new File(args[0]);
         DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
         f.setValidating(true); // Default is false
         DocumentBuilder b = f.newDocumentBuilder();
         // ErrorHandler h = new DefaultHandler();
         ErrorHandler h = new MyErrorHandler();
         b.setErrorHandler(h);
         Document d = b.parse(x);
      } catch (ParserConfigurationException e) {
         System.out.println(e.toString());
      } catch (SAXException e) {
         System.out.println(e.toString());
      } catch (IOException e) {
         System.out.println(e.toString());
      }
   }
   private static class MyErrorHandler implements ErrorHandler {
      public void warning(SAXParseException e) throws SAXException {
         System.out.println("Warning: ");
         printInfo(e);
      }
      public void error(SAXParseException e) throws SAXException {
         System.out.println("Error: ");
         printInfo(e);
      }
      public void fatalError(SAXParseException e) throws SAXException {
         System.out.println("Fatal error: ");
         printInfo(e);
      }
      private void printInfo(SAXParseException e) {
      	 System.out.println("   Public ID: "+e.getPublicId());
      	 System.out.println("   System ID: "+e.getSystemId());
      	 System.out.println("   Line number: "+e.getLineNumber());
      	 System.out.println("   Column number: "+e.getColumnNumber());
      	 System.out.println("   Message: "+e.getMessage());
      }
   }
}


