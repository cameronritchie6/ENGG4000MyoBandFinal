#include <Arduino.h>
#include <string.h>
#include <iostream>
#include <string.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>


// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

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
    Serial.println("Write BOY");
  };  
  void onRead(BLECharacteristicCallbacks* pCharacteristic){
    clientRead  = true;
    Serial.println("READ BOY");
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
  //  Serial.println(outputCode);

    
  void onRead(BLECharacteristic* pCharacteristic){
   //If we need to know a reading flag
  }
};


void setup() {

  clientWrite = false;

  Serial.begin(115200);
  Serial.println("Starting BLE work!");
  BLEDevice::init("BigChugginz");
   pServer = BLEDevice::createServer();
              pServer->setCallbacks(new MyServerCallbacks());
  pService = pServer->createService(SERVICE_UUID);


  pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE                                                                                                                  
                                       );


  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setCallbacks(new MyCallbacks());
  pCharacteristic->setValue("Hello World says Neil");
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
      Serial.println("CONTRACTION ERROR");
      signalOut =ContractionError;
      }//end else
      pCharacteristic->setValue(signalOut); //output the contraction code
    }//end change detection

      break;
     
    }


   

  default :
    Serial.println("Invalid Data request code");

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
