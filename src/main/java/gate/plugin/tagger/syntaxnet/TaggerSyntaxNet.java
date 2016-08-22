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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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

    // Need to set the classloader here because the thread context classloader is what
    // grpc uses to dynamically load the correct ManagedChannel provider. By default
    // this is the original URL classloader which does not know about the plugin-jars.
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    
    ManagedChannel channel = null;
    try {
      if(channel==null) channel = ManagedChannelBuilder.
            forAddress(getServerAddress(), getServerPort()).
            usePlaintext(true).build();
    } catch (Exception ex) {
      throw new GateRuntimeException("Could not obtain channel",ex);
    }

    // NOTE: getting the stub currently only works if the protob 
    // jar is in lib-static, not from ivy. Not sure why.
    ParseyServiceGrpc.ParseyServiceBlockingStub stub = null;
    try {
      stub = ParseyServiceGrpc.newBlockingStub(channel);      
    } catch (Exception ex) {
      ex.printStackTrace(System.out);
      throw new GateRuntimeException("Error creating stub",ex);
    }
    return stub;

  } 


  public void shutdown() {
    if(stub==null) return;
    ManagedChannel channel = (ManagedChannel)stub.getChannel();
    if(channel == null) return;
    try {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      System.err.println("Problem shutting down the channel, but continuing.");
      ex.printStackTrace();
    }

  } 



  @Override
  protected Document process(Document document) {
    System.err.println("DEBUG: processing document "+document.getName());
    if(isInterrupted()) {
      interrupted = false;
      throw new GateRuntimeException("Processing has been interrupted");
    }
    
    // From tests I could not find an easy way to map multiple texts sent to the service
    // to the multiple responses (for each sentence) we get back. So we could add three texts
    // and then get back 8 sentences and the token offsets for each sentence would be relative
    // to the start of the sentence. 
    ParseyApi.ParseyRequest.Builder b =ParseyApi.ParseyRequest.newBuilder(); 
    if(getContainingAnnotationType() != null && !getContainingAnnotationType().isEmpty()) {
      AnnotationSet anns = document.getAnnotations(getInputAnnotationSet()).get(getContainingAnnotationType());
      System.err.println("DEBUG: containing annotations: "+anns.size());
      for(Annotation ann : anns) {
        String text = gate.Utils.stringFor(document, ann);
        b.addText(text);       
        processSpan(b,document,text,Utils.start(ann));
      }
    } else {
      String text = document.getContent().toString();
      b.addText(text);
      processSpan(b,document,text,0L);
    }
    return document;
  }
  
  public void processSpan(ParseyApi.ParseyRequest.Builder b, 
          Document document, String text, long spanOffset) {
    ParseyApi.ParseyResponse resp = stub.parse(b.build());
    List<Sentence> sentences = resp.getResultList();
    AnnotationSet outset = document.getAnnotations(getOutputAnnotationSet());
    System.err.println("DEBUG annotating span at "+spanOffset);
    long runningOffset = 0;
    for(int i=0;i<sentences.size();i++) {
      Sentence s = sentences.get(i);
      String sentenceText = s.getText();
      System.err.println("DEBUG: Processing sentence with text >"+sentenceText+"<");
      System.err.println("DEBUG: text / length:                >"+text+"< "+text.length());
      System.err.println("DEBUG: start/length/end: "+(spanOffset+runningOffset)+"/"+sentenceText.length()+"/"+
              (spanOffset+runningOffset+sentenceText.length()));
      
      // create an annotation for the whole sentence. The id of this annotation will
      // be used for the head of the token which has head id -1 (the root)
      int sid = Utils.addAnn(outset, spanOffset+runningOffset, spanOffset+runningOffset+sentenceText.length(),
              "Sentence", Utils.featureMap());
      
      // The dependency parses represent edges as numbers of other tokens in
      // the token sequence. We replace this by referring to the annotation
      // by its id instead.
      // We do this by going through this list once, creating the annotations and storing the 
      // annotations in a parallel list, then going through this list another time and 
      // creating the edge annotations using the parallel list to get the annotation ids. 

      List<Annotation> tokenAnns = new ArrayList<Annotation>(s.getTokenList().size());
      for(Token token : s.getTokenList()) {
        System.err.println("Processing token: "+token);
        long startOffset = spanOffset + runningOffset + token.getStart();
        long endOffset = spanOffset + runningOffset + token.getEnd() + 1;
        FeatureMap fm = Factory.newFeatureMap();
        token.getCategory();
        String label = token.getLabel();
        String tag = token.getTag();
        String pos = token.getCategory();
        String word = token.getWord();
        String breaklevel = token.getBreakLevel().toString();
        fm.put("word",word);
        fm.put("category",pos);
        fm.put("label",label);
        fm.put("tag",tag);
        fm.put("breaklevel",breaklevel);
        int id = gate.Utils.addAnn(outset, startOffset, endOffset, "Token", fm);
        tokenAnns.add(outset.get(id));        
      } // for token : tokens
      // Now add the head pointer as an annotation id
      int j = 0;
      for(Token token : s.getTokenList()) {
        Annotation tokenAnn = tokenAnns.get(j);        
        int head = token.getHead();
        int headId = -99999;
        if(head == -1) {
          headId = sid;
        } else {
          headId = tokenAnns.get(head).getId();
        }
        tokenAnn.getFeatures().put("headId", headId);
        j++;
      }
      runningOffset += sentenceText.length();
    } // for sentence : sentences

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
