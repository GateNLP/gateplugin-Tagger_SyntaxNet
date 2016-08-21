/*
 *  Copyright (c) The University of Sheffield.
 *
 *  This file is free software, licensed under the 
 *  GNU Library General Public License, Version 2.1, June 1991.
 *  See the file LICENSE.txt that comes with this software.
 *
 */
package gate.plugin.tagger.syntaxnet;

import cali.nlp.ParseyApi;
import cali.nlp.ParseyServiceGrpc;
import com.google.common.base.Supplier;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.TimeUnit;
import syntaxnet.SentenceOuterClass.Sentence;
import syntaxnet.SentenceOuterClass.Token;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.GateRuntimeException;
import io.grpc.netty.NettyChannelProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Processing resource for using the Google NLP service.
 * 
 * @author Johann Petrak
 */
@CreoleResource(name = "Tagger_SyntaxNet",
        comment = "Annotate documents using a Tensorflow Serving SyntaxNet server",
        // icon="taggerIcon.gif",
        helpURL = "https://github.com/GateNLP/gateplugin-Tagger_SyntaxNet/wiki/Tagger_SyntaxNet"
)
public class TaggerSyntaxNet extends AbstractDocumentProcessor {

  // PR PARAMETERS
  protected String containingAnnotationType = "";

  @CreoleParameter(comment = "The annotation that covers the document text to annotate", defaultValue = "")
  @RunTime
  @Optional
  public void setContainingAnnotationType(String val) {
    containingAnnotationType = val;
  }

  public String getContainingAnnotationType() {
    return containingAnnotationType;
  }

  protected String inputAnnotationSet = "";
  @CreoleParameter(comment = "The input annotation set", defaultValue = "")
  @RunTime
  @Optional
  public void setInputAnnotationSet(String val) {
    inputAnnotationSet = val;
  }

  public String getInputAnnotationSet() {
    return inputAnnotationSet;
  }
  
  protected String outputAnnotationSet = "";
  @CreoleParameter(comment = "The output annotation set", defaultValue = "")
  @RunTime
  @Optional
  public void setOutputAnnotationSet(String val) {
    outputAnnotationSet = val;
  }

  public String getOutputAnnotationSet() {
    return outputAnnotationSet;
  }
 
  public String serverAddress = "http://127.0.0.1";
  @CreoleParameter(comment = "The SyntaxNet server address",defaultValue = "http:127.0.0.1")
  @RunTime
  @Optional
  public void setServerAddress(String val) {
    serverAddress = val;
  }
  public String getServerAddress() { return serverAddress; }
  
  public Integer serverPort = 9000;
  @CreoleParameter(comment = "The SyntaxNet server port",defaultValue="9000")
  @RunTime
  @Optional
  public void setServerPort(Integer val) {
    serverPort = val;
  }
  public Integer getServerPort() { return serverPort; }


  // FIELDS

   ParseyServiceGrpc.ParseyServiceBlockingStub stub = null;
 
  // HELPER METHODS

  public ParseyServiceGrpc.ParseyServiceBlockingStub getStub() {
    System.err.println("DEBUG: creating stub");    
    /*
    String className = "com.google.common.base.Supplier";
    try {
      Class.forName(className,false,this.getClass().getClassLoader());
    } catch (Exception ex) {
      throw new GateRuntimeException("Could not find class "+className);
    }
    */
    //Supplier<List<String>> supplier = null;
    
    ManagedChannel channel = null;
    try {
      
      channel = new NettyChannelProvider().builderForAddress(getServerAddress(),getServerPort()).usePlaintext(true).build();
              
    } catch (Exception ex) {
      System.err.println("DEBUG: Ignored stack trace:");
      ex.printStackTrace(System.err);
    }

    try {
      if(channel==null) channel = ManagedChannelBuilder.
            forAddress(getServerAddress(), getServerPort()).
            usePlaintext(true).build();
    } catch (Exception ex) {
      System.err.println("DEBUG: Ignored stack trace:");
      ex.printStackTrace(System.err);
    }
      
    try {
      if(channel==null) channel = NettyChannelBuilder.
            forAddress(getServerAddress(), getServerPort()).
            usePlaintext(true).build();
      
    } catch (Exception ex) {
      System.err.println("DEBUG: Ignored stack trace:");
      ex.printStackTrace(System.err);      
    }
    System.err.println("DEBUG: got channel: "+channel);
    
    if(channel==null) {
      throw new GateRuntimeException("Could not get a channel");
    }
    
    ParseyServiceGrpc.ParseyServiceBlockingStub stub = null;
    try {
      stub = ParseyServiceGrpc.newBlockingStub(channel);
    } catch (Exception ex) {
      throw new GateRuntimeException("Error creating stub",ex);
    }
    System.err.println("DEBUG: got stub: "+stub);
    return stub;

  } 


  public void shutdown() {
    System.err.println("DEBUG: shutting down..");
    if(stub==null) return;
    ManagedChannel channel = (ManagedChannel)stub.getChannel();
    if(channel == null) return;
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      // TODO
    }

  } 



  @Override
  protected Document process(Document document) {
    System.err.println("DEBUG: processing document "+document.getName());
    if(isInterrupted()) {
      interrupted = false;
      throw new GateRuntimeException("Processing has been interrupted");
    }
    // For each span add the start offset to the array and add the span to the 
    // builder
    List<Long> startOffsets = new ArrayList<Long>();
    ParseyApi.ParseyRequest.Builder b =ParseyApi.ParseyRequest.newBuilder(); 
    if(getContainingAnnotationType() != null && !getContainingAnnotationType().isEmpty()) {
      AnnotationSet anns = document.getAnnotations(getInputAnnotationSet()).get(getContainingAnnotationType());
      for(Annotation ann : anns) {
        String text = gate.Utils.stringFor(document, ann);
        startOffsets.add(Utils.start(ann));
        b.addText(text);
      }
    } else {
      String text = document.getContent().toString();
      startOffsets.add(0L);
      b.addText(text);
    }
    // get the response for all the texts we added to the builder
    System.err.println("DEBUG: creating and sending request");
    ParseyApi.ParseyResponse resp = stub.parse(b.build());
    System.err.println("DEBUG: got response: "+resp);
    List<Sentence> sentences = resp.getResultList();
    System.err.println("DEBUG: got sentences: "+sentences);
    if(sentences.size() != startOffsets.size()) {
      throw new GateRuntimeException("Something went wrong: different number of spans and returned sentences");
    }
    
    AnnotationSet outset = document.getAnnotations(getOutputAnnotationSet());
    
    for(int i=0;i<sentences.size();i++) {
      long sentenceOffset = startOffsets.get(i);
      Sentence s = sentences.get(i);
      // The dependency parses represent edges as numbers of other tokens in
      // the token sequence. We replace this by referring to the annotation
      // by its id instead.
      // We do this by going through this list once, creating the annotations and storing the 
      // annotations in a parallel list, then going through this list another time and 
      // creating the edge annotations using the parallel list to get the annotation ids. 

      List<Annotation> tokenAnns = new ArrayList<Annotation>(s.getTokenList().size());
      for(Token token : s.getTokenList()) {
        long startOffset = sentenceOffset + token.getStart();
        long endOffset = sentenceOffset + token.getEnd();
        FeatureMap fm = Factory.newFeatureMap();
        token.getCategory();
        String label = token.getLabel();
        String tag = token.getTag();
        String pos = token.getCategory();
        fm.put("category",pos);
        fm.put("label",label);
        fm.put("tag",tag);
        int id = gate.Utils.addAnn(outset, startOffset, endOffset, "Token", fm);
        tokenAnns.add(outset.get(id));        
      } // for token : tokens
      // Now add the head pointer as an annotation id
      int j = 0;
      for(Token token : s.getTokenList()) {
        Annotation tokenAnn = tokenAnns.get(j);        
        int head = token.getHead();
        int headId = tokenAnns.get(head).getId();
        tokenAnn.getFeatures().put("headId", headId);
        j++;
      }
    }
    return document;
  }
  

  @Override
  protected void beforeFirstDocument(Controller ctrl) {
    stub = getStub();    
  }

  @Override
  protected void afterLastDocument(Controller ctrl, Throwable t) {
    shutdown();
  }

  @Override
  protected void finishedNoDocument(Controller ctrl, Throwable t) {
    shutdown();
  }

}
