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

package com.caucho.v5.bartender.pod;

import io.baratine.service.Result;
import io.baratine.service.ResultStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import com.caucho.v5.amp.spi.ActorAmp;
import com.caucho.v5.amp.spi.HeadersAmp;
import com.caucho.v5.amp.spi.MethodAmp;
import com.caucho.v5.amp.spi.MethodRefAmp;
import com.caucho.v5.util.L10N;

/**
 * MethodAmp adapted from a MethodRef, where the MethodRef has the sending
 * logic.
 * 
 * Use to match the proxy logic.
 */
public class MethodAmpAdapter implements MethodAmp
{
  private static final L10N L = new L10N(MethodAmpAdapter.class);
  private static final Logger log = Logger.getLogger(MethodAmpAdapter.class.getName());
  
  private final MethodRefAmp _methodRef;
  
  public MethodAmpAdapter(MethodRefAmp methodRef)
  {
    _methodRef = methodRef;
  }

  @Override
  public boolean isClosed()
  {
    return _methodRef.getService().isClosed();
  }

  @Override
  public String name()
  {
    return _methodRef.getName();
  }

  @Override
  public boolean isDirect()
  {
    return false;
  }

  @Override
  public Annotation[] getAnnotations()
  {
    try {
      return _methodRef.getAnnotations();
    } catch (StackOverflowError e) {
      throw new UnsupportedOperationException(getClass().getName() + " " + _methodRef.getClass().getSimpleName());
    }
  }

  @Override
  public Class<?>[] getParameterTypes()
  {
    if (_methodRef instanceof MethodRefAmp) {
      try {
        return ((MethodRefAmp) _methodRef).getParameterClasses();
      } catch (StackOverflowError e) {
        throw new UnsupportedOperationException(getClass().getName() + " " + _methodRef.getClass().getSimpleName());
      }
    }
    else {
      return null;
    }
  }

  @Override
  public Type[] getGenericParameterTypes()
  {
    if (_methodRef instanceof MethodRefAmp) {
      try {
        return ((MethodRefAmp) _methodRef).getParameterTypes();
      } catch (StackOverflowError e) {
        throw new UnsupportedOperationException(getClass().getName() + " " + _methodRef.getClass().getSimpleName());
      }
    }
    else {
      return null;
    }
  }

  @Override
  public Class<?> getReturnType()
  {
    if (_methodRef instanceof MethodRefAmp) {
      try {
        Type type = ((MethodRefAmp) _methodRef).getReturnType();
        
        if (type instanceof Class) {
          return (Class) type;
        }
        else {
          return null;
        }
      } catch (StackOverflowError e) {
        throw new UnsupportedOperationException(getClass().getName() + " " + _methodRef.getClass().getSimpleName());
      }
    }
    else {
      return null;
    }
  }

  @Override
  public Annotation[][] getParameterAnnotations()
  {
    return null;
  }

  @Override
  public boolean isVarArgs()
  {
    return false;
  }

  @Override
  public void send(HeadersAmp headers, ActorAmp actor)
  {
    _methodRef.send(headers);
  }

  @Override
  public void send(HeadersAmp headers, ActorAmp actor, Object arg1)
  {
    _methodRef.send(headers, arg1);
  }

  @Override
  public void send(HeadersAmp headers, ActorAmp actor, Object arg1, Object arg2)
  {
    _methodRef.send(headers, arg1, arg2);
  }

  @Override
  public void send(HeadersAmp headers, ActorAmp actor, Object arg1,
                   Object arg2, Object arg3)
  {
    _methodRef.send(headers, arg1, arg2, arg3);
  }

  @Override
  public void send(HeadersAmp headers, ActorAmp actor, Object[] args)
  {
    _methodRef.send(headers, args);
  }

  @Override
  public void query(HeadersAmp headers, Result<?> result, ActorAmp actor)
  {
    _methodRef.query(headers, result);
  }

  @Override
  public void query(HeadersAmp headers, Result<?> result, ActorAmp actor,
                    Object arg1)
  {
    _methodRef.query(headers, result, arg1);
  }

  @Override
  public void query(HeadersAmp headers, Result<?> result, ActorAmp actor,
                    Object arg1, Object arg2)
  {
    _methodRef.query(headers, result, arg1, arg2);
  }

  @Override
  public void query(HeadersAmp headers, Result<?> result, ActorAmp actor,
                    Object arg1, Object arg2, Object arg3)
  {
    _methodRef.query(headers, result, arg1, arg2, arg3);
  }

  @Override
  public void query(HeadersAmp headers, Result<?> result, ActorAmp actor,
                    Object[] args)
  {
    _methodRef.query(headers, result, args);
  }

  /*
  @Override
  public <T,R> void stream(HeadersAmp headers, 
                           QueryRefAmp queryRef, 
                           ActorAmp actor,
                           CollectorAmp<T,R> consumer, 
                           Object[] args)
  {
    MethodRefAmp methodRef = (MethodRefAmp) _methodRef;
    
    methodRef.collect(headers, queryRef, (CollectorAmp) consumer, args);
  }
  */

  @Override
  public <T> void stream(HeadersAmp headers,
                         ResultStream<T> result, 
                         ActorAmp actor,
                         Object[] args)
  {
    MethodRefAmp methodRef = (MethodRefAmp) _methodRef;
    
    methodRef.stream(headers, result, args);
  }

  /*
  @Override
  public ActorAmp getActorInvoke(ActorAmp actorDeliver)
  {
    return actorDeliver;
  }
  */
  
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[" + _methodRef + "]";
  }
}