-------------------------

About EXIClient

This is an extension of Jive Software's Smack 3.3.0 library in order to support the EXI compression method in XMPP networks as described by XEP-0322, 
which can be found at

	http://xmpp.org/extensions/xep-0322.html	

	
-------------------------
	
Setup

Together with this package is there a folder called "schemas". This folder is used by the software to process XML santzas for compression 
and decompression. Therefore, the "schemas" folder and its contents should be placed in the base folder of the project containing this implementation.
Other schema files describing stanzas that might be used by the application may also be included in the mentioned folder. This will allow this 
implementation to use that information to make compression more efficient.


-------------------------

Copyright 2014 ¿¿¿Javier Placencio???

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
