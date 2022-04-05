#ifndef MAIN_H
#define MAIN_H

//input pins (16 toatal button avaliable, 1 analog input 0-255 but inputs can be changed)
#define BUTTON1 5
#define BUTTON2 18
#define BUTTON3 19  //You can use any pins on the board
#define BUTTON4 21  //but I don't recommend the ones that are used


// Define GPIO numbers (GPIOn)
#define LED GPIO_NUM_23
#define BUTTON GPIO_NUM_22
#define TMRSEL GPIO_NUM_16
#define EN5V GPIO_NUM_26
#define EMG1 32
#define EMG2 33
#define CONNECTED_LED_INDICATOR_PIN GPIO_NUM_2//pin that goes high while there's a device connected

// Function prototypes
double signalCheck(int Pin);
bool changeDetection(int checkedPin);
int tolleraceCheck(double voltageIn);
int buttonPress(double voltageIn, double voltageIn2);
void initADC();
int readADC(int channelNum);
long map(long x, long in_min, long in_max, long out_min, long out_max);
void setup();
void loop();
void delay(int t);
void writeLED(int state);
void blinkLED();
#endif