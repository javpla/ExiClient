Copyright 2014 Javier Placencio

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.



------------------
 About EXIClient
------------------
EXIClient is an XMPP Client supporting EXI compression as described by XEP-0322. It is an extended version of Jive Software's Smack 3.3.0 library, 
which can be found at

	http://xmpp.org/extensions/xep-0322.html	

	

------------------
 Setup
------------------
Together within this package there is a folder called "schemas". This folder is used by the software to process XML santzas for compression 
and decompression. Therefore the "schemas" folder and its contents should be placed in the base folder of the project containing this implementation.
Other schema files describing stanzas that might be used by the application may also be included in the same folder. This will allow the 
implementation to use that information and make compression more efficient.

OBS: Schemas referencing other schemas that are not present in the "schemas" folder will not be included for compression. This will be notified by console.


------------------
 How to use?
------------------
EXIClient extends Smack, therefore it inherits all connection and configuration classes from it. To start an EXI compressed connection however,
it is required to use a new Connection class called EXIXMPPConnection. This class requires a EXISetupConfiguration class which contains all
compression parameters necessary. You can find examples of connections in the Example.java file contained with this package.

