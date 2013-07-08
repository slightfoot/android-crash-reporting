# Android Crash Reporting
by Simon Lightfoot <simon@demondevelopers.com>

Example of how you can custom roll-your-own crash reporting for your app.

This quick example will capture the following information:
*  Basic application details
*  Stack-trace and reason for crash
*  Screenshot of your app when the crash happened (no special permissions required!)
*  Android event log
*  Android system log

It could easily be expanded to log more. If requested I might improve this or
even make this into a new .aar android library.

__NOTE: It does not generate crash reports when you are debugging your app.__
