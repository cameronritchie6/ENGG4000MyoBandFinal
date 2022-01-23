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


const int potPin = 4; //analog pin on esp 32
double voltageIn=0;
double VoltageMax = 3.3; //max vltage applied to POT
float ADCResolution = 12; //The ESP32 has a 12bit approximation register for 
bool clientWrite;
bool clientRead;
bool deviceConnected;
//char outputCode[];
double signalCheck(int Pin);

class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
    Serial.println("BTConnected");
  };
  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
  }
};

/*
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
*/

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
   // std::string strCode = pCharacteristic->getValue();
   //outputCode() = strCode;

  
    //clientWrite  = true;
  //  Serial.println(outputCode);

  };  
  void onRead(BLECharacteristic* pCharacteristic){
   //If we need to know a reading flag
  };  
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


double oldVoltage =voltageIn;

  voltageIn=signalCheck(potPin); //probe the voltage
  oldVoltage = round( oldVoltage * 100.0 ) / 100.0; //round to two didgits to compare if there is a change
  voltageIn = round( voltageIn * 100.0 ) / 100.0; //round to two didgits to compare if there is a change from previous value

  if(oldVoltage!=voltageIn){ //if there is a difference betwen voltage ratings - display the change
  char voltString[8]; //declairng the strig that the voltage value will be converted to
dtostrf(voltageIn, 1, 2, voltString); //convert the double into a string
pCharacteristic->setValue(voltString); 
Serial.print(oldVoltage);
Serial.print(" ");
Serial.print(voltageIn);
Serial.print(" ");
Serial.println(voltString);
Serial.println(clientWrite);
}

//Serial.println(outputCode);
  delay(2000);

}




double signalCheck(int Pin){
int potValue = analogRead(Pin);
double ADCRatio= VoltageMax/(pow(2,ADCResolution)-1);
 double voltageMeasurement = ADCRatio*potValue;
 
return voltageMeasurement ;
}


