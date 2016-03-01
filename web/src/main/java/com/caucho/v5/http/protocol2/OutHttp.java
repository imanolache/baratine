/*
 * Copyright (c) 1998-2015 Caucho Technology -- all rights reserved
 *
 * This file is part of Baratine(TM)
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Baratine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Baratine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baratine; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.v5.http.protocol2;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.amp.queue.DeliverAmpBase;
import com.caucho.v5.amp.queue.Outbox;
import com.caucho.v5.amp.queue.QueueService;
import com.caucho.v5.amp.queue.QueueServiceBuilderImpl;
import com.caucho.v5.io.IoUtil;
import com.caucho.v5.io.TempBuffer;
import com.caucho.v5.io.WriteBuffer;
import com.caucho.v5.util.BitsUtil;
import com.caucho.v5.util.L10N;

/**
 * InputStreamHttp reads a single HTTP frame.
 */
public class OutHttp implements AutoCloseable
{
  private static final L10N L = new L10N(OutHttp.class);
  
  private static final Logger log
    = Logger.getLogger(OutHttp.class.getName());
  
  private WriteBuffer _os;
  
  private AtomicInteger _openCount = new AtomicInteger();
  
  private AtomicBoolean _isClosedPeer = new AtomicBoolean();
  
  private AtomicBoolean _isClosed = new AtomicBoolean();

  private final QueueService<MessageHttp> _queue;
  
  private SettingsHttp _settings = new SettingsHttp();

  private OutHeader _writerHeader;
  
  private int _nextStreamId = 1;

  private PeerHttp _peer;

  private ConnectionHttp2 _conn;

  public OutHttp(ConnectionHttp2 conn, PeerHttp peer)
  {
    Objects.requireNonNull(conn);
    
    _conn = conn;
    _peer = peer;
    
    _queue = createQueue();
  }
  
  public OutHttp(WriteBuffer os, PeerHttp peer)
  {
    this(new ConnectionHttp2(peer), peer);
    
    init(os);
  }
  
  public void init(WriteBuffer os)
  {
    _os = os;
    _openCount.set(1);
    
    switch (_peer) {
    case CLIENT:
      _nextStreamId = 1;
      break;
      
    case SERVER:
      _nextStreamId = 2;
      break;
      
    default:
      throw new IllegalStateException(String.valueOf(_peer));
    }
    
    _writerHeader = new OutHeader(os);
  }

  /*
  public void setWindow(int window)
  {
    if (window <= 0) {
      throw new IllegalArgumentException(String.valueOf(window));
    }

    _settings.setInitialWindowSize(window);
  }
  */

  public int getWindow()
  {
    return _settings.getInitialWindowSize();
  }

  void updateSettings(SettingsHttp peerSettings)
  {
    _settings.setInitialWindowSize(peerSettings.getInitialWindowSize());
  }
  
  public void writeConnectionHeader()
    throws IOException
  {
    _os.write(Http2Constants.CONNECTION_HEADER);
  }

  public OutHeader getOutHeader()
  {
    return _writerHeader;
  }
  
  private QueueService<MessageHttp> createQueue()
  {
    QueueServiceBuilderImpl<MessageHttp> builder
      = new QueueServiceBuilderImpl<>();
    builder.capacity(64);
    
    return builder.build(new WriterServiceImpl());
  }

  public int nextStream(ChannelHttp2 channel)
  {
    if (_isClosedPeer.get()) {
      throw new IllegalStateException(L.l("Peer HTTP client is closed"));
    }
    
    int id = _nextStreamId;
    
    _nextStreamId = id + 2;
    
    channel.init(_conn, id);
      
    // _conn.putChannel(id, streamOut);
    
    _openCount.incrementAndGet();

    return id;
  }
  
  public void data(int streamId, TempBuffer tBuf, int offset, int length,
                   FlagsHttp flags)
  {
    _queue.offer(new MessageData(streamId, tBuf, offset, length, flags));
    _queue.wake();
  }

  /**
   * Message when the window allows more data.
   */
  public void dataCont(ChannelOutHttp2 stream)
  {
    _queue.offer(new MessageDataCont(stream));
    _queue.wake();
  }

  public ChannelOutHttp2 getStream(int streamId)
  {
    return _conn.getChannel(streamId).getOutChannel();
  }

  public void flow(int streamId, int delta)
  {
    _queue.offer(new MessageFlow(streamId, delta));
    _queue.wake();
  }
  
  public void offer(MessageHttp message)
  {
    // log.warning("OFFER: " + message);
    _queue.offer(message);
    _queue.wake();
  }
  
  public void writeSettings(SettingsHttp settings)
    throws IOException
  {
    int settingCount = 2;
    int length = 5 * settingCount;
    
    WriteBuffer os = _os;
    
    os.write(length >> 8);
    os.write(length);
    os.write(Http2Constants.FRAME_SETTINGS);
    os.write(0);
    
    BitsUtil.writeInt(os, 0);
    
    os.write(Http2Constants.SETTINGS_MAX_CONCURRENT_STREAMS);
    BitsUtil.writeInt(os, settings.getMaxConcurrentStreams());
    
    os.write(Http2Constants.SETTINGS_INITIAL_WINDOW_SIZE);
    BitsUtil.writeInt(os, settings.getInitialWindowSize());
  }

  /**
   * data (0)
   */
  void writeData(int streamId, byte []buffer, int offset, int length, int flags)
    throws IOException
  {
    WriteBuffer os = _os;
    
    if (os == null) {
      return;
    }
    
    // ChannelHttp2 channel = _conn.getChannel(streamId);
    
    /*
    if (channel == null) {
      System.out.println("WRIT: " + streamId + " with close");
    }
    */
    
    if (offset >= 8) {
      offset -= 8;
      
      BitsUtil.writeInt16(buffer, offset, length);
      buffer[offset + 2] = Http2Constants.FRAME_DATA;
      buffer[offset + 3]= (byte) flags;
      BitsUtil.writeInt(buffer, offset + 4, streamId);
      
      os.write(buffer, offset, length + 8);
    }
    else {
      os.write((byte) (length >> 8)); 
      os.write((byte) (length));
      os.write(Http2Constants.FRAME_DATA);
      os.write(flags);
      
      BitsUtil.writeInt(os, streamId);
      
      if (length > 0) {
        os.write(buffer, offset, length);
      }
    }
    
    if ((flags & Http2Constants.END_STREAM) != 0) {
      closeWrite(streamId);
    }
  }
  
  void closeWrite(int streamId)
  {
    ChannelHttp2 channel = _conn.getChannel(streamId);
    
    if (channel != null) {
      channel.closeWrite();
    }
  }

  void writeFlow(int streamId, int credit)
    throws IOException
  {
    WriteBuffer os = _os;
    
    if (os == null) {
      return;
    }
    
    int length = 4;
    int flags = 0;
    
    os.write((byte) (length >> 8)); 
    os.write((byte) (length));
    os.write(Http2Constants.FRAME_WINDOW_UPDATE);
    os.write(flags);
    
    BitsUtil.writeInt(os, streamId);
    BitsUtil.writeInt(os, credit);
  }

  void writeBlock(int streamId, int credit)
    throws IOException
  {
    WriteBuffer os = _os;
    
    if (os == null) {
      return;
    }
    
    int length = 0;
    int flags = 0;
    
    os.write((byte) (length >> 8)); 
    os.write((byte) (length));
    os.write(Http2Constants.FRAME_BLOCKED);
    os.write(flags);
    
    BitsUtil.writeInt(os, streamId);
  }
  
  void streamClose()
  {
    _openCount.decrementAndGet();
    
    if (_openCount.get() == 0 && _isClosedPeer.get()) {
      close();
    }
  }

  void writeGoAway()
    throws IOException
  {
    int lastStream = 0;
    
    WriteBuffer os = _os;
    
    int length = 8;
    
    os.write((byte) (length >> 8)); 
    os.write((byte) (length));
    os.write(Http2Constants.FRAME_GOAWAY);
    os.write(0);
    
    BitsUtil.writeInt(os, 0);
    
    BitsUtil.writeInt(os, lastStream);
    
    int errorCode = 0;
    
    BitsUtil.writeInt(os, errorCode);
    
    os.flush();
    
    if (_conn.isClosed()) {
      os.close();
    }

    _conn.onWriteGoAway();
    
    // os is not closed because after the go-away, flow window acks might
    // still be needed
    
    synchronized (_isClosed) {
      _isClosed.set(true);
      _isClosed.notifyAll();
    }
  }

  public void writeBlock(int streamId)
    throws IOException
  {
    WriteBuffer os = _os;
    
    int length = 0;
    
    os.write((byte) (length >> 8)); 
    os.write((byte) (length));
    os.write(Http2Constants.FRAME_BLOCKED);
    os.write(0);
    
    BitsUtil.writeInt(os, streamId);
  }

  void writeReset(int streamId, int errorCode)
    throws IOException
  {
    WriteBuffer os = _os;
    
    int length = 4;
    
    os.write((byte) (length >> 8)); 
    os.write((byte) (length));
    os.write(Http2Constants.FRAME_RST_STREAM);
    os.write(0);
    
    BitsUtil.writeInt(os, streamId);
    
    BitsUtil.writeInt(os, errorCode);
    
    if (streamId == 0) {
      IoUtil.close(os);
    
      synchronized (_isClosed) {
        _isClosed.set(true);
        _isClosed.notifyAll();
      }
    }
    else {
      closeWrite(streamId);
    }
  }

  void writePriority(int streamId, int streamRef, int weight)
    throws IOException
  {
    WriteBuffer os = _os;
    
    int length = 5;
    
    os.write((byte) (length >> 8)); 
    os.write((byte) (length));
    os.write(Http2Constants.FRAME_PRIORITY);
    os.write(0);
    
    BitsUtil.writeInt(os, streamId);
    
    BitsUtil.writeInt(os, streamRef);
    os.write(weight);
  }

  /**
   * XXX: used during handshake
   */
  void flush()
    throws IOException
  {
    WriteBuffer os = _os;
        
    if (os != null) {
      os.flush();
    }
  }

  @Override
  public void close()
  {
    _queue.offer(new MessageGoAway());
    _queue.wake();
  }
  
  public void flushAndWait(long timeout, TimeUnit unit)
  {
    MessageFlush flush = new MessageFlush();
    
    _queue.offer(flush);
    _queue.wake();
    
    flush.get(timeout, unit);
  }

  public void waitForClose()
  {
    synchronized (_isClosed) {
      if (! _isClosed.get()) {
        try {
          _isClosed.wait(1000);
        } catch (Exception e) {
          log.log(Level.FINER, e.toString(), e);
        }
      }
    }
  }
  
  private class WriterServiceImpl extends DeliverAmpBase<MessageHttp> 
  {
    @Override
    public void deliver(MessageHttp message, Outbox<MessageHttp> outbox)
    {
      try {
        message.deliver(_os, OutHttp.this);
      } catch (Exception e) {
        log.log(Level.FINER, e.toString(), e);
      }
    }
    
    @Override
    public void afterBatch()
    {
      try {
        WriteBuffer os = _os;
        // log.warning("FLUSH: " + this + " " + os);
    
        if (os != null) {
          os.flush();
        }
      } catch (Exception e) {
        log.log(Level.FINER, e.toString(), e);
      }
    }
  }
}