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

package com.caucho.v5.websocket.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.caucho.v5.io.ReadBuffer;

import io.baratine.service.ServiceRef;

/**
 * WebSocketClient
 */
class ThreadClientTask implements Runnable {
  private final Logger log = Logger.getLogger(ThreadClientTask.class.getName());
  
  private WebSocketClientBaratine _client;

  private ReadBuffer _is;
  
  ThreadClientTask(WebSocketClientBaratine client,
                   ReadBuffer is)
  {
    _client = client;
    _is = is;
  }
  
  @Override
  public void run()
  {
    try {
      //while (! _client.isClosed() && wsEndpointReader.onRead()) {

      while (_client.readFrame()) {
        ServiceRef.flushOutbox();
        if (_is.fillBuffer() <= 0) {
          break;
        }
      }
    } catch (Exception e) {
      log.log(Level.WARNING, e.toString(), e);
    } finally {
      _client.close();
    }
  }
}