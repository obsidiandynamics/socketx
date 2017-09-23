package com.obsidiandynamics.socketx;

import java.nio.*;

public class XEndpointLambdaListener<E extends XEndpoint> implements XEndpointListener<E> {
  @FunctionalInterface public interface OnConnect<E extends XEndpoint> {
    void onConnect(E endpoint);
  }
  
  @FunctionalInterface public interface OnText<E extends XEndpoint> {
    void onText(E endpoint, String message);
  }
  
  @FunctionalInterface public interface OnBinary<E extends XEndpoint> {
    void onBinary(E endpoint, ByteBuffer message);
  }
  
  @FunctionalInterface public interface OnPing<E extends XEndpoint> {
    void onPing(E endpoint, ByteBuffer data);
  }
  
  @FunctionalInterface public interface OnPong<E extends XEndpoint> {
    void onPong(E endpoint, ByteBuffer data);
  }
  
  @FunctionalInterface public interface OnDisconnect<E extends XEndpoint> {
    void onDisconnect(E endpoint, int statusCode, String reason);
  }
  
  @FunctionalInterface public interface OnClose<E extends XEndpoint> {
    void onClose(E endpoint);
  }
  
  @FunctionalInterface public interface OnError<E extends XEndpoint> {
    void onError(E endpoint, Throwable cause);
  }
  
  private OnConnect<? super E> onConnect;
  
  private OnText<? super E> onText;
  
  private OnBinary<? super E> onBinary;
  
  private OnPing<? super E> onPing;
  
  private OnPong<? super E> onPong;
  
  private OnDisconnect<? super E> onDisconnect;
  
  private OnClose<? super E> onClose;
  
  private OnError<? super E> onError;
  
  @Override
  public void onConnect(E endpoint) {
    if (onConnect != null) onConnect.onConnect(endpoint);
  }

  @Override
  public void onText(E endpoint, String message) {
    if (onText != null) onText.onText(endpoint, message);
  }

  @Override
  public void onBinary(E endpoint, ByteBuffer message) {
    if (onBinary != null) onBinary.onBinary(endpoint, message);
  }

  @Override
  public void onPing(E endpoint, ByteBuffer data) {
    if (onPing != null) onPing.onPing(endpoint, data);
  }

  @Override
  public void onPong(E endpoint, ByteBuffer data) {
    if (onPong != null) onPong.onPong(endpoint, data);
  }

  @Override
  public void onDisconnect(E endpoint, int statusCode, String reason) {
    if (onDisconnect != null) onDisconnect.onDisconnect(endpoint, statusCode, reason);
  }

  @Override
  public void onClose(E endpoint) {
    if (onClose != null) onClose.onClose(endpoint);
  }

  @Override
  public void onError(E endpoint, Throwable cause) {
    if (onError != null) onError.onError(endpoint, cause);
  }

  public final XEndpointLambdaListener<E> onConnect(OnConnect<? super E> onConnect) {
    this.onConnect = onConnect;
    return this;
  }

  public final XEndpointLambdaListener<E> onText(OnText<? super E> onText) {
    this.onText = onText;
    return this;
  }

  public final XEndpointLambdaListener<E> onBinary(OnBinary<? super E> onBinary) {
    this.onBinary = onBinary;
    return this;
  }

  public final XEndpointLambdaListener<E> onPing(OnPing<? super E> onPing) {
    this.onPing = onPing;
    return this;
  }

  public final XEndpointLambdaListener<E> onPong(OnPong<? super E> onPong) {
    this.onPong = onPong;
    return this;
  }

  public final XEndpointLambdaListener<E> onDisconnect(OnDisconnect<? super E> onDisconnect) {
    this.onDisconnect = onDisconnect;
    return this;
  }

  public final XEndpointLambdaListener<E> onClose(OnClose<? super E> onClose) {
    this.onClose = onClose;
    return this;
  }

  public final XEndpointLambdaListener<E> onError(OnError<? super E> onError) {
    this.onError = onError;
    return this;
  }
}
