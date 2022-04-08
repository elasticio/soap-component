# 1.2.9 (April 8, 2022)
* Added possibility to handle WSDL files where the "message" doesn't contain an element
* Updated the Sailor version to 3.3.9

# 1.2.8 (March 15, 2022)
* Added component pusher build script

# 1.2.7 (February 11, 2022)
* Add configuration field `Request timeout` for `Call` action: timeout period in milliseconds (1-1140000) while component waiting for server response. Defaults to 60000 (60 sec).

# 1.2.6 (October 15, 2021)
* Update Sailor version to 3.3.6
* `Call` action: add an option to emit a platform message instead of throwing an exception in case of a SOAP fault

# 1.2.5 (December 18, 2020)
* Update Sailor version to 3.3.1
* Annual audit of the component code to check if it exposes sensitive data in the logs

# 1.2.4 (July 24, 2020)
## General Changes
* Fix bug for some cases with WSDL behind basic auth  

# 1.2.3 (June 22, 2020)
## General Changes
* Remove the job which updates docs on code changes 

# 1.2.2 (May 29, 2020)
## General Changes
* Replace weight in component.json with order

# 1.2.1 (May 7, 2020)
## General Changes
* Fix component.json field order
* Fix component.json descriptions and links
* Add basic authorization support to Call action
* Improved SOAP Body parsing
    
# 1.2.0 (September 25, 2019)
## Actions
    * Add Soap Reply action
## Triggers
    * Add Receive SOAP Request trigger

# 1.1.0 (May 20, 2019)
## Call action
    * Optimize memory consumptions, refactor code and tests.
    * Add circle ci status badge.
