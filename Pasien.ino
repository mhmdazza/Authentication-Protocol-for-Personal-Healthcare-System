//-------- PASIEN 1 ----------

#include <EEPROM.h>
#include "PRESENT (Master).h"
#include <Ticker.h>
/////////////////// BISMILLAHIRRAHMANIRRAHIM /////////////////////////

///////////// SHA 256 ////////////////

#include <string.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>

char hex[256];
uint8_t data[256];
int start = 0;
int seconds = 0;
uint8_t hash[32];
String pin;
String B5;
String B6;
String B7;
String N1="0";
//unsigned long int kkk[5]={0,0,0,0,0};
#define SHA256_BLOCK_SIZE 32

typedef struct {
  uint8_t data[64];
  uint32_t datalen;
  unsigned long long bitlen;
  uint32_t state[8];
} SHA256_CTX;

void sha256_init(SHA256_CTX *ctx);
void sha256_update(SHA256_CTX *ctx, const uint8_t data[], size_t len);
void sha256_final(SHA256_CTX *ctx, uint8_t hash[]);

#define ROTLEFT(a,b) (((a) << (b)) | ((a) >> (32-(b))))
#define ROTRIGHT(a,b) (((a) >> (b)) | ((a) << (32-(b))))

#define CH(x,y,z) (((x) & (y)) ^ (~(x) & (z)))
#define MAJ(x,y,z) (((x) & (y)) ^ ((x) & (z)) ^ ((y) & (z)))
#define EP0(x) (ROTRIGHT(x,2) ^ ROTRIGHT(x,13) ^ ROTRIGHT(x,22))
#define EP1(x) (ROTRIGHT(x,6) ^ ROTRIGHT(x,11) ^ ROTRIGHT(x,25))
#define SIG0(x) (ROTRIGHT(x,7) ^ ROTRIGHT(x,18) ^ ((x) >> 3))
#define SIG1(x) (ROTRIGHT(x,17) ^ ROTRIGHT(x,19) ^ ((x) >> 10))

static const uint32_t k[64] = {
  0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
  0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
  0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
  0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
  0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
  0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
  0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
  0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
};

void sha256_transform(SHA256_CTX *ctx, const uint8_t data[]) {
  uint32_t a, b, c, d, e, f, g, h, i, j, t1, t2, m[64];

  for (i = 0, j = 0; i < 16; ++i, j += 4)
    m[i] = ((uint32_t)data[j] << 24) | ((uint32_t)data[j + 1] << 16) | ((uint32_t)data[j + 2] << 8) | ((uint32_t)data[j + 3]);
  for ( ; i < 64; ++i)
    m[i] = SIG1(m[i - 2]) + m[i - 7] + SIG0(m[i - 15]) + m[i - 16];

  a = ctx->state[0];
  b = ctx->state[1];
  c = ctx->state[2];
  d = ctx->state[3];
  e = ctx->state[4];
  f = ctx->state[5];
  g = ctx->state[6];
  h = ctx->state[7];

  for (i = 0; i < 64; ++i) {
    t1 = h + EP1(e) + CH(e,f,g) + k[i] + m[i];
    t2 = EP0(a) + MAJ(a,b,c);
    h = g;
    g = f;
    f = e;
    e = d + t1;
    d = c;
    c = b;
    b = a;
    a = t1 + t2;
  }

  ctx->state[0] += a;
  ctx->state[1] += b;
  ctx->state[2] += c;
  ctx->state[3] += d;
  ctx->state[4] += e;
  ctx->state[5] += f;
  ctx->state[6] += g;
  ctx->state[7] += h;
}

void sha256_init(SHA256_CTX *ctx)
{
  ctx->datalen = 0;
  ctx->bitlen = 0;
  ctx->state[0] = 0x6a09e667;
  ctx->state[1] = 0xbb67ae85;
  ctx->state[2] = 0x3c6ef372;
  ctx->state[3] = 0xa54ff53a;
  ctx->state[4] = 0x510e527f;
  ctx->state[5] = 0x9b05688c;
  ctx->state[6] = 0x1f83d9ab;
  ctx->state[7] = 0x5be0cd19;
}

void sha256_update(SHA256_CTX *ctx, const uint8_t data[], size_t len) {
  uint32_t i;

  for (i = 0; i < len; ++i) {
    ctx->data[ctx->datalen] = data[i];
    ctx->datalen++;
    if (ctx->datalen == 64) {
      sha256_transform(ctx, ctx->data);
      ctx->bitlen += 512;
      ctx->datalen = 0;
    }
  }
}

void sha256_final(SHA256_CTX *ctx, uint8_t hash[]) {
  uint32_t i;

  i = ctx->datalen;

  // Pad whatever data is left in the buffer.
  if (ctx->datalen < 56) {
    ctx->data[i++] = 0x80;
    while (i < 56)
      ctx->data[i++] = 0x00;
  }
  else {
    ctx->data[i++] = 0x80;
    while (i < 64)
      ctx->data[i++] = 0x00;
    sha256_transform(ctx, ctx->data);
    memset(ctx->data, 0, 56);
  }

  // Append to the padding the total message's length in bits and transform.
  ctx->bitlen += ctx->datalen * 8;
  ctx->data[63] = ctx->bitlen;
  ctx->data[62] = ctx->bitlen >> 8;
  ctx->data[61] = ctx->bitlen >> 16;
  ctx->data[60] = ctx->bitlen >> 24;
  ctx->data[59] = ctx->bitlen >> 32;
  ctx->data[58] = ctx->bitlen >> 40;
  ctx->data[57] = ctx->bitlen >> 48;
  ctx->data[56] = ctx->bitlen >> 56;
  sha256_transform(ctx, ctx->data);

  // Since this implementation uses little endian byte ordering and SHA uses big endian,
  // reverse all the bytes when copying the final state to the output hash.
  for (i = 0; i < 4; ++i) {
    hash[i]      = (ctx->state[0] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 4]  = (ctx->state[1] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 8]  = (ctx->state[2] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 12] = (ctx->state[3] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 16] = (ctx->state[4] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 20] = (ctx->state[5] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 24] = (ctx->state[6] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 28] = (ctx->state[7] >> (24 - i * 8)) & 0x000000ff;
  }
}

char *btoh(char *dest, uint8_t *src, int len) {
  char *d = dest;
  while( len-- ) sprintf(d, "%02x", (unsigned char)*src++), d += 2;
  return dest;
}

String SHA256(String data) 
{
  uint8_t data_buffer[data.length()];
  
  for(int i=0; i<data.length(); i++)
  {
    data_buffer[i] = (uint8_t)data.charAt(i);
  }
  
  SHA256_CTX ctx;
  ctx.datalen = 0;
  ctx.bitlen = 512;
  
  sha256_init(&ctx);
  sha256_update(&ctx, data_buffer, data.length());
  sha256_final(&ctx, hash);
  return(btoh(hex, hash, 32));
}

//String tostr(String str,char a[64]){
//  int i = 0;
//  while(str.length()<15){
//    int b=a[i];
//    str=str+String(b);
//    i++;
//  }
//  return str;
//}
//
//uint64_t toint(String str){
//  uint64_t x;
//  String y;
//  char chr[str.length()];
//  str.toCharArray(chr,16);
//  for(int i=0;i<(str.length());i++){
//    y=chr[i];
//    x=(x*10)+y.toInt();  
//  }
//  return x;
//}
//
//String potong15(String sha){
//  char a[64];
//  sha.toCharArray(a,64);
//  String str;
//  str=tostr(str,a);
//  str=str.substring(0,15);
//  return str;
//}

String xorString(String a,String b){
  int z=a.length()+1;
  char c1[z];
  char c2[z];
  String temp1;
  String temp2;
  String hasil;
  int d;
  int e;
  int state;
  while(a.length()!=b.length()){
    b="0"+b;
  }
  a.toCharArray(c1,(z));
  b.toCharArray(c2,(z));
  for(int i=0;i<z;i++){
    temp1=c1[i];
    //Serial.print(temp1);
    temp2=c2[i];
    state=hextoint(temp1)^hextoint(temp2);
    hasil=hasil+String(state,HEX);
  }
  return hasil;
}

int hextoint(String a){
  if (a=="a"){
    return 10;
  }
  else if(a=="b"){
    return 11;
  }
  else if(a=="c"){
    return 12;
  }
  else if(a=="d"){
    return 13;
  }
  else if(a=="e"){
    return 14;
  }
  else if(a=="f"){
    return 15;
  }
  else{
    return a.toInt();
  }
}
////////////////////// SHA 256 ////////////////////////

///////////////////// PULSE SENSOR ////////////////////

Ticker flipper;

//  VARIABLES
int blinkPin = 15;                // pin to blink led at each beat
int fadePin = 12;                 // pin to do fancy classy fading blink at each beat
int fadeRate = 0;                 // used to fade LED on with PWM on fadePin

volatile int rate[10];                    // array to hold last ten IBI values
volatile unsigned long sampleCounter = 0; // used to determine pulse timing
volatile unsigned long lastBeatTime = 0;  // used to find IBI
volatile int P =512;                      // used to find peak in pulse wave, seeded
volatile int T = 512;                     // used to find trough in pulse wave, seeded
volatile int thresh = 512;                // used to find instant moment of heart beat, seeded
volatile int amp = 100;                   // used to hold amplitude of pulse waveform, seeded
volatile boolean firstBeat = true;        // used to seed rate array so we startup with reasonable BPM
volatile boolean secondBeat = false;      // used to seed rate array so we startup with reasonable BPM

// these variables are volatile because they are used during the interrupt service routine!
volatile int BPM;                   // used to hold the pulse rate
volatile int Signal;                // holds the incoming raw data
volatile int IBI = 600;             // holds the time between beats, must be seeded! 
volatile boolean Pulse = false;     // true when pulse wave is high, false when it's low
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.
///////////////////////////////////////////////////////////////////////////////


///////////////////// ESP WIFI /////////////////////
// Loading the ESP8266WiFi library and the PubSubClient library
#include <ESP8266WiFi.h>
#include <PubSubClient.h>

const char* ssid = "Ulfidiana";
const char* password = "ulfidiana";
const char* mqtt_server = "mqtt.eclipse.org";
//String SGk = "123";
String SGk;       //disimpan di EEPROM 11
String GID;       //disimpan di EEPROM 22    
String SID;       //disimpan di EEPROM 33

///////// EEPROM //////////
void writeString(char add,String data);
String read_String(char add);
////////////////////////////

//const char* ssid = "AP_STSN";
//const char* password = "BersamaDisiplinSukses";
//const char* mqtt_server = "192.168.41.21";
WiFiClient espClient;
PubSubClient client(espClient);

void setup_wifi() {
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("WiFi connected - ESP IP address: ");
  Serial.println(WiFi.localIP());
  }

// This functions is executed when some device publishes a message to a topic that your ESP8266 is subscribed to
// Change the function below to add logic to your program, so when a device publishes a message to a topic that 
// your ESP8266 is subscribed you can actually do something
void callback(String topic, byte* message, unsigned int length) {
  Serial.print("Message arrived on topic: ");
  Serial.print(topic);
  Serial.print(". Message: ");
  String messageTemp;
  
  for (int i = 0; i < length; i++) {
    Serial.print((char)message[i]);
    messageTemp += (char)message[i];
  }
  Serial.println();

  // Feel free to add more if statements to control more GPIOs with MQTT
  
  // If a message is received on the topic home/office/esp1/gpio2, you check if the message is either 1 or 0. Turns the ESP GPIO according to the message
  if(topic=="esp8266/pasien1/B5"){
    B5 = messageTemp;
    Serial.print("B5 terima : ");
    Serial.println(B5);
  }
  if(topic=="esp8266/pasien1/B6"){
    B6 = messageTemp;
    Serial.print("B6 terima : ");
    Serial.println(B6);
  }
  if(topic=="esp8266/pasien1/B7"){
    B7 = messageTemp;
//    Serial.print("B567 : ");
//    Serial.println(B5+B6+B7);

  ///// AMBIL DATA DARI EEPROM //////
      unsigned long StartTime = micros();
      SGk = read_String(0);
      Serial.print("SGk simpan : ");
      Serial.println(SGk);
      GID = read_String(65);
      Serial.print("GID simpan : ");
      Serial.println(GID);
      SID = read_String(130);
      Serial.print("SID simpan : ");
      Serial.println(SID);

      String pin = SGk+GID;
      //Serial.print("SGk+GID dan panjang : ");
      //Serial.println(SGk+GID);
      int teks = pin.length();
      //Serial.println(teks);
      String ptx = pin.substring(0,teks);
      String sha = SHA256(ptx);
      //Serial.println(sha);
//      sha=potong15(sha);
//      uint64_t x=toint(sha);
//      uint64_t y=toint(B5);
//      unsigned int xi=x^y;
//      String N1=String(xi);
      N1 = xorString(sha,B5);
      N1 = N1.substring(58,64);
      Serial.println();
      Serial.print("Ru : ");
      Serial.println(N1);
      sha=SHA256(N1);
//      sha=potong15(sha);
//      x=toint(sha);
//      y=toint(B6);
//      xi=0;
//      xi=x^y;
//      String N2 = String(xi);
      String N2 = xorString(sha,B6);
      N2 = N2.substring(58,64);
      Serial.print("Rg : ");
      Serial.println(N2);
      String B7hitung=SHA256(SGk+N1+N2);
      Serial.print("B7 terima : ");
      Serial.println(B7);
      Serial.print("B7 hitung : ");
      Serial.println(B7hitung);
      if (B7 == B7hitung){
        Serial.println("B7 valid!");
        Serial.println();
//        String N3 = "639574";
        String N3 = bilacak();
//        Serial.print("Rs : ");
//        Serial.println(n3);
        String SK3=SHA256(N1+N2+N3);
        Serial.print("Kunci Sesi Pasien (SKs) : ");
        Serial.println(SK3);
        sha=SHA256(SGk+N2);
        sha=sha.substring(0,15);
  //      sha=potong15(sha);
  //      unsigned int xi=sha.substring(10,15).toInt();
  //      xi=xi^(N3.toInt());
  //      String B8=sha.substring(0,10)+String(xi);
        String B8 = xorString(sha,N3);
        B8 = B8.substring(0,15);
  //      int PB8 = B8.length();
  //      String pB8 = String(PB8);
        Serial.print("B8 : ");
        Serial.println(B8);
        String B9=SHA256(SK3+SID+GID+N3);
        Serial.print("B9 : ");
        Serial.println(B9);        
        char b8[256];
        B8.toCharArray(b8,256);
        char b9[256];
        String b89 = B8+B9;
        b89.toCharArray(b9,256);
  //      char B89[256];
  //      b89.toCharArray(B89,256);
  //      Serial.print("B89: ");
  //      Serial.println(B89);
        client.publish("/esp8266/pasien1/B8",b8);
        client.publish("/esp8266/pasien1/B9",b9);
        unsigned long CurrentTime = micros();
        unsigned long ElapsedTime = CurrentTime - StartTime;
        Serial.print("Performa Waktu Sensor : ");
        Serial.print(ElapsedTime);
        Serial.println(" mikrodetik");
      }
  }
  if(topic=="esp8266/pasien1/SGk"){
      SGk = messageTemp;
      Serial.print("Menulis SGk di Alamat 0 : ");
      Serial.println(SGk);
      writeString(0, SGk);  //Address alamat EEPROM dan tipe data 'string'
  }
  if(topic=="esp8266/pasien1/GID"){
      Serial.println(messageTemp);
      GID = messageTemp;
      Serial.print("Menulis GID di Alamat 65 : ");
      Serial.println(GID);
      writeString(65, GID);  //Address alamat EEPROM dan tipe data 'string'
  }
  if(topic=="esp8266/pasien1/SNk"){
      Serial.println(messageTemp);
      SID = messageTemp;
      Serial.print("Menulis SID di Alamat 130 : ");
      Serial.println(SID);
      writeString(130, SID);  //Address alamat EEPROM dan tipe data 'string'
  }
  Serial.println();
}

// This functions reconnects your ESP8266 to your MQTT broker
// Change the function below if you want to subscribe to more topics with your ESP8266 
void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
     /*
     YOU  NEED TO CHANGE THIS NEXT LINE, IF YOU'RE HAVING PROBLEMS WITH MQTT MULTIPLE CONNECTIONS
     To change the ESP device ID, you will have to give a unique name to the ESP8266.
     Here's how it looks like now:
       if (client.connect("ESP8266Client")) {
     If you want more devices connected to the MQTT broker, you can do it like this:
       if (client.connect("ESPOffice")) {
     Then, for the other ESP:
       if (client.connect("ESPGarage")) {
      That should solve your MQTT multiple connections problem

     THE SECTION IN loop() function should match your device name
    */
    if (client.connect("ESPpasien1")) {
      Serial.println("connected");  
      // Subscribe or resubscribe to a topic
      // You can subscribe to more topics (to control more LEDs in this example)
      client.subscribe("esp8266/pasien1/B567");
      client.subscribe("esp8266/pasien1/B5");
      client.subscribe("esp8266/pasien1/B6");
      client.subscribe("esp8266/pasien1/B7");
      client.subscribe("esp8266/pasien1/SGk");
      client.subscribe("esp8266/pasien1/GID");
      client.subscribe("esp8266/pasien1/SNk");
      client.subscribe("esp8266/5");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

// The setup function sets your ESP GPIOs to Outputs, starts the serial communication at a baud rate of 115200
// Sets your mqtt broker and sets the callback function
// The callback function is what receives messages and actually controls the LEDs

//  ------------------------- VOID INTERRUPT() -------------------------------
void interruptSetup(){     
// Initializes Ticker to have flipper run the ISR to sample every 2mS as per original Sketch.
  flipper.attach_ms(2, ISRTr);     
} 


// THIS IS THE TICKER INTERRUPT SERVICE ROUTINE. 
// Ticker makes sure that we take a reading every 2 miliseconds
void ISRTr(){                         // triggered when flipper fires....
  cli();                               // disable interrupts while we do this
  Signal = analogRead(A0);              // read the Pulse Sensor 
  sampleCounter += 2;                         // keep track of the time in mS with this variable
  int N = sampleCounter - lastBeatTime;       // monitor the time since the last beat to avoid noise

    //  find the peak and trough of the pulse wave
  if(Signal < thresh && N > (IBI/5)*3){       // avoid dichrotic noise by waiting 3/5 of last IBI
    if (Signal < T){                        // T is the trough
      T = Signal;                         // keep track of lowest point in pulse wave 
    }
  }

  if(Signal > thresh && Signal > P){          // thresh condition helps avoid noise
    P = Signal;                             // P is the peak
  }                                        // keep track of highest point in pulse wave

  //  NOW IT'S TIME TO LOOK FOR THE HEART BEAT
  // signal surges up in value every time there is a pulse
  if (N > 250){                                   // avoid high frequency noise
    if ( (Signal > thresh) && (Pulse == false) && (N > (IBI/5)*3) ){        
      Pulse = true;                               // set the Pulse flag when we think there is a pulse
      digitalWrite(blinkPin,HIGH);                // turn on pin 13 LED
      IBI = sampleCounter - lastBeatTime;         // measure time between beats in mS
      lastBeatTime = sampleCounter;               // keep track of time for next pulse

      if(secondBeat){                        // if this is the second beat, if secondBeat == TRUE
        secondBeat = false;                  // clear secondBeat flag
        for(int i=0; i<=9; i++){             // seed the running total to get a realisitic BPM at startup
          rate[i] = IBI;                      
        }
      }

      if(firstBeat){                         // if it's the first time we found a beat, if firstBeat == TRUE
        firstBeat = false;                   // clear firstBeat flag
        secondBeat = true;                   // set the second beat flag
        sei();                               // enable interrupts again
        return;                              // IBI value is unreliable so discard it
      }   


      // keep a running total of the last 10 IBI values
      word runningTotal = 0;                  // clear the runningTotal variable    

      for(int i=0; i<=8; i++){                // shift data in the rate array
        rate[i] = rate[i+1];                  // and drop the oldest IBI value 
        runningTotal += rate[i];              // add up the 9 oldest IBI values
      }

      rate[9] = IBI;                          // add the latest IBI to the rate array
      runningTotal += rate[9];                // add the latest IBI to runningTotal
      runningTotal /= 10;                     // average the last 10 IBI values 
      BPM = 60000/runningTotal;               // how many beats can fit into a minute? that's BPM!
      QS = true;                              // set Quantified Self flag 
      // QS FLAG IS NOT CLEARED INSIDE THIS ISR
    }                       
  }

  if (Signal < thresh && Pulse == true){   // when the values are going down, the beat is over
    digitalWrite(blinkPin,LOW);            // turn off pin 13 LED
    Pulse = false;                         // reset the Pulse flag so we can do it again
    amp = P - T;                           // get amplitude of the pulse wave
    thresh = amp/2 + T;                    // set thresh at 50% of the amplitude
    P = thresh;                            // reset these for next time
    T = thresh;
  }

  if (N > 2500){                           // if 2.5 seconds go by without a beat
    thresh = 512;                          // set thresh default
    P = 512;                               // set P default
    T = 512;                               // set T default
    lastBeatTime = sampleCounter;          // bring the lastBeatTime up to date        
    firstBeat = true;                      // set these to avoid noise
    secondBeat = false;                    // when we get the heartbeat back
  }

  sei();                                   // enable interrupts when youre done!
}// end isr
// ----------------------------------------------------------------------------------

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);             // we agree to talk fast!
  EEPROM.begin(4096);
  //unsigned long int kunciii[5]={0x1234,0x5678,0x9abc,0xdef1,0x2345};
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
  interruptSetup();                 // sets up to read Pulse Sensor signal every 2mS 
}

void loop() {
  // put your main code here, to run repeatedly:
  if (!client.connected()) {
    reconnect();
  }
  if(!client.loop())
     /*
     YOU  NEED TO CHANGE THIS NEXT LINE, IF YOU'RE HAVING PROBLEMS WITH MQTT MULTIPLE CONNECTIONS
     To change the ESP device ID, you will have to give a unique name to the ESP8266.
     Here's how it looks like now:
       client.connect("ESP8266Client");
     If you want more devices connected to the MQTT broker, you can do it like this:
       client.connect("ESPOffice");
     Then, for the other ESP:
       client.connect("ESPGarage");
      That should solve your MQTT multiple connections problem

     THE SECTION IN recionnect() function should match your device name
    */
    client.connect("ESPpasien1");

///////////////////////// UKUR PULSE /////////////////////////
    //sendDataToProcessing('S', Signal);     // send Processing the raw Pulse Sensor data
    if (QS == true){                       // Quantified Self flag is true when arduino finds a heartbeat
      fadeRate = 255;                  // Set 'fadeRate' Variable to 255 to fade LED with pulse
      sendDataToProcessing('B',BPM);   // send heart rate with a 'B' prefix
      //sendDataToProcessing('Q',IBI);   // send time between beats with a 'Q' prefix
      QS = false;                      // reset the Quantified Self flag for next time    
      }
    ledFadeToBeat();
      
    delay(20);                             //  take a break
///////////////////////// UKUR PULSE /////////////////////////  
    
    //char denyut[11]="77.7";
    //client.publish("/esp8266/pulse",denyut);
    //Serial.println(denyut);
//    int malik = 88;
//  
//    unsigned long int m[]={0x0000,0x0000,0x0000,0x0000};
//    unsigned long int m1[]={0x0000,0x0000,0x0000,malik};
//    unsigned long int kuncipresent[5],kkk[]={0x1234,0x5678,0x9abc,0xdef1,0x2345};
//
//    for(int i=0;i<5;i++){
//      kuncipresent[i]=kkk[i];
//    }
//    encryptPRESENT(m1,kkk,31);
//    printf("encryption = \n");
//    printf("%.4x %.4x %.4x %.4x \n",m1[0],m1[1],m1[2],m1[3]);
//    String pesan = String(m1[0],HEX)+String(m1[1],HEX)+String(m1[2],HEX)+String(m1[3],HEX);
//    Serial.print("pesan : ");
//    Serial.println(pesan);
//    char pesan1[16];
//    pesan.toCharArray(pesan1,16);
//    client.publish("/esp8266/pulse",pesan1);
  
//    decryptPRESENT(m1,k, 31);
//  
//    printf("Decryption = \n");
//    printf("%.4x %.4x %.4x %d \n",m1[0],m1[1],m1[2],m1[3]);
//    int denyutterima = m1[3];
//    Serial.print("denyut : ");
//    Serial.println(denyutterima);
//    delay(5000);
}

// ------------------------ VOID UKUR PULSE ----------------------------------
void ledFadeToBeat(){
    fadeRate -= 15;                         //  set LED fade value
    fadeRate = constrain(fadeRate,0,255);   //  keep LED fade value from going into negative numbers!
    analogWrite(fadePin,fadeRate);          //  fade LED
  }


void sendDataToProcessing(char symbol, int data ){
    //Serial.print("Denyut jantung : ");
    //Serial.print(symbol);                // symbol prefix tells Processing what type of data is coming
    //Serial.println(data);                // the data to send culminating in a carriage return
    unsigned long int m[]={0x0000,0x0000,0x0000,0x0000};
    unsigned long int m1[]={0x0000,0x0000,0x0000,data};
    unsigned long int kuncipresent[5],kkk[]={0x1234,0x5678,0x9abc,0xdef1,0x2345};
    //unsigned long int kuncipresent[5]=kkk
//    Serial.print("Ru : ");
//    Serial.println(N1);
    if (N1 != "0"){
      int kunci5 = N1.toInt();
      //Serial.print("kkk[5] : ");
      //Serial.println(kunci5);
      kkk[4] = kunci5;
    }
//    for(int i=0;i<5;i++){
//      Serial.print(kkk[i]);
//    }
    encryptPRESENT(m1,kkk,31);
//    printf("encryption = \n");
//    printf("%.4x %.4x %.4x %.4x \n",m1[0],m1[1],m1[2],m1[3]);
    String pesan = padding(String(m1[0],HEX))+padding(String(m1[1],HEX))+padding(String(m1[2],HEX))+padding(String(m1[3],HEX));
    //String pesan = (padding(m1[0]),padding(m1[1]),padding(m1[2]),padding(m1[3]));
    //Serial.print("Denyut jantung enkripsi : ");
    //Serial.println(pesan);
    char pesan1[64];
    pesan.toCharArray(pesan1,64);
    client.publish("/esp8266/pasien1/pulse",pesan1);
    delay(1000);
  }
//------------------------------------------------------------------------------------

// --------------------- VOID READ/WRITE EEPROM ----------------------------------
void writeString(char add,String data)
{
  int _size = data.length();
  int i;
  for(i=0;i<_size;i++)
  {
    EEPROM.write(add+i,data[i]);
  }
  EEPROM.write(add+_size,'\0');   //Termination null character dari String Data
  EEPROM.commit();
}
 
String read_String(char add)
{
  int i;
  char data[100]; //Max 100 Bytes
  int len=0;
  unsigned char k;
  k=EEPROM.read(add);
  while(k != '\0' && len<500)   //Baca sampai null character
  {    
    k=EEPROM.read(add+len);
    data[len]=k;
    len++;
  }
  data[len]='\0';
  return String(data);
}

String padding (String m1){
  //String m = String(m1,HEX);
  while (m1.length()!=4){
    m1 = "0"+m1;
  }
  return m1;
}

String bilacak(){
  long n=0;
  Serial.print("Rs : ");
  n = random(100000,1000000);
  Serial.println(n);
  String acak = String(n);
  return acak;
}

//unsigned long int kunciPRESENT(String kunci){
//  kunci = kunci.substring(0,16);
//  for (int i=0; i<kunci.length(); i++){
//  
//  }
//  int z=a.length()+1;
//  char c1[z];
//  char c2[z];
//  String temp1;
//  String temp2;
//  String hasil;
//  int d;
//  int e;
//  int state;
//  while(a.length()!=b.length()){
//    b="0"+b;
//  }
//  a.toCharArray(c1,(z));
//  b.toCharArray(c2,(z));
//  for(int i=0;i<z;i++){
//    temp1=c1[i];
//    Serial.print(temp1);
//    temp2=c2[i];
//    state=hextoint(temp1)^hextoint(temp2);
//    hasil=hasil+String(state,HEX);
//}
// ---------------------------------------------------------------------------------
