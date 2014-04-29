import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import hypermedia.video.*; 
import processing.net.*; 
import processing.video.*; 
import java.io.*; 
import java.nio.ByteBuffer; 
import java.awt.Shape; 
import java.awt.Polygon; 
import interfascia.*; 
import java.awt.TextField; 
import java.awt.Rectangle; 
import java.awt.Point; 
import java.lang.NumberFormatException; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CVServer extends PApplet {









 





/**
 * GeneralServer2.0
 * @author CleoSchneider drawing on work done by Audrey St. John
 *
 * GeneralServer2.0 combines the functionality of the original GeneralServer
 * some better detection techniques implemented for detecting holes. The server
 * opens a socket on the specified port and using a camera takes the difference
 * between a reference shot and current background to find all "blobs" within the
 * image. These blobs good be holes or solid objects. We then send the information
 * out to all programs listening on specified port.
 **/

/**
 * Fields
 **/
//We may send to types of blobs a shell or solid object
//or we may send a hole or filled object
static final int SHELL = 0;
static final int HOLE = 1;
static final int FACES=2;
static final int RESUME_DELAY = 10;

//if we want to delay sending information so that we don't
//overload the server
static final int DELAY_MAX = 0;

//minimum width of a hole if we are considering holes
int HOLE_THRESH = 75;

//if we want to include a timer of sorts for this we will use this field
boolean resumeAble = false;

//booleans to tell whether we are sending holes, shells, or both
boolean holesEnabled = false;
boolean shellsEnabled = false;
boolean facesEnabled=false;

//the count
int counter = 0;

//the server
Server myServer;

//an instance of opencv
OpenCV opencv;

//timer for resume
int elapsedTime = 0;

// width and height to be scaled to
int targetW = 800;
int targetH = 500;

// what camera is going to capture
int captureW=320;
int captureH=240;

//the offset of the image from the edge of the frame or other images
int imWidthOffset = 10;
int imHeightOffset = 10;

//width and height of the frame
//int w = 1200;
//int h = 400;
int w = captureW*4+3*imWidthOffset;
int h=captureH*2;


//macros for offsetting x coordinates
int DISPLACE_XHOLE = imWidthOffset + (2*(w/4));
int DISPLACE_XSHELL = imWidthOffset + w/4;
int DISPLACE_XFACE= imWidthOffset+(3*(w/4));

//the height of the space for the buttons
int buttonPanelHeight = 75;

//threshold for the image
boolean shellThresholding = false, holeThresholding = false, flipHorizontal=false;
int threshold = 208;
int threshold2 = 24;
int shellThresh = 80;

//the current starting positions for the most recent box
float selStartX;
float selStartY;

//save start and end points for the selected rectangle
ArrayList selRectStartX;
ArrayList selRectStartY;
ArrayList selRectEndX;
ArrayList selRectEndY;

ArrayList<AreaOfInterest> interestAreas;


//the scaled positions for the box
ArrayList scaleX;
ArrayList scaleY;

//an array for the pixels
int[] pix;

//boolean for if we have set the area of interest
boolean rectSet = false;

//the button fields
//the interfascia version of a JPanel
GUIController c;

//the buttons
IFButton resetAreas, newRefShot, setHoleThresh, quit;

//the checkboxes
IFCheckBox flipHor, hole, shell, resume, shellThreshEnable, holeThreshEnable, faces;

IFCheckBox dragHoleThreshold, dragShellThreshold;

// Interfascia textfield to input shell thresholding value
IFTextField shellThresholdTextInput;
//  Interfascia button to set the shell threshhold to the value in its text field
IFButton setShellThreshold; 

// Interfascia textfield to input hole thresholding value
IFTextField holeThresholdTextInput;
// Interfascia button to set the hole threshhold to the value in its text field
IFButton setHoleThreshold; 


//how many blobs we have to send over on every tic
int numSentBlobs;

Capture video;

/**
 * Setup
 * Like a main method sets up the frame for reading images from the camera selected
 **/
public void setup() {

  //set the size of the entire frame
  size( w+imWidthOffset, (h/2) + imHeightOffset*3 + buttonPanelHeight);

  //initialize the list of starting positions   
  selRectStartX = new ArrayList();
  selRectStartY = new ArrayList();
  selRectEndX = new ArrayList();
  selRectEndY = new ArrayList();

  //initialize the list of scaling lists
  scaleX = new ArrayList();
  scaleY = new ArrayList();

  // Starts a myServer on port 5204
  myServer = new Server(this, 5204); 

  //creates a new instance of openCV
  opencv = new OpenCV( this );

  //begins reading images from the camera
  //can pass width, height, and index of camera to be used

  // ASJ update 3/7/13 to new processing 2.0b7
  //opencv.capture((w/4)-imWidthOffset,h/2);
  int captureW=(w/4)-imWidthOffset;
  int captureH=h/2;

  captureW=320; 
  captureH=240;
  opencv.allocate(captureW, captureH);
  opencv.threshold(255); // Clears allocated image
  video = new Capture(this, captureW, captureH);
  video.start();
  // end ASJ    


  // opencv.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );    // load the FRONTALFACE description file
  createButtons();

  selRectStartX.add((float)imWidthOffset);
  selRectStartY.add((float)imHeightOffset);
  selRectEndX.add((float) w/4);
  selRectEndY.add((float) h/2 + imHeightOffset);

  //add the width and height to the scaled arraylists 
  scaleX.add((float) targetW/Math.abs(imWidthOffset-(w/4)));
  scaleY.add((float) targetH/Math.abs(imHeightOffset-(h/2+imHeightOffset)));
}

/**
 * Like the second part of the main method
 * Executed line by line after the setup()
 **/
public void draw() {
  if ( setScreen() )
  {   


    //dont fill in the shape
    noFill();

    //walk through the blobs and send a long string concatinated by the indices for better indexing
    StringBuffer s = new StringBuffer();

    //the number of blobs to be sent over
    numSentBlobs = 0;

    //check whether holes or shells were enabled and egin detecting them
    checkTypeEnabled(s);

    //if we have chosen an area of interest, maintain the bounding rectangle
    //echo the bounding rectangle in the difference frame
    drawSelectedAreas();

    //delay how often we send information using a counter
    if (counter<DELAY_MAX) {
      counter++;
      return;
    }

    myServer.write(0);  

    //write the string to the server
    if (s.length() > 2) {

      //insert the number of blobs at the beginning of the string
      s.insert(0, Integer.toString(numSentBlobs) + ":" );

      //filter out whether we are using a timer or not
      filterResume(s);
    }

    //a counter so we don't send so many things
    counter = 0;
  }
}

/**
 * Check whether holes or shells were enabled, and if they were display the images and create the output strings
 *
 **/
public void checkTypeEnabled(StringBuffer s) {
  //create the string for shells if they are enabled
  if (shellsEnabled) {

    //apply appropriate filters for shells
    Blob[] shells = detectShells();

    //push the current coordinate system onto the matrix stack
    pushMatrix();

    //translate the matrix over to the difference image to the right
    translate(DISPLACE_XSHELL, imHeightOffset);
    s.append(createOutputString(shells, SHELL));

    //restore the previous coordinate system
    popMatrix();
  }

  //create the string for holes if they are enabled
  if (holesEnabled) {

    //apply appropriate filters for holes
    Blob[] holes = detectHoles();

    //push the current coordinate system onto the matrix stack
    pushMatrix();

    //translate the matrix over to the difference image to the right
    translate(DISPLACE_XHOLE, imHeightOffset);
    s.append(createOutputString(holes, HOLE));

    //restore the previous coordinate system
    popMatrix();
  }
  if (facesEnabled) {
    Rectangle[] faces;
    String s2;
    pushMatrix();

    translate(DISPLACE_XFACE, imHeightOffset);

    faces=detectFaces();

    
    for ( int i=0; i<faces.length; i++ ) {

      for ( int j = 1; j < selRectStartX.size(); j++ ) {
        //calculate the scaled coordinates for the blob
        double[] scaledInfo = calcScaled( faces[i].x, faces[i].y, faces[i].width, faces[i].height, j, true );

        //save the current scaled values
        double currentScaledX, currentScaledY, currentScaledWidth, currentScaledHeight;

        // if within bounds, then scaledInfo is not null
        if ( scaledInfo != null ) {
          currentScaledX = scaledInfo[0];

          // for flipping
          if ( flipHorizontal ) {
            currentScaledX = targetW - currentScaledX;
          }

          currentScaledY = scaledInfo[1];
          currentScaledWidth= scaledInfo[2];
          currentScaledHeight = scaledInfo[3];


          numSentBlobs++;
          
          println("FACE!!!" );
           s2=(Integer.toString(numSentBlobs-1+i) + "," + Integer.toString((int)currentScaledX) + "Y" + Integer.toString((int)currentScaledY) 
            + "W" + Integer.toString((int)currentScaledWidth) + "H" + Integer.toString((int)currentScaledHeight) + "T" + Integer.toString( FACES ) + "I" + j + ";");
          
          if (s !=null) {
            s.append(s2);
          }
        }
      }
    } 

    popMatrix();
  }
}

public Rectangle[] detectFaces() {

  opencv.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );    // load the FRONTALFACE description file
  // uncomment the following for creating windows executable
  //opencv.cascade( "haarcascade_frontalface_alt.xml" );

  opencv.copy( video );    
  opencv.read();


  image( opencv.image(), 0, 0 );

  // detect anything ressembling a FRONTALFACE
  Rectangle[] faces = opencv.detect();

  // draw detected face area(s)
  noFill();
  stroke(255, 0, 0);
  for ( int i=0; i<faces.length; i++ ) {
    rect( faces[i].x, faces[i].y, faces[i].width, faces[i].height );
  }
  return faces;
}
/**
 * setScreen
 * creates the initial setup for the screen on every draw
 * returns true if video was available
 **/
public boolean setScreen() {
  // ASJ 3/7/13 for processing 2.0b7
  //   opencv.read();
  if (video.available()) {
    video.read();
    video.loadPixels();
    opencv.copy(video);
    opencv.read();
    // ASJ


    if ( flipHorizontal )
      opencv.flip( OpenCV.FLIP_HORIZONTAL );

    // image in memory 
    image( opencv.image(OpenCV.MEMORY), imWidthOffset, imHeightOffset);

    //   //put the binary image on the right hand side of the reference image
    image( opencv.image(OpenCV.GRAY), DISPLACE_XSHELL, imHeightOffset);

    //put another image for detecting holes so that we can see both at the same time
    image( opencv.image(OpenCV.GRAY), DISPLACE_XHOLE, imHeightOffset);  

    //put another image for detecting faces so that we can see both at the same time
    image( opencv.image(OpenCV.GRAY), DISPLACE_XFACE, imHeightOffset);

    // make sure last thing is curent image for ref shot
    opencv.copy(video);
    opencv.read();

    return true;
  }
  else
    return false;
}


/**
 * createButtons
 * create all buttons for this app
 **/
public void createButtons() {

  //create buttons at the bottom of the screen
  c = new GUIController(this);

  //put the reset button in the lower left hand corner
  resetAreas = new IFButton("Reset Areas", imWidthOffset+20, h/2 + imHeightOffset*2);

  //put the refShot button next to the reset button
  newRefShot = new IFButton("Ref Shot", (w/6)+imWidthOffset+20, h/2 + imHeightOffset*2); 

  //put the checkboxes in next to the buttons
  hole = new IFCheckBox("Enable Holes", (w/2)+(w/8)-45, h/2 + imHeightOffset*2);

  //update the next spot

  shell = new IFCheckBox("Enable Shells", (w/4)+(w/8)-45, h/2 + imHeightOffset*2);

  faces= new IFCheckBox("Enable Faces", (3*(w/4))+(w/8)-45, h/2 + imHeightOffset*2);
  //update the next spot
  flipHor = new IFCheckBox("Flip Horizontally", (3*(w/4))+(w/8)-45, h/2 + imHeightOffset*6);

  //update the next spot
  resume = new IFCheckBox("Enable Timer", (w/6)+imWidthOffset+20, h/2 + imHeightOffset*6);

  // original x and y: (w/2)+(w/8)-45, h/2 + imHeightOffset*6 
  shellThreshEnable = new IFCheckBox("Shell Threshold", (w/4)+(w/8)-115, h/2 + imHeightOffset*6);

  // original location: holeThreshEnable = new IFCheckBox("Hole Threshold",  (w/2)+(w/8)-45, h/2 + imHeightOffset*6);
  holeThreshEnable = new IFCheckBox("Hole Threshold", (w/2)+(w/8)-115, h/2 + imHeightOffset*6);


  shellThresholdTextInput = new IFTextField("Input Threshold", (w/4) + (w/8), h/2 + imHeightOffset*6, 40);
  setShellThreshold = new IFButton("Update Threshold", (w/4) + (w/8) + 45, h/2 + imHeightOffset*6);
  dragShellThreshold = new IFCheckBox("Drag Threshold", (w/4) + (w/8), h/2 + imHeightOffset*5 + 35);

  holeThresholdTextInput = new IFTextField("Input Threshold", (w/2) + (w/8), h/2 + imHeightOffset*6, 40);
  setHoleThreshold = new IFButton("Update Threshold", (w/2) + (w/8) + 45, h/2 + imHeightOffset*6);
  dragHoleThreshold = new IFCheckBox("Drag Threshold", (w/2) + (w/8), h/2 + imHeightOffset*5 + 35);


  //add a quit button
  quit = new IFButton("Quit", imWidthOffset+20, h/2 + imHeightOffset * 6);

  //add the buttons to the GUIController
  c.add(resetAreas);
  c.add(newRefShot);
  c.add(hole);
  c.add(shell);
  c.add(faces);
  c.add(flipHor);
  c.add(resume);
  c.add(shellThreshEnable);
  c.add(holeThreshEnable);
  c.add(quit);

  c.add(shellThresholdTextInput);
  c.add(setShellThreshold);
  c.add(holeThresholdTextInput);
  c.add(setHoleThreshold);
  c.add(dragShellThreshold);
  c.add(dragHoleThreshold);

  //add listeners to each of the components in the controller
  resetAreas.addActionListener(this);
  newRefShot.addActionListener(this);
  hole.addActionListener(this);
  shell.addActionListener(this);
  faces.addActionListener(this);
  flipHor.addActionListener(this);
  resume.addActionListener(this);
  shellThreshEnable.addActionListener(this);
  holeThreshEnable.addActionListener(this);
  quit.addActionListener(this);
  setShellThreshold.addActionListener(this);
  setHoleThreshold.addActionListener(this);

  shellThresholdTextInput.setValue(shellThresh + "");
  holeThresholdTextInput.setValue(threshold2 + "");
}

/**
 * Draw all areas in the selectedrect arraylist
 **/
public void drawSelectedAreas()
{
  //walk along the arraylist
  for ( int i = 1; i < selRectStartX.size(); i++ )
  {
    // left side
    boundArea(getFloatOut(selRectStartX, i), getFloatOut(selRectStartY, i), 
    Math.abs(getFloatOut(selRectStartX, i) - getFloatOut(selRectEndX, i)), Math.abs( getFloatOut(selRectStartY, i) - getFloatOut(selRectEndY, i)));
    // middle
    boundArea(getFloatOut(selRectStartX, i) + DISPLACE_XSHELL-imWidthOffset, getFloatOut(selRectStartY, i), 
    Math.abs(getFloatOut(selRectStartX, i) - getFloatOut(selRectEndX, i)), Math.abs( getFloatOut(selRectStartY, i) - getFloatOut(selRectEndY, i)));
    // right side
    boundArea(getFloatOut(selRectStartX, i) + DISPLACE_XHOLE-imWidthOffset, getFloatOut(selRectStartY, i), 
    Math.abs(getFloatOut(selRectStartX, i) - getFloatOut(selRectEndX, i)), Math.abs( getFloatOut(selRectStartY, i) - getFloatOut(selRectEndY, i)));
  }
}

/**
 * createOutputString
 * given an array of blobs walk along and append a stringBuffer
 * @return: the string ready for output
 **/
public String createOutputString(Blob[] blobs, int type) {
  //(Kim how the string is formatted for holes and shells)
  StringBuffer bs = new StringBuffer();
  String s;
  //walk along the blobs and send them over if they are in bounds
  for ( int i=0; i<blobs.length; i++ ) {

    //create a bounding rectangle for the blob
    createBlobRect(blobs[i]);

    //create the centroid
    createBlobCentroid(blobs[i]);

    //fill the shape
    fillBlob(blobs[i]);

    //check if the blob is in bounds 
    //walk along all of the areas of interest and check if the blob is contained inside
    for ( int j = 1; j < selRectStartX.size(); j++ )
    {
      s = filterTest( blobs[i], j, numSentBlobs, type );
      //if a string was returned then append the stringBuffer
      if (s !=null) {

        //check if the blob is actually a hole
        bs.append(s);

        //advance the count of the number of blobs to send
        numSentBlobs++;
        if (Integer.parseInt(s.substring(s.length()-2, s.length()-1))==HOLE) {
          //fill the blob that was found
          fillFollowed( blobs[i], HOLE);
        } 
        else {
          //fill the blob that was found
          fillFollowed( blobs[i], SHELL);
        }
      }
    }
  }
  return bs.toString();
}

/**
 * createBlobRect
 * draw a rectangle around the passed blob
 **/
public void createBlobRect(Blob blob) {
  Rectangle bounding_rect = blob.rectangle;
  // rectangle around the blob
  noFill();
  stroke( blob.isHole ? 128 : 64 );
  rect( bounding_rect.x, bounding_rect.y, bounding_rect.width, bounding_rect.height );
}

/**
 * createBlobCentroid
 * draw a + at the center of the blob
 **/
public void createBlobCentroid(Blob blob) {
  //get the center point of the current blob        
  Point centroid = blob.centroid;
  // centroid
  stroke(0, 0, 255);
  line( centroid.x-5, centroid.y, centroid.x+5, centroid.y );
  line( centroid.x, centroid.y-5, centroid.x, centroid.y+5 );
  noStroke();
  fill(0, 0, 255);
}

/**
 * fillBlob
 * fill in the points of the blob with purple
 **/
public void fillBlob(Blob blob) {
  //get the points bounding the blob
  Point[] points = blob.points;

  //fill in the shape
  fill(255, 0, 255, 64);
  stroke(255, 0, 255);

  //if there are points start a shape
  if ( points.length>0 ) {
    beginShape();

    //use each of the points as a vertex
    for ( int j=0; j<points.length; j++ ) {
      vertex( points[j].x, points[j].y );
    }
    //end the shape
    endShape(CLOSE);
  } 

  //reset the stroke  
  noStroke();
  fill(255, 0, 255);
}

/**
 * detectHoles
 * use the binary inverse filter in order to get optimal hole detection
 * take the difference between the image in memory and the current image,
 * convert it to grayscale, and put it through two filters
 * @return: an array of blobs
 **/
public Blob[] detectHoles() {
  //restore the RGB image
  //   opencv.restore();
  opencv.copy( video );    
  opencv.read();




  //take the difference between the reference shot and the current image
  opencv.absDiff(); 

  //convert the image to grayscale  
  opencv.convert( OpenCV.GRAY);

  //set the threshold
  opencv.threshold( threshold2 );

  //use the binary inverse to round the grayscale colors to either black or white
  opencv.threshold(threshold, 255, OpenCV.THRESH_BINARY_INV);

  //put the binary image on the right hand side of the reference image
  image( opencv.image(OpenCV.GRAY), DISPLACE_XHOLE, imHeightOffset);

  //save the pixels from the current difference in the array pix because when we ask
  //for blobs it changes the image in the opencv buffer
  pix = opencv.pixels(OpenCV.BUFFER);

  //get the blobs from opencv
  return opencv.blobs( 5, ((w/3)*(h/2))/3, 20, true );
}

/**
 * detectShells
 * take the difference between the image in memory and the current image,
 * put it through the default RGB filter using threshold2
 * @return: an array of blobs
 **/
public Blob[] detectShells() {
  //the default set up is to deal with shell so we only need return the blobs  
  //get the blobs from opencv
  opencv.absDiff();

  //set the threshold
  opencv.threshold( shellThresh);

  //put the binary image on the right hand side of the reference image
  image( opencv.image(OpenCV.GRAY), DISPLACE_XSHELL, imHeightOffset);


  return opencv.blobs( 5, ((w/3)*(h/2))/3, 20, true );
}

/**
 * ActionPerformed
 * dictates what we should do when a button is clicked
 **/
public void actionPerformed(GUIEvent e) {
  //check which button or check box was clicked
  if ( e.getSource() == resetAreas) {

    //reset all the areas of interest
    rectSet = false;

    //reset the initial area
    selRectStartX.clear();// = 10;
    selRectStartY.clear();// = 10;
    selRectEndX.clear();// = w+10;
    selRectEndY.clear();// = h+10;
  }

  if ( e.getSource() == newRefShot) {
    //take a new reference shot
    // updated 3/7/13 for processing 2.0b7

    println("about to take new reference shot. ************");
    opencv.allocate(captureW, captureH);
    opencv.copy(video);
    opencv.remember( OpenCV.BUFFER );  // works for first time, but opencv crashes somewhere internally if shells and things are enabled. 
    // opencv
    println("new reference shot taken. ************");
  }

  if ( e.getSource() == hole) {
    //if the checkbox is selected then set the program to look for holes
    holesEnabled = !holesEnabled;
    println("sending holes: " + Boolean.toString(holesEnabled));
  }

  if ( e.getSource() == shell) {
    shellsEnabled = !shellsEnabled;
    println("sending shells: " + Boolean.toString(shellsEnabled));
  }
  if ( e.getSource() == faces) {
    facesEnabled = !facesEnabled;
    println("sending faces: " + Boolean.toString(facesEnabled));
  }
  if ( e.getSource() == flipHor) {
    //if the threshEnable is checked then we may adjust the threshold
    flipHorizontal = !flipHorizontal;
    println("flip horizontal: " + Boolean.toString(flipHorizontal));
  }


  if ( e.getSource() == resume) {
    //if the checkbox is selected then set the program to start the timer
    resumeAble = !resumeAble;
    println("resume: " + Boolean.toString(resumeAble));
  }

  if ( e.getSource() == shellThreshEnable) {
    //if the threshEnable is checked then we may adjust the threshold
    shellThresholding = !shellThresholding;
    println("thresholding: " + Boolean.toString(shellThresholding));
  }

  if ( e.getSource() == holeThreshEnable) {
    //if the threshEnable is checked then we may adjust the threshold
    holeThresholding = !holeThresholding;
    println("thresholding: " + Boolean.toString(holeThresholding));
  }

  if (e.getSource() == quit) {
    //if quit is clicked then stop the program
    stop();
  }

  if (e.getSource() == setShellThreshold) {
    if (shellThresholding) {
      try {
        shellThresh = Integer.parseInt(shellThresholdTextInput.getValue());
      } 
      catch (NumberFormatException exception) {
      }
    }
  }


  // (Kim)
  /**
   * from MouseDragged: 
   *    if(holeThresholding){
   *      threshold = int( map(mouseX,0,width,0,255) );
   *      threshold2 = int( map(mouseY,0,height,0,255) );
   *      println("holeThresh:" + threshold2);
   *      thresh = true;
   *    }
   * (have not yet determined the difference between the two threshold variables)   
   **/
  if (e.getSource() == setHoleThreshold) {
    if (holeThresholding) {
      try {
        threshold = Integer.parseInt(holeThresholdTextInput.getValue());
        threshold2 = Integer.parseInt(holeThresholdTextInput.getValue());
      } 
      catch (NumberFormatException exception) {
      }
    }
  }
}

/**
 * Filter out whether we are using the resume or not
 **/
public void filterResume(StringBuffer s) {
  //if we are using the timer then deal with the timer and insert the signifier
  if (resumeAble) {
    if (elapsedTime>=RESUME_DELAY) {
      //insert a signifier for the RESUME
      s.insert(0, "R");
      elapsedTime = 0;
    }
    else {
      //there's nothing of interest
      if (elapsedTime<RESUME_DELAY+2) {
        //advance the timer
        elapsedTime++;
      }
    }
  }

  //write the blobs of interest to the server
  myServer.write(s.toString() + "\n");
  println(s.toString() + "\n");
}

/**
 * FilterTest 
 * Test if the the blob is above the threshold for a hole
 **/
public String filterTest( Blob b, int indexOfInterest, int numBlobbers, int type ) {
  //calculate the scaled coordinates for the blob
  double[] scaledInfo = calcScaled( b.centroid.x, b.centroid.y, b.rectangle.width, b.rectangle.height, indexOfInterest, false );

  //save the current scaled values
  double currentScaledX, currentScaledY, currentScaledWidth, currentScaledHeight;

  // if within bounds, then scaledInfo is not null
  if ( scaledInfo != null ) {
    currentScaledX = scaledInfo[0];
    currentScaledY = scaledInfo[1];
    currentScaledWidth= scaledInfo[2];
    currentScaledHeight = scaledInfo[3];

    //if it's a hole within the right threshold return the string
    //if it is a shell then append and return the string regardless of whether it is white
    if ( (type == HOLE && isHole(b)) || (type == SHELL && !b.isHole) ) {
      return Integer.toString(numBlobbers) + "," + Integer.toString((int)currentScaledX) + "Y" + Integer.toString((int)currentScaledY) 
        + "W" + Integer.toString((int)currentScaledWidth) + "H" + Integer.toString((int)currentScaledHeight) + "T" + Integer.toString( type ) 
          // added ASJ for area of interest (-1 since indices seem to start at 1)
          + "I" + Integer.toString(indexOfInterest-1) + ";";
    }
  }
  return null;
}


/** 
 * Calculates scaled x, y, w, h for given x, y, w, h with respect to area of interest at
 * index. If the x, y do not fall within bounds of that area of interest, returns null
 **/
public double[] calcScaled( float x, float y, float wid, float hei, int areaOfInterestIndex, boolean isCheckingFace )
{
  //scale the rectangle around the blob according to the size of the area of interest

  println("in calcScaled. calling checkShellWithinInterest.");


  float[] updatedParameters = checkShellWithinInterest(x, y, wid, hei, areaOfInterestIndex, isCheckingFace);
  if (updatedParameters == null /*|| updatedParameters.length() != 4*/) {
    return null;
  }

  println("in calc scaled. original information; x = " + x + ", y = " + y + ", wid = " + wid + ", hei = " + hei + ".");

  x = updatedParameters[0];
  y = updatedParameters[1];
  wid = updatedParameters[2];
  hei = updatedParameters[3];

  println("in calc scaled. new information; x = " + x + ", y = " + y + ", wid = " + wid + ", hei = " + hei + ".");

  //scale the x and y coordinates      
  double currentScaledX = ((x - getFloatOut( selRectStartX, areaOfInterestIndex ))*getFloatOut( scaleX, areaOfInterestIndex )); 
  double currentScaledY = ((y - getFloatOut( selRectStartY, areaOfInterestIndex ))*getFloatOut( scaleY, areaOfInterestIndex )); 

  //scale the width and height
  double currentScaledWidth = wid*getFloatOut( scaleX, areaOfInterestIndex );
  double currentScaledHeight = hei*getFloatOut( scaleY, areaOfInterestIndex );

  //put the newly scaled values into an array and return that array
  double[] info = {
    currentScaledX, currentScaledY, currentScaledWidth, currentScaledHeight
  };

  println("scaled information: currentScaledX = " + currentScaledX + ", currentScaledY = " + currentScaledY + ", currentScaledWidth = " + currentScaledWidth + ", currentScaledHeight = " + currentScaledHeight + ". \n");
  return info;
} 

/**
 * Checks whether the blob is within the area of interest; if it is partially within, 
 * takes the center x,y coordinates for the area within interest, as well as width 
 * and height for the same
 *
 * @param x the center x coordinate of the blob in question
 * @param y the center y coordinate of the blob in question
 * @param w the width of the blob in question 
 * @param h the height of the blob in question 
 * @param areaOfInterestIndex the index of the area of interest to check against
 * @param isCheckingFace true if the blob is a face
 * @return float array of length 4, containing updated x, y, width, height 
 *             the x, y coordinates represent center of updated blob.
 **/
public float[] checkShellWithinInterest(float x, float y, float w, float h, int areaOfInterestIndex, boolean isCheckingFace) 
{
  // for some reason as yet unknown, the coordinates were reliably off by 10 for shells and 60 for faces
  // due to difficulty in testing holes precisely, their precise offset is uncertain
  // if offset is determined, would check isCheckingFace to an int; 
  //    expected values 0, 1, 2 to represent the three possibilities
  float changeInX, changeInY;

  if (isCheckingFace) {
    println("checking a face.");
    changeInX = 65;
    changeInY = 60;
  } 
  else {
    println("checking a shell.");
    changeInX = 10;
    changeInY = 10;
  }

  float endX, startX, startY, endY, interestSX, interestEX, interestSY, interestEY; 

  // stating and ending x, y coordinates of blob
  startX = x - w/2 + changeInX;
  endX = x + w/2 + changeInX;  
  endY = y + h/2 + changeInY;
  startY = y - h/2 + changeInY; 

  // stating and ending x, y coordinates of area of interest
  interestSX = getFloatOut( selRectStartX, areaOfInterestIndex );
  interestEX = getFloatOut( selRectEndX, areaOfInterestIndex );
  interestSY = getFloatOut( selRectStartY, areaOfInterestIndex );
  interestEY = getFloatOut( selRectEndY, areaOfInterestIndex ); 

  println("\nin checkShellWithinInterest \nshell: startX = " + startX + ", endX = " + endX + ", startY = " + startY + ", endY = " + endY + ".");
  println("areaOfInterest: startX = " + interestSX + ", endX = " + interestEX + ", startY = " + interestSY + ", endY = " + interestEY+ ". \n");

  // if true, blob is not within area of interest (due to x)
  if (startX > interestEX || endX < interestSX) {
    println("*** x is out of bounds of interest area; returning null from checkShellWithinInterest");
    return null;
  } 
  // if startX is less than that of area of interest, adjusts startX
  if (startX < interestSX) {
    println("* startX was less than interestSX; now is equal \n");
    startX = interestSX;
  }
  // if endX is less than that of area of interest, adjusts endX
  if (endX > interestEX) {
    println("* endX was more than interestEX; now is equal");
    endX = interestEX;
  }

  // if true, blob is not within area of interest (due to y)
  if (startY > interestEY || endY < interestSY) {
    println("*** y is out of bounds of interest area; returning null from checkShellWithinInterest \n");
    return null;
  }
  // if startY is less than that of area of interest, adjusts startY
  if (startY < interestSY) {
    println("* startY was less than interestSY; now is equal");
    startY = interestSY;
  }
  // if endY is less than that of area of interest, adjusts endY 
  if (endY > interestEY) {
    println("* endY was more than interestEX; now is equal");
    endY = interestEY;
  }

  println();

  // calculates new width, height, and center x,y
  w = endX - startX;
  h = endY - startY;
  x = startX + w/2;
  y = startY + h/2;

  float[] updatedInfo = {
    x, y, w, h
  };
  return updatedInfo;
}

/**
 * GetFloatOut takes the double in the array list and changes it to a float 
 **/
public float getFloatOut( ArrayList list, int index )
{
  // println("In getFloatOut. list = " + list.toString());

  if (index < list.size()) {
    return ((Float)list.get(index)).floatValue();
  } 
  else {
    println("In getFloatOut. invalid index; returning regative one. ******");
    println("index = " + index + ", size of list = " + list.size());
    return -1;
  }
}

/**
 * Draw the bounding rectangle for the area of interest
 **/
public void boundArea(float start, float end, float w, float h) {
  noFill();
  stroke(255, 0, 0);
  rect(start, end, w, h);
}

/**
 * Fill the blob that is being followed
 **/
public void fillFollowed(Blob b, int type) {
  println("following" + " type: " + type );
  //get the points bounding the blob
  Point[] points = b.points;

  //if we are dealing with a hole we want to mark it green
  //otherwise we will mark it blue
  if (type == HOLE) {
    println("following holes");
    //fill in the shape
    fill(0, 255, 0, 64);
    stroke(0, 255, 0);
    //if there are points start a shape
    if ( points.length>0 ) {
      beginShape();
      //use each of the points as a vertex
      for ( int j=0; j<points.length; j++ ) {
        vertex( points[j].x - DISPLACE_XHOLE, points[j].y );
        println("orig x: " + points[j].x + "\ntranslated x: " + (points[j].x-DISPLACE_XHOLE));
      }
      //end the shape
      endShape(CLOSE);
    } 
    //reset the stroke  
    noStroke();
    fill(0, 255, 0);
  }
  else {
    //fill in the shape
    fill(0, 0, 255, 64);
    stroke(0, 0, 255);
    //if there are points start a shape
    if ( points.length>0 ) {
      beginShape();
      //use each of the points as a vertex
      for ( int j=0; j<points.length; j++ ) {
        vertex( points[j].x - DISPLACE_XSHELL, points[j].y );
      }
      //end the shape
      endShape(CLOSE);
    } 
    //reset the stroke  
    noStroke();
    fill(0, 0, 255);
  }
}

/**
 * findIndex
 * Given an x and y coordinate find the one dimensional index into an array
 * @param: i = the rows (y coors), j = the cols (x coors)
 **/
public int findIndex(int i, int j) {
  return i*((w/4)-imWidthOffset) + j;
}

/**
 * createPolygon
 * given a blob, return the polygon contained inside, so that we may check for insideness
 **/
public Polygon createPolygon(Blob blob) {

  //an array of ints for all the x's
  int[] all_x = new int[blob.points.length];
  int[] all_y = new int[blob.points.length];

  //a polygon to test for insideness
  Polygon p;

  //walk along the blobs, create a polygon for each and check if the click is contained in the blob
  //pass the blob along to get the average color
  for (int i = 0; i< blob.points.length; i++) {
    //put the current point into our array 
    all_x[i] = blob.points[i].x;
    all_y[i] = blob.points[i].y;
  }
  //create a new polygon
  return new Polygon(all_x, all_y, blob.points.length);
}


/**
 * isWhite uses a polygon to test if the inside of the blob is white or not
 **/
public boolean isWhite(Blob blob, Polygon p, int[] pix) {

  //get the starting coordinate of the blob passed
  int startX = blob.rectangle.x;
  int startY = blob.rectangle.y;

  //walk along all of the points in the bounding rectangle
  //if the point is in the polygon then add it to the average color
  for (int j = startY; j<startY + blob.rectangle.height; j++) {
    for (int k = startX; k<startX + blob.rectangle.width; k++) {
      //if the polygon contains the point, then add it to the average color
      if (p.inside(k, j)) {
        //check if the pixel is white or not
        if (( red(pix[findIndex(j, k)]) != 255) && ( red(pix[findIndex(j, k)]) != 255)
          && ( red(pix[findIndex(j, k)]) != 255) )
          return false;
      }
    }
  }
  //the polygon is white meaning it is a hole
  return true;
}

/**
 * Set the starting coordinates for the bounding rectangle
 **/
public void mousePressed() {
  if (/*!holeThresholding && !shellThresholding && (Kim) */pointBtw(imWidthOffset, imWidthOffset+w/2, imHeightOffset, imHeightOffset+h/2, mouseX, mouseY)) {
    selStartX = mouseX;
    selStartY = mouseY;
    rectSet = true;
  }
}

/**
 * On release set the boolean for area of interest so we may choose a threshold
 * draw the bounding rectangle
 **/
public void mouseReleased() {
  //only draw the rectangle representing area of interest if it is within the reference frame
  //not the difference frame
  if (/* (Kim) !holeThresholding && !shellThresholding &&*/ rectSet && pointBtw(imWidthOffset, imWidthOffset + w/2, imHeightOffset, imHeightOffset + h/2, selStartX, selStartY) && pointBtw(imWidthOffset, imWidthOffset + w/2, imHeightOffset, imHeightOffset + h/2, mouseX, mouseY)) {
    rectSet = false;

    /** 
     * add the area of interest. 
     * the starting x and y of the area is taken from the current position of the mouse 
     * or the position at the start of the drag--whichever one is smaller.
     **/
    if (selStartX <= mouseX && selStartY <= mouseY) { 
      addAreaOfInterest( selStartX, selStartY, mouseX, mouseY );
    }
    else if (selStartX <= mouseX && selStartY > mouseY) {
      addAreaOfInterest( selStartX, mouseY, mouseX, selStartY );
    }
    else if (selStartX > mouseX && selStartY <= mouseY) {
      addAreaOfInterest( mouseX, selStartY, selStartX, mouseY );
    }
    else {
      addAreaOfInterest( mouseX, mouseY, selStartX, selStartY );
    }
  }
}

/**
 * MouseDragged
 * If we have no area of interest, create one
 * Otherwise we are adjusting the threshold
 **/
public void mouseDragged() {
  //a boolean to hold if either was thresholding
  boolean thresh = false;

  //set a new hole threshold for the image depending on the light
  if (holeThresholding && dragHoleThreshold.isSelected()) {
    threshold = PApplet.parseInt( map(mouseX, 0, width, 0, 255) );
    threshold2 = PApplet.parseInt( map(mouseY, 0, height, 0, 255) );
    holeThresholdTextInput.setValue(threshold2 + "");
    println("holeThresh:" + threshold2);
    thresh = true;
  }
  //set a new shell threshold for the image depending on the light 
  if (shellThresholding && dragShellThreshold.isSelected()) {
    shellThresh = PApplet.parseInt( map(mouseY, 0, height, 0, 255) );
    shellThresholdTextInput.setValue(shellThresh + "");
    println("\nin MouseDragged! shellThresh = " + shellThresh + ". *****\n");
    thresh = true;
  }

  // if the start of the selection and the end of the selection are within the image area
  if (!thresh && pointBtw(imWidthOffset, imWidthOffset + w/2, imHeightOffset, imHeightOffset + h/2, selStartX, selStartY) 
    && pointBtw(imWidthOffset, imWidthOffset + w/2, imHeightOffset, imHeightOffset + h/2, mouseX, mouseY)) {

    /** 
     * draw the area selected by the drag 
     * the starting x and y of the area is taken from the current position of the mouse 
     * or the position at the start of the drag--whichever one is smaller.
     **/
    if (selStartX <= mouseX && selStartY <= mouseY) { 
      boundArea(selStartX, selStartY, Math.abs(selStartX - mouseX), Math.abs(selStartY - mouseY));
    }
    else if (selStartX <= mouseX && selStartY > mouseY) {
      boundArea(selStartX, mouseY, Math.abs(selStartX - mouseX), Math.abs(selStartY - mouseY));
    }
    else if (selStartX > mouseX && selStartY <= mouseY) {
      boundArea(mouseX, selStartY, Math.abs(selStartX - mouseX), Math.abs(selStartY - mouseY));
    }
    else {
      boundArea(mouseX, mouseY, Math.abs(selStartX - mouseX), Math.abs(selStartY - mouseY));
    }
  }
}

/**
 * Point lies between
 * checks if a point lies within a rectangle
 * the rectangle must be entered left side first, then right
 * then top, then bottom
 **/
public boolean pointBtw(double xLeft, double xRight, double yTop, double yBottom, double pointX, double pointY) {
  //checks the parameters and returns true if the point lies between the sides
  if (pointX>=xLeft && pointX<=xRight && pointY>=yTop && pointY<=yBottom) {
    println("point found between returning true");
    return true;
  }
  println("point not found between returning false");
  return false;
}

/**
 * Add an area of interest to the scene
 **/
public void addAreaOfInterest( float startX, float startY, float endX, float endY )
{
  if (!dragHoleThreshold.isSelected() && !dragShellThreshold.isSelected()) {
    //bound new area
    boundArea(startX, startY, Math.abs(startX - endX), Math.abs(startY - endY));
    //add the coordinates to the arraylist
    selRectStartX.add( startX );
    selRectStartY.add( startY );
    selRectEndX.add( endX );
    selRectEndY.add( endY );
    //add the width and height to the scaled arraylists 
    scaleX.add( targetW/Math.abs(startX - endX) );
    scaleY.add( targetH/Math.abs(startY - endY) );
  }
}

/**
 * Check if the blob provided is a hole
 **/
public boolean isHole( Blob b )
{
  return isWhite(b, createPolygon(b), pix);
}

public void stop() {
  opencv.stop();
  super.stop();
  System.exit(0);
}

class AreaOfInterest {
  protected int startX;
  protected int startY;
  protected int endX;
  protected int endY;

  public AreaOfInterest(int startX, int startY, int endX, int endY) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endY;
    this.endY = endY;
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CVServer" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
