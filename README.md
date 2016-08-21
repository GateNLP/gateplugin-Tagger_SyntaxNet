# GATE Plugin for a Google SyntaxNet Server


This GATE plugin annotates documents using a Tensorflow Serving server 
running SyntaxNet. 


## Build Notes

This uses a traditional ant-based build procedure as for other GATE plugins.
However, part of the source code is generated Java code for the protobuf 
data structures and the gRPC communication with the server. This is the 
only use the pom.xml fie currently has: if the .proto files change, it 
can be used to re-generate the sources which will then get copied into
the src directory again.

