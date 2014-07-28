import os
import time
import XMPP
from threading import Thread

'''status definition'''
#Person
Nobody = 'Nobody'
Child = 'Child'
Adult = 'Adult'
#Context
Normal = 'Normal'
Cooking = 'Cooking'
#Distance
Far = 'Far'
Near ='Near'
#Mode
Before = 'Before'
After = 'After'
#state
Off = 0
On = 1

'''Variable:'''
class Actuator_State:
	Lock = 1
	LED = 1
	Alarm = 0
class Context_State:
	Person = Nobody
	Distance = Far
	Context = Normal
	mode = Before
	thisCookingTime=0
	totalCookingTime=0
	context_Cooking= False
class Mode:
	name = Before
	session_TimeOut_Normal=500
	session_TimeOut_Cooking=500
	length_of_CookingTime=500
C = 0
distance_threshold = 10

'''function'''
def Setting_Mode(name,session_TimeOut_Normal,session_TimeOut_Cooking,length_of_CookingTime):
	New_Mode = Mode()
	New_Mode.name = name
	New_Mode.session_TimeOut_Normal = session_TimeOut_Normal
	New_Mode.session_TimeOut_Cooking = session_TimeOut_Cooking
	New_Mode.length_of_CookingTime = length_of_CookingTime
	return New_Mode
XMPP_Client = XMPP()
input = [0,0,0,0]
def Context_Change(XMPP_state,Current_State):
	if XMPP_state.Person == Current_State.Person:
		if XMPP_state.Distance == Current_State.Distance:
			if XMPP_state.Context == Current_State.Context:
				if XMPP_state.mode == Current_State.mode:
					return 0
				else:
					return 1
			else:
				return 1
		else:
			return 1
	else:
		return 1
def Context_Update(Previous,Current):
	Previous.Person = Current.Person
	Previous.Distance = Current.Distance
	Previous.Context = Current.Context
	Previous.mode = Current.mode
	return Previous

def XMPP_Scribe():
	XMPP_Client.XMPPsubscribe_DEMO('/Device/type/profile/IR')
			#XMPP_State.Context = raw_input('Context[Normal|Cooking]:')
		#XMPP_State.mode = raw_input('mode[Before|After]:')
def Get_XMPP_State():
	height1 = int(XMPP_Client.input[0])
	height2 = int(XMPP_Client.input[1])
	height3 = int(XMPP_Client.input[2])
	distance = int(XMPP_Client.input[3])
	if height1 >= 4 or height2 >=4 or height3 >=4:
		Person_result = Adult
	elif height1 ==1 and height2 ==1 and height3 ==1:
		Person_result = Nobody
	else:
		Person_result =Child
	if distance <= distance_threshold:
		Distance_result = Near
	else:
		Distance_result = Far
	XMPP_State.Person = Person_result
	XMPP_State.Distance = Distance_result

def Send_ActuatorState_To_XMPP(State):
	print 'Lock:',State.Lock,',',
	print 'LED:',State.LED,',',
	print 'Alarm:',State.Alarm
	Value = str(State.Lock) + ',' + str(State.LED) + ',' + str(State.Alarm)
	XMPP_Client.XMPPpublish('/Device/type/profile/feedback','2','3',Value,'5','6','7','8')

'''main'''

XMPP_State = Context_State()
Current_State = Context_State()
Previous_State = Context_State()
Actuator_Control_State = Actuator_State()
Current_Mode = Mode()
Before_Mode = Mode()
After_Mode = Mode()
Before_Mode = Setting_Mode('Before', 10, 20, 30)
After_Mode = Setting_Mode('After',500, 60000, 600000)
Time_Lock = 0
enter_time = 0
leaving_time = 0
start_time = 0
end_time = 0
count_time = 0
total_time = 0
XMPP_thread = Thread(target=XMPP_Scribe, args=())
#XMPP_Client.XMPPinit('pub','pub','192.168.4.101','5222','pubsub.wukong.ccc.ntu.edu.tw')
XMPP_Client.XMPPinit('pub','pub','192.168.0.103','5222','pubsub.wukong.ccc.ntu.edu.tw')
Send_ActuatorState_To_XMPP(Actuator_Control_State)
XMPP_thread.start()
time.sleep(2)
Current_State.Context = Cooking
while(True):
	time.sleep(0.1)
	print Current_State.Person, Current_State.Distance
	#Check context status
	if Time_Lock == 1:
		if Current_State.Context == Normal:
			if time.time() >= end_time:
				Actuator_Control_State.Lock = 1
				Actuator_Control_State.LED = Actuator_Control_State.Lock
				Send_ActuatorState_To_XMPP(Actuator_Control_State)
				Time_Lock = 0
		elif Current_State.Context == Cooking:
			if time.time() >= end_time:
				Actuator_Control_State.Lock = 1
				Actuator_Control_State.LED = Actuator_Control_State.Lock
				Send_ActuatorState_To_XMPP(Actuator_Control_State)
				Time_Lock = 0
			if Current_State.Person == Nobody:
				count_time = leaving_time - enter_time
				if count_time >= total_time:
					Actuator_Control_State.Lock = 1
					Actuator_Control_State.LED = Actuator_Control_State.Lock
					Send_ActuatorState_To_XMPP(Actuator_Control_State)
					Time_Lock = 0
	#Check if the Context is changed
	#XMPP_State = Get_XMPP_State(XMPP_State)
	Get_XMPP_State()
	if Context_Change(XMPP_State,Current_State) == True:
		Previous_State = Context_Update(Previous_State,Current_State)
		Current_State = Context_Update(Current_State,XMPP_State)
		print 'change'
	else:
		#print 'the same'
		continue
	
	# Check if the Context is Cooking
	if Current_State.Context == Normal:
		Current_State.context_Cooking = False
		Current_State.totalCookingTime = 0
	elif Current_state.Context_State == Cooking:
		Current_State.context_Cooking = True
	
	#Setting Current Mode
	if Current_State.mode == Before:
		Current_Mode = Before_Mode
	elif Current_State.mode == After:
		Current_Mode = After_Mode

	
	#Current person is Nobody
	if Current_State.Person == Nobody:
		print 'Current person is nobody'
		leaving_time = time.time()
		if Previous_State.Person == Adult:
			if Current_State.Context == Normal:
				print 'Current Context is Normal, the cabinet will be locked after',  Current_Mode.session_TimeOut_Normal , 'seconds'
				start_time = leaving_time
				end_time = start_time + Current_Mode.session_TimeOut_Normal
				Time_Lock = 1
				print 'Normal'
			elif Current_State.Context == Cooking and Time_Lock == 0:
				print 'Current Context is Cooking, the cabinet will be locked after', Current_Mode.session_TimeOut_Cooking, 'seconds' 
				count_time = 0
				start_time = enter_time
				total_time = Current_Mode.session_TimeOut_Cooking
				end_time = enter_time + Current_Mode.length_of_CookingTime
				Time_Lock = 1
				print 'Cooking'
		Actuator_Control_State.Alarm = 0
	
	#Current person is Adult
	elif Current_State.Person == Adult:
		print 'Current person is adult'
		if Current_State.Distance == Near:
			Actuator_Control_State.Lock = 0
			if Actuator_Control_State.Alarm ==1:
				Actuator_Control_State.Alarm = 0
		#if Current_State.Distance == Far and Current_State.Context != Cooking:
			#Actuator_Control_State.Lock = 1
		enter_time = time.time()


	#Current person is Child
	elif Current_State.Person == Child:
		#Child just enters
		print 'Current person is child'
		if Previous_State.Person != Child:
			if Actuator_Control_State.Lock == 0:
				Actuator_Control_State.Lock =1
		if Current_State.Distance == Near:
			Actuator_Control_State.Alarm =1
		elif Current_State.Distance == Far:
			Actuator_Control_State.Alarm = 0
	else:
		print "error"
	Actuator_Control_State.LED = Actuator_Control_State.Lock
	Send_ActuatorState_To_XMPP(Actuator_Control_State)




