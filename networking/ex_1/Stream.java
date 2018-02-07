import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Stream {
    public static void main(String[] args) {

        String site = "http://www.";
        Scanner reader = new Scanner(System.in); 
        System.out.println("Enter a website in name.com format: ");
        String temp = reader.next();
        reader.close();
        String fullsite = site+temp;
        try {

            URL source = new URL(fullsite);
            //store source from buffer
            BufferedReader buffread
                     = new BufferedReader(new InputStreamReader(source.openStream()));
            String inputLine;
            //convert and print to console
            while ((inputLine = buffread.readLine()) != null) {
                System.out.println(inputLine);
            }
            //close reader
            buffread.close();
        } catch (MalformedURLException me) {
            System.out.println("MalformedURLException: " + me);
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
        }
    }
}
