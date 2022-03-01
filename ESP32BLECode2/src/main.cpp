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

//pin that goes high while there's a device connected
#define CONNECTED_LED_INDICATOR_PIN 2

//inputValues[0] is the first 8 buttons, [1] is the next 8, [2] is the analog input
//Each one of the bits represnets a button. 1 == pressed 0 == not pressed
//uint8_t inputValues[3] = {0b00000000, 0b00000000, 0x0};----------undo this issomething happens
//uint8_t inputValues[] = {0b00000000, 0b00000000, 0x0,0x0};---
uint8_t inputValues[] = {0b00000000, 0x0,0x0};

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
      uint8_t* value = (uint8_t*)(me->getValue().c_str());
      //ESP_LOGI(LOG_TAG, "special keys: %d", *value);
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
    0x95, 0x03, // REPORT_COUNT (3)----------delete if not working
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
inputValues[0] |= 00000001;
  inputValues[1] = analogRead(ANALOGSTICK); //DELETE IF NOT WORKING
   inputValues[2] = analogRead(ANALOGSTICK2);//DELETE IF NOT WORKING
}

void loop() {

  //inputValues[1] = analogRead(ANALOGSTICK); 
 //  inputValues[2] = analogRead(ANALOGSTICK2);

  
  

//inputValues[0] |= 00000001;
    if((changeDetection(potPin1)||changeDetection(potPin2))&&connected){
      inputValues[1] = analogRead(potPin1);
       inputValues[2] = analogRead(potPin2);
    input->setValue(inputValues, sizeof(inputValues));
    input->notify();
    delay(200);
    }

    

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

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

/*
#include <BleGamepad.h>
BleGamepad bleGamepad;


const int potPin = 4;                // Potentiometer is connected to GPIO 34 (Analog ADC1_CH6) 
const int numberOfPotSamples = 5;     // Number of pot samples to take (to smooth the values)
const int delayBetweenSamples = 4;    // Delay in milliseconds between pot samples
const int delayBetweenHIDReports = 5; // Additional delay in milliseconds between HID reports

void setup() 
{
  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  bleGamepad.begin();
}

void loop() 
{
  if(bleGamepad.isConnected()) 
  {
    int potValues[numberOfPotSamples];  // Array to store pot readings
    int potValue = 0;   // Variable to store calculated pot reading average

    // Populate readings
    for(int i = 0 ; i < numberOfPotSamples ; i++)
    {
      potValues[i] = analogRead(potPin);
      potValue += potValues[i];
      delay(delayBetweenSamples);
    }

    // Calculate the average
    potValue = potValue / numberOfPotSamples;

    // Map analog reading from 0 ~ 4095 to 32737 ~ -32737 for use as an axis reading
    int adjustedValue = map(potValue, 0, 4095, 32737, -32737);

    // Update X axis and auto-send report
    bleGamepad.setX(adjustedValue);
    delay(delayBetweenHIDReports);
   
    // The code below (apart from the 2 closing braces) is for pot value degugging, and can be removed
    // Print readings to serial port
    Serial.print("Sent: ");
    Serial.print(adjustedValue);
    Serial.print("\tRaw Avg: ");
    Serial.print(potValue);
    Serial.print("\tRaw: {");

    // Iterate through raw pot values, printing them to the serial port
    for(int i = 0 ; i < numberOfPotSamples ; i++)
    {
      Serial.print(potValues[i]);

      // Format the values into a comma seperated list
      if(i == numberOfPotSamples-1)
      {
        Serial.println("}");
      }
      else
      {
        Serial.print(", ");  
      }
    }    
  }
}

*/
/* 


#define GamepadChar_UUID BLEUUID((uint16_t)0x2A19) //this defines the characterustic UUID of a HID device
#define PnPChar_UUID BLEUUID((uint16_t)0x2A50) //Pnp Characteristic or gamepad **REQUIRED
#define GamepadService_UUID BLEUUID((uint16_t)0x1812) //this defies the service of an HID
#define ReportMapChar_UUID BLEUUID((uint16_t)0x2A4B) //report map is the characteristic that the HID device data will be sent through
#define ReportRefDescript_UUID BLEUUID((uint16_t)0x2908) //not sure what this is for but it mentions it in the profile.... (may not need?)

#define BatteryLevelChar_UUID BLEUUID((uint16_t)0x2A19) //Battery Level Character ID **REquired
#define BatteryLevelService_UUID BLEUUID((uint16_t)0x180F) //Battery Service **REQUIERD
#define BatteryLevelDescriptor_UUID (BLEUUID((uint16_t)0x2901)) //Battery Level % decriptor **REquired


BLEServer *pServerGamepad;
BLEService *pServiceGamepad;
BLECharacteristic *pCharacteristicGamepad;



bool deviceConnected;
class MyServerCallback: public BLEServerCallbacks {
  void onConnect(BLEServer* pServerBattery) {
    deviceConnected = true;
    //Serial.println("BTConnected");
  };
  void onDisconnect(BLEServer* pServerBattery) {
    deviceConnected = false;
  }
};



void setup(){

  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  BLEDevice::init("BatteryTest");
   pServerBattery = BLEDevice::createServer();
              pServerBattery->setCallbacks(new MyServerCallback());             
  pServiceBattery = pServerBattery->createService(BatteryLevelService_UUID);
   pCharacteristicBattery = pServiceBattery->createCharacteristic(
                                         BatteryLevelChar_UUID,
                                         BLECharacteristic::PROPERTY_READ|
                                         BLECharacteristic::PROPERTY_NOTIFY                                                                                                                                                    
                                       );

  pCharacteristicBattery->setValue("Battery Level:");
pCharacteristicBattery->addDescriptor(&BatteryLevelDescriptor);
   pCharacteristicBattery->addDescriptor(new BLE2902());
   BLEAdvertising *pAdvertisingBattery = BLEDevice::getAdvertising();
  pAdvertisingBattery->addServiceUUID(BatteryLevelService_UUID);
  pAdvertisingBattery->setScanResponse(true); //Let Client request Service names and more info (Required for phones)
  pAdvertisingBattery->setMinPreferred(0x06);
  pAdvertisingBattery->setMinPreferred(0x12);
 // BLEDevice::startAdvertising();
   pServiceBattery->start();
   pServerBattery->getAdvertising()->start();


   pServerBattery->pnp()
} //end battery setup


  uint8_t level = 57;

void loop() {


   pCharacteristicBattery->setValue(&level, 1);
  pCharacteristicBattery->notify();
  
  delay(5000);

  level++;
  Serial.println(int(level));

  if (int(level)==100)
  level=0;
}
*/


/*

//-----------------------Battery Charge code---------------------------------

#define BatteryLevelChar_UUID BLEUUID((uint16_t)0x2A19) //this defines the characterustic UUID of a HID device
#define BatteryLevelService_UUID BLEUUID((uint16_t)0x180F) //keep
//#define BatteryLevelDescriptor_UUID (BLEUUID((uint16_t)0x2901))
BLEDescriptor BatteryLevelDescriptor(BLEUUID((uint16_t)0x2901));

BLEServer *pServerBattery;
BLEService *pServiceBattery;
BLECharacteristic *pCharacteristicBattery;



bool deviceConnected;
class MyServerCallback: public BLEServerCallbacks {
  void onConnect(BLEServer* pServerBattery) {
    deviceConnected = true;
    //Serial.println("BTConnected");
  };
  void onDisconnect(BLEServer* pServerBattery) {
    deviceConnected = false;
  }
};



void setup(){

  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  BLEDevice::init("BatteryTest");
   pServerBattery = BLEDevice::createServer();
              pServerBattery->setCallbacks(new MyServerCallback());             
  pServiceBattery = pServerBattery->createService(BatteryLevelService_UUID);
   pCharacteristicBattery = pServiceBattery->createCharacteristic(
                                         BatteryLevelChar_UUID,
                                         BLECharacteristic::PROPERTY_READ|
                                         BLECharacteristic::PROPERTY_NOTIFY                                                                                                                                                    
                                       );

  pCharacteristicBattery->setValue("Battery Level:");
pCharacteristicBattery->addDescriptor(&BatteryLevelDescriptor);
   pCharacteristicBattery->addDescriptor(new BLE2902());
   BLEAdvertising *pAdvertisingBattery = BLEDevice::getAdvertising();
  pAdvertisingBattery->addServiceUUID(BatteryLevelService_UUID);
  pAdvertisingBattery->setScanResponse(true); //Let Client request Service names and more info (Required for phones)
  pAdvertisingBattery->setMinPreferred(0x06);
  pAdvertisingBattery->setMinPreferred(0x12);
 // BLEDevice::startAdvertising();
   pServiceBattery->start();
   pServerBattery->getAdvertising()->start();
} //end battery setup


  uint8_t level = 57;

void loop() {


   pCharacteristicBattery->setValue(&level, 1);
  pCharacteristicBattery->notify();
  
  delay(5000);

  level++;
  Serial.println(int(level));

  if (int(level)==100)
  level=0;
}

*/


/*

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"



BLEServer *pServer;
BLEService *pService;
BLECharacteristic *pCharacteristic;


const int potPin1 = 4; //analog pin on esp 32
const int potPin2 = 15; //analog pin 2 on esp 32 
double VoltageMax = 3.3; //max vltage applied to POT
float ADCResolution = 12; //The ESP32 has a 12bit approximation register for 
bool clientWrite;
bool clientRead;
bool deviceConnected;
int outputCode;

const int singleAnalogOut = 1; //the code that has to be written to the BLE server to begin sending single analog voltage readning
const int doubleAnalogOut = 2; //the code that has to be written to the BLE server to begin sending two analog voltage readnings
const int controlInput = 3; //the code that has to be written to the BLE server to begin sending contraction or co-contraction values


double oldVoltage1;
double oldVoltage2;
double voltageIn;
double voltageIn2;
double ThresholdVoltage =2.0; //Threshold voltage in (V) to declare a contraction or not on the EMG signal
double emgTollerance = 0.1; // the tollerance of fluctuation that the emg signal can have around the threshold voltage before a change state
int tolleranceFlag =0;
int SqeezeFlag=0;

std::string Electrode1ContractionCode="1";
std::string Electrode2ContractionCode="2";
std::string CocontractionCode="3";
std::string noContractionCode="4";
std::string ContractionError ="5";





double signalCheck(int Pin);
boolean changeDetection(int checkedPin);
int tolleraceCheck(double voltageIn);

class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
    Serial.println("BTConnected");
  };
  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
  }
};


class CharacteristicCallbacks: public BLECharacteristicCallbacks {
  
  void onWrite(BLECharacteristicCallbacks* pCharacteristic){
    clientWrite  = true;
   // Serial.println("Write BOY");
  };  
  void onRead(BLECharacteristicCallbacks* pCharacteristic){
    clientRead  = true;
   // Serial.println("READ BOY");
  };  
};


class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
    std::string strCode = pCharacteristic->getValue();
   char outputCodeChar;
   outputCodeChar =strCode[0];
   Serial.println(outputCodeChar);
    outputCode = outputCodeChar-'0';
    }
  

  
    //clientWrite  = true;
   // Serial.println(outputCode);

    
  void onRead(BLECharacteristic* pCharacteristic){
   //If we need to know a reading flag
  }
};


void setup() {

  clientWrite = false;

  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  BLEDevice::init("ESP32_BLE");
   pServer = BLEDevice::createServer();
              pServer->setCallbacks(new MyServerCallbacks());
  pService = pServer->createService(SERVICE_UUID);


  pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE |
                                         BLECharacteristic::PROPERTY_NOTIFY
                                         
                                       );

  
  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setCallbacks(new MyCallbacks());
  pCharacteristic->setValue("Begin...");
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true); //Let Client request Service names and more info (Required for phones)
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {

switch(outputCode){

  case singleAnalogOut: {//what will happen when the BLE code is 1 (single analog output)
    if(changeDetection(potPin1)){
     char voltString[8]; //declairng the strig that the voltage value will be converted to
     dtostrf(voltageIn, 1, 2, voltString); //convert the double into a string
     pCharacteristic->setValue(voltString); 
     Serial.println(voltString);
     pCharacteristic->notify();
    } //end if

  break; //end the singleAnalogOut case
  }

  case doubleAnalogOut:{

    if(changeDetection(potPin1)||changeDetection(potPin2)){
    char voltString1[8]; //declairng the strig that the voltage value will be converted to
    char voltString2[8]; 
     dtostrf(voltageIn, 1, 2, voltString1); //convert the double into a string
     dtostrf(voltageIn2, 1, 2, voltString2); //convert the double into a string
     std::string doubleVoltageString = std::string(voltString1) + " " +voltString2; //combine the two voltage readings as one string
    
     pCharacteristic->setValue(doubleVoltageString); //end the two analog values to the BLE server
     Serial.println(doubleVoltageString.c_str() );
     pCharacteristic->notify();
  }//end if change detection 
  break;
  } //end double analog output case

  case controlInput:{
    std::string signalOut;
    if(changeDetection(potPin1)||changeDetection(potPin2)){
     int signal1 = tolleraceCheck(voltageIn);
     int signal2 = tolleraceCheck(voltageIn2);

    
      if(signal1 ==1 && signal2 ==0 ){ //contraction on electrode 1
      Serial.println("CONTRACTION ELECTRODE 1");
      signalOut=Electrode1ContractionCode;

       
      }
      else if(signal1 ==0 && signal2 ==1 ){ //contraction on electrode 2
      Serial.println("CONTRACTION ELECTRODE 2");
      signalOut=Electrode2ContractionCode;
        
      }
      else if(signal1 ==1 && signal2 ==1 ){ //co-contraction
      Serial.println("CO-CONTRACTION");
      signalOut=CocontractionCode;

        
      }
      else if(signal1 ==0 && signal2 ==0 ){ //no contraction
      Serial.println("NO CONTRACTION");
      signalOut=noContractionCode;     

      }
      else{ //is there is an error
      //Serial.println("CONTRACTION ERROR");
      signalOut =ContractionError;
      }//end else
      pCharacteristic->setValue(signalOut); //output the contraction code
      pCharacteristic->notify();
    }//end change detection

      break;
     
    }


   

  default :
    //Serial.println("Invalid Data request code");
    Serial.print("");
} //end switch case statement





//Serial.println(outputCode);
  delay(500);

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

*/

//***************** Signal Tollerance Check *********************//
//Recieved the voltage of the signal, then checks to see if its a contraction or not
/*

int tolleraceCheck(double voltageInCheck){
if(voltageInCheck>ThresholdVoltage&&SqeezeFlag==0){ //Begin the squeeze
  SqeezeFlag=1;
}
else if (voltageInCheck<ThresholdVoltage&&SqeezeFlag==1){ //End the squeeze &check fluctation
  SqeezeFlag=0; 
}
return SqeezeFlag;
} //end tollerance check

*/

//*****************  END Signal Tollerance Check *******************//
