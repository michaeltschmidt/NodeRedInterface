/**
 *  MikeTest
 *
 *  Copyright 2017 Mike Schmidt
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "NodeRedInterface", namespace: "michaeltschmidt", author: "Mike Schmidt") {
		capability "Sensor"
		capability "Switch"

		attribute "myattribute", "string"

		command "mycommand"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    
    preferences{
        input("deviceIP", "string", title:"IP Address", description: "IP Address", required: true, displayDuringSetup: true)
        input("devicePort", "string", title:"Port", description: "Port", defaultValue: 1880 , required: true, displayDuringSetup: true)
        input("uri", "string", title:"NodeRed API Path", description: "/myAPIPath/", displayDuringSetup: true)
        input("oncmd", "string", title:"NodeRed JSON ON Command", description: "cmd=mycommand&val=myval&etc.", required: true, displayDuringSetup: true)
        input("offcmd", "string", title:"NodeRed JSON OFF Command", description: "cmd=mycommand&val=myval&etc.", required: true, displayDuringSetup: true)
	}
    
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
    		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        		attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
        		attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
        		attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
        		attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
    		}
            tileAttribute ("device.myattribute", key: "SECONDARY_CONTROL") {
        		attributeState "default", label:'${currentValue}'
    		}
		}

        main(["switch"])
    }
}

def parse(String description) {
    def msg = parseLanMessage(description)
    log.debug msg
    def json = msg.json
    sendEvent(name: "myattribute",  value: json.cmd)
}

def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on", isStateChange: true)
    myCmd(settings['oncmd'])
}

def off() {
	log.debug "Executing 'off'"
    sendEvent(name: "switch", value: "off", isStateChange: true)
    myCmd(settings['offcmd'])
}

def myCmd(cmd)
{
    def host = (settings['deviceIP'])
    def port = (settings['devicePort'])
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    def headers = [:] 
    headers.put("HOST", "$host:$port")
 	
    def path = "/" + (settings['uri']) + "/?cmd=$cmd"
 
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
    	path: path,
    	headers: headers
        )    	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
}
  
private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex.toUpperCase()
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport.toUpperCase()
}