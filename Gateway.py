# SAMPE SETTING DATA REGIST SENSOR

# SUDAH BISA APP GANTIPASSWORD,DENYUT JANTUNG (alhamdulillah)

from flask import Flask, request, render_template, jsonify, make_response
from datetime import datetime
import paho.mqtt.client as mqtt
import random
import json
import collections
import collections.abc
import paho.mqtt.subscribe as subscribe
import paho.mqtt.publish as publish
import mysql.connector as mariadb
import random
import string
import socket
import hashlib
import time
from json import dumps

mariadb_connection = mariadb.connect(user='pi', password='raspberry', database='user', host='localhost')
cursor = mariadb_connection.cursor()


app = Flask(__name__)	

@app.route('/')
def hello_world():
	return 'Hello World'

@app.route('/register',methods = ['POST'])
def abc():
	if request.method=='POST':
		hasil=request.form
		ID=request.form['name']
		HPW=request.form['password']
		IMEI=request.form['IMEI']
		
		CID = cid(ID)
		GID = gid()    
		Gj = gj()    
		A1=a1(GID,Gj,HPW,CID)
		A2=a2(ID,Gj,HPW)

		command="INSERT INTO user2_tabel (name,password,IMEI,CID,GID,Gj) \
				 VALUES ('"+ID+"','"+HPW+"','"+IMEI+"','"+CID+"','"+GID+"','"+Gj+"')"

		try:
			cursor.execute(command)
			mariadb_connection.commit()
			cursor.execute("SELECT * FROM user2_tabel WHERE name='"+ID+"'")
			cursor.fetchall()
			A={}
			if cursor.rowcount>0:
				print("ID :"+ID)
				print("CID : "+CID)
				print("GID : " + GID)
				print("Gj : " + Gj)
				print("HPW : "+HPW)
				print("A1 : "+A1)
				print("A2 : "+A2)
				A['sukses']="success"
				A['nama']=ID
				A['A1']=A1
				A['A2']=A2
				A['CID']=CID
				A['GID']=GID
				print("SUKSES REGISTRASI "+ID+"!")
				return jsonify(A)
			else:
				A['sukses']="gagal"
				print("GAGAL REGISTRASI DOKTER!")
				return jsonify(A)
		except mariadb.Error as error:
			print("GAGAL REGISTRASI DOKTER!")
			respon="Error!: {}".format(error)
			return command
def gj():
  cpuserial = "0000000000000000"
  try:
    f = open('/proc/cpuinfo','r')
    for line in f:
      if line[0:6]=='Serial':
        cpuserial = line[10:26]
    f.close()
  except:
    cpuserial = "ERROR000000000"
  return cpuserial

def gid():
	GID = socket.gethostname()
	GID = GID+"1"
	return GID 
	
def cid(ID):
	now = datetime.now()
	waktu = now.strftime("%d/%m/%Y %H:%M:%S")
	inputhash1=ID+waktu
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	return outputhash1

def sha256(inputhash):
    outputhash = \
        hashlib.sha256(inputhash.encode()).hexdigest()
    return outputhash

def a1(GID, Gj, HPW, CID):
	inputhash=CID+GID+Gj
	outputhash = \
		hashlib.sha256(inputhash.encode()).hexdigest()
	A1 = xorString(outputhash, HPW)
	return A1

def a2(ID,Gj,HPW):
	h1=ID+Gj
	h2=ID+HPW
	outputhash1 = \
		hashlib.sha256(h1.encode()).hexdigest()
	outputhash2 = \
		hashlib.sha256(h2.encode()).hexdigest()
	A2=xorString(outputhash1, outputhash2)
	return A2

def a3(ID,PW,IMEI):
	h1=ID+PW
	outputhash1 = \
		hashlib.sha256(h1.encode()).hexdigest()
	outputhash11=''.join(str(ord(c)) for c in outputhash1)
	outputhash11=outputhash11[:15]
	outputhash11=int(outputhash11)
	IMEI=int(IMEI)
	A3=outputhash11^IMEI
	A3=str(A3)
	return A3

def xorString(string1, string2):
	panjang1 = len(string1)
	panjang2 = len(string2)
	if (panjang1>panjang2):
		for i in range (panjang1-panjang2):
			string2 = '0'+string2
	elif(panjang1<panjang2):
		for i in range (panjang2-panjang1):
			string1 = '0'+string1
	out2 = ""
	for j in range (panjang1):
		int1 = int(string1[j:j+1],16)
		#print(str(int1))
		int2 = int(string2[j:j+1],16)
		out = int1^int2
		out = str(out)
		if(out == "10"):
			out = "a"
		elif(out == "11"):
			out = "b"
		elif(out == "12"):
			out = "c"
		elif(out == "13"):
			out = "d"
		elif(out == "14"):
			out = "e"
		elif(out == "15"):
			out = "f"
		out2 += out
	return out2

@app.route('/login',methods = ['POST'])

def login():
	def on_connect(client, userdata, flags, rc):
		print("Connected with result code "+str(rc))
		# Subscribing in on_connect() - if we lose the connection and
		# reconnect then subscriptions will be renewed.
		client.subscribe("/esp8266/pasien1/pulse")
		client.subscribe("/esp8266/pasien2/pulse")
		client.subscribe("/esp8266/pasien1/B89")
		client.subscribe("/esp8266/pasien1/B8")
		client.subscribe("/esp8266/pasien1/B9")
		client.subscribe("/esp8266/pasien2/B89")
		client.subscribe("/esp8266/pasien2/B8")
		client.subscribe("/esp8266/pasien2/B9")

		# The callback for when a PUBLISH message is received from the server.
	def on_message(client, userdata, msg):
		for i in range (60):
			if msg.topic == "/esp8266/pasien1/pulse":
				# msgtopic = str(msg.topic)
				pulse = msg.payload.decode()
				now = datetime.now()
				waktu = now.strftime("%d/%m/%Y %H:%M:%S")
				i = str(i)
				sql = "INSERT INTO pasien1 (id, denyut, waktu) VALUES ('"+i+"','"+pulse+"','"+waktu+"')"
				cursor.execute(sql)
				mariadb_connection.commit()
				i = int(i)
				sql2 = ("SELECT * FROM pasien1")
				cursor.execute(sql2)
				hasil = cursor.fetchall()
				for row in hasil:
					nomor = row[0]
				
				nomor = str(nomor)
				if nomor == "60" or 60:
					# i = int(i)
					sql3 = "DELETE FROM pasien1"
					cursor.execute(sql3)
					mariadb_connection.commit()
					i = 0
				
			if msg.topic == "/esp8266/pasien2/pulse":
				# msgtopic = str(msg.topic)
				pulse = msg.payload.decode()
				now = datetime.now()
				waktu = now.strftime("%d/%m/%Y %H:%M:%S")
				i = str(i)
				sql = "INSERT INTO pasien2 (id, denyut, waktu) VALUES ('"+i+"','"+pulse+"','"+waktu+"')"
				cursor.execute(sql)
				mariadb_connection.commit()
				i = int(i)
				sql2 = ("SELECT * FROM pasien2")
				cursor.execute(sql2)
				hasil = cursor.fetchall()
				for row in hasil:
					nomor = row[0]
				
				nomor = str(nomor)
				if nomor == "60" or 60:
					# i = int(i)
					sql3 = "DELETE FROM pasien2"
					cursor.execute(sql3)
					mariadb_connection.commit()
					i = 0

	client = mqtt.Client()
	client.on_connect = on_connect
	client.on_message = on_message
	client.connect("mqtt.eclipse.org", 1883, 60)
	client.loop_start()# Create an MQTT client and attach our routines to it.
			
	if request.method == 'POST':
		hasil = request.form
		B2_get=request.form['B2']
		B3_get=request.form['B3']
		B4_get=request.form['B4']
		CID_get=request.form['CID']
		print("CID terima : "+CID_get)
		SID_get=request.form['SID']
		GID_get=request.form['GID']

		datapasien = ("SELECT * FROM pasien_tabel WHERE SID='"+SID_get+"'")
		cursor.execute(datapasien)
		hasilpasien = cursor.fetchall()
		for row in hasilpasien:
			SID = row[1]
			SGk = row[2]

		GID = gid()
		print("SID terima = "+SID_get)
		print("SID simpan = "+SID)
		print("GID terima = "+GID_get)
		print("GID simpan = "+GID)
		if SID_get == SID and GID_get == GID:
			print("SID dan GID valid!")
			begin = time.time()
			Gj = gj()
			B1 = b1(CID_get, GID_get, Gj)
			N1 = n1(B1, B2_get)	
			ID_hitung = id(B3_get, N1, B1)
			B4 = b4(CID_get, GID_get, B1, ID_hitung, N1)
			print("ID terima = "+ID_hitung)
			print("B4 terima = "+B4_get)
			print("B4 hitung = "+B4)
			cursor.execute("SELECT * FROM user2_tabel WHERE name='"+ID_hitung+"'")
			cursor.fetchall()
			A={}
			if cursor.rowcount>0 and B4==B4_get:
				print("ID dan B4 valid!")
				ID = ID_hitung
				N2random = random.randrange(1, 1000000)
				N2 = str(N2random)
				print("Rg : "+N2)
				SGkN2 = SGk+N2
				N1N2 = N1+N2
				print("SGK : "+SGk)
				B5 = b5(SGk, GID, N1)
				B6 = b6(N1, N2)
				B7 = b7(SGk, N1, N2)
				print("B7 : "+B7)
				client.publish("esp8266/"+SID+"/B5",B5)
				client.publish("esp8266/"+SID+"/B6",B6)
				client.publish("esp8266/"+SID+"/B7",B7)
				client.loop_start()
		
# ########### COMMENT UNTUK MATIKAN DATA DARI NODEMCU ##########	
				# time.sleep(2)
# 				msg1 = subscribe.simple("/esp8266/"+SID+"/B8", hostname="mqtt.eclipse.org")
# # 				print("%s %s" % (msg.topic, msg.payload))
# 				B8terima = msg1.payload.decode()
# 				B8terima = str(B8terima)
# 				B8terima = B8terima[:64]
				msg2 = subscribe.simple("/esp8266/"+SID+"/B9", hostname="mqtt.eclipse.org")
# 				print("%s %s" % (msg.topic, msg.payload))
				B89terima = msg2.payload.decode()
				B89terima = str(B89terima)
				B8terima = B89terima[:15]
				B8terima = str(B8terima)
				B9terima = B89terima[15:len(B89terima)]
				print("B8 terima : "+B8terima)
				N3 = n3(B8terima, SGkN2)
				B9 = b9(N1N2, N3, SID, GID)
############################################################
			####### PENGGANTI DATA DARI NODEMCU (-) N3 tidak acak
				# N3 = "639574"  
				# B8 = b8(SGk, N2, N3)
				# print("B8 : "+B8)
				# N3 = n3(B8, SGkN2)
				# B9 = b9(N1N2, N3, SID, GID)
				# print("B9 terima : "+B9)
			#####################################################
				print("B9 terima : "+B9terima)	
				print("B9 hitung : "+B9)
				if B9 == B9terima:
					print("B9 valid!")
					#CIDbaru=randomString(10)
					CIDbaru = cid(ID)
					B10=b10(CIDbaru, GID, Gj, N1, CID_get)
					B11=b11(ID, N1, N2)
					B12=b12(N1, N2, N3)
					B13=b13(ID, Gj, N3, CIDbaru)
					B14=b14(N1, N2, N3, ID, B10, CIDbaru)
					end = time.time()
					print("CIDbaru = "+CIDbaru)
					print("B14 = "+B14)
					A['sukses']="SUKSES LOGIN"
					A['name']= ID
					A['B10'] = B10
					A['B11'] = B11
					A['B12'] = B12
					A['B13'] = B13
					A['B14'] = B14
					A['sukses'] = "SUKSES LOGIN"
					print("SUKSES LOGIN")
					print(f"Performa Waktu Gateway : {(end-begin)*1000000} mikrodetik")
					return jsonify(A)
				else:
					print("B9 tidak valid!")
			else:
				print("ID atau B4 tidak valid!")
		else:
			print("SID atau GID tidak valid!")
	else:
		A['name'] = ID
		return jsonify(A) 
		print("GAGAL LOGIN!") 

def cid(ID):
	now = datetime.now()
	waktu = now.strftime("%d/%m/%Y %H:%M:%S")
	inputhash1=ID+waktu
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	return outputhash1
	
def xorString(string1, string2):
	panjang1 = len(string1)
	panjang2 = len(string2)
	if (panjang1>panjang2):
		for i in range (panjang1-panjang2):
			string2 = '0'+string2
	elif(panjang1<panjang2):
		for i in range (panjang2-panjang1):
			string1 = '0'+string1
	out2 = ""
	for j in range (panjang1):
		int1 = int(string1[j:j+1],16)
		#print(str(int1))
		int2 = int(string2[j:j+1],16)
		out = int1^int2
		out = str(out)
		if(out == "10"):
			out = "a"
		elif(out == "11"):
			out = "b"
		elif(out == "12"):
			out = "c"
		elif(out == "13"):
			out = "d"
		elif(out == "14"):
			out = "e"
		elif(out == "15"):
			out = "f"
		out2 += out
	return out2

def gid():
	GID = socket.gethostname()
	GID = GID+"1"
	return GID

def gj():
  cpuserial = "0000000000000000"
  try:
    f = open('/proc/cpuinfo','r')
    for line in f:
      if line[0:6]=='Serial':
        cpuserial = line[10:26]
    f.close()
  except:
    cpuserial = "ERROR000000000"
  return cpuserial

def b1(CID, GID, Gj):
	inputhash1=CID+GID+Gj
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	return outputhash1

def n1(B1, B2_get):
	N1 = xorString(B1, B2_get)
	N1 = N1[58:64]
	print("Ru : "+N1)
	return N1

def id(B3_get, N1, B1):
	inputhash2 = N1+B1
	outputhash2 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	ID = xorString(B3_get, outputhash2)
	length = len(ID)
	output = ""
	num = 0
	for i in range(length):
		num = num * 10 + (ord(ID[i]) - ord('0'))
		if (num>=32 and num <=122):
			ch = chr(num)
			#print(ch, end = "")
			output += ch
			num = 0
	ID = output	
	#print("ID terima : "+ID)
	return ID

def b4(CID, GID, B1, ID, N1):
	inputhash = CID+GID+B1+ID+N1
	B4 = \
		hashlib.sha256(inputhash.encode()).hexdigest()
	return B4

def b5(SGk, GID, N1):
	inputhash1=SGk+GID
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B5 = xorString(outputhash1, N1)
	print("B5 : "+B5)
	return B5

def b6(N1, N2):
	inputhash1=N1
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B6 = xorString(outputhash1, N2)
	print("B6 : "+B6)
	return B6

def b7(SGk, N1, N2):
	inputhash1=SGk+N1+N2
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B7=outputhash1
	return B7

def n3(B8terima, SGkN2):
	inputhash1=SGkN2
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	outputhash1=outputhash1[:15]
	N3 = xorString(B8terima, outputhash1)
	N3 = N3[9:15]
	print("Rs : "+N3)
	return N3

def b8(SGk, N2, N3):
	inputhash1=SGk+N2
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B8 = xorString(outputhash1, N3)
	return B8

def b9(N1N2, N3, SID, GID):
	inputhash = N1N2+N3
	SK2 = \
		hashlib.sha256(inputhash.encode()).hexdigest()
	print("Kunci Sesi Gateway (SKg) : "+SK2)
	inputhash2 = SK2+SID+GID+N3
	B9 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	return B9

def b10(CIDbaru, GID, Gj, N1, CID_get):
	inputhash1 = CIDbaru+GID+Gj
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	inputhash2 = N1+CID_get
	outputhash2 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	B10 = xorString(outputhash1, outputhash2)
	return B10

def b11(ID, N1, N2):
	inputhash1=N1+ID
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B11 = xorString(outputhash1, N2)
	return B11

def b12(N1, N2, N3):
	inputhash1=N1+N2
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B12 = xorString(outputhash1, N3)
	return B12

def b13(ID, Gj, N3, CIDbaru):
	inputhash1=ID+Gj
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	inputhash2=outputhash1+N3
	outputhash2 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	B13 = xorString(outputhash2, CIDbaru)
	B13=str(B13)
	return B13

def b14(N1, N2, N3, ID, B10, CIDbaru):
	inputhash1=N1+N2+N3
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	inputhash2=outputhash1+ID+B10+CIDbaru
	outputhash2 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	B14=outputhash2
	return B14

@app.route('/registerSensor',methods = ['POST'])

def registerSensor():
	client = mqtt.Client()
	client.connect("mqtt.eclipse.org", 1883, 60)
	client.loop_start()# Create an MQTT client and attach our routines to it.
	
	if request.method == 'POST':
		SID = request.form['SID']
		GID = gid()    
		Gj = gj()        
		nomor = SID[6:7]
		print("id pasien : "+str(nomor))
		SGk = sgk(SID, Gj, nomor)
		command= ("INSERT INTO pasien_tabel (id, SID, SGk) VALUES ('"+nomor+"','"+SID+"','"+SGk+"')") 
		try:
			cursor.execute(command)
			mariadb_connection.commit()
			A={}
			if cursor.rowcount>0:
				client.publish("esp8266/"+SID+"/SGk",SGk)
				#time.sleep(2)
				client.publish("esp8266/"+SID+"/GID",GID)
				#time.sleep(2)
				client.publish("esp8266/"+SID+"/SNk",SID)
				print("Berhasil REGISTRASI "+SID+"!")
				A['sukses']="success"
				A['SID']=SID
				A['GID']=GID
				return jsonify(A)
			else:
				A['sukses']="gagal"
				print("GAGAL REGISTRASI PASIEN!")
				return jsonify(A)
		except mariadb.Error as error:
			print("GAGAL REGISTRASI PASIEN!")
			print("Error: {}".format(error))

def gj():
  cpuserial = "0000000000000000"
  try:
    f = open('/proc/cpuinfo','r')
    for line in f:
      if line[0:6]=='Serial':
        cpuserial = line[10:26]
    f.close()
  except:
    cpuserial = "ERROR000000000"
  return cpuserial

def gid():
	GID = socket.gethostname()
	GID = GID+"1"
	return GID 

def sgk(SID, Gj, nomor):
	inputhash=SID+Gj+nomor
	outputhash = hashlib.sha256(inputhash.encode()).hexdigest()
	SGk = str(outputhash)
	return SGk 

@app.route('/ganti_password',methods = ["POST"])
def ganti_password():
	if request.method == 'POST':
		B2_get=request.form['B2']
		B3_get=request.form['B3']
		B15_get=request.form['B15']
		CID_get=request.form['CID']
		GID_get=request.form['GID']

		Gj = gj()
		B1=b1(CID_get, GID_get, Gj)
		Ru=ru(B1,B2_get)
		ID_get=id_get(B3_get,Ru,B1)
		cursor.execute("SELECT * FROM user2_tabel WHERE name='"+ID_get+"'")
		cursor.fetchall()
		print("ID terima : "+ID_get)
		# print("ID simpan : "+ID)
		A={}
		if cursor.rowcount>0:
			print("ID valid!")
			ID = ID_get
			B15=b15(CID_get, GID_get, B1, ID, Ru)
			print("B15 terima : "+B15_get)
			print("B15 simpan : "+B15)
			if B15 == B15_get:
				print("B15 valid!")
				CIDbaru=cid(ID)
				B16=b16(CIDbaru,GID_get,Gj,Ru,CID_get)
				B17=b17(ID,Gj,Ru,CIDbaru)
				B18=b18(ID,CID_get,CIDbaru,B1,B16)
				A['sukses'] = "SUKSES GANTI PASSWORD"
				A['name']= ID
				A['B16']= B16
				A['B17']= B17
				A['B18']= B18
				A['CIDbaru'] = CIDbaru
				return jsonify(A)
			else:
				print("B15 tidak valid!")
				A['sukses'] = "B15 tidak valid!"
				return jsonify(A)				
		else: 
			print("ID tidak valid")
			A['sukses'] = "ID tidak valid!"
			return jsonify(A)

def xorString(string1, string2):
	panjang1 = len(string1)
	panjang2 = len(string2)
	if (panjang1>panjang2):
		for i in range (panjang1-panjang2):
			string2 = '0'+string2
	elif(panjang1<panjang2):
		for i in range (panjang2-panjang1):
			string1 = '0'+string1
	out2 = ""
	for j in range (panjang1):
		int1 = int(string1[j:j+1],16)
		#print(str(int1))
		int2 = int(string2[j:j+1],16)
		out = int1^int2
		out = str(out)
		if(out == "10"):
			out = "a"
		elif(out == "11"):
			out = "b"
		elif(out == "12"):
			out = "c"
		elif(out == "13"):
			out = "d"
		elif(out == "14"):
			out = "e"
		elif(out == "15"):
			out = "f"
		out2 += out
	return out2
	
def gj():
  cpuserial = "0000000000000000"
  try:
    f = open('/proc/cpuinfo','r')
    for line in f:
      if line[0:6]=='Serial':
        cpuserial = line[10:26]
    f.close()
  except:
    cpuserial = "ERROR000000000"
  return cpuserial

def b1(CID,GID,Gj):
	inputhash1=CID+GID+Gj
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B1 = outputhash1
	print("B1 : "+B1)
	return B1

def ru(B1,B2_get):
	Ru = xorString(B1, B2_get)
	Ru = Ru[58:64]
	print("Ru : "+Ru)
	return Ru

def id_get(B3_get,Ru,B1):
	inputhash1=Ru+B1
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	ID = xorString(B3_get, outputhash1)
	length = len(ID)
	output = ""
	num = 0
	for i in range(length):
		num = num * 10 + (ord(ID[i]) - ord('0'))
		if (num>=32 and num <=122):
			ch = chr(num)
			#print(ch, end = "")
			output += ch
			num = 0
	ID_get = output 	
	return ID_get

def b15(CID,GID,B1,ID,Ru):
	inputhash1=CID+GID+B1+ID+Ru
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	B15=outputhash1
	return B15

def cid(ID):
	now = datetime.now()
	waktu = now.strftime("%d/%m/%Y %H:%M:%S")
	inputhash1=ID+waktu
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	return outputhash1

def b16(CIDbaru,GID,Gj,Ru,CID):
	inputhash1=CIDbaru+GID+Gj
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	inputhash2=Ru+CID
	outputhash2 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	B16 = xorString(outputhash1, outputhash2)
	print("B16 : "+B16)
	return B16

def b17(ID,Gj,Ru,CIDbaru):
	inputhash1=ID+Gj
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	inputhash2=outputhash1+Ru
	outputhash2 = \
		hashlib.sha256(inputhash2.encode()).hexdigest()
	B17 = xorString(outputhash2, CIDbaru)
	print("B17 : "+B17)
	return B17

def b18(ID,CID,CIDbaru,B1,B16):
	inputhash1=ID+CID+CIDbaru+B1+B16
	outputhash1 = \
		hashlib.sha256(inputhash1.encode()).hexdigest()
	print("B18 : "+outputhash1)
	return outputhash1

@app.route('/denyut',methods = ['POST'])
def ukur_denyut():
	SID = request.form['SID']
	print("SID : "+SID)
	denyut = ("SELECT * FROM "+SID+"")
	cursor.execute(denyut)
	rows = cursor.fetchall()
	objects_list = []
	for row in rows:
		d = collections.OrderedDict()
		d['id'] = row[0]
		d['denyut'] = row[1]
		d['waktu'] = row[2]
		objects_list.append(d)
	
	j = make_response(json.dumps({'semuaDenyut':objects_list}))
	print (j)

	return j

if __name__ == '__main__':
   app.run(debug=True, port=1234, host='0.0.0.0')