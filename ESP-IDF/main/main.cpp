#include <stdio.h>
#include "include/bleLibs/BLEDevice.h"
#include "include/bleLibs/BLEUtils.h"
#include "include/bleLibs/BLEServer.h"
#include "include/bleLibs/BLE2902.h"
// #include "include/bleLibs/HIDTypes.h"
#include "include/bleLibs/BLEHIDDevice.h"





extern "C" void app_main(void)
{
    printf("\n\nStill working \n");
}



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
#define E1 32
#define E2 33


const int potPin1 = 32; //analog pin on esp 32
const int potPin2 = 33; //analog pin 2 on esp 32 
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
int buttonPress(double voltageIn, double voltageIn2);

//pin that goes high while there's a device connected
#define CONNECTED_LED_INDICATOR_PIN 2

uint8_t inputValues[] = {0b00000000, 0x0,0x0};

class MyCallbacks : public BLEServerCallbacks { //Class that does stuff when device disconects or connects
    void onConnect(BLEServer* pServer) {
      connected = true;
    //   Serial.println("Connected");
      BLE2902* desc = (BLE2902*)input->getDescriptorByUUID(BLEUUID((uint16_t)0x2902));
      desc->setNotifications(true);

      digitalWrite(CONNECTED_LED_INDICATOR_PIN, HIGH);
    }

    void onDisconnect(BLEServer* pServer) {
      connected = false;
    //   Serial.println("Disconnected");
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


  BLEDevice::init("MyoBand");
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
    0x09, 0x30,   // USAGE (X) ----------------DELETE IF NOT WORKING
    0x09, 0x31, // USAGE (Y)
    0x15, 0x00, // LOGICAL_MINIMUM (-127) ------------delete if not working
    0x25, 0xff, // LOGICAL_MAXIMUM (127)
   // 0x36, 0x00,0x80, // PHYSICAL_MINIMUM (0) -----------delete if not working
   // 0x46, 0xff, 0x7f, // PHYSICAL_MAXIMUM (255)
    
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
pAdvertising->setScanResponse(true);
pAdvertising->start();
hid->setBatteryLevel(7);

ESP_LOGD(LOG_TAG, "Advertising started!");
delay(portMAX_DELAY);

};


// Define GPIO numbers (GPIOn)
#define LED 23
#define BUTTON 22
#define TMRSEL 16
#define EN5V 26
#define EMG1 32
#define EMG2 33

void setup() {
  Serial.begin(115200);

// Set pin modes
  pinMode(TMRSEL, OUTPUT);        // Charging timer selection
  pinMode(EN5V, OUTPUT);          // 5V regulator enable
  pinMode(LED, OUTPUT);           // Status LED (active low)
  pinMode(BUTTON, INPUT_PULLUP);  // Push button (active low)
  

  // Initialize pin states
  digitalWrite(EN5V, HIGH);    // Enable 5V regulator
  digitalWrite(TMRSEL, HIGH);  // Set max charge time to 4.5 hours
  digitalWrite (LED, HIGH);    // Turn LED off by default
  
  xTaskCreate(taskServer, "server", 20000, NULL, 5, NULL);
inputValues[0] |= 0b00000001;
  inputValues[1] = analogRead(ANALOGSTICK); //DELETE IF NOT WORKING
   inputValues[2] = analogRead(ANALOGSTICK2);//DELETE IF NOT WORKING
}

void loop() {

  
  
       if(connected){
      inputValues[0] = buttonPress( signalCheck(E1),  signalCheck(E2));
                   inputValues[1] =  map(analogRead(E1), 0, 4095, 0, 255);
                   inputValues[2] =  map(analogRead(E2), 0, 4095, 0, 255);
    input->setValue(inputValues, sizeof(inputValues));
    input->notify();

    }

    

delay(10);
}


double signalCheck(int Pin){
int potValue = analogRead(Pin);
double ADCRatio= VoltageMax/(pow(2,ADCResolution)-1);
 double voltageMeasurement = ADCRatio*potValue;
//  Serial.print("SIGNAL: ");
//  Serial.println(voltageMeasurement);
 
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

int buttonPress(double voltageIn, double voltageIn2){
     int signal1 = tolleraceCheck(voltageIn);
     int signal2 = tolleraceCheck(voltageIn2);

    
      if(signal1 ==1 && signal2 ==0 ){ //contraction on electrode 1
    //   Serial.println("CONTRACTION ELECTRODE 1");
      return 0b00000001;

       
      }
      else if(signal1 ==0 && signal2 ==1 ){ //contraction on electrode 2
    //   Serial.println("CONTRACTION ELECTRODE 2");
      return 0b00000010;
        
      }
      else if(signal1 ==1 && signal2 ==1 ){ //co-contraction
    //   Serial.println("CO-CONTRACTION");
      return 0b00000100;

        
      }
      else if(signal1 ==0 && signal2 ==0 ){ //no contraction
    //   Serial.println("NO CONTRACTION");
      return 0b00000000;   

      }
      else{ //is there is an error
      //Serial.println("CONTRACTION ERROR");
      return 0b00000000;  
      }//end else
      
    }//end change detection