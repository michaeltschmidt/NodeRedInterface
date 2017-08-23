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

	tiles(scale: 2) {
        // standard tile with actions named
   		standardTile("actionFlat", "device.switch", width: 2, height: 2, decoration: "flat") {
    		state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    		state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
	}
        // value tile (read only)
        valueTile("status", "device.myattribute", decoration: "flat", width: 6, height: 2) {
            state "status", label:'${currentValue}'
        }

        // the "switch" tile will appear in the Things view
        main(["actionFlat","status"])
    }
}

// parse events into attributes
def parse(String description) {
    def msg = parseLanMessage(description)
    log.debug msg
    def json = msg.json
    sendEvent(name: "myattribute",  value: json.cmd, isStateChange: true)
}

// handle commands
def on() {
	log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on", isStateChange: true)
    myCmd("ON")
}

def off() {
	log.debug "Executing 'off'"
    sendEvent(name: "switch", value: "off", isStateChange: true)
    myCmd("OFF")
}

def myCmd(cmd)
{
    def host = "192.168.1.100" 
    def port = "1880"
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(port)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    def headers = [:] 
    headers.put("HOST", "$host:$port")
 	
    def path = "/test/?cmd=$cmd"
 
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
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}