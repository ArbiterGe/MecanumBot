#include <SoftwareSerial.h>
#include <Servo.h>
SoftwareSerial mySerial(4, 2); // RX, TX

String command = ""; // Stores response of the HC-06 Bluetooth device
String lastCommand = "asdf";
Servo FR;
Servo FL;
Servo BR;
Servo BL;
int left = 0;
int right = 0;
int Bleft = 0;
int Bright = 0;

void setup() {
  // Open serial communications:
  Serial.begin(9600);
  Serial.println("Type AT commands!");
  FR.attach(9, 1000, 2000); 
  FL.attach(5, 1000, 2000);
  BR.attach(10, 2000, 1000);
  BL.attach(6, 2000, 1000);
  FR.write(90);     //  x - x
  FL.write(90);     //  - - -
  BR.write(90);     //  x - x
  BL.write(90);
  
  // The HC-06 defaults to 9600 according to the datasheet.
  mySerial.begin(9600);
}

void loop() {
  // Read device output if available.
  if (mySerial.available()) {
    while(mySerial.available()) {
        command += mySerial.readStringUntil('\n');
        if (command != lastCommand) {
          int idx1 = command.indexOf(':', 0);
          int idx2 = command.indexOf(':', idx1);
          int idx3 = command.indexOf(':', idx2);
          String first = command.substring(0, idx1);
            if (first.startsWith("-")) {
            first = first.substring(1);
            left = first.toInt();
            left *= -1;
          }
          else {
            left = first.toInt();
          }
          String second = command.substring(idx1+1,idx2);
          if (second.startsWith("-")) {
            second = second.substring(1);
            right = second.toInt();
            right *= -1;
          }
          else {
            right = second.toInt();
          }
  
          String third = command.substring(idx2+1,idx3);
          if (second.startsWith("-")) {
            third = third.substring(1);
            Bleft = third.toInt();
            Bleft *= -1;
          }
          else {
            Bleft = third.toInt();
          }
  
          String fourth = command.substring(idx3+1);
          if (fourth.startsWith("-")) {
            fourth = fourth.substring(1);
            Bright = fourth.toInt();
            Bright *= -1;
          }
          else {
            Bright = fourth.toInt();
          }
          drive(left, right, Bleft, Bright);
        }
        lastCommand = command;
        command = "";
    }
  }
  
  // Read user input if available.
  //if (Serial.available()){
  //  delay(10); // The delay is necessary to get this working!
  //  mySerial.write(Serial.read());
 // }
}

void drive(int l, int r, int bl, int br) {
  FR.write(r);     //  x - x
  FL.write(l);     //  - - -
  BR.write(br);     //  x - x
  BL.write(bl);
}
