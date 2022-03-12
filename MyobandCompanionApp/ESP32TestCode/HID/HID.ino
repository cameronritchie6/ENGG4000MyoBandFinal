#include <Arduino.h>
#include <string.h>
#include <iostream>
#include <string.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include "HIDTypes.h"
#include "BLEHIDDevice.h"
#include <driver/adc.h>


BLEHIDDevice* hid; //declare hid device
BLECharacteristic* input; //Characteristic that inputs button values to devices
BLECharacteristic* output; //Characteristic that takes input from client

//Stores if the board is connected or not
bool connected = false;

//input pins (16 toatal button avaliable, 1 analog input 0-255 but inputs can be changed)
#define BUTTON1 5
#define BUTTON2 18
#define BUTTON3 19  //You can use any pins on the board
#define BUTTON4 21  //but I don't recommend the ones that are used
#define ANALOG1 32 //for flashing or are input only.
#define ANALOGSTICK 4 //the analog stick
#define ANALOGSTICK2 15 //pot 2


const int potPin1 = 4; //analog pin on esp 32
const int potPin2 = 15; //analog pin 2 on esp 32 
double VoltageMax = 3.3; //max vltage applied to POT
float ADCResolution = 12; //The ESP32 has a 12bit approximation register for 
bool clientWrite;
bool clientRead;
bool deviceConnected;
int outputCode;

double oldVoltage1;
double oldVoltage2;
double voltageIn;
double voltageIn2;
double ThresholdVoltage =2.0; //Threshold voltage in (V) to declare a contraction or not on the EMG signal
double emgTollerance = 0.1; // the tollerance of fluctuation that the emg signal can have around the threshold voltage before a change state
int tolleranceFlag =0;
int SqeezeFlag=0;


double signalCheck(int Pin);
boolean changeDetection(int checkedPin);
int tolleraceCheck(double voltageIn);
int buttonPress(int voltageIn, int voltageIn2);

//pin that goes high while there's a device connected
#define CONNECTED_LED_INDICATOR_PIN 2

//inputValues[0] is the first 8 buttons, [1] is the next 8, [2] is the analog input
//Each one of the bits represnets a button. 1 == pressed 0 == not pressed
//uint8_t inputValues[3] = {0b00000000, 0b00000000, 0x0};----------undo this issomething happens
//uint8_t inputValues[] = {0b00000000, 0b00000000, 0x0,0x0};---
// uint8_t inputValues[] = {0b00000000, 0x0,0x0};
uint8_t inputValues[] = {0b00000000};

class MyCallbacks : public BLEServerCallbacks { //Class that does stuff when device disconects or connects
    void onConnect(BLEServer* pServer) {
      connected = true;
      Serial.println("Connected");
      BLE2902* desc = (BLE2902*)input->getDescriptorByUUID(BLEUUID((uint16_t)0x2902));
      desc->setNotifications(true);

      digitalWrite(CONNECTED_LED_INDICATOR_PIN, HIGH);
    }

    void onDisconnect(BLEServer* pServer) {
      connected = false;
      Serial.println("Disconnected");
      BLE2902* desc = (BLE2902*)input->getDescriptorByUUID(BLEUUID((uint16_t)0x2902));
      desc->setNotifications(false);
      
      digitalWrite(CONNECTED_LED_INDICATOR_PIN, LOW);
    }
};

class MyOutputCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic* me) {
      // uint8_t* value = (uint8_t*)(me->getValue().c_str());
      //ESP_LOGI(LOG_TAG, "special keys: %d", *value);

Serial.println("HELLO RECEIVED");
      std::string testing = me -> getValue();
      Serial.println(testing.c_str());
    }
};

void taskServer(void*) {


  BLEDevice::init("GAME-CONTROLLER");
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyCallbacks());

  hid = new BLEHIDDevice(pServer);
  input = hid->inputReport(1); // <-- input REPORTID from report map
  output = hid->outputReport(1); // <-- output REPORTID from report map

  output->setCallbacks(new MyOutputCallbacks());

  std::string name = "IBME";
  hid->manufacturer()->setValue(name);

  hid->pnp(0x02, 0xe502, 0xa111, 0x0210);
  hid->hidInfo(0x00, 0x02);

  BLESecurity *pSecurity = new BLESecurity();
  //  pSecurity->setKeySize();
  pSecurity->setAuthenticationMode(ESP_LE_AUTH_BOND);

  const uint8_t report[] = { //This is where the amount, type, and value range of the inputs are declared
    0x05, 0x01, // USAGE_PAGE (Generic Desktop)
    0x09, 0x05, // USAGE (Gamepad)
    0xa1, 0x01, // COLLECTION (Application)
    0x85, 0x01, //   REPORT_ID (1)
    0x15, 0x00, // LOGICAL_MINIMUM (0)
    0x25, 0x01, // LOGICAL_MAXIMUM (1)
    0x35, 0x00, // PHYSICAL_MINIMUM (0)
    0x45, 0x01, // PHYSICAL_MAXIMUM (1)
    0x75, 0x01, // REPORT_SIZE (1)
    //0x95, 0x10, // REPORT_COUNT (16) --------uncomment if not working
    //0x95, 0x03
    0x95, 0x01, // REPORT_COUNT (3)----------delete if not working
    0x05, 0x09, // USAGE_PAGE (Button)
    0x19, 0x01, // USAGE_MINIMUM (Button 1)
    //0x29, 0x10, // USAGE_MAXIMUM (Button 16)
    0x29, 0x03, // USAGE_MAXIMUM (Button 3)----------delete if not working
    0x81, 0x02, // INPUT (Data,Var,Abs)
    0x95, 0x01,                    //     REPORT_COUNT (1)----------delete if not working
    0x75, 0x05,                    //     REPORT_SIZE (5)----------delete if not working
   0x81, 0x03,                    //     INPUT (Cnst,Var,Abs)----------delete if not working
    0x05, 0x01, // USAGE_PAGE (Generic Desktop)
    0x26, 0xff, 0x00, // LOGICAL_MAXIMUM (255)
    0x46, 0xff, 0x00, // PHYSICAL_MAXIMUM (255)
    0x09, 0x30,   //     USAGE (X) ----------------DELETE IF NOT WORKING
    0x09, 0x31, // USAGE (Y)
    0x75, 0x08, // REPORT_SIZE (8)
    //0x95, 0x01, // REPORT_COUNT (1)
    0x95, 0x02, // REPORT_COUNT (2) //x and y  ----DELETE if not working
    0x81, 0x02, // INPUT (Data,Var,Abs)
    0xc0 // END_COLLECTION
  };


hid->reportMap((uint8_t*)report, sizeof(report));
hid->startServices();

BLEAdvertising *pAdvertising = pServer->getAdvertising();
pAdvertising->setAppearance(HID_GAMEPAD);
pAdvertising->addServiceUUID(hid->hidService()->getUUID());
pAdvertising->start();
hid->setBatteryLevel(7);

ESP_LOGD(LOG_TAG, "Advertising started!");
delay(portMAX_DELAY);

};

void setup() {
  Serial.begin(115200);
  xTaskCreate(taskServer, "server", 20000, NULL, 5, NULL);
inputValues[0] |= 0b00000001;
  // inputValues[1] = analogRead(ANALOGSTICK); //DELETE IF NOT WORKING
  //  inputValues[2] = analogRead(ANALOGSTICK2);//DELETE IF NOT WORKING

   Serial.println("HEY THERE");
}


void loop() {

  //inputValues[1] = analogRead(ANALOGSTICK); 
 //  inputValues[2] = analogRead(ANALOGSTICK2);

  
  

//inputValues[0] |= 00000001;
    // if((changeDetection(potPin1)||changeDetection(potPin2))&&connected){
      while (connected) {
        inputValues[0] = 0b00000001;
        // inputValues[0] = buttonPress(analogRead(potPin1), analogRead(potPin2));
      // inputValues[1] = analogRead(potPin1);
      //  inputValues[2] = analogRead(potPin2);
    input->setValue(inputValues, sizeof(inputValues));
    input->notify();
    delay(200);
      }
      
    // }
    

delay(200);
}


double signalCheck(int Pin){
int potValue = analogRead(Pin);
double ADCRatio= VoltageMax/(pow(2,ADCResolution)-1);
 double voltageMeasurement = ADCRatio*potValue;
 
return voltageMeasurement ;
}



boolean changeDetection(int checkedPin){

  if(checkedPin == potPin1){
   voltageIn=signalCheck(checkedPin); //probe the voltage
  oldVoltage1 = round( oldVoltage1 * 100.0 ) / 100.0; //round to two didgits to compare if there is a change
  voltageIn = round( voltageIn * 100.0 ) / 100.0; //round to two didgits to compare if there is a change from previous value

  if(oldVoltage1 !=voltageIn){
    oldVoltage1 = voltageIn;
    return true;
  } //end if
  else {
oldVoltage1 = voltageIn;
    return false;
  } //end else

}
else if(checkedPin == potPin2){
  voltageIn2=signalCheck(checkedPin); //probe the voltage
  oldVoltage2 = round( oldVoltage2 * 100.0 ) / 100.0; //round to two didgits to compare if there is a change
  voltageIn2 = round( voltageIn2 * 100.0 ) / 100.0; //round to two didgits to compare if there is a change from previous value
  if(oldVoltage2 !=voltageIn2){
    oldVoltage2 = voltageIn2;
    return true;
  } //end if
  else {
oldVoltage2 = voltageIn2;
    return false;
  } //end else
} //end elseif
} //end changeDetection

//***************** Signal Tollerance Check *********************//
//Recieved the voltage of the signal, then checks to see if its a contraction or not


int tolleraceCheck(double voltageInCheck){
if(voltageInCheck>ThresholdVoltage&&SqeezeFlag==0){ //Begin the squeeze
  SqeezeFlag=1;
}
else if (voltageInCheck<ThresholdVoltage&&SqeezeFlag==1){ //End the squeeze &check fluctation
  SqeezeFlag=0; 
}
return SqeezeFlag;
} //end tollerance check



//*****************  END Signal Tollerance Check *******************//

int buttonPress(int voltageIn, int voltageIn2){
     int signal1 = tolleraceCheck(voltageIn);
     int signal2 = tolleraceCheck(voltageIn2);

    
      if(signal1 ==1 && signal2 ==0 ){ //contraction on electrode 1
      Serial.println("CONTRACTION ELECTRODE 1");
      return 0b00000001;

       
      }
      else if(signal1 ==0 && signal2 ==1 ){ //contraction on electrode 2
      Serial.println("CONTRACTION ELECTRODE 2");
      return 0b00000010;
        
      }
      else if(signal1 ==1 && signal2 ==1 ){ //co-contraction
      Serial.println("CO-CONTRACTION");
      return 0b00000100;

        
      }
      else if(signal1 ==0 && signal2 ==0 ){ //no contraction
      Serial.println("NO CONTRACTION");
      return 0b00000000;   

      }
      else{ //is there is an error
      //Serial.println("CONTRACTION ERROR");
      return 0b00000000;  
      }//end else
      
    }//end change detection
