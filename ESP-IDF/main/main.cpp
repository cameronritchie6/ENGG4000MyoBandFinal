#include <stdio.h>
#include "include/bleLibs/BLEDevice.h"
#include "include/bleLibs/BLEUtils.h"
#include "include/bleLibs/BLEServer.h"
#include "include/bleLibs/BLE2902.h"
#include "include/bleLibs/HIDTypes.h"
#include "include/bleLibs/BLEHIDDevice.h"
#include <driver/adc.h>
// Include FreeRTOS for delay
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>
#include <math.h>
#include "include/main.h"


BLEHIDDevice* hid; //declare hid device
BLECharacteristic* input; //Characteristic that inputs button values to devices
BLECharacteristic* output; //Characteristic that takes input from client

//Stores if the board is connected or not
bool connected = false;


float ADCResolution = 12; //The ESP32 has a 12bit approximation register for 

bool clientWrite;
bool clientRead;
bool deviceConnected;
int outputCode;

double ThresholdVoltage =2.0; //Threshold voltage in (V) to declare a contraction or not on the EMG signal
double emgTollerance = 0.1; // the tollerance of fluctuation that the emg signal can have around the threshold voltage before a change state
int tolleranceFlag =0;
int SqeezeFlag=0;

const double VoltageMax = 3.3; //max voltage applied to POT

uint8_t inputValues[] = {0b00000000, 0x0,0x0};  // HID report map values sent out

class MyCallbacks : public BLEServerCallbacks { //Class that does stuff when device disconects or connects
    void onConnect(BLEServer* pServer) {
      connected = true;
      printf("Connected");
      BLE2902* desc = (BLE2902*)input->getDescriptorByUUID(BLEUUID((uint16_t)0x2902));
      desc->setNotifications(true);
      gpio_set_level(CONNECTED_LED_INDICATOR_PIN, 1);

    //   digitalWrite(CONNECTED_LED_INDICATOR_PIN, HIGH);
    }

    void onDisconnect(BLEServer* pServer) {
      connected = false;
      printf("Disconnected");
      BLE2902* desc = (BLE2902*)input->getDescriptorByUUID(BLEUUID((uint16_t)0x2902));
      desc->setNotifications(false);
      gpio_set_level(CONNECTED_LED_INDICATOR_PIN, 0);
    //   digitalWrite(CONNECTED_LED_INDICATOR_PIN, LOW);
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

// ESP_LOGD(LOG_TAG, "Advertising started!");
// delay(portMAX_DELAY);
vTaskDelay(portMAX_DELAY);

};

extern "C" void app_main(void)
{
    setup();
    while(true) {
       loop(); 
    }
    
}

void setup() {

    // Initialize ADC
    initADC();

    // Initialize pin states
    gpio_set_level(EN5V, 1); // Enable 5V regulator
    gpio_set_level(TMRSEL, 1); // Set max charge time to 4.5 hours
    gpio_set_level(LED, 1); // Turn LED off by default
    gpio_set_pull_mode(BUTTON, GPIO_PULLUP_ONLY);   // Set button to pull up

    // Display board has turned on
    blinkLED();

    // Create BLE server
    xTaskCreate(taskServer, "server", 20000, NULL, 5, NULL);
}

void loop() {
  
       if(connected){
      inputValues[0] = buttonPress( signalCheck(EMG1),  signalCheck(EMG2));
                   inputValues[1] =  map(readADC(EMG1), 0, 4095, 0, 255);
                   inputValues[2] =  map(readADC(EMG2), 0, 4095, 0, 255);
    input->setValue(inputValues, sizeof(inputValues));
    input->notify();

    }

    if (!gpio_get_level(BUTTON))
    {
        // Button is on
        writeLED(0);
    }
    

    delay(10);    
}


void writeLED(int state) {
    gpio_set_level(LED, state);
}

// Blink LED
void blinkLED() {
    int numBlinks = 3;  // number of times to blink LED
    int delayTime = 500;    // milliseconds to delay
    for (int i = 0; i < numBlinks; i++) {
        writeLED(0);
        delay(delayTime);
        writeLED(1);
        delay(delayTime);
    }
}

// Delay by t milliseconds
void delay(int t) {
    vTaskDelay(t / portTICK_RATE_MS);
}


void initADC() {
    // Turn on ADC
    adc_power_on();
}

int readADC(int channelNum) {
    // Electrode 1 = channel 4
    // Electrode 2 = channel 5
    // Both ADC1
    if (channelNum == EMG1) {
        // Return value for electrode 1
        return adc1_get_raw(ADC1_CHANNEL_4);
    } else if (channelNum == EMG2) {
        // Return value for electrode 2
        return adc1_get_raw(ADC1_CHANNEL_5);
    }
    
    return 0;
}

long map(long x, long in_min, long in_max, long out_min, long out_max) {
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}




double signalCheck(int Pin){
int potValue = readADC(Pin);
double ADCRatio= VoltageMax/(pow(2,ADCResolution)-1);
double voltageMeasurement = ADCRatio*potValue;
printf("SIGNAL: %lf", voltageMeasurement);
 
return voltageMeasurement ;
}




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
      printf("CONTRACTION ELECTRODE 1");
      return 0b00000001;

       
      }
      else if(signal1 ==0 && signal2 ==1 ){ //contraction on electrode 2
      printf("CONTRACTION ELECTRODE 2");
      return 0b00000010;
        
      }
      else if(signal1 ==1 && signal2 ==1 ){ //co-contraction
      printf("CO-CONTRACTION");
      return 0b00000100;

        
      }
      else if(signal1 ==0 && signal2 ==0 ){ //no contraction
      printf("NO CONTRACTION");
      return 0b00000000;   

      }
      else{ //is there is an error
      printf("CONTRACTION ERROR");
      return 0b00000000;  
      }//end else
      
    }//end change detection