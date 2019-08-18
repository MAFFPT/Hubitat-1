/**
 *  ****************  App Watchdog 2 Parent ****************
 *
 *  Design Usage:
 *  See if any of the compatible apps need an update, all in one place.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *
 */

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "AppWatchdogParentVersion"
	state.version = "v2.0.0"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
            schedule("0 0 3 ? * * *", setVersion)
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name:"App Watchdog 2",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "See if any of the compatible apps need an update, all in one place.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    importUrl: ""
)

preferences {
     page name: "mainPage", title: "", install: true, uninstall: true
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def uninstalled() {                  // Modified from @Stephack
    childDevices.each { deleteChildDevice(it.deviceNetworkId) }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "There are ${childApps.size()} child apps"
    createVirtualDevice()
    childApps.each {child ->
    	log.info "Child app: ${child.label}"
    }
}

def mainPage() {					// Modified from @Cobra Code
    dynamicPage(name: "mainPage") {
    	installCheck()
		if(state.appInstalled == 'COMPLETE'){
			section(getFormat("title", "${app.label}")) {
				paragraph "<div style='color:#1A77C9'>See if any of the compatible apps need an update, all in one place.</div>"
				paragraph getFormat("line")
			}
			section("Instructions:", hideable: true, hidden: true) {
				paragraph "<b>Information</b>"
				paragraph "See if any of the compatible apps need an update, all in one place."
                paragraph "Note: This will only track apps, not drivers. Hopefully at some point it will also track drivers. Thanks"
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
				app(name: "anyOpenApp", appName: "App Watchdog 2 Child", namespace: "BPTWorld", title: "<b>Add a new 'App Watchdog 2' child</b>", multiple: true)
			}
            // ** App Watchdog Code **
            section("This app supports App Watchdog! Click here for more Information", hideable: true, hidden: true) {
				paragraph "<b>Information</b><br>See if any compatible app needs an update, all in one place!"
                paragraph "<b>Requirements</b><br> - Must install the app 'App Watchdog'. Please visit <a href='https://community.hubitat.com/t/release-app-watchdog/9952' target='_blank'>this page</a> for more information.<br> - When you are ready to go, turn on the switch below<br> - Then select 'App Watchdog Data' from the dropdown.<br> - That's it, you will now be notified automaticaly of updates."
                input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")
			}
            if(sendToAWSwitch) {
                section(getFormat("header-green", "${getImage("Blank")}"+" App Watchdog")) {    
                    if(sendToAWSwitch) input(name: "awDevice", type: "capability.actuator", title: "Please select 'App Watchdog Data' from the dropdown", submitOnChange: true, required: true, multiple: false)
			        if(sendToAWSwitch && awDevice) setVersion()
                }
            }
            // ** End App Watchdog Code **
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			display()
		}
	}
}

def installCheck(){
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app and create the data device"}
  	}
  	else{
    	log.info "Parent Installed OK"
  	}
}

def createVirtualDevice() {                        // Modified from @Stephack
    def childDevice = getChildDevices()?.find {it.device.deviceNetworkId == "AW_${app.id}"}      
    if (!childDevice) {
        if(logEnable) log.debug "In createVirtualDevice - Creating Virtual Device"
        childDevice = addChildDevice("BPTWorld", "App Watchdog Driver", "AW_${app.id}", null,[completedSetup: true, label: "App Watchdog Data"]) 
    	
        if(logEnable) log.debug "In createVirtualDevice - Created Virtual Device [${childDevice}]"
	}
    else {
        if(logEnable) log.debug "In createVirtualDevice - Virtual Device already created - Skipping"
	}
}

def getImage(type) {				// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){		// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display(){
    setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>App Watchdog 2 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}         
