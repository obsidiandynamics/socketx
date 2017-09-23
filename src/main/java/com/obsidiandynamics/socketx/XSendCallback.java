package com.obsidiandynamics.socketx;

public interface XSendCallback {
  void onComplete(XEndpoint endpoint);

  void onError(XEndpoint endpoint, Throwable cause);
  
  void onSkip(XEndpoint endpoint);
}
