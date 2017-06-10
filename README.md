# WifiSpiToggle #

Manually or automatically (based on wifi SSID) toggle "receive SIP calls" on android. Needs root access and android >= 7 (Nougat). Unfortunately, root is needed, see https://github.com/robert7k/sipswitch/issues/4.

Install, add widget to home screen. By clicking on the widget, you can toggle between "A" (automatic switching), red "M" (SIP receive calls disabled), and green "M" (SIP receive calls enabled).

If you start the app, you can add wifi SSIDs for which SIP should be enabled, if the field is empty, SIP receive is enabled for all wifi networks.

The notification has to be present in automatic mode to avoid being killed by the android OS, because only for an active background service the "network change" broadcasts work with android >=7.

The source code of this project has been published under the GNU GENERAL PUBLIC LICENSE, see COPYING.txt.