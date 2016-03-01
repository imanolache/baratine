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

package com.caucho.v5.amp.actor;

import java.lang.reflect.Type;

import com.caucho.v5.amp.ServiceRefAmp;
import com.caucho.v5.amp.spi.MethodRefAmp;

/**
 * Sender for an actor ref.
 */
public class MethodRefDynamic extends MethodRefWrapper
{
  private ServiceRefDynamic _serviceRefDynamic;
  private ServiceRefAmp _serviceRef;
  
  private String _methodName;
  private Type _type;
  
  private MethodRefAmp _methodRef;
  
  MethodRefDynamic(ServiceRefDynamic serviceRefDynamic,
                   String methodName)
  {
    _serviceRefDynamic = serviceRefDynamic;
    _methodName = methodName;
  }
  
  MethodRefDynamic(ServiceRefDynamic serviceRefDynamic,
                   String methodName,
                   Type type)
  {
    _serviceRefDynamic = serviceRefDynamic;
    _methodName = methodName;
    _type = type;
  }
  
  @Override
  public MethodRefAmp getDelegate()
  {
    ServiceRefAmp serviceRef = _serviceRefDynamic.delegate();
    
    if (serviceRef != _serviceRef) {
      _serviceRef = serviceRef;
      
      if (_type != null) {
        _methodRef = serviceRef.getMethod(_methodName, _type);
      }
      else {
        _methodRef = serviceRef.getMethod(_methodName);
      }
    }

    return _methodRef;
  }
}